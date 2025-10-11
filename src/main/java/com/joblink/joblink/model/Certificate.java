package com.joblink.joblink.model;

import lombok.Data;

@Data
public class Certificate {
    private Integer certificateId;
    private Integer seekerId;
    private String issuingOrganization;
    private String certificateImageUrl;
    private Integer yearOfCompletion;
}
