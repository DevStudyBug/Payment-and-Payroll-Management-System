package com.aurionpro.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizationOnboardingStatusResponseDto {

	private Long organizationId;
	private String organizationName;
	private String organizationStatus; // PENDING / UNDER_REVIEW / ACTIVE / REJECTED

	// Document status tracking
	private String documentStage; // NOT_UPLOADED / UPLOADED / UNDER_REVIEW / APPROVED / REJECTED
	private int totalDocuments;
	private int approvedDocuments;
	private int rejectedDocuments;
	private int pendingDocuments;

	private List<String> missingDocuments;
	// Bank status tracking
	private String bankStage; // NOT_PROVIDED / PROVIDED / UNDER_REVIEW / APPROVED / REJECTED
	private String bankRejectionReason;
	private String bankVerifiedBy;
	private LocalDateTime bankVerifiedAt;

	private List<DocumentSummaryDto> documents;

	private String onboardingProgress; // short overall label (e.g., “IN_PROGRESS”, “COMPLETED”)
	private String message; // detailed message for dashboard
}
