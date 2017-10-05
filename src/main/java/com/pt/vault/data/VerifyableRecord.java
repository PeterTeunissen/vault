package com.pt.vault.data;

import java.io.UnsupportedEncodingException;

import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.pt.vault.hasher.HashEngine;

@MappedSuperclass
public abstract class VerifyableRecord {

	protected String hashValue;

	public String getHashValue() {
		return hashValue;
	}

	public void setHashValue(String hashValue) {
		this.hashValue = hashValue;
	}

	public abstract void addHashableFields(HashCodeBuilder hcb);

	public String getEncryptedHashValue(HashEngine he) throws IllegalStateException, UnsupportedEncodingException {
		HashCodeBuilder hcb = new HashCodeBuilder(17, 37);
		addHashableFields(hcb);
		return he.getSecretHash(String.valueOf(hcb.toHashCode()));
	}

	public boolean isHashOK(HashEngine he) throws IllegalStateException, UnsupportedEncodingException {
		return getEncryptedHashValue(he).equals(getHashValue());
	}

}
