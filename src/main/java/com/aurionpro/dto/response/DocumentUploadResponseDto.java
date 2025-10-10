package com.aurionpro.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentUploadResponseDto {
    private Long employeeId;
    private Long documentId; // For single document reupload
    private String currentStatus;
    private List<DocumentUploadResultDto> uploadedDocuments;
    private List<String> failedDocuments;
    private String overallResult; // SUCCESS, PARTIAL, FAILED
    private String message;
}