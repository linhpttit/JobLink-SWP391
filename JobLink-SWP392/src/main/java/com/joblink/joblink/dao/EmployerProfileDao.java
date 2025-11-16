package com.joblink.joblink.dao;

import com.joblink.joblink.auth.model.EmployerProfile; // Sửa lại package model nếu cần
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmployerProfileDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<EmployerProfile> employerProfileRowMapper = (rs, rowNum) -> {
        EmployerProfile profile = new EmployerProfile();
        profile.setEmployerId(rs.getInt("employer_id"));
        profile.setUserId(rs.getInt("user_id"));
        profile.setCompanyName(rs.getString("company_name"));
        profile.setIndustry(rs.getString("industry"));
        profile.setLocation(rs.getString("location"));
        profile.setPhoneNumber(rs.getString("phone_number"));
        profile.setDescription(rs.getString("description"));

        // --- CÁC TRƯỜNG ĐÃ CẬP NHẬT ---
        profile.setLogoUrl(rs.getString("logo_url"));
        profile.setCompanySize(rs.getString("company_size"));
        profile.setEmail(rs.getString("email"));
        profile.setWebsiteUrl(rs.getString("website_url"));

        return profile;
    };

    public EmployerProfile findById(int employerId) {
        try {
            String sql = "SELECT * FROM EmployerProfile WHERE employer_id = ?";
            return jdbcTemplate.queryForObject(sql, employerProfileRowMapper, employerId);
        } catch (Exception e) {
            return null;
        }
    }

    public EmployerProfile findByUserId(int userId) {
        try {
            String sql = "SELECT * FROM EmployerProfile WHERE user_id = ?";
            return jdbcTemplate.queryForObject(sql, employerProfileRowMapper, userId);
        } catch (Exception e) {
            return null;
        }
    }
}