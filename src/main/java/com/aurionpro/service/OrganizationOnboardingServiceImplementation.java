package com.aurionpro.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.exception.NotFoundException;
import com.aurionpro.dto.request.BankDetailsRequestDto;
import com.aurionpro.dto.request.DocumentUploadRequestDto;
import com.aurionpro.dto.response.OrganizationOnboardingResponseDto;
import com.aurionpro.entity.DocumentEntity;
import com.aurionpro.entity.OrganizationBankAccountEntity;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.entity.UserEntity;
import com.aurionpro.repo.DocumentRepository;
import com.aurionpro.repo.OrganizationBankAccountRepository;
import com.aurionpro.repo.OrganizationRepository;
import com.aurionpro.repo.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationOnboardingServiceImplementation implements OrganizationOnboardingService {

	private final OrganizationRepository organizationRepository;
	private final DocumentRepository documentRepository;
	private final OrganizationBankAccountRepository bankRepository;
	private final CloudinaryService cloudinaryService;
	private final ModelMapper modelMapper;
	private final UserRepository userRepository;

	@Override
	public OrganizationOnboardingResponseDto uploadDocument(Authentication authentication, List<MultipartFile> files,
			String metaJson) throws JsonProcessingException {

		// Step 1: Get authenticated user and organization
		UserEntity user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = user.getOrganization();
		if (org == null) {
			throw new NotFoundException("User is not associated with any organization.");
		}

		// Parse JSON safely (GlobalExceptionHandler will catch malformed JSON)
		ObjectMapper mapper = new ObjectMapper();

		if (metaJson.trim().startsWith("[")) {
			// Multiple document upload
			List<DocumentUploadRequestDto> metaList = mapper.readValue(metaJson,
					new TypeReference<List<DocumentUploadRequestDto>>() {
					});
			return uploadMultipleDocuments(org, files, metaList);
		} else {
			// Single document upload
			DocumentUploadRequestDto meta = mapper.readValue(metaJson, DocumentUploadRequestDto.class);
			MultipartFile singleFile = files.isEmpty() ? null : files.get(0);
			return uploadSingleDocument(org, singleFile, meta);
		}
	}

	/**
	 * Single document upload helper
	 */
	private OrganizationOnboardingResponseDto uploadSingleDocument(OrganizationEntity org, MultipartFile file,
			DocumentUploadRequestDto req) {

		if (file == null || file.isEmpty()) {
			throw new InvalidOperationException("No file provided for upload.");
		}

		String fileUrl = cloudinaryService.uploadFile(file);

		DocumentEntity doc = modelMapper.map(req, DocumentEntity.class);
		doc.setFileUrl(fileUrl);
		doc.setStatus("PENDING");
		doc.setOrganization(org);
		documentRepository.save(doc);

		org.setDocumentUploaded(true);
		updateStatus(org);

		return buildResponse(org, "Document uploaded successfully.");
	}

	/**
	 * Multiple document upload helper
	 */
	public OrganizationOnboardingResponseDto uploadMultipleDocuments(OrganizationEntity org, List<MultipartFile> files,
			List<DocumentUploadRequestDto> metaList) {

		if (files == null || files.isEmpty()) {
			throw new InvalidOperationException("No files provided for upload.");
		}

		if (files.size() != metaList.size()) {
			throw new InvalidOperationException("Number of files and metadata entries do not match.");
		}

		for (int i = 0; i < files.size(); i++) {
			MultipartFile file = files.get(i);
			DocumentUploadRequestDto meta = metaList.get(i);

			String fileUrl = cloudinaryService.uploadFile(file);

			DocumentEntity doc = modelMapper.map(meta, DocumentEntity.class);
			doc.setFileUrl(fileUrl);
			doc.setStatus("PENDING");
			doc.setOrganization(org);
			documentRepository.save(doc);
		}

		org.setDocumentUploaded(true);
		updateStatus(org);

		return buildResponse(org, "Multiple documents uploaded successfully.");
	}

	/// Add Bank Details for authenticated organization

	@Override
	public OrganizationOnboardingResponseDto addBankDetails(Authentication authentication, BankDetailsRequestDto req) {

		UserEntity user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = user.getOrganization();
		if (org == null) {
			throw new NotFoundException("User is not associated with any organization.");
		}

		OrganizationBankAccountEntity bank = modelMapper.map(req, OrganizationBankAccountEntity.class);
		bank.setOrganization(org);
		bank.setVerificationStatus("PENDING");
		bankRepository.save(bank);

		org.setBankDetailsProvided(true);
		updateStatus(org);

		return buildResponse(org, "Bank details submitted for admin review.");
	}

//Helper method
	private void updateStatus(OrganizationEntity org) {
		if (org.isDocumentUploaded() && org.isBankDetailsProvided()) {
			org.setStatus("UNDER_REVIEW");
		}
		organizationRepository.save(org);
	}

	private OrganizationOnboardingResponseDto buildResponse(OrganizationEntity org, String message) {
		return OrganizationOnboardingResponseDto.builder().orgId(org.getOrgId()).orgName(org.getOrgName())
				.documentUploaded(org.isDocumentUploaded()).bankDetailsProvided(org.isBankDetailsProvided())
				.status(org.getStatus()).message(message).build();
	}
}
