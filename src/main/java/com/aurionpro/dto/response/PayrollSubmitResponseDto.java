package com.aurionpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PayrollSubmitResponseDto {
    private String message;
    private Long paymentRequestId;
    private String status;
}
