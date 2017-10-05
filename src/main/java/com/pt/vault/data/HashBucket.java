package com.pt.vault.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONException;
import org.json.JSONObject;

@Entity
public class HashBucket extends VerifyableRecord {

	public static final String SEQ_GEN_KEY = "HashBucket";
	public static final int DEFAULT_SIZE = 2;

	@Id
	private Long id;
	private Long bucketLevel;
	private int bucketSize;
	private int entriesUsed = 0;
	@Column(length = 8000)
	private String records = "{ }";
	private Long parentHashBucketOID;
	private Long siblingHashBucketOID;
	private String recordType = SEQ_GEN_KEY;

	public Long getSiblingHashBucketOID() {
		return siblingHashBucketOID;
	}

	public void setSiblingHashBucketOID(Long siblingHashBucketOID) {
		this.siblingHashBucketOID = siblingHashBucketOID;
	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public HashBucket() {
		String bz = System.getProperty("bucketSize");
		bucketSize = DEFAULT_SIZE;
		if (bz != null && bz.length() != 0) {
			try {
				bucketSize = Integer.valueOf(bz);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public HashBucket(int bucketSize) {
		this.bucketSize = bucketSize;
	}

	public Long getBucketLevel() {
		return bucketLevel;
	}

	public void setBucketLevel(Long bucketLevel) {
		this.bucketLevel = bucketLevel;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRecords() {
		return records;
	}

	public void setRecords(String records) {
		this.records = records;
	}

	public String getHashValue() {
		return hashValue;
	}

	public Long getParentHashBucketOID() {
		return parentHashBucketOID;
	}

	public void setParentHashBucketOID(Long hashBucketOID) {
		this.parentHashBucketOID = hashBucketOID;
	}

	public int getBucketSize() {
		return bucketSize;
	}

	public void setBucketSize(int bucketSize) {
		this.bucketSize = bucketSize;
	}

	public int getEntriesUsed() {
		return entriesUsed;
	}

	public void setEntriesUsed(int entriesUsed) {
		this.entriesUsed = entriesUsed;
	}

	public String toString() {
		return "HB ID=" + id + ",Level=" + bucketLevel + ",size=" + bucketSize + ",used=" + entriesUsed + ",records="
				+ records + ",hashValue=" + getHashValue() + ",parentHashBucketOID="
				+ ((parentHashBucketOID == null) ? "<null>" : parentHashBucketOID) + ",siblingHashBucket="
				+ ((siblingHashBucketOID == null) ? "<null>" : siblingHashBucketOID);
	}

	public List<Long> getReferencedOIDs() {
		List<Long> oids = new ArrayList<Long>();
		try {
			JSONObject o = new JSONObject(records);
			Iterator i = o.keys();
			while (i.hasNext()) {
				Object rec = i.next();
				oids.add(Long.valueOf((String) rec));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return oids;
	}

	public void removeEntry(Long oid) {
		try {
			JSONObject o = new JSONObject(records);
			Iterator i = o.keys();
			boolean found = false;
			while (i.hasNext() && !found) {
				Object rec = i.next();
				if (Long.valueOf((String) rec).equals(oid)) {
					found = true;
				}
			}
			if (!found) {
				throw new Exception("Entry does not exist in this bucket!");
			}
			o.remove(String.valueOf(oid));
			records = o.toString(2);
			entriesUsed--;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private void updateEntry(Long oid, String h, boolean failIfExists) {
		try {
			JSONObject o = new JSONObject(records);
			Iterator i = o.keys();
			boolean entryExists = false;
			int used = 0;
			while (i.hasNext()) {
				Object rec = i.next();
				if (Long.valueOf((String) rec).equals(oid)) {
					entryExists = true;
				}
				if (entryExists && failIfExists) {
					throw new Exception("Entry already exists!");
				}
				used++;
			}
			// If we had to add it to this bucket but the bucket is full, throw
			// an exception
			if (used == bucketSize && !entryExists) {
				throw new RuntimeException(
						"Can not add (" + oid + "," + h + ") to this bucket (" + this.toString() + ")");
			}
			o.put(String.valueOf(oid), h);
			records = o.toString(2);
			if (!entryExists) {
				entriesUsed++;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void updateEntry(long oid, String h) {
		updateEntry(oid, h, false);
	}

	public void addEntry(Long oid, String h) {
		updateEntry(oid, h, true);
	}

	@Override
	public void addHashableFields(HashCodeBuilder hcb) {
		hcb.append(id);
		hcb.append(bucketSize);
		try {
			addJSONHash(hcb);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private void addJSONHash(HashCodeBuilder bldr) throws JSONException {
		JSONObject o = new JSONObject(records);
		Iterator i = o.keys();
		while (i.hasNext()) {
			Object rec = i.next();
			String hash = o.getString((String) rec);
			bldr.append(hash);
		}
	}
}
