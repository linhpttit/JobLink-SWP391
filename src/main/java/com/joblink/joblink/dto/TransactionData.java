package com.joblink.joblink.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionData {
    // Tên cột PHẢI TRÙNG KHỚP với tên cột trong Google Sheet
    @JsonProperty("CONTENT")
    private String CONTENT;

    @JsonProperty("PRICE")
    private BigDecimal PRICE;

//    @JsonProperty("DATE")
//    private local.date.time DATE;

    @JsonProperty("BANK")
    private String BANK;
}