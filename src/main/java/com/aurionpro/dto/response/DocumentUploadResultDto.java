package com.aurionpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentUploadResultDto {
    private String documentType;
    private String fileName;
    private String fileUrl;
    private boolean success;
    private String message;
}
