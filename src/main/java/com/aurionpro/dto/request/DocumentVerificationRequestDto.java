package com.aurionpro.dto.request;

import lombok.Data;

@Data
public class DocumentVerificationRequestDto {
	private boolean approved;
	private String rejectionReason;
}
