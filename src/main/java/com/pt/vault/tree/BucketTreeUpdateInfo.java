package com.pt.vault.tree;

import java.util.HashMap;
import java.util.Map;

import com.pt.vault.data.HashBucket;

public class BucketTreeUpdateInfo {

	private Map<Long, HashBucket> bucketsToUpdate = new HashMap<Long, HashBucket>();

	public void setChangedBucket(HashBucket hb) {
		bucketsToUpdate.put(hb.getId(), hb);
	}

	public Map<Long, HashBucket> getBucketsToUpdate() {
		return bucketsToUpdate;
	}

	public void setBucketsToUpdate(Map<Long, HashBucket> bucketsToUpdate) {
		this.bucketsToUpdate = bucketsToUpdate;
	}
}
