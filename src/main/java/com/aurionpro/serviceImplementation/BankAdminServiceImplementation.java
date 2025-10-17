package com.aurionpro.serviceImplementation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.aurionpro.dto.response.BankOrgRegisterResponseDto;
import com.aurionpro.dto.response.OrgRegisterResponseDto;
import com.aurionpro.dto.response.OrganizationDocumentDto;
import com.aurionpro.entity.DocumentEntity;
import com.aurionpro.entity.OrganizationBankAccountEntity;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.repo.OrganizationRepository;
import com.aurionpro.service.BankAdminService;
import com.aurionpro.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankAdminServiceImplementation implements BankAdminService {

	private final OrganizationRepository organizationRepo;
	private final EmailService emailService;

	@Override
	public List<OrgRegisterResponseDto> getPendingOrganizations() {
		return organizationRepo.findAll().stream().filter(org -> "PENDING".equals(org.getStatus()))
				.map(org -> OrgRegisterResponseDto.builder().orgId(org.getOrgId()).orgName(org.getOrgName())
						.email(org.getEmail()).status(org.getStatus()).message("Pending admin verification").build())
				.collect(Collectors.toList());
	}

	@Override
	public List<BankOrgRegisterResponseDto> getUnderReviewOrganizations() {
		return organizationRepo.findAll().stream().filter(org -> "UNDER_REVIEW".equals(org.getStatus())).map(org -> {
			List<OrganizationDocumentDto> docs = org.getDocuments().stream()
					.map(doc -> OrganizationDocumentDto.builder().fileName(doc.getFileName()).fileUrl(doc.getFileUrl())
							.fileType(doc.getFileType()).status(doc.getStatus()).uploadedAt(doc.getUploadedAt())
							.verifiedAt(doc.getVerifiedAt()).build())
					.collect(Collectors.toList());

			String bankStatus = org.getBankAccount() != null ? org.getBankAccount().getVerificationStatus() : "PENDING";
			String bankRemarks = org.getBankAccount() != null ? org.getBankAccount().getRemarks() : null;

			return BankOrgRegisterResponseDto.builder().orgId(org.getOrgId()).orgName(org.getOrgName())
					.email(org.getEmail()).status(org.getStatus()).message("Organization under admin review")
					.documents(docs).bankVerificationStatus(bankStatus).bankRemarks(bankRemarks).build();
		}).collect(Collectors.toList());
	}

	// Organization Verification
	@Override
	public String verifyOrganization(Long orgId) {
		OrganizationEntity org = organizationRepo.findById(orgId)
				.orElseThrow(() -> new RuntimeException("Organization not found"));

		boolean allDocsApproved = org.getDocuments().stream().allMatch(d -> "APPROVED".equals(d.getStatus()));

		OrganizationBankAccountEntity bank = org.getBankAccount();
		boolean bankVerified = bank != null && "APPROVED".equals(bank.getVerificationStatus());

		if (!allDocsApproved) {
			return "Cannot activate organization: Some documents are still pending verification.";
		}

		if (!bankVerified) {
			return "Cannot activate organization: Bank details not verified.";
		}

		org.setStatus("ACTIVE");
		organizationRepo.save(org);

		// Send approval email
		emailService.sendBankAdminVerificationEmail(org);

		return "Organization activated successfully.";
	}

	// Organization Rejection
	@Override
	public String rejectOrganization(Long orgId, String remarks) {
		OrganizationEntity org = organizationRepo.findById(orgId)
				.orElseThrow(() -> new RuntimeException("Organization not found"));

		org.setStatus("REJECTED");
		org.setRemarks(remarks);
		organizationRepo.save(org);

		// Send rejection email
		emailService.sendBankAdminRejectionEmail(org, remarks);

		return "Organization rejected with reason: " + remarks;
	}

	// Document Verification
	@Override
	public String verifyDocument(Long orgId, Long docId) {
		OrganizationEntity org = organizationRepo.findById(orgId)
				.orElseThrow(() -> new RuntimeException("Organization not found"));

		DocumentEntity doc = org.getDocuments().stream().filter(d -> d.getDocumentId().equals(docId)).findFirst()
				.orElseThrow(() -> new RuntimeException("Document not found"));

		doc.setStatus("APPROVED");
		doc.setVerifiedAt(LocalDateTime.now());
		organizationRepo.save(org);

		// Send document verified email
		emailService.sendDocumentVerifiedEmail(org, doc.getFileName());

		return "Document verified successfully.";
	}

	// Document Rejection
	@Override
	public String rejectDocument(Long orgId, Long docId, String reason) {
		OrganizationEntity org = organizationRepo.findById(orgId)
				.orElseThrow(() -> new RuntimeException("Organization not found"));

		DocumentEntity doc = org.getDocuments().stream().filter(d -> d.getDocumentId().equals(docId)).findFirst()
				.orElseThrow(() -> new RuntimeException("Document not found"));

		doc.setStatus("REJECTED");
		org.setStatus("REJECTED");
		doc.setVerifiedAt(LocalDateTime.now());
		doc.setRejectionReason(reason);
		organizationRepo.save(org);

		// Send rejection email
		emailService.sendDocumentRejectedEmail(org, doc.getFileName(), reason);

		return "Document rejected. Reason: " + reason;
	}

	// Bank Details Verification
	@Override
	public String verifyBankDetails(Long orgId) {
		OrganizationEntity org = organizationRepo.findById(orgId)
				.orElseThrow(() -> new RuntimeException("Organization not found"));

		OrganizationBankAccountEntity bank = org.getBankAccount();
		if (bank == null)
			return "No bank details found for verification.";

		bank.setVerificationStatus("APPROVED");
		bank.setVerifiedAt(LocalDateTime.now());
		organizationRepo.save(org);

		// Send verified email
		emailService.sendBankDetailsVerifiedEmail(org);

		return "Bank details verified successfully.";
	}

	// Bank Details Rejection
	@Override
	public String rejectBankDetails(Long orgId, String reason) {
		OrganizationEntity org = organizationRepo.findById(orgId)
				.orElseThrow(() -> new RuntimeException("Organization not found"));

		OrganizationBankAccountEntity bank = org.getBankAccount();
		if (bank == null)
			return "No bank details found for rejection.";

		bank.setVerificationStatus("REJECTED");
		bank.setVerifiedAt(LocalDateTime.now());
		bank.setRemarks(reason);
		organizationRepo.save(org);

		// Send rejection email
		emailService.sendBankDetailsRejectedEmail(org, reason);

		return "Bank details rejected. Reason: " + reason;
	}

	// get all org
	@Override
	public List<OrgRegisterResponseDto> getAllOrganizations() {
		return organizationRepo.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
	}

	@Override
	public List<OrgRegisterResponseDto> getOrganizationsByStatus(String status) {
		return organizationRepo.findAll().stream()
				.filter(org -> org.getStatus() != null && org.getStatus().equalsIgnoreCase(status)).map(this::mapToDto)
				.collect(Collectors.toList());
	}

	private OrgRegisterResponseDto mapToDto(OrganizationEntity org) {
		List<OrganizationDocumentDto> docs = org.getDocuments() != null ? org.getDocuments().stream()
				.map(doc -> OrganizationDocumentDto.builder().docId(doc.getDocumentId()).docName(doc.getFileType())
						.fileName(doc.getFileName()).fileUrl(doc.getFileUrl()).fileType(doc.getFileType())
						.status(doc.getStatus() != null ? doc.getStatus() : "PENDING").uploadedAt(doc.getUploadedAt())
						.verifiedAt(doc.getVerifiedAt()).build())
				.collect(Collectors.toList()) : List.of();

		OrganizationBankAccountEntity bank = org.getBankAccount();

		String bankStatus = (bank != null && bank.getVerificationStatus() != null) ? bank.getVerificationStatus()
				: "NOT_SUBMITTED";

		String bankRemarks = (bank != null) ? bank.getRemarks() : null;

		String accountHolderName = bank != null ? bank.getAccountHolderName() : null;
		String accountNumber = bank != null ? bank.getAccountNumber() : null;
		String ifscCode = bank != null ? bank.getIfscCode() : null;
		String bankName = bank != null ? bank.getBankName() : null;

		return OrgRegisterResponseDto.builder().orgId(org.getOrgId()).orgName(org.getOrgName()).email(org.getEmail())
				.status(org.getStatus() != null ? org.getStatus() : "PENDING").documents(docs)
				.accountHolderName(accountHolderName).accountNumber(accountNumber).ifscCode(ifscCode).bankName(bankName)
				.bankVerificationStatus(bankStatus).bankRemarks(bankRemarks)
				.message("Organization record retrieved successfully").build();
	}

}
