package com.pt.vault.repo;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.pt.vault.data.AuditRecord;
import com.pt.vault.hasher.HashEngine;

public interface TheVault {

	public void setHashEngine(HashEngine he);

	public Long insertAuditRecord(AuditRecord record) throws Exception;

	public boolean removeAuditRecord(Long auditRecordOID) throws IllegalStateException, UnsupportedEncodingException;

	public boolean removeAuditRecords(List<Long> auditRecordOIDs)
			throws IllegalStateException, UnsupportedEncodingException;

	public List<String> checkForTampering() throws IllegalStateException, UnsupportedEncodingException;

}
