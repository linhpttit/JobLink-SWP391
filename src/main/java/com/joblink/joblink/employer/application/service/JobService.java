package com.joblink.joblink.employer.application.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.joblink.joblink.employer.application.model.JobCardVM;
import com.joblink.joblink.employer.application.spec.JobPostingSpecs;
import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.entity.Province;
import com.joblink.joblink.repository.ApplicationRepository;
import com.joblink.joblink.repository.JobPostingRepository;
import com.joblink.joblink.repository.ProvinceRepository;

import lombok.RequiredArgsConstructor;

@Service("NewJobService")
@RequiredArgsConstructor
public class JobService implements IJobService{

    private final JobPostingRepository jobRepo;
    private final ProvinceRepository provinceRepo;
    private final ApplicationRepository appRepo;
    
    @Override
    public Page<JobCardVM> search(String q, Long provinceId, String workType, Integer minSalary,
                                  Pageable pageable) {

        Specification<JobPosting> spec = Specification.where(JobPostingSpecs.active())
                .and(JobPostingSpecs.keyword(q))
                .and(JobPostingSpecs.provinceIs(provinceId))
                .and(JobPostingSpecs.workTypeIs(workType))
                .and(JobPostingSpecs.salaryAtLeast(minSalary));

        Page<JobPosting> page = jobRepo.findAll(spec, pageable);

        // Batch load application counts (Applications.job_id is Integer)
        List<Integer> idsAsInt = page.getContent().stream()
                .map(j -> j.getJobId() == null ? null : j.getJobId().intValue())
                .filter(Objects::nonNull)
                .toList();

        Map<Integer, Long> counts = new HashMap<>();
        if (!idsAsInt.isEmpty()) {
            for (Map<String, Object> row : appRepo.countGroupedByJobId(idsAsInt)) {
                Integer id = (Integer) row.get("jobId");
                Long cnt = (Long) row.get("cnt");
                counts.put(id, cnt);
            }
        }

        List<JobCardVM> mapped = page.getContent().stream().map(j -> {
            int apps = (j.getJobId() == null) ? 0 : counts.getOrDefault(j.getJobId().intValue(), 0L).intValue();
            return toVM(j, apps);
        }).toList();

        return new PageImpl<>(mapped, pageable, page.getTotalElements());
    }

    public List<Province> findAllProvinces() {
        return provinceRepo.findAll(Sort.by("provinceName").ascending());
    }

    /* ====================== helpers ====================== */

    private JobCardVM toVM(JobPosting j, int applicationsCount) {
        return JobCardVM.builder()
                .jobId(j.getJobId())
                .title(nz(j.getTitle(), ""))
                .position(nz(j.getPosition(), ""))
                .provinceName(j.getProvince() != null ? nz(j.getProvince().getProvinceName(), "Toàn quốc") : "Toàn quốc")
                .yearExperience(nz(j.getYearExperience(), "Không yêu cầu"))
                .workType(nz(j.getWorkType(), null))
                .salaryText(formatVndRange(j.getSalaryMin(), j.getSalaryMax()))
                .postedAtText(timeAgo(j.getPostedAt()))
                .submissionDeadlineText(j.getSubmissionDeadline() != null
                        ? "Hạn: " + j.getSubmissionDeadline().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : null)
                .applicationsCount(applicationsCount)
                .build();
    }

    private String nz(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
    }

    private String formatVnd(BigDecimal v) {
        if (v == null) return null;
        // show as integer VND (no decimals)
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        return nf.format(v);
    }

    private String formatVndRange(BigDecimal min, BigDecimal max) {
        String l = formatVnd(min);
        String r = formatVnd(max);
        if (l == null && r == null) return "Thỏa thuận";
        if (l != null && r != null) return l + " – " + r + " VND";
        return (l != null ? l : r) + " VND";
    }

    private String timeAgo(LocalDateTime dt) {
        if (dt == null) return "";
        Duration d = Duration.between(dt, LocalDateTime.now());
        long days = d.toDays();
        if (days > 30) return (days / 30) + " tháng trước";
        if (days >= 1) return days + " ngày trước";
        long hours = d.toHours();
        if (hours >= 1) return hours + " giờ trước";
        long mins = Math.max(1, d.toMinutes());
        return mins + " phút trước";
    }
}
