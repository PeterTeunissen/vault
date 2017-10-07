package com.pt.vault.data;

public class TamperError {

	public enum RecordType {
		HASH_BUCKET, AUDIT_RECORD;
	}

	public enum ErrorType {
		AUDIT_RECORD_HASH_FAIL, AUDIT_RECORD_NO_HASH_BUCKET, AUDIT_RECORD_NON_EXISTING_HASH_BUCKET, HASH_BUCKET_HASH_FAIL, HASH_BUCKET_NON_EXISTING_AUDIT_RECORD, HASH_BUCKET_NON_EXISTING_PARENT, HASH_BUCKET_NON_EXISTING_CHILD, HASH_BUCKET_REF_HASH_NOT_SAME
	}

	private String error;
	private Long oid;
	private RecordType recordType;
	private ErrorType errorType;

	public TamperError(RecordType recType, ErrorType errType, String errorText, Long oid) {
		this.recordType = recType;
		this.errorType = errType;
		this.error = errorText;
		this.oid = oid;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Long getOid() {
		return oid;
	}

	public void setOid(Long oid) {
		this.oid = oid;
	}

	public ErrorType getErrorType() {
		return errorType;
	}

	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}

	public RecordType getRecordType() {
		return recordType;
	}

	public void setRecordType(RecordType recordType) {
		this.recordType = recordType;
	}

	public String toString() {
		return "TamperError {RecordType=" + recordType.name() + ",ErrorType=" + errorType.name() + ",Error=" + error
				+ ",OID=" + oid + "}";
	}
}
