package com.joblink.joblink.service;

import com.joblink.joblink.dto.ApplicationFilter;
import com.joblink.joblink.entity.Application;
import com.joblink.joblink.repository.ApplicationRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    /**
     * Lấy danh sách applications với filter (cho trang chính)
     */
    public Page<Application> getApplicationsWithFilters(ApplicationFilter filter, Long employerId) {
        // Xử lý các tham số null trước khi truyền vào repository
        String search = filter.getSearch() != null ? filter.getSearch() : "";
        List<String> positions = filter.getPositions() != null ? filter.getPositions() : List.of();
        List<String> statuses = filter.getStatuses() != null ? filter.getStatuses() : List.of();
        List<String> educationLevels = filter.getEducationLevels() != null ? filter.getEducationLevels() : List.of();
        String location = filter.getLocation() != null ? filter.getLocation() : "";

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Page<Object[]> results = applicationRepository.findApplicationsWithFiltersNative(
                search,
                positions,
                filter.getMinExperience(),
                filter.getMaxExperience(),
                statuses,
                educationLevels,
                location,
                employerId,
                pageable
        );

        List<Application> applications = results.getContent().stream()
                .map(this::mapToApplication)
                .collect(Collectors.toList());

        return new PageImpl<>(applications, pageable, results.getTotalElements());
    }

    /**
     * Lấy danh sách applications ĐÃ BOOKMARK (cho trang saved)
     */
    public Page<Application> getSavedApplicationsWithFilters(ApplicationFilter filter, Long employerId) {
        // Xử lý các tham số null trước khi truyền vào repository
        String search = filter.getSearch() != null ? filter.getSearch() : "";
        List<String> positions = filter.getPositions() != null ? filter.getPositions() : List.of();
        List<String> statuses = filter.getStatuses() != null ? filter.getStatuses() : List.of();
        List<String> educationLevels = filter.getEducationLevels() != null ? filter.getEducationLevels() : List.of();
        String location = filter.getLocation() != null ? filter.getLocation() : "";

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Page<Object[]> results = applicationRepository.findSavedApplicationsWithFiltersNative(
                search,
                positions,
                filter.getMinExperience(),
                filter.getMaxExperience(),
                statuses,
                educationLevels,
                location,
                employerId,
                pageable
        );

        List<Application> applications = results.getContent().stream()
                .map(this::mapToApplication)
                .collect(Collectors.toList());

        return new PageImpl<>(applications, pageable, results.getTotalElements());
    }

    /**
     * Map Object[] từ native query sang Application entity
     * FIXED: Xử lý null values an toàn
     */
    private Application mapToApplication(Object[] result) {
        Application app = new Application();

        // Xử lý các field với kiểm tra null
        if (result[0] != null) app.setApplicationId(((Number) result[0]).longValue());
        if (result[1] != null) app.setJobId(((Number) result[1]).intValue());
        if (result[2] != null) app.setSeekerId(((Number) result[2]).intValue());
        app.setStatus(result[3] != null ? (String) result[3] : "");

        if (result[4] != null) {
            app.setAppliedAt(((Timestamp) result[4]).toLocalDateTime());
        } else {
            app.setAppliedAt(LocalDateTime.now());
        }

        if (result[5] != null) {
            app.setLastStatusAt(((Timestamp) result[5]).toLocalDateTime());
        }

        app.setCvUrl((String) result[6]);
        app.setNote((String) result[7]);

        // Các transient fields
        app.setCandidateName((String) result[8]);
        app.setCandidateEmail((String) result[9]);
        app.setCandidatePhone((String) result[10]);
        app.setAvatarUrl((String) result[11]);
        app.setLocation((String) result[12]);

        if (result[13] != null) {
            app.setExperienceYears(((Number) result[13]).intValue());
        } else {
            app.setExperienceYears(0);
        }

        app.setPosition((String) result[14]);
        app.setEducation((String) result[15]);

        // Xử lý Boolean saved
        if (result[16] != null) {
            app.setSaved(((Number) result[16]).intValue() == 1);
        } else {
            app.setSaved(false);
        }

        return app;
    }

    /**
     * Cập nhật trạng thái application
     */
    public Application updateApplicationStatus(Long applicationId, String status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));

        application.setStatus(status);
        application.setLastStatusAt(LocalDateTime.now());
        return applicationRepository.save(application);
    }

    /**
     * Toggle bookmark/lưu ứng viên
     * FIXED: Thêm try-catch để xử lý lỗi
     */
    public boolean toggleBookmark(Long applicationId, Long employerId) {
        try {
            boolean currentState = applicationRepository.isApplicationBookmarked(applicationId, employerId);

            if (currentState) {
                applicationRepository.removeBookmark(employerId, applicationId);
                return false;
            } else {
                applicationRepository.addBookmark(employerId, applicationId);
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to toggle bookmark: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra ứng viên đã được bookmark chưa
     */
    public boolean isBookmarked(Long applicationId, Long employerId) {
        return applicationRepository.isApplicationBookmarked(applicationId, employerId);
    }

    /**
     * Lấy các tùy chọn filter (positions, locations, education levels, statuses)
     */
    public Map<String, Object> getFilterOptions() {
        Map<String, Object> options = new HashMap<>();
        try {
            options.put("positions", applicationRepository.findDistinctPositions());
            options.put("locations", applicationRepository.findDistinctLocations());
            options.put("educationLevels", applicationRepository.findDistinctEducationLevels());
            options.put("statuses", applicationRepository.findDistinctStatuses());
        } catch (Exception e) {
            // Trả về danh sách rỗng nếu có lỗi
            options.put("positions", List.of());
            options.put("locations", List.of());
            options.put("educationLevels", List.of());
            options.put("statuses", List.of());
        }
        return options;
    }

    /**
     * Kiểm tra ứng viên đã apply job chưa
     */
    public boolean hasApplied(Integer jobId, Integer seekerId) {
        return applicationRepository.existsByJobIdAndSeekerId(jobId, seekerId);
    }

    /**
     * Lấy application đơn giản (cho testing)
     */
    public Page<Application> getApplicationsSimple(Pageable pageable) {
        Page<Object[]> results = applicationRepository.findApplicationsSimple(pageable);

        List<Application> applications = results.getContent().stream()
                .map(this::mapToApplication)
                .collect(Collectors.toList());

        return new PageImpl<>(applications, pageable, results.getTotalElements());
    }
}