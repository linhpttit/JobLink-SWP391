package com.joblink.joblink.dao;

import com.joblink.joblink.model.Certificate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class CertificateDao {
    private final JdbcTemplate jdbc;

    public CertificateDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Certificate> findBySeekerId(int seekerId) {
        String sql = """
            SELECT certificate_id, seeker_id, issuing_organization, certificate_image_url, year_of_completion
            FROM Certificates WHERE seeker_id = ?
            ORDER BY year_of_completion DESC
            """;
        return jdbc.query(sql, (rs, rowNum) -> {
            Certificate cert = new Certificate();
            cert.setCertificateId(rs.getInt("certificate_id"));
            cert.setSeekerId(rs.getInt("seeker_id"));
            cert.setIssuingOrganization(rs.getString("issuing_organization"));
            cert.setCertificateImageUrl(rs.getString("certificate_image_url"));
            cert.setYearOfCompletion(rs.getInt("year_of_completion"));
            return cert;
        }, seekerId);
    }

    public int create(Certificate certificate) {
        String sql = """
            INSERT INTO Certificates (seeker_id, issuing_organization, certificate_image_url, year_of_completion)
            VALUES (?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, certificate.getSeekerId());
            ps.setString(2, certificate.getIssuingOrganization());
            ps.setString(3, certificate.getCertificateImageUrl());
            ps.setInt(4, certificate.getYearOfCompletion());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void update(Certificate certificate) {
        String sql = """
            UPDATE Certificates 
            SET issuing_organization = ?, certificate_image_url = ?, year_of_completion = ?
            WHERE certificate_id = ?
            """;
        jdbc.update(sql,
                certificate.getIssuingOrganization(),
                certificate.getCertificateImageUrl(),
                certificate.getYearOfCompletion(),
                certificate.getCertificateId()
        );
    }

    public void delete(int certificateId) {
        String sql = "DELETE FROM Certificates WHERE certificate_id = ?";
        jdbc.update(sql, certificateId);
    }
}
