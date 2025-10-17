package com.aurionpro.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeOnboardingStatusDto {
    private Long employeeId;
    private String employeeName;
    private String employeeStatus;

    private String documentStage; // e.g., NOT_UPLOADED, PARTIALLY_UPLOADED, UNDER_REVIEW, APPROVED, REJECTED
    private int totalDocuments;
    private int approvedDocuments;
    private int rejectedDocuments;
    private int pendingDocuments;
    private List<String> missingDocuments;

    private String bankStage;
    private String bankRejectionReason;
   

    private String onboardingProgress; // COMPLETED, PARTIAL, FAILED, IN_PROGRESS, IN_REVIEW
    private String message;

    private List<DocumentSummaryDto> documents;
}
