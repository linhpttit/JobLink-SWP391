package com.joblink.joblink.employer.application.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ApplicationsPageVM {

    private List<JobLite> jobs;           // for the filter
    private List<ApplicationRow> items;   // table rows
    private Stats stats;                  // counters
    private PageMeta meta;                // pagination

    /* ===== Sub VMs ===== */
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class JobLite {
        private Long jobId;
        private String title;
        public static JobLite of(Long id, String t){ return new JobLite(id,t); }
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ApplicationRow {
        private Long id;
        private String candidateName;
        private String phone;
        private String email;
        private String jobTitle;
        private String company;
        private LocalDateTime appliedAt;
        private String status;      
        private String statusLabel; 
        private String cvName;
        private String cvUrl;
        private String cvSize;
        private String notes;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Stats {
        private int total;
        private int reviewing;
        private int accepted;
        private int denied;
    }

    @Getter @Setter @NoArgsConstructor
    public static class PageMeta {
        private int page;
        private int size;
        private long total;
        private int totalPages;
        private long from;
        private long to;

        public static PageMeta of(int page, int size, long total){
            PageMeta m = new PageMeta();
            m.page = Math.max(0, page);
            m.size = Math.max(1, size);
            m.total = Math.max(0, total);
            m.totalPages = (int) Math.ceil((double) total / m.size);
            m.from = (long) m.page * m.size + (total == 0 ? 0 : 1);
            m.to = Math.min((long) (m.page + 1) * m.size, total);
            return m;
        }
    }
}
