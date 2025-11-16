package com.joblink.joblink.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class JobSkillDao {
    private final JdbcTemplate jdbc;

    public JobSkillDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Get all skills required for a job
    public List<Map<String, Object>> getSkillsByJobId(int jobId) {
        String sql = """
            SELECT s.skill_id, s.skill_name, s.category
            FROM JobRequiredSkills jrs
            JOIN Skills s ON jrs.skill_id = s.skill_id
            WHERE jrs.job_id = ?
            ORDER BY s.skill_name
            """;
        return jdbc.queryForList(sql, jobId);
    }

    // Get all jobs that require a specific skill
    public List<Map<String, Object>> getJobsBySkillId(int skillId) {
        String sql = """
            SELECT DISTINCT j.job_id, j.job_title, j.experience_level, 
                   j.salary_min, j.salary_max, j.location, c.company_name,
                   j.posted_at
            FROM JobRequiredSkills jrs
            JOIN Jobs j ON jrs.job_id = j.job_id
            JOIN Companies c ON j.company_id = c.company_id
            WHERE jrs.skill_id = ? AND j.is_active = 1
            ORDER BY j.posted_at DESC
            """;
        return jdbc.queryForList(sql, skillId);
    }

    // Get jobs by multiple skills (intersection)
    public List<Map<String, Object>> getJobsByMultipleSkills(List<Integer> skillIds, int provinceId) {
        if (skillIds.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < skillIds.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }

        String sql = """
            SELECT DISTINCT j.job_id, j.job_title, j.experience_level,
                   j.salary_min, j.salary_max, j.location, c.company_name,
                   j.posted_at
            FROM Jobs j
            JOIN Companies c ON j.company_id = c.company_id
            WHERE j.job_id IN (
                SELECT job_id FROM JobRequiredSkills
               "WHERE skill_id IN (" + placeholders + ")"
                GROUP BY job_id
                HAVING COUNT(DISTINCT skill_id) >= ?
            )
            AND j.is_active = 1
            """ + (provinceId > 0 ? "AND j.location = (SELECT province_name FROM Provinces WHERE province_id = ?)" : "") + """
            ORDER BY j.posted_at DESC
            """;

        List<Object> params = new ArrayList<>(skillIds);
        params.add(skillIds.size());
        if (provinceId > 0) {
            params.add(provinceId);
        }

        return jdbc.queryForList(sql, params.toArray());
    }

    // Check if job requires a specific skill
    public boolean jobRequiresSkill(int jobId, int skillId) {
        String sql = "SELECT COUNT(*) FROM JobRequiredSkills WHERE job_id = ? AND skill_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, jobId, skillId);
        return count != null && count > 0;
    }
}