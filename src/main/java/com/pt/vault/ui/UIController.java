package com.pt.vault.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pt.vault.data.AuditRecord;
import com.pt.vault.data.HashBucket;
import com.pt.vault.data.SequenceGen;
import com.pt.vault.repo.AuditRecordRepository;
import com.pt.vault.repo.HashBucketRepository;
import com.pt.vault.repo.SequenceGenRepo;
import com.pt.vault.repo.TheVaultImpl;

@Component
@Controller
public class UIController {

	@Autowired
	private AuditRecordRepository aRepo;

	@Autowired
	private HashBucketRepository hRepo;

	@Autowired
	private SequenceGenRepo sRepo;

	@Autowired
	private TheVaultImpl vault;

	@RequestMapping("/buckets")
	@ResponseBody
	public List<HashBucket> getBuckets() {
		List<HashBucket> allBuckets = new ArrayList<HashBucket>();
		for (HashBucket hb : hRepo.findAll()) {
			allBuckets.add(hb);
		}
		return allBuckets;
	}

	@RequestMapping("/records")
	@ResponseBody
	public List<AuditRecord> getAuditRecord() {
		List<AuditRecord> allRecords = new ArrayList<AuditRecord>();
		for (AuditRecord ar : aRepo.findAll()) {
			allRecords.add(ar);
		}
		return allRecords;

	}

	@RequestMapping("/check")
	@ResponseBody
	public Object checkTamper() {
		try {
			return vault.checkForTampering();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@RequestMapping("/addData")
	@ResponseBody
	public Object addRecord(@RequestParam(defaultValue = "", required = true, name = "data") String auditData)
			throws Exception {
		AuditRecord ar = new AuditRecord();
		ar.setD1(auditData);
		vault.insertAuditRecord(ar);
		return dump();
	}

	@RequestMapping("/addSimpleData")
	@ResponseBody
	public Object addSimpleRecord(@RequestParam(defaultValue = "", required = true, name = "data") String auditData)
			throws Exception {
		AuditRecord ar = new AuditRecord();
		ar.setD1(auditData);
		Long a = vault.insertAuditRecord(ar);
		return a;
	}

	@RequestMapping("/removeRecord")
	@ResponseBody
	public Object removeRecord(@RequestParam(defaultValue = "", required = true, name = "oid") Long oid)
			throws IllegalStateException, UnsupportedEncodingException {
		vault.removeAuditRecord(oid);
		return dump();
	}

	@RequestMapping("/removeAll")
	@ResponseBody
	public Object removeRecord(@RequestParam(defaultValue = "", required = true, name = "confirm") String confirm)
			throws IllegalStateException, UnsupportedEncodingException {
		if (confirm.equals("Yes")) {
			hRepo.deleteAll();
			aRepo.deleteAll();
			sRepo.deleteAll();
		}
		return dump();
	}

	@RequestMapping("/dump")
	@ResponseBody
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
}
