
package com.joblink.joblink.dao;

import com.joblink.joblink.model.PremiumSubscription;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class PremiumSubscriptionDao {
    private final JdbcTemplate jdbc;

    public PremiumSubscriptionDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public PremiumSubscription findActiveByUserId(int userId) {
        String sql = """
            SELECT s.subscription_id, s.user_id, s.employer_id, s.seeker_id, s.package_id,
                   s.status, s.start_date, s.end_date, s.is_active, s.created_at, s.updated_at,
                   p.name as package_name, p.code as package_code
            FROM PremiumSubscriptions s
            LEFT JOIN PremiumPackages p ON s.package_id = p.package_id
            WHERE s.user_id = ? AND s.is_active = 1 AND s.status = 'ACTIVE' AND s.end_date > GETDATE()
            ORDER BY s.end_date DESC
            """;
        List<PremiumSubscription> results = jdbc.query(sql, (rs, rowNum) -> {
            PremiumSubscription sub = new PremiumSubscription();
            sub.setSubscriptionId(rs.getInt("subscription_id"));
            sub.setUserId(rs.getInt("user_id"));
            sub.setEmployerId(rs.getInt("employer_id"));
            sub.setSeekerId(rs.getInt("seeker_id"));
            sub.setPackageId(rs.getInt("package_id"));
            sub.setStatus(rs.getString("status"));
            if (rs.getTimestamp("start_date") != null) {
                sub.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
            }
            if (rs.getTimestamp("end_date") != null) {
                sub.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
            }
            sub.setIsActive(rs.getBoolean("is_active"));
            if (rs.getTimestamp("created_at") != null) {
                sub.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("updated_at") != null) {
                sub.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
            sub.setPackageName(rs.getString("package_name"));
            sub.setPackageCode(rs.getString("package_code"));
            return sub;
        }, userId);
        return results.isEmpty() ? null : results.get(0);
    }

    public int create(PremiumSubscription subscription) {
        String sql = """
            INSERT INTO PremiumSubscriptions (user_id, employer_id, seeker_id, package_id, status, start_date, end_date, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, subscription.getUserId());
            if (subscription.getEmployerId() != null) {
                ps.setInt(2, subscription.getEmployerId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            if (subscription.getSeekerId() != null) {
                ps.setInt(3, subscription.getSeekerId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setInt(4, subscription.getPackageId());
            ps.setString(5, subscription.getStatus());
            ps.setTimestamp(6, java.sql.Timestamp.valueOf(subscription.getStartDate()));
            ps.setTimestamp(7, java.sql.Timestamp.valueOf(subscription.getEndDate()));
            ps.setBoolean(8, subscription.getIsActive());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }
}
