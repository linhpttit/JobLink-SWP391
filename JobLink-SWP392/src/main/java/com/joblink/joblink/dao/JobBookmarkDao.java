package com.joblink.joblink.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public List<Map<String, Object>> getBookmarkedJobsWithDetails(int seekerId) {
		String sql = """
			SELECT 
				jb.bookmark_id,
				jb.job_id,
				jb.created_at AS bookmarked_at,
				jp.title AS job_title,
				jp.position,
				jp.work_type,
				jp.salary_min,
				jp.salary_max,
				jp.submission_deadline,
				jp.posted_at,
				ep.company_name,
				ep.employer_id,
				ep.industry,
				ep.location AS company_location
			FROM JobBookmarks jb
			INNER JOIN JobsPosting jp ON jb.job_id = jp.job_id
			INNER JOIN EmployerProfile ep ON jp.employer_id = ep.employer_id
			WHERE jb.seeker_id = ? AND jp.status = 'ACTIVE'
			ORDER BY jb.created_at DESC
			""";
		
		return jdbc.query(sql, (rs, rowNum) -> {
			Map<String, Object> job = new HashMap<>();
			job.put("bookmarkId", rs.getInt("bookmark_id"));
			job.put("jobId", rs.getLong("job_id"));
			job.put("bookmarkedAt", rs.getTimestamp("bookmarked_at"));
			job.put("jobTitle", rs.getString("job_title"));
			job.put("position", rs.getString("position"));
			job.put("workType", rs.getString("work_type"));
			job.put("salaryMin", rs.getBigDecimal("salary_min"));
			job.put("salaryMax", rs.getBigDecimal("salary_max"));
			job.put("submissionDeadline", rs.getDate("submission_deadline"));
			job.put("postedAt", rs.getTimestamp("posted_at"));
			job.put("companyName", rs.getString("company_name"));
			job.put("employerId", rs.getLong("employer_id"));
			job.put("industry", rs.getString("industry"));
			job.put("companyLocation", rs.getString("company_location"));
			return job;
		}, seekerId);
	}

	public int countBookmarksBySeeker(int seekerId) {
		Integer count = jdbc.queryForObject(
			"SELECT COUNT(*) FROM JobBookmarks WHERE seeker_id = ?",
			Integer.class, seekerId
		);
		return count != null ? count : 0;
	}
}


