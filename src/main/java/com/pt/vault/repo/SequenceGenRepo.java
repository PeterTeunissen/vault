package com.pt.vault.repo;

import org.springframework.data.repository.CrudRepository;

import com.pt.vault.data.SequenceGen;

public interface SequenceGenRepo extends CrudRepository<SequenceGen, String> {

}
