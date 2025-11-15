package com.joblink.joblink.dao;

import com.joblink.joblink.model.Skill2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class SkillDao {
    private final JdbcTemplate jdbc;

    public SkillDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Skill2> findBySeekerId(int seekerId) {
        String sql = """
            SELECT skill_id, seeker_id, skill_name, years_of_experience, description
            FROM SeekerSkills WHERE seeker_id = ?
            ORDER BY years_of_experience DESC
            """;
        return jdbc.query(sql, (rs, rowNum) -> {
            Skill2 skill = new Skill2();
            skill.setSkillId(rs.getInt("skill_id"));
            skill.setSeekerId(rs.getInt("seeker_id"));
            skill.setSkillName(rs.getString("skill_name"));
            skill.setYearsOfExperience(rs.getInt("years_of_experience"));
            skill.setDescription(rs.getString("description"));
            return skill;
        }, seekerId);
    }

    public int create(Skill2 skill) {
        String sql = """
            INSERT INTO SeekerSkills (seeker_id, skill_name, years_of_experience, description)
            VALUES (?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, skill.getSeekerId());
            ps.setString(2, skill.getSkillName());
            ps.setInt(3, skill.getYearsOfExperience());
            ps.setString(4, skill.getDescription());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void update(Skill2 skill) {
        String sql = """
            UPDATE SeekerSkills 
            SET skill_name = ?, years_of_experience = ?, description = ?
            WHERE skill_id = ?
            """;
        jdbc.update(sql,
                skill.getSkillName(),
                skill.getYearsOfExperience(),
                skill.getDescription(),
                skill.getSkillId()
        );
    }

    public void delete(int skillId) {
        String sql = "DELETE FROM SeekerSkills WHERE skill_id = ?";
        jdbc.update(sql, skillId);
    }
}
