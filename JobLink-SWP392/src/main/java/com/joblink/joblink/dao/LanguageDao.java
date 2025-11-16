package com.joblink.joblink.dao;

import com.joblink.joblink.model.Language;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class LanguageDao {
    private final JdbcTemplate jdbc;

    public LanguageDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Language> findBySeekerId(int seekerId) {
        String sql = """
            SELECT language_id, seeker_id, language_name, certificate_type
            FROM Languages WHERE seeker_id = ?
            """;
        return jdbc.query(sql, (rs, rowNum) -> {
            Language lang = new Language();
            lang.setLanguageId(rs.getInt("language_id"));
            lang.setSeekerId(rs.getInt("seeker_id"));
            lang.setLanguageName(rs.getString("language_name"));
            lang.setCertificateType(rs.getString("certificate_type"));
            return lang;
        }, seekerId);
    }

    public int create(Language language) {
        String sql = """
            INSERT INTO Languages (seeker_id, language_name, certificate_type)
            VALUES (?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, language.getSeekerId());
            ps.setString(2, language.getLanguageName());
            ps.setString(3, language.getCertificateType());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void update(Language language) {
        String sql = """
            UPDATE Languages 
            SET language_name = ?, certificate_type = ?
            WHERE language_id = ?
            """;
        jdbc.update(sql,
                language.getLanguageName(),
                language.getCertificateType(),
                language.getLanguageId()
        );
    }

    public void delete(int languageId) {
        String sql = "DELETE FROM Languages WHERE language_id = ?";
        jdbc.update(sql, languageId);
    }
}
