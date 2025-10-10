package com.aurionpro.service;

import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dto.request.EmployeeBankDetailsRequestDto;
import com.aurionpro.dto.response.DocumentUploadResponseDto;
import com.aurionpro.dto.response.EmployeeBankDetailsResponseDto;
import com.aurionpro.dto.response.EmployeeOnboardingStatusDto;

public interface EmployeeService {
	public DocumentUploadResponseDto uploadDocument(String username, MultipartFile file, String docType);

	public DocumentUploadResponseDto uploadDocuments(String username, MultipartFile[] files, String[] docTypes);

	public EmployeeBankDetailsResponseDto addBankDetails(String username, EmployeeBankDetailsRequestDto request);

	public EmployeeOnboardingStatusDto getOnboardingStatus(String username);

	public DocumentUploadResponseDto reuploadRejectedDocument(String username, Long documentId, MultipartFile file);

	public EmployeeBankDetailsResponseDto reuploadRejectedBankDetails(String username,
			EmployeeBankDetailsRequestDto request);
}
