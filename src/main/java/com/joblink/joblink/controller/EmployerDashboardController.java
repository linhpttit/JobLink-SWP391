package com.joblink.joblink.controller;

import com.joblink.joblink.dto.CandidateRecommendationDTO;
import com.joblink.joblink.dto.CandidateByPositionDTO;
import com.joblink.joblink.dto.CandidateTrendDTO;
import com.joblink.joblink.dto.CandidateStatusDTO;
import com.joblink.joblink.dto.DashboardStatsDTO;
import com.joblink.joblink.dto.MarketAnalysisDTO;
import com.joblink.joblink.service.CandidateRecommendationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/employer")
public class EmployerDashboardController {

    private final JdbcTemplate jdbc;
    private final CandidateRecommendationService recommendationService;

    public EmployerDashboardController(JdbcTemplate jdbc, 
                                      CandidateRecommendationService recommendationService) {
        this.jdbc = jdbc;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        // Kiểm tra login
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            return "redirect:/signin";
        }

        com.joblink.joblink.dto.UserSessionDTO user = 
            (com.joblink.joblink.dto.UserSessionDTO) userObj;
        
        if (!"employer".equalsIgnoreCase(user.getRole())) {
            return "redirect:/signin";
        }

        try {
            // Lấy employer_id từ user_id
            Integer employerId = jdbc.queryForObject(
                "SELECT employer_id FROM EmployerProfile WHERE user_id = ?",
                Integer.class,
                user.getUserId()
            );

            if (employerId != null) {
                // Thống kê số lượng job postings
                Integer totalJobs = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM JobsPosting WHERE employer_id = ?",
                    Integer.class,
                    employerId
                );

                // Thống kê số ứng viên ứng tuyển
                Integer totalApplications = jdbc.queryForObject(
                    """
                    SELECT COUNT(DISTINCT a.application_id)
                    FROM Applications a
                    INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
                    WHERE jp.employer_id = ?
                    """,
                    Integer.class,
                    employerId
                );

                // Số ứng viên đang chờ xử lý
                Integer pendingReview = jdbc.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM Applications a
                    INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
                    WHERE jp.employer_id = ? AND a.status = 'submitted'
                    """,
                    Integer.class,
                    employerId
                );

                // Số ứng viên đã review
                Integer reviewedCount = jdbc.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM Applications a
                    INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
                    WHERE jp.employer_id = ? AND a.status = 'reviewed'
                    """,
                    Integer.class,
                    employerId
                );

