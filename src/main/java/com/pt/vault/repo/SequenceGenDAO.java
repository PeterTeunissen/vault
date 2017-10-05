package com.pt.vault.repo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pt.vault.data.AuditRecord;
import com.pt.vault.data.HashBucket;
import com.pt.vault.data.SequenceGen;

@Component
public class SequenceGenDAO {

	@Autowired
	private SequenceGenRepo repo;

	private Map<String, Long> offsets = new HashMap<String, Long>();

	public SequenceGenDAO() {
		offsets.put(AuditRecord.SEQ_GEN_KEY, 0L);
		offsets.put(HashBucket.SEQ_GEN_KEY, 999L);
	}

	public void setRangeOffset(String key, Long offset) {
		offsets.put(key, offset);
	}

	public synchronized Long getNextNum(String key) {
		Long v = null;
		SequenceGen s = repo.findOne(key);
		if (s == null) {
			s = new SequenceGen(key);
			if (offsets.get(key) != null) {
				s.setLastUsed(offsets.get(key));
			}
		}
		v = s.getNextNum();
		repo.save(s);
		return v;
	}
}
