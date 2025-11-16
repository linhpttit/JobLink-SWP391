
package com.joblink.joblink.exception;

import com.joblink.joblink.dto.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ChatResponse> handleRestClientException(RestClientException e) {
        log.error("REST API call failed", e);
        return ResponseEntity.ok(ChatResponse.builder()
                .message("Không thể kết nối đến dịch vụ AI. Vui lòng thử lại sau!")
                .type("error")
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ChatResponse> handleGeneralException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity.ok(ChatResponse.builder()
                .message("Đã xảy ra lỗi không mong muốn. Vui lòng thử lại!")
                .type("error")
                .build());
    }
}