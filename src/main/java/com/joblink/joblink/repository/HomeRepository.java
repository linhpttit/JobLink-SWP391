package com.joblink.joblink.repository;

import com.joblink.joblink.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class HomeRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public HomeStatsDTO getHomeStats() {
        String sql = """
            SELECT 
                (SELECT COUNT(*) FROM JobsPosting WHERE status = 'ACTIVE') AS liveJobs,
                (SELECT COUNT(*) FROM EmployerProfile) AS companies,
                (SELECT COUNT(*) FROM JobSeekerProfile) AS candidates,
                (SELECT COUNT(*) FROM JobsPosting 
                 WHERE posted_at >= DATEADD(DAY, -7, GETDATE())) AS newJobs
            """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                HomeStatsDTO dto = new HomeStatsDTO();
                dto.setLiveJobs(rs.getInt("liveJobs"));
                dto.setCompanies(rs.getInt("companies"));
                dto.setCandidates(rs.getInt("candidates"));
                dto.setNewJobs(rs.getInt("newJobs"));
                return dto;
            });
        } catch (Exception e) {
            e.printStackTrace();
            // Return empty stats if error
            HomeStatsDTO dto = new HomeStatsDTO();
            dto.setLiveJobs(0);
            dto.setCompanies(0);
            dto.setCandidates(0);
            dto.setNewJobs(0);
            return dto;
        }
    }

    public List<CategoryStatsDTO> getPopularCategories() {
        String sql = """
            SELECT TOP 8
                c.category_id,
                c.name,
                COUNT(j.job_id) AS jobCount
            FROM Categories c
            LEFT JOIN JobsPosting j ON c.category_id = j.category_id AND j.status = 'ACTIVE'
            GROUP BY c.category_id, c.name
            ORDER BY jobCount DESC
            """;

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                CategoryStatsDTO dto = new CategoryStatsDTO();
                dto.setCategoryId(rs.getInt("category_id"));
                dto.setName(rs.getString("name"));
                dto.setJobCount(rs.getInt("jobCount"));
                return dto;
            });
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<FeaturedJobDTO> getFeaturedJobs() {
        String sql = """
            SELECT TOP 5
                j.job_id,
                j.title,
                e.company_name,
                COALESCE(CONCAT_WS(', ', d.district_name, p.province_name), e.location, N'Remote') AS location,
                j.salary_min,
                j.salary_max,
                DATEDIFF(DAY, GETDATE(), j.submission_deadline) AS daysRemaining,
                j.posted_at
            FROM JobsPosting j
            JOIN EmployerProfile e ON j.employer_id = e.employer_id
            LEFT JOIN Provinces p ON j.province_id = p.province_id
            LEFT JOIN Districts d ON j.district_id = d.district_id
            WHERE j.status = 'ACTIVE' 
                AND j.submission_deadline > GETDATE()
            ORDER BY j.posted_at DESC
            """;

        try {
            List<FeaturedJobDTO> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
                FeaturedJobDTO dto = new FeaturedJobDTO();
                dto.setJobId(rs.getInt("job_id"));
                dto.setTitle(rs.getString("title"));
                dto.setCompanyName(rs.getString("company_name"));
                dto.setLocation(rs.getString("location"));

                // Handle null values
                if (rs.getObject("salary_min") != null) {
                    dto.setSalaryMin(rs.getBigDecimal("salary_min"));
                }
                if (rs.getObject("salary_max") != null) {
                    dto.setSalaryMax(rs.getBigDecimal("salary_max"));
                }

                dto.setDaysRemaining(rs.getInt("daysRemaining"));
                dto.setPostedAt(rs.getTimestamp("posted_at").toLocalDateTime());
                return dto;
            });

            System.out.println("✅ Featured Jobs found: " + result.size());
            return result;

        } catch (Exception e) {
            System.err.println("❌ Error loading featured jobs:");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<TopCompanyDTO> getTopCompanies() {
        String sql = """
            SELECT TOP 4
                e.employer_id,
                e.company_name,
                COALESCE(e.location, N'Vietnam') AS location,
                COUNT(j.job_id) AS openPositions
            FROM EmployerProfile e
            LEFT JOIN JobsPosting j ON e.employer_id = j.employer_id 
                AND j.status = 'ACTIVE' 
                AND j.submission_deadline > GETDATE()
            GROUP BY e.employer_id, e.company_name, e.location
            HAVING COUNT(j.job_id) > 0
            ORDER BY openPositions DESC
            """;

        try {
            List<TopCompanyDTO> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
                TopCompanyDTO dto = new TopCompanyDTO();
                dto.setEmployerId(rs.getInt("employer_id"));
                dto.setCompanyName(rs.getString("company_name"));
                dto.setLocation(rs.getString("location"));
                dto.setOpenPositions(rs.getInt("openPositions"));
                return dto;
            });

            System.out.println("✅ Top Companies found: " + result.size());
            return result;

        } catch (Exception e) {
            System.err.println("❌ Error loading top companies:");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}