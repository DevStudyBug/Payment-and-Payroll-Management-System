package com.aurionpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentSummaryDto {
	private Long documentId;
	private String documentName;
	private String fileType;
	private String status;
	private String rejectionReason;

}
