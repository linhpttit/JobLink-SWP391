
package com.joblink.joblink.dao;

import com.joblink.joblink.model.CVExport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class CVExportDao {
    private final JdbcTemplate jdbc;

    public CVExportDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int create(CVExport export) {
        String sql = """
            INSERT INTO CVExports (seeker_id, template_id, file_name, file_path, file_size_kb, exported_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, export.getSeekerId());
            ps.setInt(2, export.getTemplateId());
            ps.setString(3, export.getFileName());
            ps.setString(4, export.getFilePath());
            ps.setInt(5, export.getFileSizeKb() != null ? export.getFileSizeKb() : 0);
            ps.setObject(6, export.getExportedAt());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }
}
