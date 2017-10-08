package com.pt.vault.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pt.vault.data.AuditRecord;
import com.pt.vault.data.HashBucket;
import com.pt.vault.data.SequenceGen;
import com.pt.vault.data.TamperError.RecordType;
import com.pt.vault.repo.AuditRecordRepository;
import com.pt.vault.repo.HashBucketRepository;
import com.pt.vault.repo.SequenceGenRepo;
import com.pt.vault.repo.TheVaultImpl;

@RestController
@RequestMapping("/vault")
public class UIController {

	@Autowired
	private AuditRecordRepository aRepo;

	@Autowired
	private HashBucketRepository hRepo;

	@Autowired
	private SequenceGenRepo sRepo;

	@Autowired
	private TheVaultImpl vault;

	@RequestMapping(value = "/buckets", method = RequestMethod.GET)
	public List<HashBucket> getBuckets() {
		List<HashBucket> allBuckets = new ArrayList<HashBucket>();
		for (HashBucket hb : hRepo.findAll()) {
			allBuckets.add(hb);
		}
		return allBuckets;
	}

	@RequestMapping(value = "/records", method = RequestMethod.GET)
	public List<AuditRecord> getAuditRecord() {
		List<AuditRecord> allRecords = new ArrayList<AuditRecord>();
		for (AuditRecord ar : aRepo.findAll()) {
			allRecords.add(ar);
		}
		return allRecords;

	}

	@RequestMapping(value = "/check", method = RequestMethod.GET)
	public Object checkTamper() {
		try {
			return vault.checkForTampering();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@RequestMapping(value = "/addData", method = RequestMethod.GET)
	public Object addRecord(@RequestParam(defaultValue = "", required = true, name = "data") String auditData)
			throws Exception {
		AuditRecord ar = new AuditRecord();
		ar.setD1(auditData);
		vault.insertAuditRecord(ar);
		return dump();
	}

	@RequestMapping(value = "/addSimpleData", method = RequestMethod.GET)
	public Object addSimpleRecord(@RequestParam(defaultValue = "", required = true, name = "data") String auditData)
			throws Exception {
		AuditRecord ar = new AuditRecord();
		ar.setD1(auditData);
		Long a = vault.insertAuditRecord(ar);
		return a;
	}

	@RequestMapping(value = "/removeRecord", method = RequestMethod.GET)
	public Object removeRecord(@RequestParam(defaultValue = "", required = true, name = "oid") Long oid)
			throws IllegalStateException, UnsupportedEncodingException {
		vault.removeAuditRecord(oid);
		return dump();
	}

	@RequestMapping(value = "/removeAll", method = RequestMethod.GET)
	public Object removeRecord(@RequestParam(defaultValue = "", required = true, name = "confirm") String confirm)
			throws IllegalStateException, UnsupportedEncodingException {
		if (confirm.equals("Yes")) {
			hRepo.deleteAll();
			aRepo.deleteAll();
			sRepo.deleteAll();
		}
		return dump();
	}

	@RequestMapping(value = "/dump", method = RequestMethod.GET)
	public List<Object> dump() {
		List<Object> db = new ArrayList<Object>();
		for (AuditRecord ar : aRepo.findAll()) {
			db.add(ar);
		}
		for (HashBucket hb : hRepo.findAll()) {
			db.add(hb);
		}
		for (SequenceGen sg : sRepo.findAll()) {
			db.add(sg);
		}
		return db;
	}

	@RequestMapping(value = "/hbtamper", method = RequestMethod.GET)
	public Object hbtamper() {
		for (HashBucket hb : hRepo.findAll()) {
			hb.setHashValue(hb.getHashValue() + " ** Tamper **");
			hRepo.save(hb);
			break;
		}

		return dump();
	}

	@RequestMapping(value = "/artamper", method = RequestMethod.GET)
	public Object artamper() {
		for (AuditRecord ar : aRepo.findAll()) {
			ar.setHashValue(ar.getHashValue() + " ** Tamper **");
			aRepo.save(ar);
			break;
		}

		return dump();
	}

	@RequestMapping(value = "/arFixTamper", method = RequestMethod.POST)
	public Object arFixTampering(@RequestParam(name = "oid", defaultValue = "", required = true) Long oid,
			@RequestParam(name = "recordType", defaultValue = "", required = true) RecordType recordType,
			@RequestParam(name = "key", defaultValue = "", required = true) String key,
			@RequestParam(name = "encryptedKey", defaultValue = "", required = true) String encryptedKey)
			throws IllegalStateException, UnsupportedEncodingException {

		vault.fixTampering(oid, recordType, key, encryptedKey);
		return checkTamper();
	}

}
