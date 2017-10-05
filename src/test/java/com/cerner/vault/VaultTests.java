package com.cerner.vault;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.pt.vault.data.AuditRecord;
import com.pt.vault.data.HashBucket;
import com.pt.vault.repo.AuditRecordRepository;
import com.pt.vault.repo.HashBucketRepository;
import com.pt.vault.repo.TheVaultImpl;

@WebAppConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
public class VaultTests {

	@Autowired
	private AuditRecordRepository aRepo;

	@Autowired
	private HashBucketRepository hRepo;

	@Autowired
	private TheVaultImpl vault;

	@Before
	public void cleanup() {
		aRepo.deleteAll();
		hRepo.deleteAll();
	}

	@Test
	public void auditRecordTamperTest() throws Exception {
		AuditRecord ar = new AuditRecord();
		ar.setD1("d1");
		ar.setD2("d2");

		Long id = vault.insertAuditRecord(ar);

		// Check should pass
		List<String> errors = vault.checkForTampering();
		System.out.println(errors);
		assert errors.isEmpty() == true;

		// Tamper with auditRecord
		AuditRecord a = aRepo.findOne(id);
		a.setD1("TamperedD1");
		aRepo.save(a);

		// Check should fail now
		errors = vault.checkForTampering();
		System.out.println(errors);
		assert errors.isEmpty() == false;

	}

	@Test
	public void hashBucketTamperTest() throws Exception {
		AuditRecord ar = new AuditRecord();
		ar.setD1("d1");
		ar.setD2("d2");

		Long id = vault.insertAuditRecord(ar);

		// Check should pass
		List<String> errors = vault.checkForTampering();
		System.out.println(errors);
		assert errors.isEmpty() == true;

		ar = aRepo.findOne(id);

		// Tamper with hashBucket
		HashBucket hb = hRepo.findOne(ar.getHashBucketOID());

		// Not updating the hashvalue after this removeEntry makes this an
		// invalid bucket!
		hb.removeEntry(ar.getId());
		// Save it anyway!
		hRepo.save(hb);

		// Check should fail now
		errors = vault.checkForTampering();
		System.out.println(errors);
		assert errors.isEmpty() == false;

	}

}
