
package com.joblink.joblink.dao;

import com.joblink.joblink.model.PremiumPackage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PremiumPackageDao {
    private final JdbcTemplate jdbc;

    public PremiumPackageDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<PremiumPackage> findByUserType(String userType) {
        String sql = """
            SELECT package_id, code, name, user_type, price, duration_days,
                   max_active_jobs, boost_credits, candidate_views, highlight,
                   cv_templates_access, messaging_enabled, seeker_networking_enabled, pdf_export_limit,
                   features, is_active, created_at, updated_at
            FROM PremiumPackages
            WHERE user_type = ? AND is_active = 1
            ORDER BY price
            """;
        return jdbc.query(sql, (rs, rowNum) -> {
            PremiumPackage pkg = new PremiumPackage();
            pkg.setPackageId(rs.getInt("package_id"));
            pkg.setCode(rs.getString("code"));
            pkg.setName(rs.getString("name"));
            pkg.setUserType(rs.getString("user_type"));
            pkg.setPrice(rs.getBigDecimal("price"));
            pkg.setDurationDays(rs.getInt("duration_days"));
            pkg.setMaxActiveJobs(rs.getInt("max_active_jobs"));
            pkg.setBoostCredits(rs.getInt("boost_credits"));
            pkg.setCandidateViews(rs.getInt("candidate_views"));
            pkg.setHighlight(rs.getBoolean("highlight"));
            pkg.setCvTemplatesAccess(rs.getBoolean("cv_templates_access"));
            pkg.setMessagingEnabled(rs.getBoolean("messaging_enabled"));
            pkg.setSeekerNetworkingEnabled(rs.getBoolean("seeker_networking_enabled"));
            pkg.setPdfExportLimit(rs.getInt("pdf_export_limit"));
            pkg.setFeatures(rs.getString("features"));
            pkg.setIsActive(rs.getBoolean("is_active"));
            if (rs.getTimestamp("created_at") != null) {
                pkg.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("updated_at") != null) {
                pkg.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
            return pkg;
        }, userType);
    }

    public PremiumPackage findById(int packageId) {
        String sql = """
            SELECT package_id, code, name, user_type, price, duration_days,
                   max_active_jobs, boost_credits, candidate_views, highlight,
                   cv_templates_access, messaging_enabled, seeker_networking_enabled, pdf_export_limit,
                   features, is_active, created_at, updated_at
            FROM PremiumPackages
            WHERE package_id = ?
            """;
        List<PremiumPackage> results = jdbc.query(sql, (rs, rowNum) -> {
            PremiumPackage pkg = new PremiumPackage();
            pkg.setPackageId(rs.getInt("package_id"));
            pkg.setCode(rs.getString("code"));
            pkg.setName(rs.getString("name"));
            pkg.setUserType(rs.getString("user_type"));
            pkg.setPrice(rs.getBigDecimal("price"));
            pkg.setDurationDays(rs.getInt("duration_days"));
            pkg.setMaxActiveJobs(rs.getInt("max_active_jobs"));
            pkg.setBoostCredits(rs.getInt("boost_credits"));
            pkg.setCandidateViews(rs.getInt("candidate_views"));
            pkg.setHighlight(rs.getBoolean("highlight"));
            pkg.setCvTemplatesAccess(rs.getBoolean("cv_templates_access"));
            pkg.setMessagingEnabled(rs.getBoolean("messaging_enabled"));
            pkg.setSeekerNetworkingEnabled(rs.getBoolean("seeker_networking_enabled"));
            pkg.setPdfExportLimit(rs.getInt("pdf_export_limit"));
            pkg.setFeatures(rs.getString("features"));
            pkg.setIsActive(rs.getBoolean("is_active"));
            if (rs.getTimestamp("created_at") != null) {
                pkg.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("updated_at") != null) {
                pkg.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
            return pkg;
        }, packageId);
        return results.isEmpty() ? null : results.get(0);
    }

    public PremiumPackage findByCode(String code) {
        String sql = """
            SELECT package_id, code, name, user_type, price, duration_days,
                   max_active_jobs, boost_credits, candidate_views, highlight,
                   cv_templates_access, messaging_enabled, seeker_networking_enabled, pdf_export_limit,
                   features, is_active, created_at, updated_at
            FROM PremiumPackages
            WHERE code = ?
            """;
        List<PremiumPackage> results = jdbc.query(sql, (rs, rowNum) -> {
            PremiumPackage pkg = new PremiumPackage();
            pkg.setPackageId(rs.getInt("package_id"));
            pkg.setCode(rs.getString("code"));
            pkg.setName(rs.getString("name"));
            pkg.setUserType(rs.getString("user_type"));
            pkg.setPrice(rs.getBigDecimal("price"));
            pkg.setDurationDays(rs.getInt("duration_days"));
            pkg.setMaxActiveJobs(rs.getInt("max_active_jobs"));
            pkg.setBoostCredits(rs.getInt("boost_credits"));
            pkg.setCandidateViews(rs.getInt("candidate_views"));
            pkg.setHighlight(rs.getBoolean("highlight"));
            pkg.setCvTemplatesAccess(rs.getBoolean("cv_templates_access"));
            pkg.setMessagingEnabled(rs.getBoolean("messaging_enabled"));
            pkg.setSeekerNetworkingEnabled(rs.getBoolean("seeker_networking_enabled"));
            pkg.setPdfExportLimit(rs.getInt("pdf_export_limit"));
            pkg.setFeatures(rs.getString("features"));
            pkg.setIsActive(rs.getBoolean("is_active"));
            if (rs.getTimestamp("created_at") != null) {
                pkg.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("updated_at") != null) {
                pkg.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
            return pkg;
        }, code);
        return results.isEmpty() ? null : results.get(0);
    }
}
