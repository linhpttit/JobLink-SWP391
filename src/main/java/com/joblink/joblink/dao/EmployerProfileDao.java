package com.joblink.joblink.dao;

import com.joblink.joblink.auth.model.EmployerProfile; // Sửa lại package model nếu cần
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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

    // Get all companies
// Get all companies (Employer Profiles)
    public List<Map<String, Object>> getAllCompanies() {
        String sql = """
        SELECT employer_id, company_name, industry, location, phone_number, 
               description, tier_level, subscription_expires_at
        FROM EmployerProfile
        ORDER BY company_name
        """;
        return jdbcTemplate.queryForList(sql);
    }


    // Get company by ID
// Get employer profile by ID
    public Map<String, Object> getCompanyById(int employerId) {
        String sql = """
        SELECT employer_id, company_name, industry, location, phone_number, 
               description, tier_level, subscription_expires_at
        FROM EmployerProfile
        WHERE employer_id = ?
        """;

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, employerId);
        return result.isEmpty() ? null : result.get(0);
    }


    // Get jobs by company
// Get all jobs posted by this employer (company)
    public List<Map<String, Object>> getJobsByCompanyId(int employerId) {
        String sql = """
        SELECT job_id, title AS job_title, location, salary_min, salary_max, posted_at, status
        FROM JobsPosting
        WHERE employer_id = ? AND status = 'Active'
        ORDER BY posted_at DESC
        """;

        return jdbcTemplate.queryForList(sql, employerId);
    }

}