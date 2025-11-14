package com.example.qaassistant.controller.transfer;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private String code;
    private String message;
    private String suggestion;
    private String details;
    private LocalDateTime timestamp;

    public ErrorResponse(String code, String message, String suggestion) {
        this.code = code;
        this.message = message;
        this.suggestion = suggestion;
        this.timestamp = LocalDateTime.now();
    }

}
