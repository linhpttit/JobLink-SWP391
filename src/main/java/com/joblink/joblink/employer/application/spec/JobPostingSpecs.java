package com.joblink.joblink.employer.application.spec;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.joblink.joblink.entity.JobPosting;

public class JobPostingSpecs {

    public static Specification<JobPosting> active() {
        return (root, q, cb) -> cb.equal(root.get("status"), "ACTIVE");
    }

    public static Specification<JobPosting> keyword(String qStr) {
        if (qStr == null || qStr.isBlank()) return null;
        String like = "%" + qStr.trim().toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("position")), like),
                cb.like(cb.lower(root.get("jobDescription")), like),
                cb.like(cb.lower(root.get("jobRequirements")), like)
        );
    }

    public static Specification<JobPosting> provinceIs(Long provinceId) {
        if (provinceId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("province").get("provinceId"), provinceId);
    }

    public static Specification<JobPosting> workTypeIs(String workType) {
        if (workType == null || workType.isBlank()) return null;
        return (root, q, cb) -> cb.equal(root.get("workType"), workType);
    }

    public static Specification<JobPosting> salaryAtLeast(Integer minVnd) {
        if (minVnd == null) return null;
        BigDecimal min = BigDecimal.valueOf(minVnd);
        return (root, q, cb) -> cb.or(
            cb.greaterThanOrEqualTo(root.get("salaryMax"), min),
            cb.greaterThanOrEqualTo(root.get("salaryMin"), min)
        );
    }
}
