package com.aurionpro.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizationOnboardingResponseDto {
    private Long orgId;
    private String orgName;
    private boolean documentUploaded;
    private boolean bankDetailsProvided;
    private String status;
    private String message;
}
