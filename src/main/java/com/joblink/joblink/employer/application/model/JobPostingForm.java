package com.joblink.joblink.employer.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobPostingForm {

    @NotBlank(message = "Trạng thái không được để trống")
    private String status = "ACTIVE";

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Vị trí không được để trống")
    @Size(max = 150)
    private String position;

    @Size(max = 255)
    private String streetAddress;

    @NotBlank(message = "Kinh nghiệm không được để trống")
    @Size(max = 150)
    private String yearExperience;

    @NotNull @Min(1)
    private Integer hiringNumber;

    @NotNull
    @FutureOrPresent(message = "Hạn nộp phải ở hiện tại hoặc tương lai")
    private LocalDate submissionDeadline;

    @NotBlank @Size(max = 255)
    private String workType; // e.g. FULL_TIME / PART_TIME ...

    @PositiveOrZero
    private BigDecimal salaryMin;

    @Positive
    private BigDecimal salaryMax;

    @NotBlank
    private String jobDescription;

    @NotBlank
    private String jobRequirements;

    @NotBlank
    private String benefits;

    @NotBlank @Size(max = 255)
    private String contactName;

    @Email @NotBlank @Size(max = 255)
    private String contactEmail;

    @NotBlank @Size(min = 9, max = 15) 
    private String contactPhone;

    @NotNull private Long employerId;
    private Long categoryId;
    @NotNull private Long skillId;
    private Long provinceId;
    private Long districtId;

}
