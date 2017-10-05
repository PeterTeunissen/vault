package com.pt.vault.data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SequenceGen {

	@Id
	private String key = "<not initialized>";
	private Long lastUsed = 0L;
	private String recordType = "SequenceGen";

	public SequenceGen() {

	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public SequenceGen(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Long getLastUsed() {
		return lastUsed;
	}

	public void setLastUsed(Long lastUsed) {
		this.lastUsed = lastUsed;
	}

	public Long getNextNum() {
		lastUsed++;
		return lastUsed;
	}

	public String toString() {
		return "S key:" + key + " lastUsed:" + lastUsed;
	}
}
