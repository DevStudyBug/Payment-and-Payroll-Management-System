package com.aurionpro.dto.request;

import java.time.LocalDateTime;

import com.aurionpro.constants.PaymentRequestType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestListDto {
    private Long paymentId;
    private String organizationName;
    private PaymentRequestType requestType;
    private String status;
    private Double amount;
    private String description;
    private LocalDateTime requestDate;
    private LocalDateTime approvalDate;
}
