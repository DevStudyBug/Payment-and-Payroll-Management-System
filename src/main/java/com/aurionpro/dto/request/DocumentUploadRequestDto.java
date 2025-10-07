package com.aurionpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentUploadRequestDto {
    @NotBlank(message = "Document name is required.")
    private String fileName;

    @NotBlank(message = "Document type is required.")
    private String fileType;
}