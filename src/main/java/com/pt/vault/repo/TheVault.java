package com.pt.vault.repo;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.pt.vault.data.AuditRecord;
import com.pt.vault.data.TamperError;
import com.pt.vault.data.TamperError.RecordType;
import com.pt.vault.hasher.HashEngine;

public interface TheVault {

	public void setHashEngine(HashEngine he);

	public Long insertAuditRecord(AuditRecord record) throws IllegalStateException, UnsupportedEncodingException;

	public void insertAuditRecords(List<AuditRecord> records)
			throws IllegalStateException, UnsupportedEncodingException;

	public boolean removeAuditRecord(Long auditRecordOID) throws IllegalStateException, UnsupportedEncodingException;

	public boolean removeAuditRecords(List<Long> auditRecordOIDs)
			throws IllegalStateException, UnsupportedEncodingException;

	public List<TamperError> checkForTampering() throws IllegalStateException, UnsupportedEncodingException;

	public void fixTampering(Long oid, RecordType recordType, String key, String encryptedKey)
			throws IllegalStateException, UnsupportedEncodingException;
}
