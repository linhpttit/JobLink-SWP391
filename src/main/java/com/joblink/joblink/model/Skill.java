package com.joblink.joblink.model;

import lombok.Data;

@Data
public class Skill {
    private Integer skillId;
    private Integer seekerId;
    private String skillName;
    private Integer yearsOfExperience;
    private String description;
}
