package com.joblink.joblink.dto;

import lombok.Data;

/**
 * DTO để truyền dữ liệu Employer Profile giữa Controller và View
 * Nếu bạn đã có file này rồi thì chỉ cần đảm bảo có đủ các field dưới đây
 */
@Data
public class EmployerProfileDto {

    private String companyName;
    private String address;        // Mapping với location trong DB
    private String phoneNumber;
    private String email;
    private String description;
    private String urlAvt;
    private String createdAt;      // Dạng String đã format "dd/MM/yyyy"

    // Constructor mặc định
    public EmployerProfileDto() {
    }

    // Constructor đầy đủ (optional)
    public EmployerProfileDto(String companyName, String address, String phoneNumber,
                              String email, String description, String urlAvt, String createdAt) {
        this.companyName = companyName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.description = description;
        this.urlAvt = urlAvt;
        this.createdAt = createdAt;
    }
}