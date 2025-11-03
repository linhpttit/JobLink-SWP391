package com.joblink.joblink.employer.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.joblink.joblink.employer.application.model.EmployerDashboardVM;
import com.joblink.joblink.employer.application.model.EmployerDashboardVM.Charts;
import com.joblink.joblink.employer.application.model.EmployerDashboardVM.DeadlineRow;
import com.joblink.joblink.employer.application.model.EmployerDashboardVM.JobLite;
import com.joblink.joblink.employer.application.model.EmployerDashboardVM.JobRow;
import com.joblink.joblink.employer.application.model.EmployerDashboardVM.Kpi;
import com.joblink.joblink.employer.application.model.EmployerDashboardVM.RecentApplicantRow;
import com.joblink.joblink.entity.Application;
import com.joblink.joblink.entity.JobPosting;
import com.joblink.joblink.repository.ApplicationRepository;
import com.joblink.joblink.repository.JobPostingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployerReportService implements IEmployerReportService {

    private final ApplicationRepository appRepo;
    private final JobPostingRepository jobRepo;
    private final IEmployerService employerService;

    @Override
    public EmployerDashboardVM getDashboard(String range, LocalDate from, LocalDate to, Long jobId) {
        // 0) Normalize range
        LocalDate today = LocalDate.now();
        if (range == null) range = "30d";
        if (!"custom".equals(range)) {
            int days = switch (range) { case "7d" -> 7; case "90d" -> 90; default -> 30; };
            to = today;
            from = today.minusDays(days - 1);
        } else {
            if (from == null || to == null) { from = today.minusDays(29); to = today; }
        }
        LocalDateTime fromDT = from.atStartOfDay();
        LocalDateTime toDT   = to.atTime(LocalTime.MAX);

        // 1) Resolve employer & jobs
        Long employerId = employerService.getCurrentEmployerId();
        List<JobPosting> allJobs = jobRepo.findByEmployerIdOrderByPostedAtDesc(employerId);

        // Optional jobId filter (must belong to employer)
        if (jobId != null) {
            boolean owned = allJobs.stream().anyMatch(j -> Objects.equals(j.getJobId(), jobId));
            if (!owned) {
                // No access ⇒ return empty with just dropdown
                return EmployerDashboardVM.builder()
                        .range(range).from(from).to(to).jobId(jobId)
                        .jobs(toLite(allJobs))
                        .kpi(emptyKpi())
                        .topJobs(List.of())
                        .deadlines(List.of())
                        .recentApplicants(List.of())
                        .chart(emptyCharts())
                        .build();
            }
            allJobs = allJobs.stream().filter(j -> Objects.equals(j.getJobId(), jobId)).toList();
        }

        Map<Long, JobPosting> jobById = allJobs.stream()
                .collect(Collectors.toMap(JobPosting::getJobId, Function.identity(), (a,b)->a, LinkedHashMap::new));
        Set<Integer> jobIdsInt = jobById.keySet().stream().map(Long::intValue).collect(Collectors.toCollection(LinkedHashSet::new));

        // If no jobs => empty dashboard
        if (jobIdsInt.isEmpty()) {
            return EmployerDashboardVM.builder()
                    .range(range).from(from).to(to).jobId(jobId)
                    .jobs(List.of())
                    .kpi(emptyKpi())
                    .topJobs(List.of())
                    .deadlines(List.of())
                    .recentApplicants(List.of())
                    .chart(emptyCharts())
                    .build();
        }

        // 2) Load applications in range (for KPIs, tables, charts)
        Sort byAppliedDesc = Sort.by(Sort.Direction.DESC, "appliedAt");
        List<Application> appsInRange = appRepo.findByJobIdInAndAppliedAtBetween(jobIdsInt, fromDT, toDT, byAppliedDesc);

        // 3) KPIs
        int liveJobs = (int) allJobs.stream()
                .filter(j -> "ACTIVE".equalsIgnoreCase(safe(j.getStatus()))
                           && (j.getSubmissionDeadline() == null || !j.getSubmissionDeadline().isBefore(today)))
                .count();

        int totalApps = appsInRange.size();

        // New apps = last 7 days (bounded by the selected range)
        LocalDateTime last7From = to.minusDays(6).atStartOfDay();
        LocalDateTime boundedLast7From = last7From.isBefore(fromDT) ? fromDT : last7From;
        int newApps = (int) appsInRange.stream()
                .filter(a -> !a.getAppliedAt().isBefore(boundedLast7From))
                .count();

        // Growth vs previous equal period
        long daysSpan = ChronoUnit.DAYS.between(from, to) + 1;
        LocalDate prevTo   = from.minusDays(1);
        LocalDate prevFrom = prevTo.minusDays(daysSpan - 1);
        long prevApps = appRepo.countByJobIdInAndAppliedAtBetween(
                jobIdsInt, prevFrom.atStartOfDay(), prevTo.atTime(LocalTime.MAX)
        );
        String growthText = growth(totalApps, prevApps);

        int avgTimeToFill = computeAvgTimeToFillDays(allJobs, appsInRange);

        Kpi kpi = Kpi.builder()
                .liveJobs(liveJobs)
                .totalApps(totalApps)
                .newApps(newApps)
                .appsGrowthText(growthText)
                .avgTimeToFill(avgTimeToFill)
                .build();

        // 4) Top jobs (by applies in range)
        Map<Long, Long> appliesByJob = appsInRange.stream()
                .collect(Collectors.groupingBy(a -> a.getJobId().longValue(), Collectors.counting()));
        List<JobRow> topJobs = appliesByJob.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    JobPosting j = jobById.get(e.getKey());
                    long applies = e.getValue();
                    long views   = 0; // Not available in your model; set 0 or add a 'views' column later
                    String cvr = (views <= 0) ? "—" : String.format(Locale.US, "%.1f%%", (applies * 100.0 / views));
                    return JobRow.builder()
                            .title(j != null ? j.getTitle() : ("Job #" + e.getKey()))
                            .views((int) views)
                            .applies((int) applies)
                            .conversionRate(cvr)
                            .build();
                })
                .toList();

        // 5) Deadlines (next 14 days)
        List<DeadlineRow> deadlines = jobRepo
                .findByEmployerIdAndSubmissionDeadlineBetweenOrderBySubmissionDeadlineAsc(
                        employerId, today, today.plusDays(14)
                ).stream()
                .map(j -> DeadlineRow.builder()
                        .title(j.getTitle())
                        .deadline(j.getSubmissionDeadline())
                        .daysLeft(daysBetween(today, j.getSubmissionDeadline()))
                        .build())
                .toList();

        // 6) Recent applicants (latest 6)
        List<RecentApplicantRow> recentApplicants = appsInRange.stream()
                .sorted(Comparator.comparing(Application::getAppliedAt).reversed())
                .limit(6)
                .map(a -> {
                    JobPosting j = jobById.get(a.getJobId().longValue());
                    return RecentApplicantRow.builder()
                            .candidateName("Ứng viên #" + a.getSeekerId()) // enhance when you have Seeker entity
                            .jobTitle(j != null ? j.getTitle() : ("Job #" + a.getJobId()))
                            .appliedAt(a.getAppliedAt())
                            .status(statusLabel(a.getStatus()))
                            .build();
                })
                .toList();

        // 7) Charts
        Charts charts = Charts.builder()
                .appsOverTime(buildAppsOverTime(from, to, appsInRange))
                .statusByJob(buildStatusByJob(allJobs, appsInRange))
                .sources(buildSources(appsInRange))
                .sparks(buildSparks(appsInRange, kpi))
                .build();

        // 8) Return VM
        return EmployerDashboardVM.builder()
                .range(range).from(from).to(to).jobId(jobId)
                .jobs(toLite(jobById.values()))
                .kpi(kpi)
                .topJobs(topJobs)
                .deadlines(deadlines)
                .recentApplicants(recentApplicants)
                .chart(charts)
                .build();
    }

    // ---------- helpers ----------

    private static List<JobLite> toLite(Collection<JobPosting> jobs) {
        return jobs.stream().map(j -> JobLite.builder()
                .jobId(j.getJobId())
                .title(j.getTitle())
                .build()).toList();
    }

    private static Kpi emptyKpi() {
        return Kpi.builder().liveJobs(0).totalApps(0).newApps(0).appsGrowthText("0%").avgTimeToFill(0).build();
    }

    private static Charts emptyCharts() {
        return Charts.builder()
                .appsOverTime(Charts.Series.builder().labels(List.of()).values(List.of()).build())
                .statusByJob(Charts.StatusByJob.builder().labels(List.of()).series(List.of()).build())
                .sources(Charts.Sources.builder().labels(List.of()).values(List.of()).build())
                .sparks(Charts.Sparks.builder()
                        .live(List.of()).apps(List.of()).newApps(List.of()).time(List.of()).build())
                .build();
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static long daysBetween(LocalDate a, LocalDate b) {
        if (a == null || b == null) return 0;
        return ChronoUnit.DAYS.between(a, b);
    }

    private static String growth(long cur, long prev) {
        if (prev <= 0 && cur > 0) return "+100%";
        if (prev == 0) return "0%";
        double pct = ((double) cur - prev) / prev * 100.0;
        return String.format(Locale.US, "%+.0f%%", pct);
    }

    private int computeAvgTimeToFillDays(List<JobPosting> jobs, List<Application> apps) {
        // Approx: first ACCEPTED per job, days from postedAt -> accepted.appliedAt
        Map<Long, LocalDateTime> firstAcceptedByJob = apps.stream()
                .filter(a -> "accepted".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.groupingBy(a -> a.getJobId().longValue(),
                        Collectors.mapping(Application::getAppliedAt,
                                Collectors.collectingAndThen(Collectors.minBy(LocalDateTime::compareTo),
                                        opt -> opt.orElse(null)))));

        List<Long> diffs = new ArrayList<>();
        for (JobPosting j : jobs) {
            LocalDateTime acceptedAt = firstAcceptedByJob.get(j.getJobId());
            if (acceptedAt != null && j.getPostedAt() != null) {
                diffs.add(ChronoUnit.DAYS.between(j.getPostedAt().toLocalDate(), acceptedAt.toLocalDate()));
            }
        }
        if (diffs.isEmpty()) return 0;
        return (int) Math.round(diffs.stream().mapToLong(Long::longValue).average().orElse(0));
    }

    private Charts.Series buildAppsOverTime(LocalDate from, LocalDate to, List<Application> apps) {
        Map<LocalDate, Long> byDay = apps.stream()
                .collect(Collectors.groupingBy(a -> a.getAppliedAt().toLocalDate(), Collectors.counting()));
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            labels.add(d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")));
            values.add(byDay.getOrDefault(d, 0L).intValue());
        }
        return Charts.Series.builder().labels(labels).values(values).build();
    }

    private Charts.StatusByJob buildStatusByJob(List<JobPosting> jobs, List<Application> apps) {
        // Reduce the noise: pick top 6 jobs by applications in this range
        Map<Long, Long> countByJob = apps.stream()
                .collect(Collectors.groupingBy(a -> a.getJobId().longValue(), Collectors.counting()));
        List<Long> topJobIds = countByJob.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(6).map(Map.Entry::getKey).toList();

        Map<Long, Map<String, Long>> byJobAndStatus = apps.stream()
                .filter(a -> topJobIds.contains(a.getJobId().longValue()))
                .collect(Collectors.groupingBy(a -> a.getJobId().longValue(),
                        Collectors.groupingBy(a -> safe(a.getStatus()), Collectors.counting())));

        // We map your statuses to dashboard buckets:
        // submitted -> Applied, reviewing -> Interview, accepted -> Hired, denied -> (we'll map to Offer as 0), viewed -> 0
        List<String> labels = topJobIds.stream()
                .map(id -> jobs.stream().filter(j -> Objects.equals(j.getJobId(), id)).findFirst()
                        .map(JobPosting::getTitle).orElse("Job #" + id))
                .toList();

        List<Integer> viewed    = new ArrayList<>();
        List<Integer> applied   = new ArrayList<>();
        List<Integer> interview = new ArrayList<>();
        List<Integer> offer     = new ArrayList<>();
        List<Integer> hired     = new ArrayList<>();

        for (Long id : topJobIds) {
            Map<String, Long> m = byJobAndStatus.getOrDefault(id, Map.of());
            viewed.add(0); // not tracked
            applied.add(m.getOrDefault("submitted", 0L).intValue());
            interview.add(m.getOrDefault("reviewing", 0L).intValue());
            offer.add(0); // no "offer" state in current model
            hired.add(m.getOrDefault("accepted", 0L).intValue());
        }

        return Charts.StatusByJob.builder()
                .labels(labels)
                .series(List.of(viewed, applied, interview, offer, hired))
                .build();
    }

    private Charts.Sources buildSources(List<Application> apps) {
        // No explicit source in your model; use a simple proxy:
        // - "CV Upload" if cvUrl present
        // - "No CV" otherwise
        long withCv = apps.stream().filter(a -> StringUtils.hasText(a.getCvUrl())).count();
        long noCv   = Math.max(0, apps.size() - withCv);
        return Charts.Sources.builder()
                .labels(List.of("CV Upload", "No CV"))
                .values(List.of((int) withCv, (int) noCv))
                .build();
    }

    private Charts.Sparks buildSparks(List<Application> apps, Kpi kpi) {
        // Lightweight sparkline: last 7 days apps counts
        LocalDate today = LocalDate.now();
        List<Integer> last7 = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            int cnt = (int) apps.stream().filter(a -> a.getAppliedAt().toLocalDate().equals(d)).count();
            last7.add(cnt);
        }
        // Repurpose for the 4 spark slots
        return Charts.Sparks.builder()
                .live(last7)   
                .apps(last7)
                .newApps(last7)
                .time(last7)
                .build();
    }

    private static String statusLabel(String code) {
        if (code == null) return "—";
        return switch (code) {
            case "submitted" -> "Applied";
            case "reviewing" -> "Interview";
            case "accepted"  -> "Hired";
            case "denied"    -> "Denied";
            case "withdrawn" -> "Withdrawn";
            default -> code;
        };
    }
}
