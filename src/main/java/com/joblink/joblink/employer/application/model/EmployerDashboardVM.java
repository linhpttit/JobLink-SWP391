package com.joblink.joblink.employer.application.model;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class EmployerDashboardVM {
    private String range; private LocalDate from; private LocalDate to; private Long jobId;

    private Kpi kpi;
    private List<JobLite> jobs;          
    private List<JobRow> topJobs;
    private List<DeadlineRow> deadlines;
    private List<RecentApplicantRow> recentApplicants;

    private Charts chart;

    /* ===== Inner view models ===== */
    @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class Kpi {
        private int liveJobs;
        private int totalApps;
        private int newApps;
        private String appsGrowthText; 
        private int avgTimeToFill;  
    }

    @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class JobLite {
        private Long jobId;
        private String title;
    }

    @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class JobRow {
        private String title;
        private int views;
        private int applies;
        private String conversionRate;
    }

    @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class DeadlineRow {
        private String title;
        private LocalDate deadline;
        private long daysLeft;
    }

    @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class RecentApplicantRow {
        private String candidateName;
        private String jobTitle;
        private LocalDateTime appliedAt;
        private String status; 
    }

    @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class Charts {
        private Series appsOverTime;
        private StatusByJob statusByJob;
        private Sources sources;
        private Sparks sparks;

        @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
        public static class Series {
            private List<String> labels;
            private List<Integer> values;
        }

        @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
        public static class StatusByJob {
            private List<String> labels;       
            private List<List<Integer>> series; 
        }

        @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
        public static class Sources {
            private List<String> labels;
            private List<Integer> values;
        }

        @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
        public static class Sparks {
            private List<Integer> live;
            private List<Integer> apps;
            private List<Integer> newApps;
            private List<Integer> time;
        }
    }
}
