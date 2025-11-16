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

		// Build dynamic order by - sử dụng tên cột từ CTE, không dùng alias e
		String orderBy = " ORDER BY open_positions DESC ";
		if ("latest".equalsIgnoreCase(sort)) orderBy = " ORDER BY latest_job_date DESC ";
		else if ("name_az".equalsIgnoreCase(sort)) orderBy = " ORDER BY company_name ASC ";
		else if ("name_za".equalsIgnoreCase(sort)) orderBy = " ORDER BY company_name DESC ";

		// Params
		String kw = emptyToNull(keyword);
		String loc = emptyToNull(location);
		String ind = emptyToNull(industry);

		// Build SQL với positional params - sử dụng string concatenation cho SQL Server
		// Sử dụng LEFT JOIN để hiển thị cả employer không có job
		// Chỉ đếm job ACTIVE và còn deadline hợp lệ
		List<EmployerOpenDto> content;
		String wherePos = """
			WHERE 1=1
			  AND (? IS NULL OR LOWER(e.company_name) LIKE '%' + LOWER(?) + '%' OR LOWER(e.industry) LIKE '%' + LOWER(?) + '%')
			  AND (? IS NULL OR LOWER(e.location) LIKE '%' + LOWER(?) + '%')
			  AND (? IS NULL OR LOWER(e.industry) LIKE '%' + LOWER(?) + '%')
			""";
		String sqlPos = """
			WITH EmployerWithStats AS (
			  SELECT e.employer_id, e.company_name, e.industry, e.location, e.description,
			         MAX(j.posted_at) AS latest_job_date,
			         COUNT(j.job_id) AS open_positions
			  FROM EmployerProfile e
			  LEFT JOIN JobsPosting j ON e.employer_id = j.employer_id
			""" + wherePos + """
			  GROUP BY e.employer_id, e.company_name, e.industry, e.location, e.description
			)
			SELECT * FROM EmployerWithStats
			""" + orderBy + """
			OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
			""";
		String countPos = """
			SELECT COUNT(DISTINCT e.employer_id)
			FROM EmployerProfile e
			""" + wherePos;

		Object[] argsList = new Object[] { kw, kw, kw, loc, loc, ind, ind, offset, size };
		Object[] argsCount = new Object[] { kw, kw, kw, loc, loc, ind, ind };

		try {
			System.out.println("🔍 [EmployersSearchDao] Executing query with params:");
			System.out.println("   - keyword: " + kw);
			System.out.println("   - location: " + loc);
			System.out.println("   - industry: " + ind);
			System.out.println("   - page: " + page + ", size: " + size);
			
			content = jdbc.query(sqlPos, mapper, argsList);
			Integer total = jdbc.queryForObject(countPos, Integer.class, argsCount);
			int totalPages = (int) Math.ceil((total == null ? 0 : total) / (double) size);
			
			System.out.println("✅ [EmployersSearchDao] Query executed successfully:");
			System.out.println("   - Found: " + (content != null ? content.size() : 0) + " employers");
			System.out.println("   - Total: " + total + ", Total pages: " + totalPages);
			
			Map<String, Object> result = new HashMap<>();
			result.put("content", content != null ? content : java.util.Collections.emptyList());
			result.put("totalPages", totalPages);
			result.put("number", page - 1);
			return result;
		} catch (Exception e) {
			System.err.println("❌ [EmployersSearchDao] Error executing query: " + e.getMessage());
			e.printStackTrace();
			Map<String, Object> result = new HashMap<>();
			result.put("content", java.util.Collections.emptyList());
			result.put("totalPages", 0);
			result.put("number", page - 1);
			return result;
		}
	}

	private String emptyToNull(String s) {
		return (s == null || s.trim().isEmpty()) ? null : s.trim();
	}
}


