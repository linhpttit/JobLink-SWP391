// File: com/joblink/joblink/dao/JobDAO.java (ĐÃ ĐƯỢC TINH GỌN)
package com.joblink.joblink.dao;

import com.joblink.joblink.auth.model.JobSearchResult; // Model này chỉ dùng cho kết quả search
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Repository
public class JobDAO {

    private final SimpleJdbcCall searchJobsCall;

    public JobDAO(DataSource dataSource) {
        // Bỏ jdbcTemplate vì không còn dùng cho các query khác
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

    // Chỉ giữ lại phương thức searchJobs
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