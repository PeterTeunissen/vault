package com.pt.vault.tree;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pt.vault.data.HashBucket;
import com.pt.vault.hasher.HashEngine;
import com.pt.vault.repo.HashBucketRepository;
import com.pt.vault.repo.SequenceGenDAO;

@Component
public class BucketTreeImpl implements BucketTree {

	@Autowired
	private HashBucketRepository hRepo;

	@Autowired
	private SequenceGenDAO sDao;

	public Long addEntry(BucketTreeUpdateInfo updateInfo, Long oid, String hashValue, HashEngine he)
			throws IllegalStateException, UnsupportedEncodingException {

		Long bucketOid = addEntry(updateInfo, oid, hashValue, 1L, he);

		return bucketOid;
	}

	private Long addEntry(BucketTreeUpdateInfo updateInfo, Long oid, String hashValue, Long level, HashEngine he)
			throws IllegalStateException, UnsupportedEncodingException {

		HashBucket hb = getMaxBucketForLevel(updateInfo, level);

		if (hb == null) {
			hb = new HashBucket();
			hb.setId(sDao.getNextNum(HashBucket.SEQ_GEN_KEY));
			hb.setBucketLevel(level);
			hb.addEntry(oid, hashValue);
			hb.setHashValue(hb.getEncryptedHashValue(he));
			updateInfo.setChangedBucket(hb);
			return hb.getId();
		}

		if (hb.getEntriesUsed() < hb.getBucketSize()) {
			hb.addEntry(oid, hashValue);
			hb.setHashValue(hb.getEncryptedHashValue(he));
			updateInfo.setChangedBucket(hb);
			bubbleUpChange(updateInfo, hb, he);
			return hb.getId();
		} else {
			HashBucket sibling = new HashBucket();
			sibling.setId(sDao.getNextNum(HashBucket.SEQ_GEN_KEY));
			sibling.setBucketLevel(level);
			sibling.addEntry(oid, hashValue);
			sibling.setHashValue(sibling.getEncryptedHashValue(he));
			updateInfo.setChangedBucket(sibling);

			if (hb.getParentHashBucketOID() == null) {
				HashBucket parent = new HashBucket();
				parent.setId(sDao.getNextNum(HashBucket.SEQ_GEN_KEY));
				parent.setBucketLevel(level + 1);
				parent.addEntry(hb.getId(), hb.getHashValue());
				parent.addEntry(sibling.getId(), sibling.getHashValue());
				parent.setHashValue(parent.getEncryptedHashValue(he));
				sibling.setParentHashBucketOID(parent.getId());
				hb.setParentHashBucketOID(parent.getId());
				updateInfo.setChangedBucket(parent);
				return sibling.getId();
			} else {
				long parentoid = addEntry(updateInfo, sibling.getId(), sibling.getHashValue(), level + 1, he);
				sibling.setParentHashBucketOID(parentoid);
				return sibling.getId();
			}
		}
	}

	private void bubbleUpChange(BucketTreeUpdateInfo updateInfo, HashBucket hb, HashEngine he)
			throws IllegalStateException, UnsupportedEncodingException {
		HashBucket child = hb;
		Long parentOID = hb.getParentHashBucketOID();
		while (parentOID != null) {
			HashBucket parent = getBucket(updateInfo, parentOID);
			if (child.getEntriesUsed() == 0) {
				parent.removeEntry(child.getId());
			} else {
				parent.updateEntry(child.getId(), child.getHashValue());
			}
			parent.setHashValue(parent.getEncryptedHashValue(he));
			updateInfo.setChangedBucket(parent);
			child = parent;
			parentOID = parent.getParentHashBucketOID();
		}
	}

	private HashBucket getBucket(BucketTreeUpdateInfo updateInfo, Long oid) {
		HashBucket hb = updateInfo.getBucketsToUpdate().get(oid);
		if (hb == null) {
			hb = hRepo.findOne(oid);
		}
		return hb;
	}

	private HashBucket getMaxBucketForLevel(BucketTreeUpdateInfo updateInfo, Long level) {
		Long maxOID = 0L;
		HashBucket maxBucket = null;
		for (Long oid : updateInfo.getBucketsToUpdate().keySet()) {
			HashBucket hb = updateInfo.getBucketsToUpdate().get(oid);
			if (hb.getBucketLevel().equals(level)) {
				if (oid > maxOID) {
					maxOID = oid;
					maxBucket = hb;
				}
			}
		}
		if (maxBucket == null) {
			maxBucket = hRepo.getLastBucketForLevel(level);
		}
		return maxBucket;
	}

	public void removeEntry(BucketTreeUpdateInfo updateInfo, Long oid, Long bucketID, HashEngine he)
			throws IllegalStateException, UnsupportedEncodingException {

		HashBucket hb = getBucket(updateInfo, bucketID);

		if (hb == null) {
			throw new RuntimeException("Bucket with OID:" + bucketID + " not found!!");
		}

		hb.removeEntry(oid);
		hb.setHashValue(hb.getEncryptedHashValue(he));
		updateInfo.setChangedBucket(hb);
		bubbleUpChange(updateInfo, hb, he);
	}

	public void commit(BucketTreeUpdateInfo updateInfo) {
		for (Long oid : updateInfo.getBucketsToUpdate().keySet()) {
			HashBucket hb = updateInfo.getBucketsToUpdate().get(oid);
			if (hb.getEntriesUsed() == 0) {
				hRepo.delete(oid);

			} else {
				hRepo.save(hb);
			}
		}
	}
}
