package com.pt.vault.repo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pt.vault.data.AuditRecord;
import com.pt.vault.data.HashBucket;
import com.pt.vault.data.TamperError;
import com.pt.vault.data.TamperError.ErrorType;
import com.pt.vault.data.TamperError.RecordType;
import com.pt.vault.hasher.HashEngine;
import com.pt.vault.hasher.PlainHasherImpl;
import com.pt.vault.tree.BucketTree;
import com.pt.vault.tree.BucketTreeUpdateInfo;

@Component
public class TheVaultImpl implements TheVault {

	@Autowired
	private AuditRecordRepository aRepo;

	@Autowired
	private HashBucketRepository hRepo;

	@Autowired
	private BucketTree bucketTree;

	@Autowired
	private SequenceGenDAO sDao;

	private HashEngine he = new PlainHasherImpl();

	public void setHashEngine(HashEngine he) {
		this.he = he;
	}

	public Long insertAuditRecord(AuditRecord record) throws IllegalStateException, UnsupportedEncodingException {

		if (record.getId() == null) {
			record.setId(sDao.getNextNum(AuditRecord.SEQ_GEN_KEY));
		}
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

	// This optimization does not persist anything in the database until the
	// end, thus reducing the number of writes a little.
	public void insertAuditRecords(List<AuditRecord> records)
			throws IllegalStateException, UnsupportedEncodingException {

		BucketTreeUpdateInfo updatedBuckets = new BucketTreeUpdateInfo();

		for (AuditRecord auditRecord : records) {
			auditRecord.setId(sDao.getNextNum(AuditRecord.SEQ_GEN_KEY));
			auditRecord.setHashValue(auditRecord.getEncryptedHashValue(he));
			auditRecord.setHashBucketOID(
					bucketTree.addEntry(updatedBuckets, auditRecord.getId(), auditRecord.getHashValue(), he));
		}

		// Save all the audit records
		aRepo.save(records);

		// Save the changed/new buckets
		bucketTree.commit(updatedBuckets);
	}

	public boolean removeAuditRecord(Long auditRecordOID) throws IllegalStateException, UnsupportedEncodingException {
		AuditRecord ar = aRepo.findOne(auditRecordOID);
		if (ar == null) {
			throw new RuntimeException("Audit Record with OID:" + auditRecordOID + " does not exist!!!");
		}

		BucketTreeUpdateInfo updatedBuckets = new BucketTreeUpdateInfo();
		bucketTree.removeEntry(updatedBuckets, ar.getId(), ar.getHashBucketOID(), he);

		// Delete the audit record
		aRepo.delete(ar);

		// Save the changed/new buckets
		bucketTree.commit(updatedBuckets);

		return true;
	}

	// This is just an optimized API to remove a set of OIDs and only
	// persist the updated buckets at the end, thus reducing the number of
	// writes.
	public boolean removeAuditRecords(List<Long> auditRecordOIDs)
			throws IllegalStateException, UnsupportedEncodingException {

		BucketTreeUpdateInfo updatedBuckets = new BucketTreeUpdateInfo();
		List<AuditRecord> recordsToDelete = new ArrayList<AuditRecord>();

		for (Long arOID : auditRecordOIDs) {
			AuditRecord ar = aRepo.findOne(arOID);
			if (ar == null) {
				throw new RuntimeException("Audit Record with OID:" + arOID + " does not exist!!!");
			}

			bucketTree.removeEntry(updatedBuckets, ar.getId(), ar.getHashBucketOID(), he);
			recordsToDelete.add(ar);
		}

		// Delete the audit records
		aRepo.delete(recordsToDelete);

		// Save the changed/new buckets
		bucketTree.commit(updatedBuckets);

		return true;
	}

	// This walks the entire structure and checks the integrity of audit records
	// and has buckets.
	// This probably needs an optimization and only do a set of AuditRecord OIDs
	// as a sample. Doing the entire database can take a very long time once we
	// load 100000s of records.
	public List<TamperError> checkForTampering() throws IllegalStateException, UnsupportedEncodingException {
		List<TamperError> checkResult = new ArrayList<TamperError>();
		// Check all audit record hashes are still correct
		for (AuditRecord ar : aRepo.findAll()) {
			if (!ar.getEncryptedHashValue(he).equals(ar.getHashValue())) {
				checkResult.add(new TamperError(RecordType.AUDIT_RECORD, ErrorType.AUDIT_RECORD_HASH_FAIL,
						"Audit Record check on hashValue failed!! " + ar.toString(), ar.getId()));
			}
			// Check that each Audit Record still points to an existing hash
			// bucket
			if (ar.getHashBucketOID() == null) {
				checkResult.add(new TamperError(RecordType.AUDIT_RECORD, ErrorType.AUDIT_RECORD_NO_HASH_BUCKET,
						"Audit Record does not point to a Hash Bucket!! " + ar.toString(), ar.getId()));
			} else if (!hRepo.hashBucketExists(ar.getHashBucketOID())) {
				checkResult
						.add(new TamperError(RecordType.AUDIT_RECORD, ErrorType.AUDIT_RECORD_NON_EXISTING_HASH_BUCKET,
								"Audit Record points to non-existing bucket!! " + ar.toString(), ar.getId()));
			}
		}
		// Check all hash bucket hashes are still correct
		for (HashBucket hb : hRepo.findAll()) {
			// Check hash values
			if (!hb.getEncryptedHashValue(he).equals(hb.getHashValue())) {
				checkResult.add(new TamperError(RecordType.HASH_BUCKET, ErrorType.HASH_BUCKET_HASH_FAIL,
						"Hash Bucket check on hashValue failed!! " + hb.toString(), hb.getId()));
			}
			// If this Hash Bucket points to another Hash Bucket, it needs to
			// exists.
			if (hb.getParentHashBucketOID() != null) {
				if (!hRepo.hashBucketExists(hb.getParentHashBucketOID())) {
					checkResult.add(new TamperError(RecordType.HASH_BUCKET, ErrorType.HASH_BUCKET_NON_EXISTING_PARENT,
							"Hash Bucket references a parent Hash Bucket that does not exist!! " + hb.toString(),
							hb.getId()));
				}
			}

			// This list will be a list of referenced AuditRecords or
			// HashBuckets
			List<Long> oids = hb.getReferencedOIDs();

			if (hb.getBucketLevel() == null) {
				// Nothing todo... this is valid
			} else if (hb.getBucketLevel() == 1L) {
				// Check that each Audit Record mentioned in a Hash Bucket still
				// exists.
				for (Long arOID : oids) {
					if (!aRepo.auditRecordExists(arOID)) {
						checkResult.add(new TamperError(RecordType.HASH_BUCKET,
								ErrorType.HASH_BUCKET_NON_EXISTING_AUDIT_RECORD,
								"Hash Bucket points to non-existing Audit Record!! " + hb.toString(), hb.getId()));

					} else {
						AuditRecord ar = aRepo.findOne(arOID);
						if (!ar.getHashValue().equals(hb.getHashValueOfOid(arOID))) {
							checkResult.add(
									new TamperError(RecordType.HASH_BUCKET, ErrorType.HASH_BUCKET_REF_HASH_NOT_SAME,
											"Hash Bucket's entry hash does not match Audit Record hash (for ARoid="
													+ arOID + ") " + hb.toString(),
											hb.getId()));
						}
					}
				}
			} else if (hb.getBucketLevel() > 1L) {
				// Check that each Hash Bucket mentioned in a non-level-1 Hash
				// Bucket still exists.
				for (Long hbOID : oids) {
					if (!hRepo.hashBucketExists(hbOID)) {
						checkResult.add(new TamperError(RecordType.HASH_BUCKET,
								ErrorType.HASH_BUCKET_NON_EXISTING_CHILD,
								"Hash Bucket points to non-existing higher level Hash Bucket!! " + hb.toString(),
								hb.getId()));
					} else {
						HashBucket hbr = hRepo.findOne(hbOID);
						if (!hbr.getHashValue().equals(hb.getHashValueOfOid(hbOID))) {
							checkResult.add(
									new TamperError(RecordType.HASH_BUCKET, ErrorType.HASH_BUCKET_REF_HASH_NOT_SAME,
											"Hash Bucket's entry hash does not match child Bucket hash (for HBoid="
													+ hbOID + ") " + hb.toString(),
											hb.getId()));
						}

					}
				}
			}
		}
		return checkResult;
	}

	// Once we detected tampering, we probably need a way to 'reset' the
	// structure so that going forward we know the data is correct again.
	public void fixTampering(Long oid, RecordType recordType, String key, String encryptedKey)
			throws IllegalStateException, UnsupportedEncodingException {

		if (!he.getSecretHash(key).equals(encryptedKey)) {
			throw new RuntimeException("The passed-in key and encrypted values do not match.");
		}

		if (recordType == RecordType.AUDIT_RECORD) {
			AuditRecord ar = aRepo.findOne(oid);
			if (ar == null) {
				throw new RuntimeException("AuditRecord with OID=" + oid + " does not exist!!");
			}
			ar.setHashBucketOID(null);
			insertAuditRecord(ar);
		} else if (recordType == RecordType.HASH_BUCKET) {
			// TBD
			// THis is more tricky...
		}

	}
}
