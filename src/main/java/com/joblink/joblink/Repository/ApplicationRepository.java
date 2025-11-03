package com.joblink.joblink.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.joblink.joblink.entity.Application;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

	boolean existsByJobIdAndSeekerId(Integer jobId, Integer seekerId);

	Page<Application> findAll(Specification<Application> spec, Pageable pageable);

	List<Application> findAll(Specification<Application> spec, Sort by);

	long count(Specification<Application> spec);

	List<Application> findByJobIdInAndAppliedAtBetween(Collection<Integer> jobIds, LocalDateTime from, LocalDateTime to,
			Sort sort);

	long countByJobIdInAndAppliedAtBetween(Collection<Integer> jobIds, LocalDateTime from, LocalDateTime to);

	long countByJobIdInAndStatusAndAppliedAtBetween(Collection<Integer> jobIds, String status, LocalDateTime from,
			LocalDateTime to);

	List<Application> findByJobIdIn(Collection<Integer> jobIds, Sort sort);

	long countByJobIdIn(Collection<Integer> jobIds);

	long countByJobIdInAndStatus(Collection<Integer> jobIds, String status);
	
	long countByJobId(Integer jobId);

    @Query("select a.jobId as jobId, count(a) as cnt from Application a where a.jobId in :ids group by a.jobId")
    List<Map<String, Object>> countGroupedByJobId(@Param("ids") Collection<Integer> ids);
}