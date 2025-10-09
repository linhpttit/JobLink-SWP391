package com.joblink.joblink.auth.model;

import lombok.Data;

@Data public class SkillCreate {
    private String name;
    private Integer level;
    private Integer years;
    private String note;
}
