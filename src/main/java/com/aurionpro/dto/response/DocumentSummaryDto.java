package com.aurionpro.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentSummaryDto {
	private String documentName;
	private String fileType;
	private String status;
	private String rejectionReason;

}
