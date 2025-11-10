package com.joblink.joblink.service;

import com.joblink.joblink.dto.CandidateRecommendationDTO;
import com.joblink.joblink.model.*;
import com.joblink.joblink.dao.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateRecommendationService {

    private final JdbcTemplate jdbcTemplate;
    private final SkillDao skillDao;
    private final ExperienceDao experienceDao;
    private final EducationDao educationDao;
    private final CertificateDao certificateDao;
    private final LanguageDao languageDao;

    /**
     * Lấy danh sách ứng viên được gợi ý cho một job posting cụ thể
     * @param jobId ID của job posting
     * @param limit Số lượng ứng viên tối đa cần lấy
     * @return Danh sách ứng viên được sắp xếp theo % phù hợp
     */
    public List<CandidateRecommendationDTO> getRecommendedCandidates(Long jobId, int limit) {
        // 1. Lấy thông tin job posting
        Map<String, Object> jobInfo = getJobInfo(jobId);
        
        if (jobInfo == null) {
            return Collections.emptyList();
        }

        String requiredSkills = (String) jobInfo.get("required_skills");
        String yearExperience = (String) jobInfo.get("year_experience");
        
        // Parse required skills
        Set<String> jobSkills = parseSkills(requiredSkills);
        
        // Parse experience requirement
        Integer minExperience = parseMinExperience(yearExperience);

        // 2. Lấy tất cả job seekers chưa ứng tuyển vào job này
        List<JobSeekerProfile> allSeekers = getAvailableJobSeekers(jobId);

        // 3. Tính toán matching score cho mỗi ứng viên
        List<CandidateRecommendationDTO> recommendations = new ArrayList<>();
        
        for (JobSeekerProfile seeker : allSeekers) {
            CandidateRecommendationDTO recommendation = calculateMatch(seeker, jobSkills, minExperience);
            if (recommendation.getMatchPercentage() > 70) { // Lấy tất cả ứng viên có ít nhất 70% match
                recommendations.add(recommendation);
            }
        }

        // 4. Sắp xếp theo % phù hợp giảm dần
        recommendations.sort((a, b) -> {
            // Ưu tiên theo match percentage
            int percentCompare = b.getMatchPercentage().compareTo(a.getMatchPercentage());
            if (percentCompare != 0) return percentCompare;
            
            // Nếu bằng nhau, ưu tiên người có nhiều năm kinh nghiệm hơn
            Integer expA = a.getExperienceYears() != null ? a.getExperienceYears() : 0;
            Integer expB = b.getExperienceYears() != null ? b.getExperienceYears() : 0;
            return expB.compareTo(expA);
        });

        // 5. Trả về tất cả kết quả (không giới hạn)
        return recommendations;
    }

    /**
     * Lấy thông tin job posting với tất cả skills yêu cầu
     */
    private Map<String, Object> getJobInfo(Long jobId) {
        try {
            // Lấy thông tin cơ bản của job
            String jobSql = """
                SELECT 
                    jp.job_id,
                    jp.title,
                    jp.year_experience,
                    jp.position,
                    jp.salary_min,
                    jp.salary_max,
                    s.name as primary_skill
                FROM JobsPosting jp
                LEFT JOIN Skills s ON jp.skill_id = s.skill_id
                WHERE jp.job_id = ? AND jp.status = 'ACTIVE'
                """;
            
            List<Map<String, Object>> jobResults = jdbcTemplate.queryForList(jobSql, jobId);
            if (jobResults.isEmpty()) {
                return null;
            }
            
            Map<String, Object> jobInfo = jobResults.get(0);
            
            // Lấy tất cả skills yêu cầu từ bảng JobSkills
            String skillsSql = """
                SELECT s.name as skill_name
                FROM JobSkills js
                INNER JOIN Skills s ON js.skill_id = s.skill_id
                WHERE js.job_id = ?
                """;
            
            List<Map<String, Object>> skillResults = jdbcTemplate.queryForList(skillsSql, jobId);
            
            // Combine primary skill và additional skills
            StringBuilder allSkills = new StringBuilder();
            String primarySkill = (String) jobInfo.get("primary_skill");
            if (primarySkill != null && !primarySkill.isEmpty()) {
                allSkills.append(primarySkill);
            }
            
            for (Map<String, Object> skillRow : skillResults) {
                String skillName = (String) skillRow.get("skill_name");
                if (skillName != null && !skillName.isEmpty()) {
                    if (allSkills.length() > 0) {
                        allSkills.append(",");
                    }
                    allSkills.append(skillName);
                }
            }
            
            jobInfo.put("required_skills", allSkills.toString());
            
            return jobInfo;
        } catch (Exception e) {
            System.err.println("Error getting job info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy tất cả job seekers có thể nhận lời mời
     * Loại bỏ những người đã ứng tuyển job này
     */
    private List<JobSeekerProfile> getAllJobSeekers() {
        try {
            String sql = """
                SELECT 
                    jsp.seeker_id,
                    jsp.user_id,
                    jsp.fullname,
                    jsp.experience_years,
                    jsp.headline,
                    jsp.avatar_url,
                    jsp.location,
                    jsp.email,
                    jsp.phone
                FROM JobSeekerProfile jsp
                INNER JOIN Users u ON jsp.user_id = u.user_id
                WHERE u.enabled = 1
                AND jsp.fullname IS NOT NULL
                AND ISNULL(jsp.receive_invitations, 1) = 1
                """;
            
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                JobSeekerProfile profile = new JobSeekerProfile();
                profile.setSeekerId(rs.getInt("seeker_id"));
                profile.setUserId(rs.getInt("user_id"));
                profile.setFullname(rs.getString("fullname"));
                profile.setExperienceYears(rs.getObject("experience_years", Integer.class));
                profile.setHeadline(rs.getString("headline"));
                profile.setAvatarUrl(rs.getString("avatar_url"));
                profile.setLocation(rs.getString("location"));
                profile.setEmail(rs.getString("email"));
                profile.setPhoneNumber(rs.getString("phone"));
                return profile;
            });
        } catch (Exception e) {
            System.err.println("Error getting job seekers: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    /**
     * Lấy job seekers chưa ứng tuyển vào job này
     */
    private List<JobSeekerProfile> getAvailableJobSeekers(Long jobId) {
        try {
            String sql = """
                SELECT 
                    jsp.seeker_id,
                    jsp.user_id,
                    jsp.fullname,
                    jsp.experience_years,
                    jsp.headline,
                    jsp.avatar_url,
                    jsp.location,
                    jsp.email,
                    jsp.phone
                FROM JobSeekerProfile jsp
                INNER JOIN Users u ON jsp.user_id = u.user_id
                WHERE u.enabled = 1
                AND jsp.fullname IS NOT NULL
                AND ISNULL(jsp.receive_invitations, 1) = 1
                AND NOT EXISTS (
                    SELECT 1 
                    FROM Applications a 
                    WHERE a.seeker_id = jsp.seeker_id 
                    AND a.job_id = ?
                )
                """;
            
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                JobSeekerProfile profile = new JobSeekerProfile();
                profile.setSeekerId(rs.getInt("seeker_id"));
                profile.setUserId(rs.getInt("user_id"));
                profile.setFullname(rs.getString("fullname"));
                profile.setExperienceYears(rs.getObject("experience_years", Integer.class));
                profile.setHeadline(rs.getString("headline"));
                profile.setAvatarUrl(rs.getString("avatar_url"));
                profile.setLocation(rs.getString("location"));
                profile.setEmail(rs.getString("email"));
                profile.setPhoneNumber(rs.getString("phone"));
                return profile;
            }, jobId);
        } catch (Exception e) {
            System.err.println("Error getting available job seekers: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Tính toán mức độ phù hợp giữa ứng viên và job
     */
    private CandidateRecommendationDTO calculateMatch(JobSeekerProfile seeker, 
                                                       Set<String> jobSkills, 
                                                       Integer minExperience) {
        int seekerId = seeker.getSeekerId();
        
        // 1. Lấy skills của ứng viên
        List<Skill> seekerSkills = skillDao.findBySeekerId(seekerId);
        
        Set<String> seekerSkillNames = seekerSkills.stream()
                .map(s -> s.getSkillName().toLowerCase().trim())
                .collect(Collectors.toSet());

        // Đếm số skill match
        long matchingSkills = jobSkills.stream()
                .filter(seekerSkillNames::contains)
                .count();

        // Tính % match dựa trên skills
        int skillMatchPercentage = jobSkills.isEmpty() ? 0 : 
                (int) ((matchingSkills * 100.0) / jobSkills.size());

        // 2. Lấy experiences để tính toán năm kinh nghiệm thực tế
        List<Experience> experiences = experienceDao.findBySeekerId(seekerId);
        Integer calculatedExperienceYears = calculateTotalExperienceYears(experiences);
        
        // Sử dụng experience_years từ profile nếu có, nếu không thì dùng calculated
        Integer actualExperienceYears = calculatedExperienceYears;

        // 3. Lấy education để đánh giá trình độ học vấn
        List<Education> educations = educationDao.findBySeekerId(seekerId);
        boolean hasRelevantEducation = !educations.isEmpty();
        
        // 4. Lấy certificates để tăng điểm bonus
        List<Certificate> certificates = certificateDao.findBySeekerId(seekerId);
        int certificateBonus = Math.min(certificates.size() * 2, 10); // Tối đa +10% từ certificates

        // 5. Kiểm tra experience match
        boolean experienceMatch = true;
        if (minExperience != null && actualExperienceYears != null) {
            experienceMatch = actualExperienceYears >= minExperience;
        }

        // 6. Tính tổng % phù hợp với công thức cải tiến
        // - 60% từ skills matching
        // - 25% từ experience matching
        // - 10% từ education
        // - 5% từ certificates (bonus)
        int finalMatchPercentage = 0;
        
        // Skills component (60%)
        finalMatchPercentage += (int) (skillMatchPercentage * 0.6);
        
        // Experience component (25%)
        if (experienceMatch) {
            finalMatchPercentage += 25;
        } else if (actualExperienceYears != null && minExperience != null) {
            // Partial credit nếu gần đạt yêu cầu
            double experienceRatio = (double) actualExperienceYears / minExperience;
            finalMatchPercentage += (int) (25 * Math.min(experienceRatio, 1.0));
        }
        
        // Education component (10%)
        if (hasRelevantEducation) {
            finalMatchPercentage += 10;
        }
        
        // Certificate bonus (5%)
        finalMatchPercentage += Math.min(certificateBonus / 2, 5);

        // 7. Lấy top 3 skills của ứng viên để hiển thị
        List<String> topSkills = seekerSkills.stream()
                .limit(3)
                .map(Skill::getSkillName)
                .collect(Collectors.toList());

        return CandidateRecommendationDTO.builder()
                .seekerId(seeker.getSeekerId())
                .fullname(seeker.getFullname())
                .avatarUrl(seeker.getAvatarUrl())
                .experienceYears(actualExperienceYears)
                .headline(seeker.getHeadline())
                .skills(topSkills)
                .matchingSkillCount((int) matchingSkills)
                .totalSkillCount(jobSkills.size())
                .matchPercentage(Math.min(finalMatchPercentage, 100)) // Cap at 100%
                .experienceMatch(experienceMatch)
                .build();
    }
    
    /**
     * Tính tổng số năm kinh nghiệm từ danh sách Experience
     * Tính dựa trên khoảng thời gian làm việc (start_date -> end_date hoặc hiện tại)
     */
    private Integer calculateTotalExperienceYears(List<Experience> experiences) {
        if (experiences == null || experiences.isEmpty()) {
            return null;
        }
        
        int totalMonths = 0;
        LocalDate now = LocalDate.now();
        
        for (Experience exp : experiences) {
            LocalDate startDate = exp.getStartDate();
            LocalDate endDate = exp.getEndDate();
            
            if (startDate != null) {
                // Nếu chưa có end_date, tính đến hiện tại
                LocalDate effectiveEndDate = (endDate != null) ? endDate : now;
                
                // Tính số tháng giữa start và end
                Period period = Period.between(startDate, effectiveEndDate);
                int months = period.getYears() * 12 + period.getMonths();
                totalMonths += months;
            }
        }
        
        // Chuyển đổi tổng số tháng thành năm (làm tròn)
        return totalMonths > 0 ? (int) Math.ceil(totalMonths / 12.0) : null;
    }

    /**
     * Parse skills từ string thành set
     */
    private Set<String> parseSkills(String skillsStr) {
        if (skillsStr == null || skillsStr.trim().isEmpty()) {
            return Collections.emptySet();
        }
        
        return Arrays.stream(skillsStr.split("[,;]"))
                .map(s -> s.toLowerCase().trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Parse minimum experience từ string (vd: "1-3 năm", "3+ năm")
     */
    private Integer parseMinExperience(String yearExperience) {
        if (yearExperience == null || yearExperience.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Tìm số đầu tiên trong string
            String numStr = yearExperience.replaceAll("[^0-9]", "");
            if (!numStr.isEmpty()) {
                return Integer.parseInt(numStr.substring(0, Math.min(2, numStr.length())));
            }
        } catch (Exception e) {
            System.err.println("Error parsing experience: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Lấy gợi ý ứng viên cho employer (dựa trên tất cả jobs của employer)
     * Tổng hợp ứng viên từ nhiều bài đăng cho đến khi đủ số lượng yêu cầu
     * Loại bỏ ứng viên trùng lặp
     */
    public List<CandidateRecommendationDTO> getRecommendedCandidatesForEmployer(Integer employerId, int limit) {
        try {
            // Lấy top 5 job postings gần nhất của employer (chưa hết hạn)
            String sql = """
                SELECT TOP 5 job_id 
                FROM JobsPosting 
                WHERE employer_id = ? 
                  AND status = 'ACTIVE'
                  AND submission_deadline > GETDATE()
                ORDER BY posted_at DESC
                """;
            
            List<Long> jobIds = jdbcTemplate.queryForList(sql, Long.class, employerId);
            
            // Map để lưu ứng viên duy nhất (key = seekerId, value = DTO)
            // Sử dụng LinkedHashMap để giữ thứ tự (ưu tiên ứng viên có % cao hơn)
            Map<Integer, CandidateRecommendationDTO> uniqueCandidates = new LinkedHashMap<>();
            
            // Duyệt qua từng job posting và tổng hợp ứng viên
            for (Long jobId : jobIds) {
                // Nếu đã đủ số lượng yêu cầu, dừng lại
                if (uniqueCandidates.size() >= limit) {
                    break;
                }
                
                // Lấy ứng viên cho job này (lấy nhiều hơn limit để có buffer)
                List<CandidateRecommendationDTO> candidates = getRecommendedCandidates(jobId, limit * 2);
                
                // Thêm ứng viên vào map (tự động loại bỏ trùng lặp)
                for (CandidateRecommendationDTO candidate : candidates) {
                    // Nếu đã đủ số lượng, dừng lại
                    if (uniqueCandidates.size() >= limit) {
                        break;
                    }
                    
                    Integer seekerId = candidate.getSeekerId();
                    
                    // Nếu ứng viên chưa có trong map, thêm vào
                    if (!uniqueCandidates.containsKey(seekerId)) {
                        uniqueCandidates.put(seekerId, candidate);
                    } else {
                        // Nếu đã có, giữ lại ứng viên có % phù hợp cao hơn
                        CandidateRecommendationDTO existing = uniqueCandidates.get(seekerId);
                        if (candidate.getMatchPercentage() > existing.getMatchPercentage()) {
                            uniqueCandidates.put(seekerId, candidate);
                        }
                    }
                }
            }
            
            // Chuyển map thành list và sắp xếp lại theo % phù hợp giảm dần
            List<CandidateRecommendationDTO> result = new ArrayList<>(uniqueCandidates.values());
            result.sort((a, b) -> {
                // Ưu tiên theo match percentage
                int percentCompare = b.getMatchPercentage().compareTo(a.getMatchPercentage());
                if (percentCompare != 0) return percentCompare;
                
                // Nếu bằng nhau, ưu tiên người có nhiều năm kinh nghiệm hơn
                Integer expA = a.getExperienceYears() != null ? a.getExperienceYears() : 0;
                Integer expB = b.getExperienceYears() != null ? b.getExperienceYears() : 0;
                return expB.compareTo(expA);
            });
            
            // Giới hạn số lượng kết quả cuối cùng
            return result.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.err.println("Error getting recommendations for employer: " + e.getMessage());
        }
        
        return Collections.emptyList();
    }
}