                // Số ứng viên đã reject
                Integer rejectedCount = jdbc.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM Applications a
                    INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
                    WHERE jp.employer_id = ? AND a.status = 'rejected'
                    """,
                    Integer.class,
                    employerId
                );

                // Số ứng viên đã được chấp nhận (hired)
                Integer acceptedCount = jdbc.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM Applications a
                    INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
                    WHERE jp.employer_id = ? AND a.status = 'hired'
                    """,
                    Integer.class,
                    employerId
                );

                model.addAttribute("totalJobs", totalJobs);
                model.addAttribute("totalApplications", totalApplications);
                model.addAttribute("pendingReview", pendingReview);
                model.addAttribute("reviewedCount", reviewedCount);
                model.addAttribute("rejectedCount", rejectedCount);
                model.addAttribute("acceptedCount", acceptedCount);
                
                // Lấy gợi ý ứng viên phù hợp
                List<CandidateRecommendationDTO> recommendedCandidates = 
                    recommendationService.getRecommendedCandidatesForEmployer(employerId, 5);
                model.addAttribute("recommendedCandidates", recommendedCandidates);
            }

        } catch (Exception e) {
            System.err.println("Error loading dashboard stats: " + e.getMessage());
            // Set default values
            model.addAttribute("totalJobs", 0);
            model.addAttribute("totalApplications", 0);
            model.addAttribute("pendingReview", 0);
            model.addAttribute("reviewedCount", 0);
            model.addAttribute("rejectedCount", 0);
            model.addAttribute("acceptedCount", 0);
            model.addAttribute("recommendedCandidates", java.util.Collections.emptyList());
        }

        model.addAttribute("user", user);
        return "employer/dashboard-content";
    }

    /**
     * API endpoint để lấy thống kê ứng viên theo vị trí
     * Trả về dữ liệu JSON cho biểu đồ
     * @param period: 7days, 1month, 3months, 6months, 12months
     */
    @GetMapping("/api/candidates-by-position")
    @ResponseBody
    public ResponseEntity<?> getCandidatesByPosition(HttpSession session,
                                                      @org.springframework.web.bind.annotation.RequestParam(defaultValue = "7days") String period) {
        try {
            // Lấy employer_id từ session
            Integer employerId = getEmployerIdFromSession(session);
            
            if (employerId == null) {
                // Trả về dữ liệu rỗng thay vì lỗi
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", java.util.Collections.emptyList());
                return ResponseEntity.ok(response);
            }

            // Xây dựng SQL query dựa trên period
            String dateFilter;
            switch (period.toLowerCase()) {
                case "1day":
                    // Lấy dữ liệu từ 00:00:00 của ngày hôm nay đến hiện tại
                    dateFilter = "AND CAST(a.applied_at AS DATE) = CAST(GETDATE() AS DATE)";
                    break;
                case "7days":
                    dateFilter = "AND a.applied_at >= DATEADD(DAY, -7, GETDATE())";
                    break;
                case "1month":
                    dateFilter = "AND a.applied_at >= DATEADD(MONTH, -1, GETDATE())";
                    break;
                case "3months":
                    dateFilter = "AND a.applied_at >= DATEADD(MONTH, -3, GETDATE())";
                    break;
                case "6months":
                    dateFilter = "AND a.applied_at >= DATEADD(MONTH, -6, GETDATE())";
                    break;
                case "12months":
                    dateFilter = "AND a.applied_at >= DATEADD(MONTH, -12, GETDATE())";
                    break;
                default:
                    dateFilter = ""; // Không filter theo thời gian
                    break;
            }

            // Query để lấy thống kê ứng viên theo vị trí
            // Đếm tổng số lượt apply vào các job posting của employer này, nhóm theo position
            // Một ứng viên có thể apply nhiều bài đăng cùng vị trí và sẽ được đếm nhiều lần
            String sql = """
                SELECT 
                    COALESCE(jp.position, 'Chưa xác định') as position,
                    COUNT(a.application_id) as candidateCount
                FROM Applications a
                INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
                WHERE jp.employer_id = ?
                """ + dateFilter + """
                
                GROUP BY jp.position
                ORDER BY candidateCount DESC
                """;

            List<CandidateByPositionDTO> stats = jdbc.query(sql, 
                (rs, rowNum) -> CandidateByPositionDTO.builder()
                    .position(rs.getString("position"))
                    .candidateCount(rs.getLong("candidateCount"))
                    .build(),
                employerId
            );

            // Chuẩn bị dữ liệu cho Chart.js
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error getting candidates by position: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Map.of("error", "Internal server error", "message", e.getMessage())
            );
        }
    }

    /**
     * API endpoint để lấy xu hướng ứng viên theo thời gian
     * Trả về dữ liệu JSON cho biểu đồ line chart
     * @param period: day, week, month, year
     */
    @GetMapping("/api/candidates-trend")
    @ResponseBody
    public ResponseEntity<?> getCandidatesTrend(HttpSession session, 
                                                 @org.springframework.web.bind.annotation.RequestParam(defaultValue = "3months") String period) {
        try {
            // Lấy employer_id từ session
            Integer employerId = getEmployerIdFromSession(session);
            
            if (employerId == null) {
                // Trả về dữ liệu rỗng thay vì lỗi
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", java.util.Collections.emptyList());
                return ResponseEntity.ok(response);
            }

            // Xây dựng SQL query dựa trên period
            String sql;
            switch (period.toLowerCase()) {
                case "7days":
                    // 7 ngày gần nhất - hiển thị theo ngày
                    sql = """
                        SELECT 
                            FORMAT(CAST(a.applied_at AS DATE), 'dd/MM') as period,
                            COUNT(a.application_id) as candidateCount
                        FROM Applications a
                        INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
                        WHERE jp.employer_id = ?
                            AND a.applied_at >= DATEADD(DAY, -7, GETDATE())
                        GROUP BY CAST(a.applied_at AS DATE)
                        ORDER BY CAST(a.applied_at AS DATE)
                        """;
                    break;
                    
                case "1month":
                    // 1 tháng gần nhất - hiển thị theo tuần
                    sql = """
                        SELECT 
                            CONCAT(N'Tuần ', DATEPART(WEEK, a.applied_at)) as period,
                            COUNT(a.application_id) as candidateCount
                        FROM Applications a
                        INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
                        WHERE jp.employer_id = ?
                            AND a.applied_at >= DATEADD(MONTH, -1, GETDATE())
                        GROUP BY YEAR(a.applied_at), DATEPART(WEEK, a.applied_at)
                        ORDER BY YEAR(a.applied_at), DATEPART(WEEK, a.applied_at)
                        """;
                    break;
                    
                case "3months":
                    // 3 tháng gần nhất - hiển thị theo tuần
                    sql = """
                        SELECT 
                            CONCAT(N'Tuần ', DATEPART(WEEK, a.applied_at)) as period,
                            COUNT(a.application_id) as candidateCount
                        FROM Applications a
                        INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
                        WHERE jp.employer_id = ?
                            AND a.applied_at >= DATEADD(MONTH, -3, GETDATE())
                        GROUP BY YEAR(a.applied_at), DATEPART(WEEK, a.applied_at)
                        ORDER BY YEAR(a.applied_at), DATEPART(WEEK, a.applied_at)
                        """;
                    break;
                    
                case "6months":
                    // 6 tháng gần nhất - hiển thị theo tháng
                    sql = """
                        SELECT 
                            CONCAT(N'Tháng ', MONTH(a.applied_at)) as period,
                            COUNT(a.application_id) as candidateCount
                        FROM Applications a
                        INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
                        WHERE jp.employer_id = ?
                            AND a.applied_at >= DATEADD(MONTH, -6, GETDATE())
                        GROUP BY YEAR(a.applied_at), MONTH(a.applied_at)
                        ORDER BY YEAR(a.applied_at), MONTH(a.applied_at)
                        """;
                    break;
                    
                case "12months":
                    // 12 tháng gần nhất - hiển thị theo tháng
                    sql = """
                        SELECT 
                            CONCAT(N'Tháng ', MONTH(a.applied_at), '/', YEAR(a.applied_at)) as period,
                            COUNT(a.application_id) as candidateCount
                        FROM Applications a
                        INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
                        WHERE jp.employer_id = ?
                            AND a.applied_at >= DATEADD(MONTH, -12, GETDATE())
                        GROUP BY YEAR(a.applied_at), MONTH(a.applied_at)
                        ORDER BY YEAR(a.applied_at), MONTH(a.applied_at)
                        """;
                    break;
                    
                default:
                    // Mặc định: 3 tháng gần nhất
                    sql = """
                        SELECT 
                            CONCAT(N'Tuần ', DATEPART(WEEK, a.applied_at)) as period,
                            COUNT(a.application_id) as candidateCount
                        FROM Applications a
                        INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
                        WHERE jp.employer_id = ?
                            AND a.applied_at >= DATEADD(MONTH, -3, GETDATE())
                        GROUP BY YEAR(a.applied_at), DATEPART(WEEK, a.applied_at)
                        ORDER BY YEAR(a.applied_at), DATEPART(WEEK, a.applied_at)
                        """;
            }

            List<CandidateTrendDTO> trends = jdbc.query(sql, 
                (rs, rowNum) -> CandidateTrendDTO.builder()
                    .period(rs.getString("period"))
                    .candidateCount(rs.getLong("candidateCount"))
                    .build(),
                employerId
            );

            // Chuẩn bị dữ liệu cho Chart.js
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", trends);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error getting candidates trend: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Map.of("error", "Internal server error", "message", e.getMessage())
            );
        }
    }

    /**
     * API endpoint để lấy thống kê trạng thái ứng viên
     * Trả về dữ liệu JSON cho biểu đồ donut chart
     * @param period: 1day, 7days, 1month, 3months, 6months, 12months
     */
    @GetMapping("/api/candidates-status")
    @ResponseBody
    public ResponseEntity<?> getCandidatesStatus(HttpSession session,
                                                  @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1day") String period) {
        try {
            // Lấy employer_id từ session
            Integer employerId = getEmployerIdFromSession(session);
            
            if (employerId == null) {
                // Trả về dữ liệu rỗng thay vì lỗi
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", java.util.Collections.emptyList());
                return ResponseEntity.ok(response);
            }

            // Xây dựng SQL query dựa trên period
            String dateFilter;
            switch (period.toLowerCase()) {
                case "1day":
                    // Lấy dữ liệu từ 00:00:00 của ngày hôm nay đến hiện tại
                    dateFilter = "AND CAST(a.applied_at AS DATE) = CAST(GETDATE() AS DATE)";
                    break;
                case "7days":
                    dateFilter = "AND a.applied_at >= DATEADD(DAY, -7, GETDATE())";
                    break;
                case "1month":
                    dateFilter = "AND a.applied_at >= DATEADD(MONTH, -1, GETDATE())";
                    break;
                case "3months":
                    dateFilter = "AND a.applied_at >= DATEADD(MONTH, -3, GETDATE())";
                    break;
                case "6months":
                    dateFilter = "AND a.applied_at >= DATEADD(MONTH, -6, GETDATE())";
                    break;
                case "12months":
                    dateFilter = "AND a.applied_at >= DATEADD(MONTH, -12, GETDATE())";
                    break;
                default:
                    dateFilter = ""; // Không filter theo thời gian
                    break;
            }

            // Query để đếm số lượng ứng viên theo từng trạng thái
            // Đếm tổng số lượt apply, một ứng viên có thể có nhiều application với các trạng thái khác nhau
            String sql = """
                SELECT 
                    a.status,
                    COUNT(a.application_id) as count
                FROM Applications a
                INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
                WHERE jp.employer_id = ?
                """ + dateFilter + """
                
                GROUP BY a.status
                ORDER BY a.status
                """;

            List<CandidateStatusDTO> statuses = jdbc.query(sql, 
                (rs, rowNum) -> {
                    String status = rs.getString("status");
                    Long count = rs.getLong("count");
                    
                    // Map status sang label tiếng Việt
                    String label;
                    switch (status.toLowerCase()) {
                        case "submitted":
                            label = "Đã nộp";
                            break;
                        case "reviewed":
                            label = "Đã xem";
                            break;
                        case "interviewed":
                            label = "Đã phỏng vấn";
                            break;
                        case "hired":
                            label = "Đã tuyển";
                            break;
                        case "rejected":
                            label = "Từ chối";
                            break;
                        default:
                            label = status;
                    }
                    
                    return CandidateStatusDTO.builder()
                        .status(status)
                        .count(count)
                        .statusLabel(label)
                        .build();
                },
                employerId
            );

            // Chuẩn bị dữ liệu cho Chart.js
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statuses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error getting candidates status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Map.of("error", "Internal server error", "message", e.getMessage())
            );
        }
    }

    /**
     * API endpoint để lấy thống kê tổng quan với phần trăm thay đổi
     * @param period: 7days, 1month, 3months, 6months, 12months
     */
    @GetMapping("/api/dashboard-stats")
    @ResponseBody
    public ResponseEntity<?> getDashboardStats(HttpSession session,
                                                @org.springframework.web.bind.annotation.RequestParam(defaultValue = "3months") String period) {
        try {
            // Lấy employer_id từ session
            Integer employerId = getEmployerIdFromSession(session);
            
            if (employerId == null) {
                // Trả về dữ liệu mặc định
                DashboardStatsDTO emptyStats = DashboardStatsDTO.builder()
                    .totalJobs(0L)
                    .totalApplications(0L)
                    .pendingReview(0L)
                    .reviewedCount(0L)
                    .rejectedCount(0L)
                    .acceptedCount(0L)
                    .jobsChangePercent(0.0)
                    .applicationsChangePercent(0.0)
                    .pendingChangePercent(0.0)
                    .reviewedChangePercent(0.0)
                    .rejectedChangePercent(0.0)
                    .acceptedChangePercent(0.0)
                    .build();
                    
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", emptyStats);
                return ResponseEntity.ok(response);
            }

            // Xác định khoảng thời gian
            String dateAddClause;
            switch (period.toLowerCase()) {
                case "1day":
                    dateAddClause = "DATEADD(DAY, -1, GETDATE())";
                    break;
                case "7days":
                    dateAddClause = "DATEADD(DAY, -7, GETDATE())";
                    break;
                case "1month":
                    dateAddClause = "DATEADD(MONTH, -1, GETDATE())";
                    break;
                case "6months":
                    dateAddClause = "DATEADD(MONTH, -6, GETDATE())";
                    break;
                case "12months":
                    dateAddClause = "DATEADD(MONTH, -12, GETDATE())";
                    break;
                default: // 3months
                    dateAddClause = "DATEADD(MONTH, -3, GETDATE())";
            }

            // Query thống kê kỳ hiện tại và kỳ trước
            String sql = String.format("""
                DECLARE @current_start DATETIME = %s;
                DECLARE @current_end DATETIME = GETDATE();
                DECLARE @period_length INT = DATEDIFF(DAY, @current_start, @current_end);
                DECLARE @previous_start DATETIME = DATEADD(DAY, -@period_length, @current_start);
                DECLARE @previous_end DATETIME = @current_start;
                
                -- Thống kê kỳ hiện tại
                SELECT 
                    (SELECT COUNT(*) FROM JobsPosting WHERE employer_id = ? AND posted_at >= @current_start AND posted_at <= @current_end) as currentJobs,
                    (SELECT COUNT(*) FROM Applications a INNER JOIN JobsPosting jp ON a.job_id = jp.job_id WHERE jp.employer_id = ? AND a.applied_at >= @current_start AND a.applied_at <= @current_end) as currentApplications,
                    (SELECT COUNT(*) FROM Applications a INNER JOIN JobsPosting jp ON a.job_id = jp.job_id WHERE jp.employer_id = ? AND a.status = N'submitted' AND a.applied_at >= @current_start AND a.applied_at <= @current_end) as currentPending,
                    (SELECT COUNT(*) FROM Applications a INNER JOIN JobsPosting jp ON a.job_id = jp.job_id WHERE jp.employer_id = ? AND a.status = N'reviewed' AND a.last_status_at >= @current_start AND a.last_status_at <= @current_end) as currentReviewed,
                    (SELECT COUNT(*) FROM Applications a INNER JOIN JobsPosting jp ON a.job_id = jp.job_id WHERE jp.employer_id = ? AND a.status = N'rejected' AND a.last_status_at >= @current_start AND a.last_status_at <= @current_end) as currentRejected,
                    (SELECT COUNT(*) FROM Applications a INNER JOIN JobsPosting jp ON a.job_id = jp.job_id WHERE jp.employer_id = ? AND a.status = N'hired' AND a.last_status_at >= @current_start AND a.last_status_at <= @current_end) as currentAccepted,
                    
                    -- Thống kê kỳ trước
                    (SELECT COUNT(*) FROM JobsPosting WHERE employer_id = ? AND posted_at >= @previous_start AND posted_at < @previous_end) as previousJobs,
                    (SELECT COUNT(*) FROM Applications a INNER JOIN JobsPosting jp ON a.job_id = jp.job_id WHERE jp.employer_id = ? AND a.applied_at >= @previous_start AND a.applied_at < @previous_end) as previousApplications,
                    (SELECT COUNT(*) FROM Applications a INNER JOIN JobsPosting jp ON a.job_id = jp.job_id WHERE jp.employer_id = ? AND a.status = N'submitted' AND a.applied_at >= @previous_start AND a.applied_at < @previous_end) as previousPending,
                    (SELECT COUNT(*) FROM Applications a INNER JOIN JobsPosting jp ON a.job_id = jp.job_id WHERE jp.employer_id = ? AND a.status = N'reviewed' AND a.last_status_at >= @previous_start AND a.last_status_at < @previous_end) as previousReviewed,
                    (SELECT COUNT(*) FROM Applications a INNER JOIN JobsPosting jp ON a.job_id = jp.job_id WHERE jp.employer_id = ? AND a.status = N'rejected' AND a.last_status_at >= @previous_start AND a.last_status_at < @previous_end) as previousRejected,
                    (SELECT COUNT(*) FROM Applications a INNER JOIN JobsPosting jp ON a.job_id = jp.job_id WHERE jp.employer_id = ? AND a.status = N'hired' AND a.last_status_at >= @previous_start AND a.last_status_at < @previous_end) as previousAccepted
                """, dateAddClause);

            DashboardStatsDTO stats = jdbc.queryForObject(sql, 
                (rs, rowNum) -> {
                    long currentJobs = rs.getLong("currentJobs");
                    long currentApplications = rs.getLong("currentApplications");
                    long currentPending = rs.getLong("currentPending");
                    long currentReviewed = rs.getLong("currentReviewed");
                    long currentRejected = rs.getLong("currentRejected");
                    long currentAccepted = rs.getLong("currentAccepted");
                    
                    long previousJobs = rs.getLong("previousJobs");
                    long previousApplications = rs.getLong("previousApplications");
                    long previousPending = rs.getLong("previousPending");
                    long previousReviewed = rs.getLong("previousReviewed");
                    long previousRejected = rs.getLong("previousRejected");
                    long previousAccepted = rs.getLong("previousAccepted");
                    
                    // Tính phần trăm thay đổi
                    double jobsChange = calculatePercentChange(previousJobs, currentJobs);
                    double applicationsChange = calculatePercentChange(previousApplications, currentApplications);
                    double pendingChange = calculatePercentChange(previousPending, currentPending);
                    double reviewedChange = calculatePercentChange(previousReviewed, currentReviewed);
                    double rejectedChange = calculatePercentChange(previousRejected, currentRejected);
                    double acceptedChange = calculatePercentChange(previousAccepted, currentAccepted);
                    
                    return DashboardStatsDTO.builder()
                        .totalJobs(currentJobs)
                        .totalApplications(currentApplications)
                        .pendingReview(currentPending)
                        .reviewedCount(currentReviewed)
                        .rejectedCount(currentRejected)
                        .acceptedCount(currentAccepted)
                        .jobsChangePercent(jobsChange)
                        .applicationsChangePercent(applicationsChange)
                        .pendingChangePercent(pendingChange)
                        .reviewedChangePercent(reviewedChange)
                        .rejectedChangePercent(rejectedChange)
                        .acceptedChangePercent(acceptedChange)
                        .build();
                },
                employerId, employerId, employerId, employerId, employerId, employerId,
                employerId, employerId, employerId, employerId, employerId, employerId
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error getting dashboard stats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Map.of("error", "Internal server error", "message", e.getMessage())
            );
        }
    }

    // Helper method để lấy employer_id từ user_id
    private Integer getEmployerIdFromSession(HttpSession session) {
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            return null;
        }
        
        com.joblink.joblink.dto.UserSessionDTO user = 
            (com.joblink.joblink.dto.UserSessionDTO) userObj;
        
        if (!"employer".equalsIgnoreCase(user.getRole())) {
            return null;
        }
        
        try {
            List<Integer> employerIds = jdbc.query(
                "SELECT employer_id FROM EmployerProfile WHERE user_id = ?",
                (rs, rowNum) -> rs.getInt("employer_id"),
                user.getUserId()
            );
            
            return employerIds.isEmpty() ? null : employerIds.get(0);
        } catch (Exception e) {
            return null;
        }
    }
    
    // Helper method để tính phần trăm thay đổi (giới hạn từ -100% đến +100%)
    private double calculatePercentChange(long previous, long current) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        
        double percentChange = ((double)(current - previous) / previous) * 100.0;
        
        // Giới hạn trong khoảng -100% đến +100%
        if (percentChange > 100.0) {
            return 100.0;
        } else if (percentChange < -100.0) {
            return -100.0;
        }
        
        return percentChange;
    }

    /**
     * API endpoint để lấy phân tích thị trường theo position
     * @param position: vị trí cần phân tích (null = tất cả positions)
     */
    @GetMapping("/api/market-analysis")
    @ResponseBody
    public ResponseEntity<?> getMarketAnalysis(HttpSession session,
                                                @org.springframework.web.bind.annotation.RequestParam(required = false) String position) {
        try {
            // Kiểm tra login
            Object userObj = session.getAttribute("user");
            if (userObj == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            com.joblink.joblink.dto.UserSessionDTO user = 
                (com.joblink.joblink.dto.UserSessionDTO) userObj;
            
            if (!"employer".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
            }

            // Query phân tích thị trường - hỗ trợ filter theo position
            String sql = """
                DECLARE @current_start DATETIME = DATEADD(MONTH, -3, GETDATE());
                DECLARE @current_end DATETIME = GETDATE();
                DECLARE @previous_start DATETIME = DATEADD(MONTH, -6, GETDATE());
                DECLARE @previous_end DATETIME = @current_start;
                DECLARE @position NVARCHAR(150) = ?;
                
                WITH CurrentPeriod AS (
                    SELECT 
                        COUNT(DISTINCT jp.job_id) as total_jobs,
                        COUNT(DISTINCT CASE WHEN jp.status = 'ACTIVE' AND jp.submission_deadline > GETDATE() THEN jp.job_id END) as active_jobs,
                        AVG(CASE WHEN jp.salary_min IS NOT NULL AND jp.salary_min > 0 THEN CAST(jp.salary_min as FLOAT) ELSE NULL END) as avg_salary_min,
                        AVG(CASE WHEN jp.salary_max IS NOT NULL AND jp.salary_max > 0 THEN CAST(jp.salary_max as FLOAT) ELSE NULL END) as avg_salary_max,
                        COUNT(DISTINCT a.application_id) as total_applicants
                    FROM JobsPosting jp
                    LEFT JOIN Applications a ON jp.job_id = a.job_id
                    WHERE jp.posted_at >= @current_start AND jp.posted_at <= @current_end
                        AND (@position IS NULL OR @position = '' OR jp.position = @position)
                ),
                PreviousPeriod AS (
                    SELECT 
                        COUNT(DISTINCT jp.job_id) as prev_total_jobs
                    FROM JobsPosting jp
                    WHERE jp.posted_at >= @previous_start AND jp.posted_at < @previous_end
                        AND (@position IS NULL OR @position = '' OR jp.position = @position)
                ),
                ExperienceStats AS (
                    SELECT 
                        jp.year_experience,
                        COUNT(*) as exp_count,
                        ROW_NUMBER() OVER (ORDER BY COUNT(*) DESC) as rn
                    FROM JobsPosting jp
                    WHERE jp.posted_at >= @current_start AND jp.posted_at <= @current_end
                        AND (@position IS NULL OR @position = '' OR jp.position = @position)
                    GROUP BY jp.year_experience
                ),
                LocationStats AS (
                    SELECT 
                        ISNULL(p.province_name, N'Chưa xác định') as location,
                        COUNT(*) as loc_count,
                        ROW_NUMBER() OVER (ORDER BY COUNT(*) DESC) as rn
                    FROM JobsPosting jp
                    LEFT JOIN Provinces p ON jp.province_id = p.province_id
                    WHERE jp.posted_at >= @current_start AND jp.posted_at <= @current_end
                        AND (@position IS NULL OR @position = '' OR jp.position = @position)
                    GROUP BY p.province_name
                )
                SELECT 
                    CASE WHEN @position IS NULL OR @position = '' THEN N'Tất cả vị trí' ELSE @position END as position,
                    cp.total_jobs,
                    cp.active_jobs,
                    ISNULL(pp.prev_total_jobs, 0) as prev_total_jobs,
                    cp.avg_salary_min,
                    cp.avg_salary_max,
                    cp.total_applicants,
                    es.year_experience as most_common_experience,
                    ls.location as top_location
                FROM CurrentPeriod cp
                CROSS JOIN PreviousPeriod pp
                LEFT JOIN ExperienceStats es ON es.rn = 1
                LEFT JOIN LocationStats ls ON ls.rn = 1
                """;

            // Xử lý position parameter (null hoặc empty string = tất cả)
            String positionParam = (position == null || position.trim().isEmpty()) ? null : position.trim();
            
            List<MarketAnalysisDTO> results = jdbc.query(sql, 
                (rs, rowNum) -> {
                    long totalJobs = rs.getLong("total_jobs");
                    long activeJobs = rs.getLong("active_jobs");
                    long prevTotalJobs = rs.getLong("prev_total_jobs");
                    long totalApplicants = rs.getLong("total_applicants");
                    
                    // Tính growth rate
                    double growthRate = calculatePercentChange(prevTotalJobs, totalJobs);
                    
                    // Tính mức lương trung bình
                    Double avgSalaryMin = rs.getObject("avg_salary_min", Double.class);
                    Double avgSalaryMax = rs.getObject("avg_salary_max", Double.class);
                    Double avgSalary = null;
                    if (avgSalaryMin != null && avgSalaryMax != null) {
                        avgSalary = (avgSalaryMin + avgSalaryMax) / 2;
                    }
                    
                    // Tính mức độ cạnh tranh
                    double avgApplicantsPerJob = totalJobs > 0 ? (double) totalApplicants / totalJobs : 0;
                    String competitionLevel;
                    if (avgApplicantsPerJob < 5) {
                        competitionLevel = "Thấp";
                    } else if (avgApplicantsPerJob < 15) {
                        competitionLevel = "Trung bình";
                    } else {
                        competitionLevel = "Cao";
                    }
                    
                    return MarketAnalysisDTO.builder()
                        .position(rs.getString("position"))
                        .totalJobs(totalJobs)
                        .activeJobs(activeJobs)
                        .growthRate(growthRate)
                        .avgSalaryMin(avgSalaryMin)
                        .avgSalaryMax(avgSalaryMax)
                        .avgSalary(avgSalary)
                        .totalApplicants(totalApplicants)
                        .avgApplicantsPerJob(avgApplicantsPerJob)
                        .competitionLevel(competitionLevel)
                        .mostCommonExperience(rs.getString("most_common_experience"))
                        .topLocation(rs.getString("top_location"))
                        .build();
                },
                positionParam
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results.isEmpty() ? null : results.get(0));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error getting market analysis: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Map.of("error", "Internal server error", "message", e.getMessage())
            );
        }
    }

    /**
     * API endpoint để lấy danh sách tất cả positions
     */
    @GetMapping("/api/positions")
    @ResponseBody
    public ResponseEntity<?> getAllPositions(HttpSession session) {
        try {
            // Kiểm tra login
            Object userObj = session.getAttribute("user");
            if (userObj == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            String sql = """
                SELECT DISTINCT ISNULL(position, N'Chưa xác định') as position
                FROM JobsPosting
                WHERE position IS NOT NULL
                ORDER BY position
                """;

            List<String> positions = jdbc.queryForList(sql, String.class);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", positions);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error getting positions: " + e.getMessage());
            return ResponseEntity.status(500).body(
                Map.of("error", "Internal server error", "message", e.getMessage())
            );
        }
    }
}
