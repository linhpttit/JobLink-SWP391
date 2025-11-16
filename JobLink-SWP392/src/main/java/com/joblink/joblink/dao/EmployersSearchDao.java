package com.joblink.joblink.dao;

import com.joblink.joblink.dto.EmployerOpenDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class EmployersSearchDao {
	private final JdbcTemplate jdbc;

	public EmployersSearchDao(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	private final RowMapper<EmployerOpenDto> mapper = (rs, rowNum) -> EmployerOpenDto.builder()
		.employerId(rs.getLong("employer_id"))
		.companyName(rs.getString("company_name"))
		.location(rs.getString("location"))
		.industry(rs.getString("industry"))
		.description(rs.getString("description"))
		.openPositions(rs.getInt("open_positions"))
		.build();

	public Map<String, Object> search(String keyword, String location, String industry, String sort,
	                                  int page, int size) {
		if (page <= 0) page = 1;
		if (size <= 0) size = 12;
		int offset = (page - 1) * size;

		// Build dynamic order by
		String orderBy = " ORDER BY open_positions DESC ";
		if ("latest".equalsIgnoreCase(sort)) orderBy = " ORDER BY latest_job_date DESC ";
		else if ("name_az".equalsIgnoreCase(sort)) orderBy = " ORDER BY e.company_name ASC ";
		else if ("name_za".equalsIgnoreCase(sort)) orderBy = " ORDER BY e.company_name DESC ";

		// Params
		String kw = emptyToNull(keyword);
		String loc = emptyToNull(location);
		String ind = emptyToNull(industry);

		// Build SQL với positional params
		List<EmployerOpenDto> content;
		String wherePos = """
			WHERE 1=1
			  AND j.status = 'ACTIVE'
			  AND (j.submission_deadline IS NULL OR j.submission_deadline >= GETDATE())
			  AND (? IS NULL OR e.company_name LIKE CONCAT('%', ?, '%') OR e.industry LIKE CONCAT('%', ?, '%'))
			  AND (? IS NULL OR e.location LIKE CONCAT('%', ?, '%'))
			  AND (? IS NULL OR e.industry LIKE CONCAT('%', ?, '%'))
			""";
		String sqlPos = """
			WITH EmployerWithStats AS (
			  SELECT e.employer_id, e.company_name, e.industry, e.location, e.description,
			         MAX(j.posted_at) AS latest_job_date,
			         COUNT(j.job_id) AS open_positions
			  FROM EmployerProfile e
			  JOIN JobsPosting j ON e.employer_id = j.employer_id
			""" + wherePos + """
			  GROUP BY e.employer_id, e.company_name, e.industry, e.location, e.description
			)
			SELECT * FROM EmployerWithStats
			""" + orderBy + """
			OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
			""";
		String countPos = """
			SELECT COUNT(*) FROM (
			  SELECT e.employer_id
			  FROM EmployerProfile e
			  JOIN JobsPosting j ON e.employer_id = j.employer_id
			""" + wherePos + """
			  GROUP BY e.employer_id
			) x
			""";

		Object[] argsList = new Object[] { kw, kw, kw, loc, loc, ind, ind, offset, size };
		Object[] argsCount = new Object[] { kw, kw, kw, loc, loc, ind, ind };

		content = jdbc.query(sqlPos, mapper, argsList);
		Integer total = jdbc.queryForObject(countPos, Integer.class, argsCount);
		int totalPages = (int) Math.ceil((total == null ? 0 : total) / (double) size);
		Map<String, Object> result = new HashMap<>();
		result.put("content", content);
		result.put("totalPages", totalPages);
		result.put("number", page - 1);
		return result;
	}

	private String emptyToNull(String s) {
		return (s == null || s.trim().isEmpty()) ? null : s.trim();
	}
}


