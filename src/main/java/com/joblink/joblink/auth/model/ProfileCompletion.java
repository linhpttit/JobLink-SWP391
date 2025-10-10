package com.joblink.joblink.auth.model;

import lombok.Data;

import java.util.List;

/* ===== Completion result ===== */
@Data
public class ProfileCompletion {
    private int percent;                 // 0..100
    private List<String> missingFields;  // ví dụ: ["Avatar","Headline","At least 1 Skill","Experience"]
}
