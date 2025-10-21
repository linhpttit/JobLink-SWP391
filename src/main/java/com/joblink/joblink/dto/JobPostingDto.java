package com.joblink.joblink.dto;

import com.joblink.joblink.entity.Category;
import com.joblink.joblink.entity.Skill;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingDto {
    private Long jobId;
    private String title;
    private Long skillId;
    private String yearExperience;
    private Integer hiringNumber;
    private LocalDate submissionDeadline;
    private Long provinceId;
    private Long districtId;
    private String streetAddress;
    private String workType;
    private String position;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;

    private String jobDescription;
    private String jobRequirements;
    private String benefits;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
}
