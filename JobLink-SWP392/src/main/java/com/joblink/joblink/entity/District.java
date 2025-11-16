package com.joblink.joblink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Districts", schema = "dbo")
public class District {
    public District(Long districtId, String districtName, Long provinceId) {
        this.districtId = districtId;
        this.districtName = districtName;
        Province province = new Province();
        province.setProvinceId(provinceId);
        this.province = province;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "district_id")
    private Long districtId;

    @Column(name = "district_name", nullable = false, length = 100)
    private String districtName;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Quan hệ N-1: Mỗi huyện thuộc về 1 tỉnh
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;

}
