package com.aurionpro.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentUploadRequestDto {
    @NotBlank(message = "Document name is required.")
    private String fileName;

    @NotBlank(message = "Document type is required.")
    private String fileType;
}