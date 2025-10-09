package com.joblink.joblink.Repository;


import com.joblink.joblink.auth.model.JobSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Repository
public class JobRepository {
    private final SimpleJdbcCall jdbcCall;

    @Autowired
    public JobRepository(DataSource dataSource) {
        this.jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_Jobs_SearchBySkills");
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

        Map<String, Object> result = jdbcCall.execute(params);
        return (List<JobSearchResult>) result.get("#result-set-1");
    }
}
