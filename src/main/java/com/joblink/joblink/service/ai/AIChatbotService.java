package com.joblink.joblink.service.ai;

import com.joblink.joblink.dto.ChatResponse;
import com.joblink.joblink.dto.JobResult;
import com.joblink.joblink.dto.EmployerResult;
import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.entity.Employer;
import com.joblink.joblink.Repository.JobPostingRepository;
import com.joblink.joblink.Repository.EmployerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIChatbotService {

    private final JobPostingRepository jobRepository;
    private final EmployerRepository employerRepository;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${grok.api.key}")
    private String grokApiKey;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";public ChatResponse processQuestion(String question) {
        log.info("Processing question: {}", question);

        // Bước 1: Phân tích intent bằng Gemini
        String intent = analyzeIntentWithGemini(question);
        log.info("Detected intent: {}", intent);

        // Bước 2: Trích xuất thông tin tìm kiếm
        Map<String, String> searchParams = extractSearchParams(question);
        log.info("Search params: {}", searchParams);

        // Bước 3: Tìm kiếm và trả về kết quả
        if ("job".equalsIgnoreCase(intent)) {
            return searchAndReturnJobs(question, searchParams);
        } else if ("employer".equalsIgnoreCase(intent)) {
            return searchAndReturnEmployers(question, searchParams);
        } else {
            return ChatResponse.builder()
                    .message("Tôi có thể giúp bạn tìm kiếm công việc hoặc thông tin về nhà tuyển dụng. Bạn muốn tìm gì?")
                    .type("general")
                    .build();
        }
    }

    private String analyzeIntentWithGemini(String question) {
        try {
            String prompt = String.format(
                    "Phân tích câu hỏi sau và xác định người dùng muốn tìm kiếm về 'job' (công việc) hay 'employer' (nhà tuyển dụng/công ty).\n" +
                            "Chỉ trả về một từ: 'job' hoặc 'employer' hoặc 'general'.\n\n" +
                            "Câu hỏi: %s\n\n" +
                            "Trả lời (chỉ một từ):", question
            );

            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = GEMINI_API_URL + "?key=" + geminiApiKey;
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                    List<Map<String, String>> partsList = (List<Map<String, String>>) contentMap.get("parts");
                    String text = partsList.get(0).get("text").toLowerCase().trim();

                    if (text.contains("job")) return "job";
                    if (text.contains("employer")) return "employer";
                }
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
        }

        return "general";
    }

    private Map<String, String> extractSearchParams(String question) {
        Map<String, String> params = new HashMap<>();
        String lowerQuestion = question.toLowerCase();

        // Trích xuất kỹ năng
        String[] skills = {"java", "python", "javascript", "react", "nodejs", "angular", "sql", "spring boot", "docker"};
        List<String> foundSkills = new ArrayList<>();
        for (String skill : skills) {
            if (lowerQuestion.contains(skill)) {
                foundSkills.add(skill);
            }
        }
        if (!foundSkills.isEmpty()) {
            params.put("skills", String.join(",", foundSkills));
        }

        // Trích xuất địa điểm
        String[] locations = {"hà nội", "hồ chí minh", "đà nẵng", "quảng ninh", "nam định"};
        for (String location : locations) {
            if (lowerQuestion.contains(location)) {
                params.put("location", location);
                break;
            }
        }

        // Trích xuất mức lương (triệu VNĐ)
        if (lowerQuestion.matches(".*\\d+\\s*(triệu|tr|million).*")) {
            String salaryStr = lowerQuestion.replaceAll(".*?(\\d+)\\s*(triệu|tr|million).*", "$1");
            params.put("minSalary", salaryStr + "000000"); // Convert to actual amount
        }

        return params;
    }

    private ChatResponse searchAndReturnJobs(String question, Map<String, String> params) {
        try {
            List<JobPosting> jobs = new ArrayList<>();

            // Tìm kiếm theo thứ tự ưu tiên
            if (params.containsKey("skills") && params.containsKey("location")) {
                // Tìm theo cả skill VÀ location
                String[] skillNames = params.get("skills").toLowerCase().split(",");
                List<String> skillList = Arrays.stream(skillNames)
                        .map(String::trim)
                        .collect(Collectors.toList());

                List<JobPosting> jobsBySkill = jobRepository.findBySkillNames(skillList);
                String location = params.get("location");

                jobs = jobsBySkill.stream()
                        .filter(job -> {
                            String jobLocation = "";
                            if (job.getProvince() != null) {
                                jobLocation += job.getProvince().getProvinceName().toLowerCase();
                            }
                            if (job.getEmployer() != null && job.getEmployer().getLocation() != null) {
                                jobLocation += " " + job.getEmployer().getLocation().toLowerCase();
                            }
                            return jobLocation.contains(location.toLowerCase());
                        })
                        .collect(Collectors.toList());

            } else if (params.containsKey("skills")) {
                // Chỉ tìm theo skill
                String[] skillNames = params.get("skills").toLowerCase().split(",");
                List<String> skillList = Arrays.stream(skillNames)
                        .map(String::trim)
                        .collect(Collectors.toList());
                jobs = jobRepository.findBySkillNames(skillList);

            } else if (params.containsKey("location")) {

                // Chỉ tìm theo location
                jobs = jobRepository.findByLocationContaining(params.get("location"));

            } else if (params.containsKey("minSalary")) {

                // Tìm theo mức lương
                Double minSalaryDouble = Double.parseDouble(params.get("minSalary"));
                BigDecimal minSalary = BigDecimal.valueOf(minSalaryDouble);
                jobs = jobRepository.findTop10ByStatusOrderByPostedAtDesc("ACTIVE")
                        .stream()
                        .filter(job ->
                                job.getSalaryMin() != null &&
                                        // Dùng compareTo: (A.compareTo(B) >= 0) nghĩa là (A >= B)
                                        job.getSalaryMin().compareTo(minSalary) >= 0
                        )
                        .collect(Collectors.toList());

            } else {
                // Lấy các job mới nhất
                jobs = jobRepository.findTop10ByStatusOrderByPostedAtDesc("ACTIVE");
            }

            if (jobs.isEmpty()) {
                return ChatResponse.builder()
                        .message("Xin lỗi, tôi không tìm thấy công việc phù hợp với yêu cầu của bạn. Bạn có thể thử:\n" +
                                "• Mở rộng khu vực tìm kiếm\n" +
                                "• Thay đổi kỹ năng tìm kiếm\n" +
                                "• Xem các công việc mới nhất")
                        .type("job")
                        .build();
            }

            // Tạo response với danh sách job (giới hạn 5 kết quả)
            List<JobResult> jobResults = jobs.stream()
                    .limit(5)
                    .map(job -> {
                        String location = "Không xác định";
                        if (job.getProvince() != null) {
                            location = job.getProvince().getProvinceName();
                            if (job.getDistrict() != null) {
                                location = job.getDistrict().getDistrictName() + ", " + location;
                            }
                        }

                        return JobResult.builder()
                                .jobId(job.getJobId())
                                .title(job.getTitle())
                                .companyName(job.getEmployer() != null ? job.getEmployer().getCompanyName() : "N/A")
                                .location(location)
                                .salary(formatSalary(job.getSalaryMin(), job.getSalaryMax()))
                                .url("/job-detail/" + job.getJobId())
                                .build();
                    })
                    .collect(Collectors.toList());

            String message = generateJobResponseMessage(question, jobResults.size(), params);

            return ChatResponse.builder()
                    .message(message)
                    .type("job")
                    .jobs(jobResults)
                    .build();

        } catch (Exception e) {
            log.error("Error searching jobs", e);
            return ChatResponse.builder()
                    .message("Đã xảy ra lỗi khi tìm kiếm công việc. Vui lòng thử lại!")
                    .type("error")
                    .build();
        }
    }

    private ChatResponse searchAndReturnEmployers(String question, Map<String, String> params) {
        try {
            List<Employer> employers;

            if (params.containsKey("location")) {
                employers = employerRepository.findByLocationContaining(params.get("location"));
            } else {
                // Lấy các employer có nhiều job đang tuyển
                employers = employerRepository.findTop10ByOrderByCompanyNameAsc();
            }

            if (employers.isEmpty()) {
                return ChatResponse.builder()
                        .message("Xin lỗi, tôi không tìm thấy nhà tuyển dụng phù hợp. Bạn có thể thử tìm kiếm với từ khóa khác!")
                        .type("employer")
                        .build();
            }

            List<EmployerResult> employerResults = employers.stream()
                    .limit(5)
                    .map(emp -> EmployerResult.builder()
                            .employerId(emp.getId())
                            .companyName(emp.getCompanyName())
                            .industry(emp.getIndustry())
                            .location(emp.getLocation())
                            .description(emp.getDescription())
                            .url("/employer/" + emp.getId())
                            .build())
                    .collect(Collectors.toList());

            String message = String.format("Tôi tìm thấy %d nhà tuyển dụng phù hợp với yêu cầu của bạn:",
                    employerResults.size());

            return ChatResponse.builder()
                    .message(message)
                    .type("employer")
                    .employers(employerResults)
                    .build();

        } catch (Exception e) {
            log.error("Error searching employers", e);
            return ChatResponse.builder()
                    .message("Đã xảy ra lỗi khi tìm kiếm nhà tuyển dụng. Vui lòng thử lại!")
                    .type("error")
                    .build();
        }
    }

    private String formatSalary(BigDecimal min, BigDecimal max) {
        BigDecimal MILLION = new BigDecimal("1000000");
        if (min == null && max == null) return "Thỏa thuận";
        if (min != null && max != null) {
            // Phải dùng .divide() và cung cấp RoundingMode
            BigDecimal minSalary = min.divide(MILLION, 0, RoundingMode.HALF_UP);
            BigDecimal maxSalary = max.divide(MILLION, 0, RoundingMode.HALF_UP);
            return String.format("%s - %s triệu", minSalary, maxSalary);
        }
        if (min != null) {
            BigDecimal minSalary = min.divide(MILLION, 0, RoundingMode.HALF_UP);
            return String.format("Từ %s triệu", minSalary);
        }

        BigDecimal maxSalary = max.divide(MILLION, 0, RoundingMode.HALF_UP);
        return String.format("Lên đến %s triệu", maxSalary);
    }

    private String generateJobResponseMessage(String question, int count, Map<String, String> params) {
        StringBuilder msg = new StringBuilder();
        msg.append(String.format("Tôi tìm thấy %d công việc phù hợp", count));

        if (params.containsKey("skills")) {
            msg.append(" cho kỹ năng ").append(params.get("skills"));
        }
        if (params.containsKey("location")) {
            msg.append(" tại ").append(params.get("location"));
        }
        msg.append(":");

        return msg.toString();
    }
}
