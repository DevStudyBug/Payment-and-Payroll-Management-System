package com.aurionpro.dto.request;

import lombok.Data;

@Data
public class BankVerificationRequestDto {
    private boolean approved;
    private String rejectionReason;
}
