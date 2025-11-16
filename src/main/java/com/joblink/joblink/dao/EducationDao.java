package com.joblink.joblink.dao;

import com.joblink.joblink.model.Education;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class EducationDao {
    private final JdbcTemplate jdbc;

    public EducationDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Education> findBySeekerId(int seekerId) {
        String sql = """
            SELECT education_id, seeker_id, university, degree_level, 
                   start_date, graduation_date, description
            FROM Education WHERE seeker_id = ?
            ORDER BY start_date DESC
            """;
        return jdbc.query(sql, (rs, rowNum) -> {
            Education edu = new Education();
            edu.setEducationId(rs.getInt("education_id"));
            edu.setSeekerId(rs.getInt("seeker_id"));
            edu.setUniversity(rs.getString("university"));
            edu.setDegreeLevel(rs.getString("degree_level"));
            if (rs.getDate("start_date") != null) {
                edu.setStartDate(rs.getDate("start_date").toLocalDate());
            }
            if (rs.getDate("graduation_date") != null) {
                edu.setGraduationDate(rs.getDate("graduation_date").toLocalDate());
            }
            edu.setDescription(rs.getString("description"));
            return edu;
        }, seekerId);
    }

    public int create(Education education) {
        String sql = """
            INSERT INTO Education (seeker_id, university, degree_level, start_date, graduation_date, description)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, education.getSeekerId());
            ps.setString(2, education.getUniversity());
            ps.setString(3, education.getDegreeLevel());
            ps.setDate(4, education.getStartDate() != null ? Date.valueOf(education.getStartDate()) : null);
            ps.setDate(5, education.getGraduationDate() != null ? Date.valueOf(education.getGraduationDate()) : null);
            ps.setString(6, education.getDescription());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void update(Education education) {
        String sql = """
            UPDATE Education 
            SET university = ?, degree_level = ?, start_date = ?, graduation_date = ?, description = ?
            WHERE education_id = ?
            """;
        jdbc.update(sql,
                education.getUniversity(),
                education.getDegreeLevel(),
                education.getStartDate() != null ? Date.valueOf(education.getStartDate()) : null,
                education.getGraduationDate() != null ? Date.valueOf(education.getGraduationDate()) : null,
                education.getDescription(),
                education.getEducationId()
        );
    }

    public void delete(int educationId) {
        String sql = "DELETE FROM Education WHERE education_id = ?";
        jdbc.update(sql, educationId);
    }
}
