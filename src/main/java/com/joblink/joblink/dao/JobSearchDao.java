package com.joblink.joblink.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class JobSearchDao {
    private final JdbcTemplate jdbc;

    public JobSearchDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Object> searchJobs(String keyword, Integer provinceId,
                                          Integer categoryId, int page, int pageSize) {
        String sql = """
            SELECT 
                j.job_id,
                j.title,
                j.work_type,
                j.salary_min,
                j.salary_max,
                j.year_experience,
                j.posted_at,
                ep.company_name,
                ep.employer_id,
                p.province_name,
                d.district_name,
                c.name as category_name,
                (SELECT STRING_AGG(s.name, ', ') FROM JobSkills js 
                 JOIN Skills s ON js.skill_id = s.skill_id 
                 WHERE js.job_id = j.job_id) as skills
            FROM JobsPosting j
            JOIN EmployerProfile ep ON j.employer_id = ep.employer_id
            LEFT JOIN Provinces p ON j.province_id = p.province_id
            LEFT JOIN Districts d ON j.district_id = d.district_id
            LEFT JOIN Categories c ON j.category_id = c.category_id
            WHERE j.status = 'ACTIVE'
            AND (? IS NULL OR j.title LIKE '%' + ? + '%')
            AND (? IS NULL OR j.province_id = ?)
            AND (? IS NULL OR j.category_id = ?)
            ORDER BY j.posted_at DESC
            OFFSET (? - 1) * ? ROWS
            FETCH NEXT ? ROWS ONLY
            """;

        List<Map<String, Object>> jobs = jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> job = new HashMap<>();
            job.put("jobId", rs.getInt("job_id"));
            job.put("title", rs.getString("title"));
            job.put("workType", rs.getString("work_type"));
            job.put("salaryMin", rs.getInt("salary_min"));
            job.put("salaryMax", rs.getInt("salary_max"));
            job.put("experience", rs.getString("year_experience"));
            job.put("postedAt", rs.getTimestamp("posted_at"));
            job.put("companyName", rs.getString("company_name"));
            job.put("employerId", rs.getInt("employer_id"));
            job.put("province", rs.getString("province_name"));
            job.put("district", rs.getString("district_name"));
            job.put("category", rs.getString("category_name"));
            job.put("skills", rs.getString("skills"));
            return job;
        }, keyword, keyword, provinceId, provinceId, categoryId, categoryId, page, pageSize, pageSize);

        // Get total count
        String countSql = """
            SELECT COUNT(*) as total FROM JobsPosting j
            WHERE j.status = 'ACTIVE'
            AND (? IS NULL OR j.title LIKE '%' + ? + '%')
            AND (? IS NULL OR j.province_id = ?)
            AND (? IS NULL OR j.category_id = ?)
            """;

        Integer total = jdbc.queryForObject(countSql, Integer.class,
                keyword, keyword, provinceId, provinceId, categoryId, categoryId);

        Map<String, Object> result = new HashMap<>();
        result.put("jobs", jobs);
        result.put("total", total != null ? total : 0);
        result.put("totalPages", Math.ceil((double) (total != null ? total : 0) / pageSize));
        return result;
    }

    public List<String> getJobTitleSuggestions(String query) {
        String sql = """
            SELECT DISTINCT TOP 10 j.title
            FROM JobsPosting j
            WHERE j.status = 'ACTIVE'
            AND j.title LIKE '%' + ? + '%'
            ORDER BY j.posted_at DESC
            """;

        return jdbc.queryForList(sql, String.class, query);
    }

    public List<Map<String, Object>> getAllProvinces() {
        String sql = "SELECT province_id, province_name FROM Provinces ORDER BY province_name";
        return jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> province = new HashMap<>();
            province.put("provinceId", rs.getInt("province_id"));
            province.put("provinceName", rs.getString("province_name"));
            return province;
        });
    }

    public List<Map<String, Object>> getAllCategories() {
        String sql = "SELECT category_id, name FROM Categories ORDER BY name";
        return jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> category = new HashMap<>();
            category.put("categoryId", rs.getInt("category_id"));
            category.put("categoryName", rs.getString("name"));
            return category;
        });
    }

    public List<Map<String, Object>> getDistrictsByProvince(Integer provinceId) {
        String sql = """
            SELECT district_id, district_name 
            FROM Districts 
            WHERE province_id = ? 
            ORDER BY district_name
            """;

        return jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> district = new HashMap<>();
            district.put("districtId", rs.getInt("district_id"));
            district.put("districtName", rs.getString("district_name"));
            return district;
        }, provinceId);
    }

    public Map<String, Object> getTopCompanyByJobCount() {
        String sql = """
            SELECT TOP 1
                ep.employer_id,
                ep.company_name,
                ep.industry,
                ep.location,
                COUNT(j.job_id) as job_count
            FROM EmployerProfile ep
            LEFT JOIN JobsPosting j ON ep.employer_id = j.employer_id AND j.status = 'ACTIVE'
            GROUP BY ep.employer_id, ep.company_name, ep.industry, ep.location
            ORDER BY job_count DESC
            """;

        List<Map<String, Object>> results = jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> company = new HashMap<>();
            company.put("employerId", rs.getInt("employer_id"));
            company.put("companyName", rs.getString("company_name"));
            company.put("industry", rs.getString("industry"));
            company.put("location", rs.getString("location"));
            company.put("jobCount", rs.getInt("job_count"));
            return company;
        });

        return results.isEmpty() ? new HashMap<>() : results.get(0);
    }

    public Map<String, Object> searchJobsWithAdvancedFilters(
            String keyword, Integer provinceId, Integer districtId, Integer categoryId,
            String workType, Integer minSalary, Integer maxSalary, String experience,
            int page, int pageSize) {

        StringBuilder sql = new StringBuilder("""
            SELECT 
                j.job_id,
                j.title,
                j.work_type,
                j.salary_min,
                j.salary_max,
                j.year_experience,
                j.posted_at,
                ep.company_name,
                ep.employer_id,
                p.province_name,
                d.district_name,
                c.name as category_name,
                (SELECT STRING_AGG(s.name, ', ') FROM JobSkills js 
                 JOIN Skills s ON js.skill_id = s.skill_id 
                 WHERE js.job_id = j.job_id) as skills
            FROM JobsPosting j
            JOIN EmployerProfile ep ON j.employer_id = ep.employer_id
            LEFT JOIN Provinces p ON j.province_id = p.province_id
            LEFT JOIN Districts d ON j.district_id = d.district_id
            LEFT JOIN Categories c ON j.category_id = c.category_id
            WHERE j.status = 'ACTIVE'
            """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND j.title LIKE ?");
            params.add("%" + keyword + "%");
        }
        if (provinceId != null) {
            sql.append(" AND j.province_id = ?");
            params.add(provinceId);
        }
        if (districtId != null) {
            sql.append(" AND j.district_id = ?");
            params.add(districtId);
        }
        if (categoryId != null) {
            sql.append(" AND j.category_id = ?");
            params.add(categoryId);
        }
        if (workType != null && !workType.isEmpty()) {
            sql.append(" AND j.work_type = ?");
            params.add(workType);
        }
        if (minSalary != null) {
            sql.append(" AND j.salary_max >= ?");
            params.add(minSalary);
        }
        if (maxSalary != null) {
            sql.append(" AND j.salary_min <= ?");
            params.add(maxSalary);
        }
        if (experience != null && !experience.isEmpty()) {
            sql.append(" AND j.year_experience LIKE ?");
            params.add("%" + experience + "%");
        }

        sql.append(" ORDER BY j.posted_at DESC");
        sql.append(" OFFSET (? - 1) * ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(page);
        params.add(pageSize);
        params.add(pageSize);

        List<Map<String, Object>> jobs = jdbc.query(sql.toString(), (rs, rowNum) -> {
            Map<String, Object> job = new HashMap<>();
            job.put("jobId", rs.getInt("job_id"));
            job.put("title", rs.getString("title"));
            job.put("workType", rs.getString("work_type"));
            job.put("salaryMin", rs.getInt("salary_min"));
            job.put("salaryMax", rs.getInt("salary_max"));
            job.put("experience", rs.getString("year_experience"));
            job.put("postedAt", rs.getTimestamp("posted_at"));
            job.put("companyName", rs.getString("company_name"));
            job.put("employerId", rs.getInt("employer_id"));
            job.put("province", rs.getString("province_name"));
            job.put("district", rs.getString("district_name"));
            job.put("category", rs.getString("category_name"));
            job.put("skills", rs.getString("skills"));
            return job;
        }, params.toArray());

        // Get total count
        StringBuilder countSql = new StringBuilder("""
            SELECT COUNT(*) as total FROM JobsPosting j
            WHERE j.status = 'ACTIVE'
            """);

        List<Object> countParams = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            countSql.append(" AND j.title LIKE ?");
            countParams.add("%" + keyword + "%");
        }
        if (provinceId != null) {
            countSql.append(" AND j.province_id = ?");
            countParams.add(provinceId);
        }
        if (districtId != null) {
            countSql.append(" AND j.district_id = ?");
            countParams.add(districtId);
        }
        if (categoryId != null) {
            countSql.append(" AND j.category_id = ?");
            countParams.add(categoryId);
        }
        if (workType != null && !workType.isEmpty()) {
            countSql.append(" AND j.work_type = ?");
            countParams.add(workType);
        }
        if (minSalary != null) {
            countSql.append(" AND j.salary_max >= ?");
            countParams.add(minSalary);
        }
        if (maxSalary != null) {
            countSql.append(" AND j.salary_min <= ?");
            countParams.add(maxSalary);
        }
        if (experience != null && !experience.isEmpty()) {
            countSql.append(" AND j.year_experience LIKE ?");
            countParams.add("%" + experience + "%");
        }

        Integer total = jdbc.queryForObject(countSql.toString(), Integer.class, countParams.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("jobs", jobs);
        result.put("total", total != null ? total : 0);
        result.put("totalPages", Math.ceil((double) (total != null ? total : 0) / pageSize));
        return result;
    }

    public Map<String, Object> getJobDetailById(Integer jobId) {
        String sql = """
            SELECT 
                j.job_id,
                j.title,
                j.work_type,
                j.salary_min,
                j.salary_max,
                j.year_experience,
                j.hiring_number,
                j.submission_deadline,
                j.job_desc,
                j.job_requirements,
                j.benefits,
                j.contact_name,
                j.contact_email,
                j.contact_phone,
                j.posted_at,
                ep.company_name,
                ep.employer_id,
                ep.industry,
                ep.location,
                p.province_name,
                d.district_name,
                c.name as category_name,
                (SELECT STRING_AGG(s.name, ', ') FROM JobSkills js 
                 JOIN Skills s ON js.skill_id = s.skill_id 
                 WHERE js.job_id = j.job_id) as skills
            FROM JobsPosting j
            JOIN EmployerProfile ep ON j.employer_id = ep.employer_id
            LEFT JOIN Provinces p ON j.province_id = p.province_id
            LEFT JOIN Districts d ON j.district_id = d.district_id
            LEFT JOIN Categories c ON j.category_id = c.category_id
            WHERE j.job_id = ? AND j.status = 'ACTIVE'
            """;

        List<Map<String, Object>> results = jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> job = new HashMap<>();
            job.put("jobId", rs.getInt("job_id"));
            job.put("title", rs.getString("title"));
            job.put("workType", rs.getString("work_type"));
            job.put("salaryMin", rs.getInt("salary_min"));
            job.put("salaryMax", rs.getInt("salary_max"));
            job.put("experience", rs.getString("year_experience"));
            job.put("hiringNumber", rs.getInt("hiring_number"));
            job.put("submissionDeadline", rs.getTimestamp("submission_deadline"));
            job.put("jobDesc", rs.getString("job_desc"));
            job.put("jobRequirements", rs.getString("job_requirements"));
            job.put("benefits", rs.getString("benefits"));
            job.put("contactName", rs.getString("contact_name"));
            job.put("contactEmail", rs.getString("contact_email"));
            job.put("contactPhone", rs.getString("contact_phone"));
            job.put("postedAt", rs.getTimestamp("posted_at"));
            job.put("companyName", rs.getString("company_name"));
            job.put("employerId", rs.getInt("employer_id"));
            job.put("industry", rs.getString("industry"));
            job.put("location", rs.getString("location"));
            job.put("province", rs.getString("province_name"));
            job.put("district", rs.getString("district_name"));
            job.put("category", rs.getString("category_name"));
            job.put("skills", rs.getString("skills"));
            return job;
        }, jobId);

        return results.isEmpty() ? null : results.get(0);
    }

    public Map<String, Object> getLiveSuggestions(String query) {
        Map<String, Object> result = new HashMap<>();
        List<String> skillSuggestions = new ArrayList<>();
        List<Map<String, Object>> companySuggestions = new ArrayList<>();

        String skillSql = """
            SELECT DISTINCT TOP 15 s.name
            FROM Skills s
            WHERE s.name LIKE ?
            ORDER BY s.name
            """;

        skillSuggestions = jdbc.queryForList(skillSql, String.class, "%" + query + "%");

        String companySql = """
            SELECT DISTINCT TOP 10
                ep.employer_id,
                ep.company_name
            FROM EmployerProfile ep
            WHERE ep.company_name LIKE ?
            ORDER BY ep.company_name
            """;

        companySuggestions = jdbc.query(companySql, (rs, rowNum) -> {
            Map<String, Object> company = new HashMap<>();
            company.put("employerId", rs.getInt("employer_id"));
            company.put("companyName", rs.getString("company_name"));
            return company;
        }, "%" + query + "%");

        result.put("skills", skillSuggestions);
        result.put("companies", companySuggestions);
        return result;
    }

    public List<Map<String, Object>> getExperienceLevels() {
        String sql = """
            SELECT DISTINCT j.year_experience
            FROM JobsPosting j
            WHERE j.status = 'ACTIVE' AND j.year_experience IS NOT NULL
            ORDER BY j.year_experience
            """;

        return jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> level = new HashMap<>();
            level.put("level", rs.getString("year_experience"));
            return level;
        });
    }

    public List<String> getWorkTypes() {
        String sql = """
            SELECT DISTINCT j.work_type
            FROM JobsPosting j
            WHERE j.status = 'ACTIVE' AND j.work_type IS NOT NULL
            ORDER BY j.work_type
            """;

        return jdbc.queryForList(sql, String.class);
    }

    public Map<String, Object> getSalaryRange() {
        String sql = """
            SELECT 
                MIN(ISNULL(j.salary_min, 0)) as min_salary,
                MAX(ISNULL(j.salary_max, 0)) as max_salary
            FROM JobsPosting j
            WHERE j.status = 'ACTIVE' AND j.salary_min IS NOT NULL
            """;

        List<Map<String, Object>> results = jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> range = new HashMap<>();
            range.put("minSalary", rs.getInt("min_salary"));
            range.put("maxSalary", rs.getInt("max_salary"));
            return range;
        });

        return results.isEmpty() ? new HashMap<>() : results.get(0);
    }

    public Map<String, Object> searchJobsByComprehensiveFilters(
            String keyword,
            Integer provinceId,
            Integer districtId,
            Integer categoryId,
            String workType,
            Integer minSalary,
            Integer maxSalary,
            String experienceLevel,
            String educationLevel,
            List<String> seekerSkills,
            Integer seekerId,
            int page,
            int pageSize) {

        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT
                j.job_id,
                j.title,
                j.work_type,
                j.salary_min,
                j.salary_max,
                j.year_experience,
                j.posted_at,
                ep.company_name,
                ep.employer_id,
                p.province_name,
                d.district_name,
                c.name as category_name,
                (SELECT STRING_AGG(s.name, ', ') FROM JobSkills js 
                 JOIN Skills s ON js.skill_id = s.skill_id 
                 WHERE js.job_id = j.job_id) as skills,
                CASE 
                    WHEN DATEDIFF(HOUR, j.posted_at, GETDATE()) <= 7*24 THEN 1 
                    ELSE 0 
                END as is_super_hot
            FROM JobsPosting j
            JOIN EmployerProfile ep ON j.employer_id = ep.employer_id
            LEFT JOIN Provinces p ON j.province_id = p.province_id
            LEFT JOIN Districts d ON j.district_id = d.district_id
            LEFT JOIN Categories c ON j.category_id = c.category_id
            WHERE j.status = 'ACTIVE'
            """);

        List<Object> params = new ArrayList<>();

        // Keyword search
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (j.title LIKE ? OR ep.company_name LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        // Location filters
        if (provinceId != null) {
            sql.append(" AND j.province_id = ?");
            params.add(provinceId);
        }
        if (districtId != null) {
            sql.append(" AND j.district_id = ?");
            params.add(districtId);
        }

        // Category filter
        if (categoryId != null) {
            sql.append(" AND j.category_id = ?");
            params.add(categoryId);
        }

        // Work type filter
        if (workType != null && !workType.isEmpty()) {
            sql.append(" AND j.work_type = ?");
            params.add(workType);
        }

        // Salary range filter
        if (minSalary != null) {
            sql.append(" AND j.salary_max >= ?");
            params.add(minSalary * 1000000); // Convert to actual salary
        }
        if (maxSalary != null) {
            sql.append(" AND j.salary_min <= ?");
            params.add(maxSalary * 1000000); // Convert to actual salary
        }

        // Experience level filter
        if (experienceLevel != null && !experienceLevel.isEmpty()) {
            sql.append(" AND j.year_experience LIKE ?");
            params.add("%" + experienceLevel + "%");
        }

        // Seeker skills matching
        if (seekerSkills != null && !seekerSkills.isEmpty()) {
            StringBuilder skillWhere = new StringBuilder(" AND EXISTS (SELECT 1 FROM JobSkills js WHERE js.job_id = j.job_id AND js.skill_id IN (");
            for (int i = 0; i < seekerSkills.size(); i++) {
                if (i > 0) skillWhere.append(",");
                skillWhere.append("SELECT skill_id FROM Skills WHERE name = ?");
            }
            skillWhere.append("))");
            sql.append(skillWhere);
            params.addAll(seekerSkills);
        }

        sql.append(" ORDER BY j.posted_at DESC");
        sql.append(" OFFSET (? - 1) * ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(page);
        params.add(pageSize);
        params.add(pageSize);

        List<Map<String, Object>> jobs = jdbc.query(sql.toString(), (rs, rowNum) -> {
            Map<String, Object> job = new HashMap<>();
            job.put("jobId", rs.getInt("job_id"));
            job.put("title", rs.getString("title"));
            job.put("workType", rs.getString("work_type"));
            job.put("salaryMin", rs.getInt("salary_min"));
            job.put("salaryMax", rs.getInt("salary_max"));
            job.put("experience", rs.getString("year_experience"));
            job.put("postedAt", rs.getTimestamp("posted_at"));
            job.put("companyName", rs.getString("company_name"));
            job.put("employerId", rs.getInt("employer_id"));
            job.put("province", rs.getString("province_name"));
            job.put("district", rs.getString("district_name"));
            job.put("category", rs.getString("category_name"));
            job.put("skills", rs.getString("skills"));
            job.put("isSuperHot", rs.getInt("is_super_hot") == 1);
            return job;
        }, params.toArray());

        // Count total results
        StringBuilder countSql = new StringBuilder("""
            SELECT COUNT(DISTINCT j.job_id) as total FROM JobsPosting j
            JOIN EmployerProfile ep ON j.employer_id = ep.employer_id
            LEFT JOIN Provinces p ON j.province_id = p.province_id
            LEFT JOIN Districts d ON j.district_id = d.district_id
            LEFT JOIN Categories c ON j.category_id = c.category_id
            WHERE j.status = 'ACTIVE'
            """);

        List<Object> countParams = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            countSql.append(" AND (j.title LIKE ? OR ep.company_name LIKE ?)");
            countParams.add("%" + keyword + "%");
            countParams.add("%" + keyword + "%");
        }
        if (provinceId != null) {
            countSql.append(" AND j.province_id = ?");
            countParams.add(provinceId);
        }
        if (districtId != null) {
            countSql.append(" AND j.district_id = ?");
            countParams.add(districtId);
        }
        if (categoryId != null) {
            countSql.append(" AND j.category_id = ?");
            countParams.add(categoryId);
        }
        if (workType != null && !workType.isEmpty()) {
            countSql.append(" AND j.work_type = ?");
            countParams.add(workType);
        }
        if (minSalary != null) {
            countSql.append(" AND j.salary_max >= ?");
            countParams.add(minSalary * 1000000);
        }
        if (maxSalary != null) {
            countSql.append(" AND j.salary_min <= ?");
            countParams.add(maxSalary * 1000000);
        }
        if (experienceLevel != null && !experienceLevel.isEmpty()) {
            countSql.append(" AND j.year_experience LIKE ?");
            countParams.add("%" + experienceLevel + "%");
        }

        Integer total = jdbc.queryForObject(countSql.toString(), Integer.class, countParams.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("jobs", jobs);
        result.put("total", total != null ? total : 0);
        result.put("totalPages", Math.ceil((double) (total != null ? total : 0) / pageSize));
        result.put("currentPage", page);
        return result;
    }
}
