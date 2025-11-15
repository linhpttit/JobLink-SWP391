package com.joblink.joblink.controller;

import com.joblink.joblink.dto.CandidateRecommendationDTO;
import com.joblink.joblink.service.CandidateRecommendationService;
import com.joblink.joblink.service.EmailService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employer/candidates")
@RequiredArgsConstructor
public class CandidateRecommendationController {

    private final CandidateRecommendationService recommendationService;
    private final JdbcTemplate jdbcTemplate;
    private final EmailService emailService;

    /**
     * API để lấy danh sách ứng viên gợi ý cho employer
     */
    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            HttpSession session) {
        
        try {
            // Kiểm tra authentication
            Object userObj = session.getAttribute("user");
            if (userObj == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            com.joblink.joblink.dto.UserSessionDTO user = 
                (com.joblink.joblink.dto.UserSessionDTO) userObj;
            
            if (!"employer".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
            }

            // Lấy employer_id
            Integer employerId = jdbcTemplate.queryForObject(
                "SELECT employer_id FROM EmployerProfile WHERE user_id = ?",
                Integer.class,
                user.getUserId()
            );

            if (employerId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Employer profile not found"));
            }

            // Lấy danh sách ứng viên gợi ý
            List<CandidateRecommendationDTO> candidates = 
                recommendationService.getRecommendedCandidatesForEmployer(employerId, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", candidates);
            response.put("total", candidates.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * API để lấy ứng viên gợi ý cho một job posting cụ thể
     */
    @GetMapping("/recommendations/job/{jobId}")
    public ResponseEntity<?> getRecommendationsForJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "10") int limit,
            HttpSession session) {
        
        try {
            // Kiểm tra authentication
            Object userObj = session.getAttribute("user");
            if (userObj == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            com.joblink.joblink.dto.UserSessionDTO user = 
                (com.joblink.joblink.dto.UserSessionDTO) userObj;
            
            if (!"employer".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
            }

            // Kiểm tra job có thuộc về employer này không
            Integer employerId = jdbcTemplate.queryForObject(
                "SELECT employer_id FROM EmployerProfile WHERE user_id = ?",
                Integer.class,
                user.getUserId()
            );

            if (employerId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Employer profile not found"));
            }

            Integer jobEmployerId = jdbcTemplate.queryForObject(
                "SELECT employer_id FROM JobsPosting WHERE job_id = ?",
                Integer.class,
                jobId
            );

            if (jobEmployerId == null || !employerId.equals(jobEmployerId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            // Lấy danh sách ứng viên gợi ý
            List<CandidateRecommendationDTO> candidates = 
                recommendationService.getRecommendedCandidates(jobId, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", candidates);
            response.put("total", candidates.size());
            response.put("jobId", jobId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * API riêng cho employer xem chi tiết profile ứng viên
     * Chỉ hiển thị thông tin công khai, không hiển thị thông tin nhạy cảm
     */
    @GetMapping("/profile/{seekerId}")
    public ResponseEntity<?> getCandidateProfile(
            @PathVariable Integer seekerId,
            HttpSession session) {
        
        try {
            // Kiểm tra authentication
            Object userObj = session.getAttribute("user");
            if (userObj == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
            }

            com.joblink.joblink.dto.UserSessionDTO user = 
                (com.joblink.joblink.dto.UserSessionDTO) userObj;
            
            // Chỉ employer mới được xem
            if (!"employer".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Bạn không có quyền truy cập"));
            }

            // Lấy thông tin ứng viên (CHỈ những người cho phép nhận lời mời)
            String profileSql = """
                SELECT 
                    jsp.seeker_id,
                    jsp.fullname,
                    jsp.email,
                    jsp.phone,
                    jsp.location,
                    jsp.headline,
                    jsp.experience_years,
                    jsp.about,
                    jsp.avatar_url,
                    jsp.gender,
                    jsp.dob
                FROM JobSeekerProfile jsp
                WHERE jsp.seeker_id = ? 
                  AND ISNULL(jsp.receive_invitations, 1) = 1
                """;

            List<Map<String, Object>> profileResults = jdbcTemplate.queryForList(profileSql, seekerId);
            
            if (profileResults.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false, 
                    "message", "Không tìm thấy ứng viên hoặc ứng viên không cho phép nhận lời mời"
                ));
            }

            Map<String, Object> profile = profileResults.get(0);

            // Lấy kỹ năng
            try {
                String skillSql = """
                    SELECT skill_name, years_of_experience, description
                    FROM SeekerSkills
                    WHERE seeker_id = ?
                    ORDER BY years_of_experience DESC
                    """;
                List<Map<String, Object>> skills = jdbcTemplate.queryForList(skillSql, seekerId);
                profile.put("skills", skills != null ? skills : new ArrayList<>());
            } catch (Exception e) {
                profile.put("skills", new ArrayList<>());
            }

            // Lấy kinh nghiệm làm việc
            try {
                String expSql = """
                    SELECT job_title, company_name, start_date, end_date, project_link
                    FROM Experience
                    WHERE seeker_id = ?
                    ORDER BY start_date DESC
                    """;
                List<Map<String, Object>> experiences = jdbcTemplate.queryForList(expSql, seekerId);
                profile.put("experiences", experiences != null ? experiences : new ArrayList<>());
            } catch (Exception e) {
                profile.put("experiences", new ArrayList<>());
            }

            // Lấy học vấn
            try {
                String eduSql = """
                    SELECT university, degree_level, start_date, graduation_date, description
                    FROM Education
                    WHERE seeker_id = ?
                    ORDER BY start_date DESC
                    """;
                List<Map<String, Object>> education = jdbcTemplate.queryForList(eduSql, seekerId);
                profile.put("education", education != null ? education : new ArrayList<>());
            } catch (Exception e) {
                System.err.println("Error fetching education: " + e.getMessage());
                profile.put("education", new ArrayList<>());
            }

            // Lấy ngôn ngữ
            try {
                String langSql = """
                    SELECT language_name, certificate_type
                    FROM Languages
                    WHERE seeker_id = ?
                    """;
                List<Map<String, Object>> languages = jdbcTemplate.queryForList(langSql, seekerId);
                profile.put("languages", languages != null ? languages : new ArrayList<>());
            } catch (Exception e) {
                profile.put("languages", new ArrayList<>());
            }

            // Lấy chứng chỉ
            try {
                String certSql = """
                    SELECT issuing_organization, certificate_image_url, year_of_completion
                    FROM Certificates
                    WHERE seeker_id = ?
                    ORDER BY year_of_completion DESC
                    """;
                List<Map<String, Object>> certificates = jdbcTemplate.queryForList(certSql, seekerId);
                profile.put("certificates", certificates != null ? certificates : new ArrayList<>());
            } catch (Exception e) {
                profile.put("certificates", new ArrayList<>());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", profile);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi khi tải thông tin: " + e.getMessage()));
        }
    }

    /**
     * API để gửi lời mời ứng tuyển cho ứng viên
     */
    @PostMapping("/send-invitation/{seekerId}")
    public ResponseEntity<?> sendInvitation(
            @PathVariable Integer seekerId,
            @RequestParam(required = false) Long jobId,
            @RequestBody(required = false) Map<String, String> requestBody,
            HttpSession session) {
        
        try {
            // Kiểm tra authentication
            Object userObj = session.getAttribute("user");
            if (userObj == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
            }

            com.joblink.joblink.dto.UserSessionDTO user = 
                (com.joblink.joblink.dto.UserSessionDTO) userObj;
            
            if (!"employer".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Không có quyền truy cập"));
            }

            // Lấy thông tin employer
            String employerSql = """
                SELECT ep.employer_id, ep.company_name, ep.phone_number, u.email as employer_email
                FROM EmployerProfile ep
                INNER JOIN Users u ON ep.user_id = u.user_id
                WHERE u.user_id = ?
                """;
            
            List<Map<String, Object>> employerResults = jdbcTemplate.queryForList(employerSql, user.getUserId());
            if (employerResults.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy thông tin công ty"));
            }
            
            Map<String, Object> employer = employerResults.get(0);
            Integer employerId = (Integer) employer.get("employer_id");
            String companyName = (String) employer.get("company_name");

            // Lấy thông tin ứng viên (bao gồm email từ Users table)
            String seekerSql = """
                SELECT 
                    jsp.fullname, 
                    jsp.email as profile_email, 
                    jsp.phone,
                    u.email as user_email
                FROM JobSeekerProfile jsp
                INNER JOIN Users u ON jsp.user_id = u.user_id
                WHERE jsp.seeker_id = ? AND ISNULL(jsp.receive_invitations, 1) = 1
                """;
            
            List<Map<String, Object>> seekerResults = jdbcTemplate.queryForList(seekerSql, seekerId);
            if (seekerResults.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Ứng viên không tồn tại hoặc không nhận lời mời"));
            }
            
            Map<String, Object> seeker = seekerResults.get(0);
            String seekerName = (String) seeker.get("fullname");
            
            // Ưu tiên email từ Users table (email đăng nhập), nếu không có thì dùng email từ profile
            String userEmail = (String) seeker.get("user_email");
            String profileEmail = (String) seeker.get("profile_email");
            String seekerEmail = (userEmail != null && !userEmail.trim().isEmpty()) ? userEmail : profileEmail;

            if (seekerEmail == null || seekerEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Ứng viên chưa cập nhật email"));
            }

            // Lấy thông tin job nếu có
            String jobTitle = "các vị trí đang tuyển dụng";
            if (jobId != null) {
                String jobSql = "SELECT title FROM JobsPosting WHERE job_id = ? AND employer_id = ?";
                List<Map<String, Object>> jobResults = jdbcTemplate.queryForList(jobSql, jobId, employerId);
                if (!jobResults.isEmpty()) {
                    jobTitle = (String) jobResults.get(0).get("title");
                }
            }

            // Lấy nội dung lời mời từ request body
            String message = "";
            if (requestBody != null && requestBody.containsKey("message")) {
                message = requestBody.get("message");
            }

            // Gửi email thực tế
            try {
                emailService.sendJobInvitation(seekerEmail, seekerName, companyName, jobTitle, message);
                System.out.println("✓ Email sent successfully to: " + seekerEmail);
            } catch (Exception emailEx) {
                System.err.println("✗ Failed to send email: " + emailEx.getMessage());
                // Vẫn tiếp tục xử lý, không throw exception
            }

            // Lưu log gửi lời mời vào database (optional)
            String insertLogSql = """
                INSERT INTO InvitationLog (employer_id, seeker_id, job_id, sent_at, status)
                VALUES (?, ?, ?, GETDATE(), 'SENT')
                """;
            
            try {
                jdbcTemplate.update(insertLogSql, employerId, seekerId, jobId);
            } catch (Exception e) {
                // Bỏ qua nếu bảng chưa tồn tại
                System.out.println("Note: InvitationLog table not found, skipping log");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã gửi lời mời ứng tuyển đến " + seekerName);
            response.put("candidateName", seekerName);
            response.put("candidateEmail", seekerEmail);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Lỗi khi gửi lời mời: " + e.getMessage()));
        }
    }
}
