package com.joblink.joblink.auth.model;

import lombok.Data;

/* ===== Language ===== */
@Data public class Language {
    private long id;
    private String name;
    private String proficiency; // e.g. A2/B1/B2/C1/C2 hoặc BASIC/FLUENT/NATIVE
}
