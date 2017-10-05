package com.pt.vault;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.pt.vault.data.AuditRecord;
import com.pt.vault.data.HashBucket;
import com.pt.vault.data.SequenceGen;
import com.pt.vault.repo.AuditRecordRepository;
import com.pt.vault.repo.HashBucketRepository;
import com.pt.vault.repo.SequenceGenDAO;
import com.pt.vault.repo.SequenceGenRepo;

@WebAppConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
public class VaultApplicationTests {

	@Autowired
	private AuditRecordRepository aRepo;

	@Autowired
	private HashBucketRepository hRepo;

	@Autowired
	private SequenceGenRepo sRepo;

	@Autowired
	private SequenceGenDAO sDao;

	@Before
	public void cleanup() {
		aRepo.deleteAll();
		hRepo.deleteAll();
	}

	@Test
	public void testAuditRepo() {
		AuditRecord ar = new AuditRecord();
		ar.setId(1L);
		ar.setD1("one");
		ar.setD2("two");

		aRepo.save(ar);

		for (AuditRecord auditRecord : aRepo.findAll()) {
			System.out.println(auditRecord.toString());
		}
	}

	@Test
	public void testHashRepo() {
		HashBucket b = new HashBucket();
		b.setBucketSize(10);
		b.setBucketLevel(2L);
		b.setId(10L);

		String j = "{\"10\":\"h1\",\"20\":\"h2\"}";

		b.setRecords(j);
		hRepo.save(b);

		Long m = hRepo.getHighestBucket();
		System.out.println("Max:" + m);

		for (HashBucket hb : hRepo.findAll()) {
			System.out.println(hb.toString());
		}

		b.addEntry(30L, "h3");
		hRepo.save(b);

		for (HashBucket hb : hRepo.findAll()) {
			System.out.println(hb.toString());
		}

		b.addEntry(40L, "h4");
		hRepo.save(b);

		for (HashBucket hb : hRepo.findAll()) {
			System.out.println(hb.toString());
		}

	}

	@Test
	public void testSeqGenRepo() {
		SequenceGen g = sRepo.findOne("NotUsed");
		assert g == null;

		g = new SequenceGen("Used");
		assert g.getLastUsed() == 0;

		assert g.getNextNum() == 1;
		sRepo.save(g);

		for (SequenceGen g2 : sRepo.findAll()) {
			System.out.println(g2.toString());
		}
	}

	@Test
	public void testSequenceGenDao() {
		System.out.println("----------------");
		assert 1 == sDao.getNextNum("new");
		for (SequenceGen g2 : sRepo.findAll()) {
			System.out.println(g2.toString());
		}
		System.out.println("----------------");
		assert 2 == sDao.getNextNum("new");
		for (SequenceGen g2 : sRepo.findAll()) {
			System.out.println(g2.toString());
		}
		System.out.println("----------------");
		assert 1 == sDao.getNextNum("new2");
		for (SequenceGen g2 : sRepo.findAll()) {
			System.out.println(g2.toString());
		}
		System.out.println("----------------");
	}
}
