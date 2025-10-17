package com.aurionpro.serviceImplementation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.exception.NotFoundException;
import com.aurionpro.dto.request.BankDetailsRequestDto;
import com.aurionpro.dto.request.DocumentUploadRequestDto;
import com.aurionpro.dto.response.DocumentSummaryDto;
import com.aurionpro.dto.response.OrganizationOnboardingResponseDto;
import com.aurionpro.dto.response.OrganizationOnboardingStatusResponseDto;
import com.aurionpro.entity.DocumentEntity;
import com.aurionpro.entity.OrganizationBankAccountEntity;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.entity.UserEntity;
import com.aurionpro.repo.DocumentRepository;
import com.aurionpro.repo.OrganizationBankAccountRepository;
import com.aurionpro.repo.OrganizationRepository;
import com.aurionpro.repo.UserRepository;
import com.aurionpro.service.CloudinaryService;
import com.aurionpro.service.OrganizationOnboardingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
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

	@Override
	public OrganizationOnboardingStatusResponseDto getOnboardingStatus(Authentication authentication) {
		UserEntity user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = user.getOrganization();
		if (org == null) {
			throw new NotFoundException("User is not associated with any organization.");
		}

		List<DocumentEntity> docs = documentRepository.findByOrganization(org);
		OrganizationBankAccountEntity bank = bankRepository.findByOrganization(org).orElse(null);

		// Define required documents (could come from DB/config later)
		List<String> requiredDocs = List.of("PAN", "GST", "LICENSE");

		// Uploaded document types
		Set<String> uploadedDocTypes = docs.stream().map(d -> d.getFileType().toUpperCase())
				.collect(Collectors.toSet());

		// Missing documents
		List<String> missingDocs = requiredDocs.stream().filter(req -> !uploadedDocTypes.contains(req.toUpperCase()))
				.collect(Collectors.toList());

		// Document stats
		long approvedCount = docs.stream().filter(d -> "APPROVED".equalsIgnoreCase(d.getStatus())).count();
		long rejectedCount = docs.stream().filter(d -> "REJECTED".equalsIgnoreCase(d.getStatus())).count();
		long pendingCount = docs.stream().filter(d -> "PENDING".equalsIgnoreCase(d.getStatus())).count();
		int totalDocs = docs.size();

		// Document stage
		String documentStage;
		if (totalDocs == 0) {
			documentStage = "NOT_UPLOADED";
		} else if (!missingDocs.isEmpty()) {
			documentStage = "PARTIALLY_UPLOADED";
		} else if (approvedCount == totalDocs) {
			documentStage = "APPROVED";
		} else if (rejectedCount > 0) {
			documentStage = "REJECTED";
		} else if (pendingCount > 0) {
			documentStage = "UNDER_REVIEW";
		} else {
			documentStage = "UPLOADED";
		}

		// Bank stage
		String bankStage;
		String bankRejectionReason = null;
		if (bank == null) {
			bankStage = "NOT_PROVIDED";
		} else {
			switch (bank.getVerificationStatus()) {
			case "APPROVED" -> bankStage = "APPROVED";
			case "REJECTED" -> {
				bankStage = "REJECTED";
				bankRejectionReason = "Bank details verification failed."+bank.getRemarks();
			}
			case "UNDER_REVIEW" -> bankStage = "UNDER_REVIEW";
			default -> bankStage = "PROVIDED";
			}
		}

		// Document summary list
		List<DocumentSummaryDto> docDtos = docs.stream()
				.map(doc -> DocumentSummaryDto.builder().documentId(doc.getDocumentId()).documentName(doc.getFileName())
						.fileType(doc.getFileType()).status(doc.getStatus())
						.rejectionReason("REJECTED".equalsIgnoreCase(doc.getStatus()) ? doc.getRejectionReason() : null)
						.build())
				.collect(Collectors.toList());

		// Overall onboarding status
		String onboardingProgress;
		String message;

		if ("APPROVED".equals(documentStage) && "APPROVED".equals(bankStage) && "ACTIVE".equals(org.getStatus())) {
			onboardingProgress = "COMPLETED";
			message = "🎉 Organization onboarding completed successfully. You can now Access The Entire Dashboard.";
		} else if ("NOT_UPLOADED".equals(documentStage) && "NOT_PROVIDED".equals(bankStage)) {
			onboardingProgress = "NOT_STARTED";
			message = "📄 Please upload required documents and provide bank details to start verification.";
		} else if ("UNDER_REVIEW".equals(documentStage) && "NOT_PROVIDED".equals(bankStage)) {
			onboardingProgress = "NOT_STARTED";
			message = "📄 Please provide bank details to start verification.";
		} else if ("REJECTED".equals(documentStage) && "REJECTED".equals(bankStage)) {
			onboardingProgress = "FAILED";
			message = "❌ Both documents and bank details were rejected. Please re-upload and re-submit.";
		} else if ("PARTIALLY_UPLOADED".equals(documentStage)) {
			onboardingProgress = "PARTIAL";
			message = "⚠️ Some required documents are missing. Please upload: " + String.join(", ", missingDocs);
		} else if ("REJECTED".equals(documentStage)) {
			onboardingProgress = "PARTIAL";
			message = "❌ Some documents were rejected. Please re-upload the rejected ones.";
		} else if ("REJECTED".equals(bankStage)) {
			onboardingProgress = "PARTIAL";
			message = "❌ Bank details were rejected. Please update and resubmit.";
		} else if ("UNDER_REVIEW".equals(documentStage) || "UNDER_REVIEW".equals(bankStage)) {
			onboardingProgress = "IN_REVIEW";
			message = "⏳ Onboarding under review. Please wait for admin verification.";
		} else {
			onboardingProgress = "IN_PROGRESS";
			message = "📋 Onboarding in progress";
		}

		return OrganizationOnboardingStatusResponseDto.builder().organizationId(org.getOrgId())
				.organizationName(org.getOrgName()).organizationStatus(org.getStatus()).documentStage(documentStage)
				.totalDocuments(totalDocs).approvedDocuments((int) approvedCount).rejectedDocuments((int) rejectedCount)
				.pendingDocuments((int) pendingCount).missingDocuments(missingDocs).bankStage(bankStage)
				.bankRejectionReason(bankRejectionReason).bankVerifiedBy(bank != null ? bank.getVerifiedBy() : null)
				.bankVerifiedAt(bank != null ? bank.getVerifiedAt() : null).documents(docDtos)
				.onboardingProgress(onboardingProgress).message(message).build();
	}

	@Override
	public OrganizationOnboardingResponseDto reuploadRejectedDocument(Authentication authentication, Long documentId,
			MultipartFile newFile, DocumentUploadRequestDto meta) {

		// Step 1: Validate user and organization
		UserEntity user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = user.getOrganization();
		if (org == null) {
			throw new NotFoundException("User is not associated with any organization.");
		}

		// Step 2: Fetch the rejected document
		DocumentEntity existingDoc = documentRepository.findById(documentId)
				.orElseThrow(() -> new NotFoundException("Document not found."));

		if (!existingDoc.getOrganization().getOrgId().equals(org.getOrgId())) {
			throw new InvalidOperationException("This document does not belong to your organization.");
		}

		if (!"REJECTED".equalsIgnoreCase(existingDoc.getStatus())) {
			throw new InvalidOperationException("You can only re-upload documents that were rejected.");
		}

		if (newFile == null || newFile.isEmpty()) {
			throw new InvalidOperationException("No file provided for re-upload.");
		}

		// Step 3: Upload new file to Cloudinary
		String newFileUrl = cloudinaryService.uploadFile(newFile);

		// Step 4: Update document details
		existingDoc.setFileUrl(newFileUrl);
		existingDoc.setFileType(meta.getFileType());
		existingDoc.setFileName(meta.getFileName());
		existingDoc.setStatus("PENDING");
		existingDoc.setRejectionReason(null);
		existingDoc.setVerifiedAt(null);

		documentRepository.save(existingDoc);

		// Step 5: Update org status (may go back to UNDER_REVIEW)
		updateStatus(org);

		return buildResponse(org, "Document re-uploaded successfully and sent for re-verification.");
	}

	@Override
	public OrganizationOnboardingResponseDto reuploadBankDetails(Authentication authentication,
			BankDetailsRequestDto req) {
		// Step 1: Fetch org
		UserEntity user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = user.getOrganization();
		if (org == null) {
			throw new NotFoundException("User is not associated with any organization.");
		}

		// Step 2: Fetch existing bank details
		OrganizationBankAccountEntity bank = bankRepository.findByOrganization(org)
				.orElseThrow(() -> new NotFoundException("Bank details not found for this organization."));

		// Step 3: Check if it’s rejected before reupload
		if (!"REJECTED".equalsIgnoreCase(bank.getVerificationStatus())) {
			throw new InvalidOperationException("Bank details can only be re-submitted if previously rejected.");
		}

		// Step 4: Update details with new info
		bank.setAccountNumber(req.getAccountNumber());
		bank.setIfscCode(req.getIfscCode());
		bank.setBankName(req.getBankName());
		bank.setAccountHolderName(req.getAccountHolderName());
		bank.setVerificationStatus("PENDING"); // reset for re-verification
		bank.setVerifiedBy(null);
		bank.setVerifiedAt(null);

		bankRepository.save(bank);

		// Step 5: Update org’s status
		org.setBankDetailsProvided(true);
		updateStatus(org);

		return buildResponse(org, "Bank details re-submitted successfully for re-verification.");
	}

