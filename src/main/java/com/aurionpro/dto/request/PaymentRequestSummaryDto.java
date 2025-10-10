package com.aurionpro.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestSummaryDto {
    private long totalRequests;
    private long totalPending;
    private long totalApproved;
    private long totalRejected;
    private long totalPaid;

    private double totalAmount;
    private double totalPaidAmount;
    private double totalPendingAmount;
}