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
public class OrgRegisterResponseDto {
	private Long orgId;
    private String orgName;
    private String email;
    private String status;
    private List<OrganizationDocumentDto> documents;
    private String bankVerificationStatus;
    private String bankRemarks;
   // private BankAccountDetailsDto bankAccount; // ✅ Added this field
    private String message;
	
}
