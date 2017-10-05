package com.pt.vault.repo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pt.vault.data.AuditRecord;
import com.pt.vault.data.HashBucket;
import com.pt.vault.hasher.HashEngine;
import com.pt.vault.hasher.PlainHasherImpl;
import com.pt.vault.tree.BucketTreeImpl;
import com.pt.vault.tree.BucketTreeUpdateInfo;

@Component
public class TheVaultImpl implements TheVault {

	@Autowired
	private AuditRecordRepository aRepo;

	@Autowired
	private HashBucketRepository hRepo;

	@Autowired
	private BucketTreeImpl bucketTree;

	@Autowired
	private SequenceGenDAO sDao;

	private HashEngine he = new PlainHasherImpl();

	public void setHashEngine(HashEngine he) {
		this.he = he;
	}

	public Long insertAuditRecord(AuditRecord record) throws Exception {

		record.setId(sDao.getNextNum(AuditRecord.SEQ_GEN_KEY));
		record.setHashValue(record.getEncryptedHashValue(he));

		BucketTreeUpdateInfo updatedBuckets = new BucketTreeUpdateInfo();

		// Find the bucket where this audit record will go.
		Long id = bucketTree.addEntry(updatedBuckets, record.getId(), record.getHashValue(), he);

		// Update the audit record with the bucket oid
		record.setHashBucketOID(id);

		// Save the audit record
		aRepo.save(record);

		// Save the changed/new buckets
		bucketTree.commit(updatedBuckets);

		return record.getId();

	}

	// TBD
	public boolean removeAuditRecords(List<Long> auditRecordOIDs)
			throws IllegalStateException, UnsupportedEncodingException {
		// This is just an optimized API to remove a set of OIDs and only
		// persist the updated buckets at the end
		return true;
	}

	public boolean removeAuditRecord(Long auditRecordOID) throws IllegalStateException, UnsupportedEncodingException {
		AuditRecord ar = aRepo.findOne(auditRecordOID);
		if (ar == null) {
			throw new RuntimeException("Audit Record with OID:" + auditRecordOID + " does not exist!!!");
		}

		BucketTreeUpdateInfo updatedBuckets = new BucketTreeUpdateInfo();
		bucketTree.removeEntry(updatedBuckets, ar.getId(), ar.getHashBucketOID(), he);

		aRepo.delete(ar);
		bucketTree.commit(updatedBuckets);

		return true;
	}

	public List<String> checkForTampering() throws IllegalStateException, UnsupportedEncodingException {
		List<String> checkResult = new ArrayList<String>();
		// Check all audit record hashes are still correct
		for (AuditRecord ar : aRepo.findAll()) {
			if (!ar.getEncryptedHashValue(he).equals(ar.getHashValue())) {
				checkResult.add("Audit Record check on hashValue failed!! " + ar.toString());
			}
			// Check that each Audit Record still points to an existing hash
			// bucket
			if (ar.getHashBucketOID() == null) {
				checkResult.add("Audit Record does not point to a Hash Bucket!! " + ar.toString());
			} else if (!hRepo.hashBucketExists(ar.getHashBucketOID())) {
				checkResult.add("Audit Record points to non-existing bucket!! " + ar.toString());
			}
		}
		// Check all hash bucket hashes are still correct
		for (HashBucket hb : hRepo.findAll()) {
			// Check hash values
			if (!hb.getEncryptedHashValue(he).equals(hb.getHashValue())) {
				checkResult.add("Hash Bucket check on hashValue failed!! " + hb.toString());
			}
			// If this Hash Bucket points to another Hash Bucket, it needs to
			// exists.
			if (hb.getParentHashBucketOID() != null) {
				if (!hRepo.hashBucketExists(hb.getParentHashBucketOID())) {
					checkResult
							.add("Hash Bucket references a parent Hash Bucket that does not exist!! " + hb.toString());
				}
			}

			if (hb.getSiblingHashBucketOID() != null) {
				if (!hRepo.hashBucketExists(hb.getSiblingHashBucketOID())) {
					checkResult
							.add("Hash Bucket references a sibling Hash Bucket that does not exist!! " + hb.toString());
				}
			}

			List<Long> oids = hb.getReferencedOIDs();

			if (hb.getBucketLevel() == null) {
				// Nothing todo... this is valid
			} else if (hb.getBucketLevel() == 1L) {
				// Check that each Audit Record mentioned in a Hash Bucket still
				// exists.
				for (Long arOID : oids) {
					if (!aRepo.auditRecordExists(arOID)) {
						checkResult.add("Hash Bucket points to non-existing Audit Record!! " + hb.toString());
					}
				}
			} else if (hb.getBucketLevel() > 1L) {
				// Check that each Hash Bucket mentioned in a non-level-1 Hash
				// Bucket still exists.
				for (Long hbOID : oids) {
					if (!hRepo.hashBucketExists(hbOID)) {
						checkResult
								.add("Hash Bucket points to non-existing higher level Hash Bucket!! " + hb.toString());
					}
				}
			}
		}
		return checkResult;
	}
}
