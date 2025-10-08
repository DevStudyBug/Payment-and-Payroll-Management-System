package com.aurionpro.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dto.request.BankDetailsRequestDto;
import com.aurionpro.dto.request.DocumentUploadRequestDto;
import com.aurionpro.dto.response.OrganizationOnboardingResponseDto;
import com.aurionpro.dto.response.OrganizationOnboardingStatusResponseDto;
import com.aurionpro.entity.OrganizationEntity;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface OrganizationOnboardingService {
	public OrganizationOnboardingResponseDto uploadDocument(Authentication authentication, List<MultipartFile> files,
			String metaJson) throws JsonProcessingException;

	public OrganizationOnboardingResponseDto addBankDetails(Authentication authentication, BankDetailsRequestDto req);

	public OrganizationOnboardingResponseDto uploadMultipleDocuments(OrganizationEntity org, List<MultipartFile> files,
			List<DocumentUploadRequestDto> metaList);

	public OrganizationOnboardingStatusResponseDto getOnboardingStatus(Authentication authentication);

	public OrganizationOnboardingResponseDto reuploadRejectedDocument(Authentication authentication, Long documentId,
			MultipartFile newFile, DocumentUploadRequestDto meta);

	public OrganizationOnboardingResponseDto reuploadBankDetails(Authentication authentication,
			BankDetailsRequestDto req);

}
