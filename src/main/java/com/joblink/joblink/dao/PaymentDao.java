
package com.joblink.joblink.dao;

import com.joblink.joblink.model.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class PaymentDao {
    private final JdbcTemplate jdbc;

    public PaymentDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int create(Payment payment) {
        String sql = """
            INSERT INTO Payment (invoice_id, provider, tx_ref, amount, status, payment_method, payment_details)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, payment.getInvoiceId());
            ps.setString(2, payment.getProvider());
            ps.setString(3, payment.getTxRef());
            ps.setBigDecimal(4, payment.getAmount());
            ps.setString(5, payment.getStatus());
            ps.setString(6, payment.getPaymentMethod());
            ps.setString(7, payment.getPaymentDetails());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public Payment findByTxRef(String txRef) {
        String sql = """
            SELECT payment_id, invoice_id, provider, tx_ref, amount, status, payment_method, payment_details, created_at
            FROM Payment
            WHERE tx_ref = ?
            """;
        List<Payment> results = jdbc.query(sql, (rs, rowNum) -> {
            Payment payment = new Payment();
            payment.setPaymentId(rs.getInt("payment_id"));
            payment.setInvoiceId(rs.getInt("invoice_id"));
            payment.setProvider(rs.getString("provider"));
            payment.setTxRef(rs.getString("tx_ref"));
            payment.setAmount(rs.getBigDecimal("amount"));
            payment.setStatus(rs.getString("status"));
            payment.setPaymentMethod(rs.getString("payment_method"));
            payment.setPaymentDetails(rs.getString("payment_details"));
            if (rs.getTimestamp("created_at") != null) {
                payment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            return payment;
        }, txRef);
        return results.isEmpty() ? null : results.get(0);
    }

    public void updateStatus(int paymentId, String status) {
        String sql = "UPDATE Payment SET status = ? WHERE payment_id = ?";
        jdbc.update(sql, status, paymentId);
    }

    public List<Payment> findByInvoiceId(int invoiceId) {
        String sql = """
            SELECT payment_id, invoice_id, provider, tx_ref, amount, status, payment_method, payment_details, created_at
            FROM Payment
            WHERE invoice_id = ?
            ORDER BY created_at DESC
            """;
        return jdbc.query(sql, (rs, rowNum) -> {
            Payment payment = new Payment();
            payment.setPaymentId(rs.getInt("payment_id"));
            payment.setInvoiceId(rs.getInt("invoice_id"));
            payment.setProvider(rs.getString("provider"));
            payment.setTxRef(rs.getString("tx_ref"));
            payment.setAmount(rs.getBigDecimal("amount"));
            payment.setStatus(rs.getString("status"));
            payment.setPaymentMethod(rs.getString("payment_method"));
            payment.setPaymentDetails(rs.getString("payment_details"));
            if (rs.getTimestamp("created_at") != null) {
                payment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            return payment;
        }, invoiceId);
    }
}
