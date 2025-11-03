package com.joblink.joblink.employer.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.joblink.joblink.employer.application.model.ApplicationsPageVM;
import com.joblink.joblink.employer.application.model.ApplicationsPageVM.ApplicationRow;
import com.joblink.joblink.employer.application.model.ApplicationsPageVM.JobLite;
import com.joblink.joblink.employer.application.model.ApplicationsPageVM.PageMeta;
import com.joblink.joblink.employer.application.model.ApplicationsPageVM.Stats;
import com.joblink.joblink.entity.Application;
import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.repository.ApplicationRepository;
import com.joblink.joblink.repository.JobPostingRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployerApplicationService implements IEmployerApplicationService {

    private final ApplicationRepository appRepo;
    private final JobPostingRepository jobRepo;
    private final EmployerService employerService;

    @Override
    public ApplicationsPageVM getApplications(String q, String status, Long jobId, String date, int page, int size) {
        // 1) Resolve employer + their jobs
        Long employerId = safeCurrentEmployerId();
        List<JobPosting> employerJobs = jobRepo.findByEmployerIdOrderByPostedAtDesc(employerId);
        Map<Long, JobPosting> jobsById = employerJobs.stream()
                .collect(Collectors.toMap(JobPosting::getJobId, Function.identity(), (a,b)->a, LinkedHashMap::new));
        Set<Integer> employerJobIdInts = jobsById.keySet().stream().map(Long::intValue).collect(Collectors.toSet());

        // 2) Filter: derive which jobIds are allowed (employer scope, optional jobId, optional q on job title)
        Set<Integer> filteredJobIds = new HashSet<>(employerJobIdInts);
        if (jobId != null) {
            Integer j = jobId.intValue();
            if (employerJobIdInts.contains(j)) filteredJobIds.retainAll(Set.of(j));
            else filteredJobIds.clear(); // job not owned by employer → empty
        }
        if (StringUtils.hasText(q)) {
            String term = q.trim().toLowerCase();
            Set<Integer> matchedByTitle = jobsById.entrySet().stream()
                    .filter(e -> e.getValue().getTitle() != null &&
                                 e.getValue().getTitle().toLowerCase().contains(term))
                    .map(e -> e.getKey().intValue())
                    .collect(Collectors.toSet());
            filteredJobIds.retainAll(matchedByTitle);
        }

        // No jobs left after filtering → return empty page
        if (filteredJobIds.isEmpty()) {
            return emptyVM(employerJobs, page, size);
        }

        // 3) Date window
        LocalDateTime from = null, to = null;
        LocalDate today = LocalDate.now();
        if (StringUtils.hasText(date)) {
            switch (date) {
                case "today" -> {
                    from = today.atStartOfDay();
                    to = today.atTime(LocalTime.MAX);
                }
                case "week" -> {
                    from = today.minusDays(6).atStartOfDay();
                    to = today.atTime(LocalTime.MAX);
                }
                case "month" -> {
                    from = today.withDayOfMonth(1).atStartOfDay();
                    to = today.atTime(LocalTime.MAX);
                }
                default -> { /* all */ }
            }
        }

        // 4) Build JPA Specification
        Specification<Application> spec = buildSpec(filteredJobIds, status, from, to);

        // 5) Page query (ordered by appliedAt desc)
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.max(size,1), Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<Application> pg = appRepo.findAll(spec, pageable);

        // 6) Stats (counts with same base spec + status variation)
        long total = appRepo.count(spec);
        long reviewing = appRepo.count(spec.and(hasStatus("reviewing")));
        long accepted  = appRepo.count(spec.and(hasStatus("accepted")));
        long denied    = appRepo.count(spec.and(hasStatus("denied")));

        // 7) Map to VM rows
        List<ApplicationRow> rows = pg.getContent().stream()
                .map(a -> toRow(a, jobsById.get(optLong(a.getJobId())), employerJobs))
                .toList();

        // 8) Build VM
        return ApplicationsPageVM.builder()
                .jobs(employerJobs.stream()
                        .map(j -> JobLite.of(j.getJobId(), j.getTitle()))
                        .toList())
                .items(rows)
                .stats(Stats.builder()
                        .total((int) total)
                        .reviewing((int) reviewing)
                        .accepted((int) accepted)
                        .denied((int) denied)
                        .build())
                .meta(PageMeta.of(pg.getNumber(), pg.getSize(), pg.getTotalElements()))
                .build();
    }

    @Override
    public String exportCsv(String q, String status, Long jobId, String date) {
        Long employerId = safeCurrentEmployerId();
        List<JobPosting> employerJobs = jobRepo.findByEmployerIdOrderByPostedAtDesc(employerId);
        Map<Long, JobPosting> jobsById = employerJobs.stream()
                .collect(Collectors.toMap(JobPosting::getJobId, Function.identity(), (a,b)->a, LinkedHashMap::new));
        Set<Integer> employerJobIdInts = jobsById.keySet().stream().map(Long::intValue).collect(Collectors.toSet());

        // reuse same filters
        Set<Integer> filteredJobIds = new HashSet<>(employerJobIdInts);
        if (jobId != null) filteredJobIds.retainAll(Set.of(jobId.intValue()));
        if (StringUtils.hasText(q)) {
            String term = q.trim().toLowerCase();
            Set<Integer> matched = jobsById.entrySet().stream()
                    .filter(e -> e.getValue().getTitle() != null &&
                                 e.getValue().getTitle().toLowerCase().contains(term))
                    .map(e -> e.getKey().intValue()).collect(Collectors.toSet());
            filteredJobIds.retainAll(matched);
        }
        if (filteredJobIds.isEmpty()) return "id,candidateName,email,jobTitle,status,appliedAt\n";

        LocalDateTime[] range = rangeFrom(date);
        Specification<Application> spec = buildSpec(filteredJobIds, status, range[0], range[1]);
        List<Application> all = appRepo.findAll(spec, Sort.by(Sort.Direction.DESC, "appliedAt"));

        StringBuilder sb = new StringBuilder();
        sb.append("id,candidateName,email,jobTitle,status,appliedAt\n");
        for (Application a : all) {
            JobPosting j = jobsById.get(optLong(a.getJobId()));
            String line = String.join(",",
                    safe(a.getApplicationId()),
                    safe(candidateNameFrom(a)),
                    safe(""), // email unknown with current model
                    safe(j != null ? j.getTitle() : ("Job #" + a.getJobId())),
                    safe(statusLabel(a.getStatus())),
                    safe(a.getAppliedAt())
            );
            sb.append(line).append("\n");
        }
        return sb.toString();
    }


    private Long safeCurrentEmployerId() {
        try {
            return employerService.getCurrentEmployerId();
        } catch (Exception e) {
            return 1L;
        }
    }

    private Specification<Application> buildSpec(Set<Integer> jobIds, String status,
                                                 LocalDateTime from, LocalDateTime to) {
        return (root, cq, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (jobIds != null && !jobIds.isEmpty()) {
                ps.add(root.get("jobId").in(jobIds));
            } else {
                // force empty result
                ps.add(cb.disjunction());
            }
            if (StringUtils.hasText(status)) {
                ps.add(cb.equal(root.get("status"), status));
            }
            if (from != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("appliedAt"), from));
            }
            if (to != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("appliedAt"), to));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private Specification<Application> hasStatus(String st) {
        return (root, cq, cb) -> cb.equal(root.get("status"), st);
    }

    private LocalDateTime[] rangeFrom(String dateKey) {
        LocalDateTime from = null, to = null;
        if (!StringUtils.hasText(dateKey)) return new LocalDateTime[]{null, null};
        LocalDate today = LocalDate.now();
        switch (dateKey) {
            case "today" -> { from = today.atStartOfDay(); to = today.atTime(LocalTime.MAX); }
            case "week"  -> { from = today.minusDays(6).atStartOfDay(); to = today.atTime(LocalTime.MAX); }
            case "month" -> { from = today.withDayOfMonth(1).atStartOfDay(); to = today.atTime(LocalTime.MAX); }
        }
        return new LocalDateTime[]{from, to};
    }

    private static Long optLong(Integer val) { return val == null ? null : val.longValue(); }

    private ApplicationRow toRow(Application a, JobPosting job, List<JobPosting> employerJobs) {
        String jobTitle = (job != null && job.getTitle() != null) ? job.getTitle() : ("Job #" + a.getJobId());
        String company = (job != null && job.getEmployer() != null) ? String.valueOf(job.getEmployer().getCompanyName()) : "—";
        return ApplicationRow.builder()
                .id(a.getApplicationId())
                .candidateName(candidateNameFrom(a)) 
                .phone(null)                         
                .email(null)                        
                .jobTitle(jobTitle)
                .company(company)
                .appliedAt(a.getAppliedAt())
                .status(a.getStatus())
                .statusLabel(statusLabel(a.getStatus()))
                .cvName(extractFileName(a.getCvUrl()))
                .cvUrl(a.getCvUrl())
                .cvSize(null)
                .notes(a.getNote())
                .build();
    }

    private static String candidateNameFrom(Application a) {
        Integer sid = a.getSeekerId();
        return sid == null ? "Ứng viên" : ("Ứng viên #" + sid);
    }

    private static String extractFileName(String url) {
        if (!StringUtils.hasText(url)) return null;
        int slash = url.lastIndexOf('/');
        return slash >= 0 ? url.substring(slash + 1) : url;
    }

    private static String statusLabel(String code) {
        if (code == null) return "—";
        return switch (code) {
            case "submitted" -> "Đã nộp";
            case "reviewing" -> "Đang xem xét";
            case "accepted"  -> "Chấp nhận";
            case "denied"    -> "Từ chối";
            case "withdrawn" -> "Rút hồ sơ";
            default -> code;
        };
    }

    private static String safe(Object v) {
        if (v == null) return "";
        String s = String.valueOf(v);
        // basic CSV escape (wrap in quotes if contains comma/quote/newline)
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static ApplicationsPageVM emptyVM(List<JobPosting> employerJobs, int page, int size) {
        return ApplicationsPageVM.builder()
                .jobs(employerJobs.stream().map(j -> JobLite.of(j.getJobId(), j.getTitle())).toList())
                .items(List.of())
                .stats(Stats.builder().total(0).reviewing(0).accepted(0).denied(0).build())
                .meta(PageMeta.of(Math.max(page,0), Math.max(size,1), 0))
                .build();
    }
}
