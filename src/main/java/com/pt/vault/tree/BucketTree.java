package com.pt.vault.tree;

import java.io.UnsupportedEncodingException;

import com.pt.vault.hasher.HashEngine;

public interface BucketTree {

	public Long addEntry(BucketTreeUpdateInfo updateInfo, Long oid, String hashValue, HashEngine he)
			throws IllegalStateException, UnsupportedEncodingException;

	public void removeEntry(BucketTreeUpdateInfo updateInfo, Long oid, Long bucketID, HashEngine he)
			throws IllegalStateException, UnsupportedEncodingException;

	public void commit(BucketTreeUpdateInfo updateInfo);
}
