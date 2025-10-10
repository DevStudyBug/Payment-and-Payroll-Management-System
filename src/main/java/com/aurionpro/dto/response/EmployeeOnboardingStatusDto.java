package com.aurionpro.dto.response;

import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeOnboardingStatusDto {
	private Long employeeId;
	private String name;
	private String status;
	private int overallProgress;
	private String statusMessage;
	private boolean isComplete;

	// new fields
	private int approvedDocuments;
	private int rejectedDocuments;
	private int pendingDocuments;

	private List<String> missingDocuments;
	private boolean bankDetailsSubmitted;
	private String bankStatus;

	private List<OnboardingStepDto> nextSteps;
}
