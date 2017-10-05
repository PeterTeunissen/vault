package com.pt.vault.data;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
public class AuditRecord extends VerifyableRecord {

	public static final String SEQ_GEN_KEY = "AuditRecord";

	@Id
	private Long id;
	private String d1;
	private String d2;
	private Long hashBucketOID;
	private String recordType = SEQ_GEN_KEY;

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getD1() {
		return d1;
	}

	public void setD1(String d1) {
		this.d1 = d1;
	}

	public String getD2() {
		return d2;
	}

	public void setD2(String d2) {
		this.d2 = d2;
	}

	public Long getHashBucketOID() {
		return hashBucketOID;
	}

	public void setHashBucketOID(Long hashBucketOID) {
		this.hashBucketOID = hashBucketOID;
	}

	public String toString() {
		return "AR ID=" + id + ",d1=" + d1 + ",d2=" + d2 + ",hash=" + hashValue + ",hashBucket="
				+ ((hashBucketOID == null) ? "<null>" : hashBucketOID);
	}

	@Override
	public void addHashableFields(HashCodeBuilder hcb) {
		hcb.append(id);
		hcb.append(d1);
		hcb.append(d2);
	}

}
