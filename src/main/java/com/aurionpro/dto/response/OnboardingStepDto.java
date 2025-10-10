package com.aurionpro.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OnboardingStepDto {
	private String priority; // HIGH / INFO
	private String action; // e.g., "Upload Missing Documents"
	private String description; // short explanation
	private String endpoint; // API path for frontend navigation
}
