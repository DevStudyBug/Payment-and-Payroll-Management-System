package com.aurionpro.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankOrgRegisterResponseDto {
    private Long orgId;
    private String orgName;
    private String email;
    private String status;
    private String message;
    private List<OrganizationDocumentDto> documents; 
    private String bankVerificationStatus; // PENDING / APPROVED / REJECTED
    private String bankRemarks;             // Optional remarks if rejected
}
