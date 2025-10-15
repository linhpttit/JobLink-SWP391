// File: JobDAO.java (PHIÊN BẢN HOÀN CHỈNH ĐÃ BỔ SUNG findById)
package com.joblink.joblink.dao;

import com.joblink.joblink.auth.model.JobPosting;
import com.joblink.joblink.auth.model.JobSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Repository
public class JobDAO {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall searchJobsCall;

    public JobDAO(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.searchJobsCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_Jobs_SearchBySkills")
                .returningResultSet("#result-set-1", (rs, rowNum) -> {
                    JobSearchResult result = new JobSearchResult();
                    result.setJobId(rs.getInt("job_id"));
                    result.setTitle(rs.getString("title"));
                    result.setCompanyName(rs.getString("company_name"));
                    result.setLocation(rs.getString("location"));
                    result.setAllSkills(rs.getString("all_skills"));
                    result.setMatchingSkillCount(rs.getInt("matching_skill_count"));
                    result.setTotalCount(rs.getInt("TotalCount"));
                    return result;
                });
    }

    private final RowMapper<JobPosting> jobPostingRowMapper = (rs, rowNum) -> {
        JobPosting job = new JobPosting();
        job.setJobId(rs.getInt("job_id"));
        job.setEmployerId(rs.getInt("employer_id"));
        job.setCategoryId(rs.getObject("category_id", Integer.class));
        job.setTitle(rs.getString("title"));
        job.setDescription(rs.getString("description"));
        job.setLocation(rs.getString("location"));
        job.setSalaryMin(rs.getBigDecimal("salary_min"));
        job.setSalaryMax(rs.getBigDecimal("salary_max"));
        job.setPostedAt(rs.getTimestamp("posted_at").toLocalDateTime());
        return job;
    };

    // === PHƯƠNG THỨC CÒN THIẾU MÀ BẠN CẦN ===
    /**
     * Tìm một công việc cụ thể bằng ID của nó.
     * @param jobId ID của công việc.
     * @return Đối tượng JobPosting hoặc null nếu không tìm thấy.
     */
    public JobPosting findById(int jobId) {
        String sql = "SELECT * FROM JobsPosting WHERE job_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, jobPostingRowMapper, jobId);
        } catch (EmptyResultDataAccessException e) {
            // queryForObject sẽ ném exception nếu không tìm thấy, chúng ta bắt và trả về null
            return null;
        }
    }

    public List<JobPosting> findByEmployerId(int employerId) {
        String sql = "SELECT * FROM JobsPosting WHERE employer_id = ? ORDER BY posted_at DESC";
        return jdbcTemplate.query(sql, jobPostingRowMapper, employerId);
    }

    public List<JobPosting> findRelatedJobs(int categoryId, int excludeJobId, int limit) {
        String sql = "SELECT TOP (?) * FROM JobsPosting WHERE category_id = ? AND job_id <> ? ORDER BY posted_at DESC";
        return jdbcTemplate.query(sql, jobPostingRowMapper, limit, categoryId, excludeJobId);
    }

    @SuppressWarnings("unchecked")
    public List<JobSearchResult> searchJobs(String skills, String location,
                                            Double minSalary, Double maxSalary,
                                            int page, int size) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("SkillNames", skills)
                .addValue("Location", location)
                .addValue("MinSalary", minSalary)
                .addValue("MaxSalary", maxSalary)
                .addValue("PageNumber", page)
                .addValue("PageSize", size);

        Map<String, Object> result = searchJobsCall.execute(params);
        return (List<JobSearchResult>) result.get("#result-set-1");
    }
}