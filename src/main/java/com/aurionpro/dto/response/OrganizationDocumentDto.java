package com.aurionpro.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizationDocumentDto {

	private Long docId;           // ✅ MUST HAVE
	 private String docName;        // ✅ MUST HAVE
	 private String fileName;
	 private String fileUrl;
	 private String fileType;
	 private String status;
	 private LocalDateTime uploadedAt;
	 private LocalDateTime verifiedAt;
}
