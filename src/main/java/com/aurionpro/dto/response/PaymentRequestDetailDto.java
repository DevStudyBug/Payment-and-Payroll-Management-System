package com.aurionpro.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.aurionpro.constants.PaymentRequestType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PaymentRequestDetailDto {
    private Long paymentId;
    private Long orgId;
    private String orgName;
    private PaymentRequestType requestType;
    private String status;
    private Double amount;
    private String description;
    private LocalDateTime requestDate;
    private LocalDateTime approvalDate;
    private String remark;
    private List<SalaryDisbursementDto> disbursements;
}
