package com.joblink.joblink.dao;

import com.joblink.joblink.model.Invoice;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class InvoiceDao {

    private final JdbcTemplate jdbc;

    public InvoiceDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ============== CREATE ==============
    public int create(Invoice invoice) {
        // issued_at có DEFAULT sysutcdatetime() => KHÔNG chèn vào
        String sql = """
            INSERT INTO dbo.Invoice
                (user_id, employer_id, seeker_id, subscription_id, amount, status, due_at)
            VALUES
                (?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, invoice.getUserId());

            if (invoice.getEmployerId() == null) ps.setNull(2, java.sql.Types.INTEGER);
            else ps.setInt(2, invoice.getEmployerId());

            if (invoice.getSeekerId() == null) ps.setNull(3, java.sql.Types.INTEGER);
            else ps.setInt(3, invoice.getSeekerId());

            if (invoice.getSubscriptionId() == null) ps.setNull(4, java.sql.Types.INTEGER);
            else ps.setInt(4, invoice.getSubscriptionId());

            ps.setBigDecimal(5, invoice.getAmount());
            ps.setString(6, invoice.getStatus());

            LocalDateTime dueAt = invoice.getDueAt();
            if (dueAt == null) ps.setNull(7, java.sql.Types.TIMESTAMP);
            else ps.setTimestamp(7, java.sql.Timestamp.valueOf(dueAt));
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    // =============== READ ===============
    public Invoice findById(int invoiceId) {
        String sql = """
            SELECT invoice_id, user_id, employer_id, seeker_id, subscription_id,
                   amount, status, issued_at, due_at, paid_at
            FROM dbo.Invoice
            WHERE invoice_id = ?
            """;
        try {
            return jdbc.queryForObject(sql, (rs, rn) -> {
                Invoice iv = new Invoice();
                iv.setInvoiceId(rs.getInt("invoice_id"));
                iv.setUserId(rs.getInt("user_id"));
                iv.setEmployerId((Integer) rs.getObject("employer_id"));
                iv.setSeekerId((Integer) rs.getObject("seeker_id"));
                iv.setSubscriptionId((Integer) rs.getObject("subscription_id"));
                iv.setAmount(rs.getBigDecimal("amount"));
                iv.setStatus(rs.getString("status"));

                var issued = rs.getTimestamp("issued_at");
                iv.setIssuedAt(issued != null ? issued.toLocalDateTime() : null);

                var due = rs.getTimestamp("due_at");
                iv.setDueAt(due != null ? due.toLocalDateTime() : null);

                var paid = rs.getTimestamp("paid_at");
                iv.setPaidAt(paid != null ? paid.toLocalDateTime() : null);
                return iv;
            }, invoiceId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Invoice> findByUserId(int userId) {
        String sql = """
            SELECT invoice_id, user_id, employer_id, seeker_id, subscription_id,
                   amount, status, issued_at, due_at, paid_at
            FROM dbo.Invoice
            WHERE user_id = ?
            ORDER BY issued_at DESC
            """;
        return jdbc.query(sql, (rs, rn) -> {
            Invoice iv = new Invoice();
            iv.setInvoiceId(rs.getInt("invoice_id"));
            iv.setUserId(rs.getInt("user_id"));
            iv.setEmployerId((Integer) rs.getObject("employer_id"));
            iv.setSeekerId((Integer) rs.getObject("seeker_id"));
            iv.setSubscriptionId((Integer) rs.getObject("subscription_id"));
            iv.setAmount(rs.getBigDecimal("amount"));
            iv.setStatus(rs.getString("status"));

            var issued = rs.getTimestamp("issued_at");
            iv.setIssuedAt(issued != null ? issued.toLocalDateTime() : null);

            var due = rs.getTimestamp("due_at");
            iv.setDueAt(due != null ? due.toLocalDateTime() : null);

            var paid = rs.getTimestamp("paid_at");
            iv.setPaidAt(paid != null ? paid.toLocalDateTime() : null);
            return iv;
        }, userId);
    }

    // ============== UPDATE ==============
    public int updateStatus(int invoiceId, String status) {
        // Nếu PAID => set paid_at = SYSUTCDATETIME(); ngược lại giữ nguyên
        String sql = """
            UPDATE dbo.Invoice
            SET status = ?,
                paid_at = CASE WHEN ? = 'PAID' THEN SYSUTCDATETIME() ELSE paid_at END
            WHERE invoice_id = ?
            """;
        return jdbc.update(sql, status, status, invoiceId);
    }

    /** Cập nhật subscription_id sau khi kích hoạt gói */
    public int updateSubscriptionId(int invoiceId, int subscriptionId) {
        String sql = "UPDATE dbo.Invoice SET subscription_id = ? WHERE invoice_id = ?";
        return jdbc.update(sql, subscriptionId, invoiceId);
    }

    // ============== (tuỳ chọn) KHÁC ==============
    public int delete(int invoiceId) {
        String sql = "DELETE FROM dbo.Invoice WHERE invoice_id = ?";
        return jdbc.update(sql, invoiceId);
    }
}
