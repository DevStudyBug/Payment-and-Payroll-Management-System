package com.aurionpro.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PayrollActionResponseDto {
    private Long paymentId;
    private String status;
    private String message;
    private String remark;
    private Double amount;
    private LocalDateTime approvalDate;
}
