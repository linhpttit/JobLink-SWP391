package com.joblink.joblink.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JobBookmarkDao {
	private final JdbcTemplate jdbc;

	public JobBookmarkDao(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public boolean exists(int seekerId, long jobId) {
		Integer cnt = jdbc.queryForObject(
			"SELECT COUNT(1) FROM JobBookmarks WHERE seeker_id = ? AND job_id = ?",
			Integer.class, seekerId, jobId
		);
		return cnt != null && cnt > 0;
	}

	public void add(int seekerId, long jobId) {
		jdbc.update(
			"INSERT INTO JobBookmarks(seeker_id, job_id) VALUES(?, ?)",
			seekerId, jobId
		);
	}

	public int remove(int seekerId, long jobId) {
		return jdbc.update(
			"DELETE FROM JobBookmarks WHERE seeker_id = ? AND job_id = ?",
			seekerId, jobId
		);
	}

	public List<Long> listJobIdsBySeeker(int seekerId) {
		return jdbc.query(
			"SELECT job_id FROM JobBookmarks WHERE seeker_id = ? ORDER BY created_at DESC",
			(rs, rowNum) -> rs.getLong("job_id"),
			seekerId
		);
	}
}


