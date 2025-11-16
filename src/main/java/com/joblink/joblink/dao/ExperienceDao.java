package com.joblink.joblink.dao;

import com.joblink.joblink.model.Experience;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ExperienceDao {
    private final JdbcTemplate jdbc;

    public ExperienceDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Experience> findBySeekerId(int seekerId) {
        String sql = """
            SELECT experience_id, seeker_id, job_title, company_name, 
                   start_date, end_date, project_link
            FROM Experience WHERE seeker_id = ?
            ORDER BY start_date DESC
            """;
        return jdbc.query(sql, (rs, rowNum) -> {
            Experience exp = new Experience();
            exp.setExperienceId(rs.getInt("experience_id"));
            exp.setSeekerId(rs.getInt("seeker_id"));
            exp.setJobTitle(rs.getString("job_title"));
            exp.setCompanyName(rs.getString("company_name"));
            if (rs.getDate("start_date") != null) {
                exp.setStartDate(rs.getDate("start_date").toLocalDate());
            }
            if (rs.getDate("end_date") != null) {
                exp.setEndDate(rs.getDate("end_date").toLocalDate());
            }
            exp.setProjectLink(rs.getString("project_link"));
            return exp;
        }, seekerId);
    }

    public int create(Experience experience) {
        String sql = """
            INSERT INTO Experience (seeker_id, job_title, company_name, start_date, end_date, project_link)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, experience.getSeekerId());
            ps.setString(2, experience.getJobTitle());
            ps.setString(3, experience.getCompanyName());
            ps.setDate(4, experience.getStartDate() != null ? Date.valueOf(experience.getStartDate()) : null);
            ps.setDate(5, experience.getEndDate() != null ? Date.valueOf(experience.getEndDate()) : null);
            ps.setString(6, experience.getProjectLink());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void update(Experience experience) {
        String sql = """
            UPDATE Experience 
            SET job_title = ?, company_name = ?, start_date = ?, end_date = ?, project_link = ?
            WHERE experience_id = ?
            """;
        jdbc.update(sql,
                experience.getJobTitle(),
                experience.getCompanyName(),
                experience.getStartDate() != null ? Date.valueOf(experience.getStartDate()) : null,
                experience.getEndDate() != null ? Date.valueOf(experience.getEndDate()) : null,
                experience.getProjectLink(),
                experience.getExperienceId()
        );
    }

    public void delete(int experienceId) {
        String sql = "DELETE FROM Experience WHERE experience_id = ?";
        jdbc.update(sql, experienceId);
    }
}
