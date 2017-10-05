package com.pt.vault.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pt.vault.data.HashBucket;

@Repository
public interface HashBucketRepository extends CrudRepository<HashBucket, Long> {

	@Query("SELECT CASE WHEN COUNT(hb) > 0 THEN true ELSE false END FROM HashBucket hb WHERE hb.id = :oid")
	boolean hashBucketExists(@Param("oid") Long oid);

	@Query("SELECT hb FROM HashBucket hb WHERE hb.bucketLevel = :level AND bucketSize<>entriesUsed")
	List<HashBucket> findOpenBucketForLevel(@Param("level") Long level);

	@Query("SELECT hb FROM HashBucket hb WHERE hb.bucketLevel = :level AND hb.siblingHashBucketOID is null")
	List<HashBucket> findLastBucketForLevel(@Param("level") Long level);

	@Query("SELECT max(hb.bucketLevel) FROM HashBucket hb")
	Long getHighestBucket();

	@Query("SELECT hb FROM HashBucket hb where hb.id = (select max(hb.id) FROM HashBucket hb where hb.bucketLevel=:level)")
	HashBucket getLastBucketForLevel(@Param("level") Long level);
}