//Helper method
	private void updateStatus(OrganizationEntity org) {
		List<String> requiredDocs = List.of("PAN", "GST", "LICENSE");
		List<DocumentEntity> uploadedDocs = documentRepository.findByOrganization(org);

		Set<String> uploadedDocTypes = uploadedDocs.stream().map(d -> d.getFileType().toUpperCase())
				.collect(Collectors.toSet());

		boolean allDocsUploaded = requiredDocs.stream().allMatch(req -> uploadedDocTypes.contains(req.toUpperCase()));

		boolean hasRejectedDocs = uploadedDocs.stream().anyMatch(d -> "REJECTED".equalsIgnoreCase(d.getStatus()));

		OrganizationBankAccountEntity bank = bankRepository.findByOrganization(org).orElse(null);
		boolean hasBankRejected = bank != null && "REJECTED".equalsIgnoreCase(bank.getVerificationStatus());

		if ((hasRejectedDocs || hasBankRejected)) {
			org.setStatus("PENDING");
		} else if (allDocsUploaded && org.isBankDetailsProvided()) {
			org.setStatus("UNDER_REVIEW");
		} else {
			org.setStatus("PENDING");
		}

		organizationRepository.save(org);
	}

	private OrganizationOnboardingResponseDto buildResponse(OrganizationEntity org, String message) {
		return OrganizationOnboardingResponseDto.builder().orgId(org.getOrgId()).orgName(org.getOrgName())
				.documentUploaded(org.isDocumentUploaded()).bankDetailsProvided(org.isBankDetailsProvided())
				.status(org.getStatus()).message(message).build();
	}

}
