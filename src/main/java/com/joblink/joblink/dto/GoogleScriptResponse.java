package com.joblink.joblink.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleScriptResponse {
    private boolean success;
    private List<TransactionData> data;
    private String message;
}