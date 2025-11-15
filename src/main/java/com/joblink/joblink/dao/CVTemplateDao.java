
package com.joblink.joblink.dao;

import com.joblink.joblink.model.CVTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CVTemplateDao {
    private final JdbcTemplate jdbc;

    public CVTemplateDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CVTemplate> findAllActive() {
        String sql = """
            SELECT template_id, template_name, template_code, description, thumbnail_url,
                   html_content, css_content, category, is_premium, is_active, display_order,
                   created_at, updated_at
            FROM CVTemplates
            WHERE is_active = 1
            ORDER BY display_order, template_name
            """;
        return jdbc.query(sql, (rs, rowNum) -> mapCVTemplate(rs));
    }

    public CVTemplate findById(int templateId) {
        String sql = """
            SELECT template_id, template_name, template_code, description, thumbnail_url,
                   html_content, css_content, category, is_premium, is_active, display_order,
                   created_at, updated_at
            FROM CVTemplates
            WHERE template_id = ?
            """;
        List<CVTemplate> results = jdbc.query(sql, (rs, rowNum) -> mapCVTemplate(rs), templateId);
        return results.isEmpty() ? null : results.get(0);
    }

    public CVTemplate findByCode(String code) {
        String sql = """
            SELECT template_id, template_name, template_code, description, thumbnail_url,
                   html_content, css_content, category, is_premium, is_active, display_order,
                   created_at, updated_at
            FROM CVTemplates
            WHERE template_code = ?
            """;
        List<CVTemplate> results = jdbc.query(sql, (rs, rowNum) -> mapCVTemplate(rs), code);
        return results.isEmpty() ? null : results.get(0);
    }

    private CVTemplate mapCVTemplate(java.sql.ResultSet rs) throws java.sql.SQLException {
        CVTemplate template = new CVTemplate();
        template.setTemplateId(rs.getInt("template_id"));
        template.setTemplateName(rs.getString("template_name"));
        template.setTemplateCode(rs.getString("template_code"));
        template.setDescription(rs.getString("description"));
        template.setThumbnailUrl(rs.getString("thumbnail_url"));
        template.setHtmlContent(rs.getString("html_content"));
        template.setCssContent(rs.getString("css_content"));
        template.setCategory(rs.getString("category"));
        template.setIsPremium(rs.getBoolean("is_premium"));
        template.setIsActive(rs.getBoolean("is_active"));
        template.setDisplayOrder(rs.getInt("display_order"));
        if (rs.getTimestamp("created_at") != null) {
            template.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            template.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return template;
    }
}
