package com.aurionpro.serviceImplementation;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.exception.NotFoundException;
import com.aurionpro.constants.RequiredDocuments;
import com.aurionpro.dto.request.EmployeeBankDetailsRequestDto;
import com.aurionpro.dto.response.DocumentUploadResponseDto;
import com.aurionpro.dto.response.DocumentUploadResultDto;
import com.aurionpro.dto.response.EmployeeBankDetailsResponseDto;
import com.aurionpro.dto.response.EmployeeOnboardingStatusDto;
import com.aurionpro.dto.response.OnboardingStepDto;
import com.aurionpro.entity.DocumentEntity;
import com.aurionpro.entity.EmployeeBankDetailsEntity;
import com.aurionpro.entity.EmployeeEntity;
import com.aurionpro.entity.UserEntity;
import com.aurionpro.repo.DocumentRepository;
import com.aurionpro.repo.EmployeeBankDetailsRepository;
import com.aurionpro.repo.EmployeeRepository;
import com.aurionpro.repo.UserRepository;
import com.aurionpro.service.EmployeeService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImplementation implements EmployeeService {

	private final UserRepository userRepository;
	private final EmployeeRepository employeeRepository;
	private final DocumentRepository documentRepository;
	private final Cloudinary cloudinary;
	private final EmployeeBankDetailsRepository employeeBankDetailsRepository;
	private final ModelMapper modelMapper;

	@Override
	public DocumentUploadResponseDto uploadDocument(String username, MultipartFile file, String docType) {
		return uploadDocuments(username, new MultipartFile[] { file }, new String[] { docType });
	}

	@Override
	public DocumentUploadResponseDto uploadDocuments(String username, MultipartFile[] files, String[] docTypes) {
		UserEntity user = userRepository.findByUsername(username)
				.orElseThrow(() -> new NotFoundException("User not found"));
		EmployeeEntity emp = user.getEmployee();

		if (emp == null)
			throw new InvalidOperationException("User is not an employee.");
		if (files == null || files.length == 0)
			throw new InvalidOperationException("No files uploaded.");
		if (docTypes == null || docTypes.length == 0)
			throw new InvalidOperationException("No document types provided.");
		if (files.length != docTypes.length)
			throw new InvalidOperationException("Files and document types count mismatch.");

		List<DocumentUploadResultDto> uploadedDocs = new ArrayList<>();
		List<String> failedUploads = new ArrayList<>();

		for (int i = 0; i < files.length; i++) {
			MultipartFile file = files[i];
			String docType = docTypes[i];

			try {
				if (file == null || file.isEmpty()) {
					failedUploads.add(docType + ": File is empty");
					uploadedDocs.add(DocumentUploadResultDto.builder().documentType(docType).fileName(null)
							.fileUrl(null).success(false).message("File is empty").build());
					continue;
				}

				if (!RequiredDocuments.REQUIRED_DOC_TYPES.contains(docType)) {
					failedUploads.add(docType + ": Invalid document type");
					uploadedDocs.add(
							DocumentUploadResultDto.builder().documentType(docType).fileName(file.getOriginalFilename())
									.fileUrl(null).success(false).message("Invalid document type").build());
					continue;
				}

				// Upload to Cloudinary
				Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder",
						"employee_documents/" + emp.getEmployeeId(), "resource_type", "auto"));

				String url = (String) uploadResult.get("secure_url");

				// Save or update document entity
				DocumentEntity doc = documentRepository.findByEmployeeAndFileType(emp, docType)
						.orElse(new DocumentEntity());
				doc.setEmployee(emp);
				doc.setOrganization(null);
				doc.setFileName(file.getOriginalFilename());
				doc.setFileUrl(url);
				doc.setFileType(docType);
				doc.setStatus("PENDING");
				doc.setUploadedAt(LocalDateTime.now());
				doc.setRejectionReason(null);
				documentRepository.save(doc);

				uploadedDocs.add(
						DocumentUploadResultDto.builder().documentType(docType).fileName(file.getOriginalFilename())
								.fileUrl(url).success(true).message("Uploaded successfully").build());

			} catch (IOException e) {
				failedUploads.add(docType + ": Cloudinary upload failed - " + e.getMessage());
				uploadedDocs.add(
						DocumentUploadResultDto.builder().documentType(docType).fileName(file.getOriginalFilename())
								.fileUrl(null).success(false).message("Upload failed: " + e.getMessage()).build());
			}
		}

		// Update employee status after upload
		updateEmployeeStatus(emp);

		// Determine overall result
		String overallResult;
		if (failedUploads.isEmpty()) {
			overallResult = "SUCCESS";
		} else if (uploadedDocs.stream().anyMatch(DocumentUploadResultDto::isSuccess)) {
			overallResult = "PARTIAL";
		} else {
			overallResult = "FAILED";
		}

		return DocumentUploadResponseDto.builder().employeeId(emp.getEmployeeId()).currentStatus(emp.getStatus())
				.uploadedDocuments(uploadedDocs).failedDocuments(failedUploads).overallResult(overallResult)
				.message(failedUploads.isEmpty() ? "All documents uploaded successfully."
						: "Some documents failed to upload. Please check details.")
				.build();
	}

	@Override
	public EmployeeBankDetailsResponseDto addBankDetails(String username, EmployeeBankDetailsRequestDto request) {
		UserEntity user = userRepository.findByUsername(username)
				.orElseThrow(() -> new NotFoundException("User not found"));
		EmployeeEntity emp = user.getEmployee();

		if (emp == null) {
			throw new InvalidOperationException("User is not an employee.");
		}

		// Check if bank details already exist
		if (employeeBankDetailsRepository.existsByEmployee(emp)) {
			throw new InvalidOperationException("Bank details already exist. Use update endpoint instead.");
		}

		// Validate account holder name matches employee name
		String fullName = (emp.getFirstName() + " " + emp.getLastName()).trim();
		if (!request.getAccountHolderName().trim().equalsIgnoreCase(fullName)) {
			throw new InvalidOperationException("Account holder name must match your registered name: " + fullName);
		}

		EmployeeBankDetailsEntity bankDetails = modelMapper.map(request, EmployeeBankDetailsEntity.class);
		bankDetails.setEmployee(emp);
		bankDetails.setIfscCode(request.getIfscCode().toUpperCase());
		bankDetails.setAccountType(request.getAccountType().toUpperCase());
		bankDetails.setVerificationStatus("PENDING");

		employeeBankDetailsRepository.save(bankDetails);

		// Update employee reference
		emp.setBankDetails(bankDetails);

		// Update employee status based on documents and bank details
		updateEmployeeStatus(emp);

		return EmployeeBankDetailsResponseDto.builder().employeeId(emp.getEmployeeId())
				.accountHolderName(bankDetails.getAccountHolderName()).bankName(bankDetails.getBankName())
				.verificationStatus(emp.getStatus())
				.message("Bank details added successfully. Status: " + bankDetails.getVerificationStatus()).build();
	}

	@Override
	public EmployeeOnboardingStatusDto getOnboardingStatus(String username) {
		UserEntity user = userRepository.findByUsername(username)
				.orElseThrow(() -> new NotFoundException("User not found"));
		EmployeeEntity emp = user.getEmployee();
		if (emp == null)
			throw new InvalidOperationException("User is not an employee.");

		Long employeeId = emp.getEmployeeId();
		String fullName = emp.getFirstName() + " " + emp.getLastName();
		String empStatus = emp.getStatus();

		// DOCUMENT STATS
		List<DocumentEntity> docs = documentRepository.findByEmployee(emp);
		int totalRequired = RequiredDocuments.REQUIRED_DOC_TYPES.size();
		long uploadedCount = docs.stream().filter(d -> RequiredDocuments.REQUIRED_DOC_TYPES.contains(d.getFileType()))
				.count();

		long approvedDocs = docs.stream().filter(d -> "APPROVED".equalsIgnoreCase(d.getStatus())).count();
		long rejectedDocs = docs.stream().filter(d -> "REJECTED".equalsIgnoreCase(d.getStatus())).count();
		long pendingDocs = docs.stream().filter(d -> "PENDING".equalsIgnoreCase(d.getStatus())).count();

		double progress = ((double) uploadedCount / totalRequired) * 70; 

		// BANK STATS
		boolean hasBank = emp.getBankDetails() != null;
		String bankStatus = hasBank ? emp.getBankDetails().getVerificationStatus() : "NOT_SUBMITTED";
		progress += hasBank ? 30 : 0; 

		// NEXT STEPS
		List<OnboardingStepDto> nextSteps = new ArrayList<>();

		// Missing Documents
		List<String> missingDocs = RequiredDocuments.REQUIRED_DOC_TYPES.stream()
				.filter(type -> docs.stream().noneMatch(d -> d.getFileType().equalsIgnoreCase(type))).toList();

		if (!missingDocs.isEmpty()) {
			nextSteps.add(OnboardingStepDto.builder().priority("HIGH").action("Upload Missing Documents")
					.description(
							"Upload " + missingDocs.size() + " required document(s): " + String.join(", ", missingDocs))
					.endpoint("/api/employee/upload-documents").build());
		}

		// Rejected Documents
		List<String> rejectedDocTypes = docs.stream().filter(d -> "REJECTED".equalsIgnoreCase(d.getStatus()))
				.map(DocumentEntity::getFileType).toList();

		if (!rejectedDocTypes.isEmpty()) {
			nextSteps.add(OnboardingStepDto.builder().priority("HIGH").action("Re-upload Rejected Documents")
					.description("Re-upload " + rejectedDocTypes.size() + " rejected document(s): "
							+ String.join(", ", rejectedDocTypes))
					.endpoint("/api/v1/employee/reupload/document/{documentId}").build());
		}

		// Bank Details Handling
		if (!hasBank) {
			nextSteps.add(OnboardingStepDto.builder().priority("HIGH").action("Submit Bank Details")
					.description("Add your bank account information for salary processing.")
					.endpoint("/api/v1/employee/add-bank-details").build());
		} else if ("REJECTED".equalsIgnoreCase(bankStatus)) {
			nextSteps.add(OnboardingStepDto.builder().priority("HIGH").action("Update Bank Details")
					.description("Your bank details were rejected. Please correct and resubmit.")
					.endpoint("/api/v1/employee/reupload/bank-details").build());
		}

		// Status-based steps
		switch (empStatus.toUpperCase()) {
		case "UNDER_REVIEW" -> nextSteps.add(OnboardingStepDto.builder().priority("INFO").action("Wait for HR Approval")
				.description("Your documents and bank details are being reviewed by HR.").endpoint(null).build());
		case "REJECTED" -> nextSteps.add(OnboardingStepDto.builder().priority("HIGH").action("Fix Rejected Items")
				.description("Some items were rejected. Please correct and resubmit.")
				.endpoint("/api/employee/reupload/rejected-items").build());
		case "ACTIVE" -> nextSteps.add(OnboardingStepDto.builder().priority("INFO").action("Onboarding Complete")
				.description("🎉 Welcome aboard! You can now access all employee features.").endpoint(null).build());
		}

		String statusMessage = switch (empStatus.toUpperCase()) {
		case "PENDING" -> "Please upload required documents and add bank details.";
		case "DOCUMENTS_UPLOADED" -> "Documents uploaded. Add your bank details.";
		case "UNDER_REVIEW" -> "Your profile is under review by HR.";
		case "ACTIVE" -> "Onboarding complete.";
		case "REJECTED" ->
			rejectedDocTypes.isEmpty() ? "Some details were rejected. Please check your email for instructions."
					: "Some documents were rejected: " + String.join(", ", rejectedDocTypes);
		default -> "Continue your onboarding process.";
		};

		return EmployeeOnboardingStatusDto.builder().employeeId(employeeId).name(fullName).status(empStatus)
				.overallProgress((int) progress).statusMessage(statusMessage)
				.isComplete("ACTIVE".equalsIgnoreCase(empStatus)).missingDocuments(missingDocs)
				.bankDetailsSubmitted(hasBank).bankStatus(bankStatus).nextSteps(nextSteps)
				.approvedDocuments((int) approvedDocs).rejectedDocuments((int) rejectedDocs)
				.pendingDocuments((int) pendingDocs).build();
	}

	@Override
	public DocumentUploadResponseDto reuploadRejectedDocument(String username, Long documentId, MultipartFile file) {
		UserEntity user = userRepository.findByUsername(username)
				.orElseThrow(() -> new NotFoundException("User not found"));
		EmployeeEntity emp = user.getEmployee();

		if (emp == null) {
			throw new InvalidOperationException("User is not an employee.");
		}

		DocumentEntity doc = documentRepository.findById(documentId)
				.orElseThrow(() -> new NotFoundException("Document not found"));

		if (doc.getEmployee() == null || !doc.getEmployee().getEmployeeId().equals(emp.getEmployeeId())) {
			throw new InvalidOperationException("This document does not belong to you.");
		}

		if (!"REJECTED".equalsIgnoreCase(doc.getStatus())) {
			throw new InvalidOperationException(
					"Only rejected documents can be re-uploaded. Current status: " + doc.getStatus());
		}

		if (file == null || file.isEmpty()) {
			throw new InvalidOperationException("File cannot be empty.");
		}

		List<DocumentUploadResultDto> uploadedDocs = new ArrayList<>();
		List<String> failedDocs = new ArrayList<>();
		String overallResult;

		try {
			// Upload to Cloudinary
			Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
					ObjectUtils.asMap("folder", "employee_documents/" + emp.getEmployeeId(), "resource_type", "auto"));

			String url = (String) uploadResult.get("secure_url");
			doc.setFileUrl(url);
			doc.setFileName(file.getOriginalFilename());
			doc.setStatus("PENDING");
			doc.setRejectionReason(null);
			doc.setUploadedAt(LocalDateTime.now());
			documentRepository.save(doc);

			// Smart employee status update
			updateEmployeeStatus(emp);

			uploadedDocs.add(DocumentUploadResultDto.builder().documentType(doc.getFileType())
					.fileName(doc.getFileName()).fileUrl(doc.getFileUrl()).success(true)
					.message("Re-uploaded successfully and sent for verification.").build());

			overallResult = "SUCCESS";

		} catch (IOException e) {
			failedDocs.add(doc.getFileType() + ": Upload failed - " + e.getMessage());
			uploadedDocs.add(DocumentUploadResultDto.builder().documentType(doc.getFileType())
					.fileName(file.getOriginalFilename()).fileUrl(null).success(false)
					.message("Upload failed: " + e.getMessage()).build());

			overallResult = "FAILED";
		}

		return DocumentUploadResponseDto.builder().employeeId(emp.getEmployeeId()).documentId(doc.getDocumentId())
				.currentStatus(emp.getStatus()).uploadedDocuments(uploadedDocs).failedDocuments(failedDocs)
				.overallResult(overallResult)
				.message(failedDocs.isEmpty() ? "Document re-uploaded successfully and sent for verification."
						: "Document re-upload failed. Please try again.")
				.build();
	}

	public EmployeeBankDetailsResponseDto reuploadRejectedBankDetails(String username,
			EmployeeBankDetailsRequestDto request) {
		UserEntity user = userRepository.findByUsername(username)
				.orElseThrow(() -> new NotFoundException("User not found"));
		EmployeeEntity emp = user.getEmployee();

		if (emp == null) {
			throw new InvalidOperationException("User is not an employee.");
		}

		EmployeeBankDetailsEntity bank = emp.getBankDetails();

		if (bank == null) {
			throw new NotFoundException("Bank details not found.");
		}

		if (!"REJECTED".equalsIgnoreCase(bank.getVerificationStatus())) {
			throw new InvalidOperationException("Only rejected bank details can be updated.");
		}

		bank.setAccountHolderName(request.getAccountHolderName());
		bank.setAccountNumber(request.getAccountNumber());
		bank.setIfscCode(request.getIfscCode().toUpperCase());
		bank.setBankName(request.getBankName());
		bank.setBranchName(request.getBranchName());
		bank.setVerificationStatus("PENDING");
		bank.setReviewerComments(null);

		employeeBankDetailsRepository.save(bank);

		// Employee back to UNDER_REVIEW
		emp.setStatus("UNDER_REVIEW");
		employeeRepository.save(emp);

		return EmployeeBankDetailsResponseDto.builder().accountHolderName(bank.getAccountHolderName())
				.bankName(bank.getBankName()).verificationStatus("PENDING")
				.message("Bank details resubmitted successfully and sent for review.").build();
	}

	private void updateEmployeeStatus(EmployeeEntity emp) {
		boolean allDocsUploaded = RequiredDocuments.REQUIRED_DOC_TYPES.stream()
				.allMatch(type -> documentRepository.existsByEmployeeAndFileType(emp, type));

		boolean allDocsApproved = RequiredDocuments.REQUIRED_DOC_TYPES.stream()
				.allMatch(type -> documentRepository.existsByEmployeeAndFileTypeAndStatus(emp, type, "APPROVED"));

		boolean anyDocRejected = documentRepository.existsByEmployeeAndStatus(emp, "REJECTED");

		boolean hasBank = emp.getBankDetails() != null;
		String bankStatus = hasBank ? emp.getBankDetails().getVerificationStatus() : "NOT_SUBMITTED";

		// All docs approved + bank approved → ACTIVE
		if (allDocsApproved && "APPROVED".equalsIgnoreCase(bankStatus)) {
			emp.setStatus("ACTIVE");
		}

		// Any doc or bank rejected → REJECTED
		else if (anyDocRejected || "REJECTED".equalsIgnoreCase(bankStatus)) {
			emp.setStatus("REJECTED");
		}

		// All docs uploaded + bank provided (approved or pending) → UNDER_REVIEW
		else if (allDocsUploaded && hasBank && ("APPROVED".equalsIgnoreCase(bankStatus)
				|| "PENDING".equalsIgnoreCase(bankStatus) || "UNDER_REVIEW".equalsIgnoreCase(bankStatus))) {
			emp.setStatus("UNDER_REVIEW");
		}

		// All docs uploaded but no bank yet → DOCUMENTS_UPLOADED
		else if (allDocsUploaded && !hasBank) {
			emp.setStatus("DOCUMENTS_UPLOADED");
		}

		// Default fallback → PENDING
		else {
			emp.setStatus("PENDING");
		}

		employeeRepository.save(emp);
	}

}