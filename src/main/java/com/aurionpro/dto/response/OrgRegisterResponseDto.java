package com.aurionpro.dto.response;

import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgRegisterResponseDto {
    private Long orgId;
    private String orgName;
    private String email;
    private String status;
    private String message;
    private List<OrganizationDocumentDto> documents;

    // 🔹 Add bank details
    private String accountHolderName;
    private String accountNumber;
    private String ifscCode;
    private String bankName;

    private String bankVerificationStatus;
    private String bankRemarks;
}
