
package com.joblink.joblink.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JobMatchingDao {
    private final JdbcTemplate jdbc;

    public JobMatchingDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Find top N jobs that match seeker's skills
     * Returns jobs with matching skill count and percentage
     */
    public List<Map<String, Object>> findTopMatchingJobs(int seekerId, int limit) {
        String sql = """
                WITH SeekerSkillNames AS (
                    SELECT DISTINCT skill_name
                    FROM SeekerSkills
                    WHERE seeker_id = ?
                ),
                JobSkillMatches AS (
                    SELECT 
                        j.job_id,
                        j.title,
                        j.description,
                        j.location,
                        j.salary_min,
                        j.salary_max,
                        j.posted_at,
                        e.employer_id,
                        e.company_name,
                        e.industry,
                        COUNT(DISTINCT js.skill_id) as total_job_skills,
                        COUNT(DISTINCT CASE 
                            WHEN s.name IN (SELECT skill_name FROM SeekerSkillNames) 
                            THEN js.skill_id 
                        END) as matching_skills
                    FROM JobsPosting j
                    INNER JOIN EmployerProfile e ON j.employer_id = e.employer_id
                    LEFT JOIN JobSkills js ON j.job_id = js.job_id
                    LEFT JOIN Skills s ON js.skill_id = s.skill_id
                    GROUP BY j.job_id, j.title, j.description, j.location, j.salary_min, j.salary_max, 
                             j.posted_at, e.employer_id, e.company_name, e.industry
                    HAVING COUNT(DISTINCT CASE 
                        WHEN s.name IN (SELECT skill_name FROM SeekerSkillNames) 
                        THEN js.skill_id 
                    END) > 0
                )
                SELECT 
                    job_id,
                    title,
                    description,
                    location,
                    salary_min,
                    salary_max,
                    posted_at,
                    employer_id,
                    company_name,
                    industry,
                    matching_skills,
                    total_job_skills,
                    CAST(matching_skills AS FLOAT) / NULLIF(total_job_skills, 0) * 100 as match_percentage
                FROM JobSkillMatches
                ORDER BY matching_skills DESC, match_percentage DESC
                OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY
                """;

        return jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> job = new HashMap<>();
            job.put("jobId", rs.getInt("job_id"));
            job.put("title", rs.getString("title"));
            job.put("description", rs.getString("description"));
            job.put("location", rs.getString("location"));
            job.put("salaryMin", rs.getBigDecimal("salary_min"));
            job.put("salaryMax", rs.getBigDecimal("salary_max"));
            job.put("postedAt", rs.getTimestamp("posted_at"));
            job.put("employerId", rs.getInt("employer_id"));
            job.put("companyName", rs.getString("company_name"));
            job.put("industry", rs.getString("industry"));
            job.put("matchingSkills", rs.getInt("matching_skills"));
            job.put("totalJobSkills", rs.getInt("total_job_skills"));
            job.put("matchPercentage", rs.getDouble("match_percentage"));
            return job;
        }, seekerId, limit);
    }

    public double calculateMatchPercentage(int seekerId, int jobId) {
        String sql = """
                WITH SeekerSkillNames AS (
                    SELECT DISTINCT skill_name
                    FROM SeekerSkills
                    WHERE seeker_id = ?
                )
                SELECT 
                    COUNT(DISTINCT js.skill_id) as total_skills,
                    COUNT(DISTINCT CASE 
                        WHEN s.name IN (SELECT skill_name FROM SeekerSkillNames) 
                        THEN js.skill_id 
                    END) as matching_skills
                FROM JobSkills js
                LEFT JOIN Skills s ON js.skill_id = s.skill_id
                WHERE js.job_id = ?
                """;

        return jdbc.queryForObject(sql, (rs, rowNum) -> {
            int total = rs.getInt("total_skills");
            int matching = rs.getInt("matching_skills");
            return total > 0 ? (matching * 100.0 / total) : 0.0;
        }, seekerId, jobId);
    }
}

