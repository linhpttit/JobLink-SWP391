package com.joblink.joblink.model;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class JobSeekerProfile {
    // Khóa
    private Integer seekerId;          // PK (identity)
    private Integer userId;            // FK -> Users.user_id (UNIQUE)

    // Trường cơ bản
    private String  fullname;
    private String  gender;
    private String  location;
    private String  headline;
    private Integer experienceYears;   // có thể null
    private String  about;


    // Mở rộng (đang được DAO map)
    private String  email;
    private String  phoneNumber;
    private LocalDate dateOfBirth;     // map từ cột dob
    private String  avatarUrl;

    // Tiến độ hoàn thành
    private Integer completionPercentage;
}
