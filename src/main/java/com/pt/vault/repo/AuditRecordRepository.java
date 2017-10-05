package com.pt.vault.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pt.vault.data.AuditRecord;

@Repository
public interface AuditRecordRepository extends CrudRepository<AuditRecord, Long> {

	@Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END FROM AuditRecord ar WHERE ar.id = :oid")
	boolean auditRecordExists(@Param("oid") Long oid);

}
