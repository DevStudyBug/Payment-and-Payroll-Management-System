package com.aurionpro.dto.response;

import java.time.LocalDateTime;

import com.aurionpro.constants.PaymentRequestType;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class VendorPaymentRequestResponseDto {
	private Long paymentId;
    private String organizationName;
    private String vendorName;
    private Double amount;
    private String description;
    private PaymentRequestType requestType;
    private String status;
    private String remark;
    private LocalDateTime requestDate;
    private LocalDateTime approvalDate;
    private String paymentRefNo;
}
