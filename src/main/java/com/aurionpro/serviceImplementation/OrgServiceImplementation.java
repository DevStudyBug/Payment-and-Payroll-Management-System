package com.aurionpro.serviceImplementation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.exception.NotFoundException;
import com.aurionpro.constants.RequiredDocuments;
import com.aurionpro.dto.request.BankVerificationRequestDto;
import com.aurionpro.dto.request.DocumentVerificationRequestDto;
import com.aurionpro.dto.response.BankReviewDto;
import com.aurionpro.dto.response.DocumentReviewDto;
import com.aurionpro.dto.response.EmployeeDetailResponseDto;
import com.aurionpro.dto.response.EmployeeListResponseDto;
import com.aurionpro.dto.response.VerificationResponseDto;
import com.aurionpro.entity.DocumentEntity;
import com.aurionpro.entity.EmployeeBankDetailsEntity;
import com.aurionpro.entity.EmployeeEntity;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.entity.UserEntity;
import com.aurionpro.repo.DocumentRepository;
import com.aurionpro.repo.EmployeeBankDetailsRepository;
import com.aurionpro.repo.EmployeeRepository;
import com.aurionpro.repo.UserRepository;
import com.aurionpro.service.EmailService;
import com.aurionpro.service.OrgService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrgServiceImplementation implements OrgService {
	private final EmployeeRepository employeeRepository;
	private final DocumentRepository documentRepository;
	private final EmployeeBankDetailsRepository bankDetailsRepository;
	private final EmailService emailService;
	private final UserRepository userRepository;

	@Override
	public List<EmployeeListResponseDto> getEmployeesByStatus(Authentication authentication, String status) {
		UserEntity user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = user.getOrganization();
		if (org == null) {
			throw new NotFoundException("User is not associated with any organization.");
		}
		List<EmployeeEntity> employees = "ALL".equalsIgnoreCase(status)
				? employeeRepository.findByOrganization_OrgId(org.getOrgId())
				: employeeRepository.findByOrganization_OrgIdAndStatusIgnoreCase(org.getOrgId(), status);

		return employees.stream().map(emp -> {
			long totalDocuments = RequiredDocuments.REQUIRED_DOC_TYPES.size();
			long uploaded = documentRepository.countByEmployee(emp);
			long approved = documentRepository.countByEmployeeAndStatus(emp, "APPROVED");
			int completion = (int) ((uploaded / (double) totalDocuments) * 100);
			return EmployeeListResponseDto.builder().employeeId(emp.getEmployeeId())
					.employeeName(emp.getFirstName() + " " + emp.getLastName()).status(emp.getStatus())
					.allDocumentsApproved(approved == totalDocuments)
					.bankApproved(emp.getBankDetails() != null
							&& "APPROVED".equalsIgnoreCase(emp.getBankDetails().getVerificationStatus()))
					.documentCompletion(Math.min(100, completion)).build();
		}).collect(Collectors.toList());
	}

	@Override
	public EmployeeDetailResponseDto getEmployeeDetails(Long employeeId) {
	    EmployeeEntity emp = employeeRepository.findById(employeeId)
	            .orElseThrow(() -> new NotFoundException("Employee not found"));

	    List<DocumentEntity> docs = documentRepository.findByEmployee(emp);
	    EmployeeBankDetailsEntity bank = emp.getBankDetails();

	    List<DocumentReviewDto> documentDtos = docs.stream()
	            .map(d -> DocumentReviewDto.builder()
	                    .documentId(d.getDocumentId())
	                    .type(d.getFileType())
	                    .fileName(d.getFileName())
	                    .fileUrl(d.getFileUrl())
	                    .status(d.getStatus())
	                    .rejectionReason(d.getRejectionReason())
	                    .uploadedAt(d.getUploadedAt())
	                    .actionRequired(getDocActionRequired(d))
	                    .build())
	            .collect(Collectors.toList());

	    BankReviewDto bankDto = null;
	    if (bank != null) {
	        bankDto = BankReviewDto.builder()
	                .accountHolderName(bank.getAccountHolderName())
	                .accountNumberMasked(bank.getAccountNumber())
	                .bankName(bank.getBankName())
	                .ifscCode(bank.getIfscCode())
	                .branchName(bank.getBranchName())
	                .status(bank.getVerificationStatus())
	                .rejectionReason(bank.getReviewerComments())
	                .build();
	    }

	   
	    UserEntity user = emp.getUser();

	    return EmployeeDetailResponseDto.builder()
	            .employeeId(emp.getEmployeeId())
	            .name(emp.getFirstName() + " " + emp.getLastName())
	            .email(user != null ? user.getEmail() : "N/A")
	            .username(user != null ? user.getUsername() : "N/A")
	            .department(emp.getDepartment() != null ? emp.getDepartment() : "N/A")
	            .designation(emp.getDesignation() != null ? emp.getDesignation() : "N/A")
	            .dateOfBirth(emp.getDob())
	            .status(emp.getStatus())
	            .documents(documentDtos)
	            .bankDetails(bankDto)
	            .build();
	}

	private String getDocActionRequired(DocumentEntity d) {
		if ("REJECTED".equalsIgnoreCase(d.getStatus()))
			return "Re-upload required";
		if ("PENDING".equalsIgnoreCase(d.getStatus()))
			return "Under review";
		return "None";
	}

	@Override

	public VerificationResponseDto verifyEmployeeDocument(Long employeeId, Long documentId,
			DocumentVerificationRequestDto request) {
		EmployeeEntity emp = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new NotFoundException("Employee not found"));
		DocumentEntity doc = documentRepository.findById(documentId)
				.orElseThrow(() -> new NotFoundException("Document not found"));

		if (doc.getEmployee() == null || doc.getEmployee().getEmployeeId() == null) {
			throw new InvalidOperationException("Document is not linked to any employee.");
		}

		if (!doc.getEmployee().getEmployeeId().equals(employeeId)) {
			throw new InvalidOperationException("Document does not belong to this employee.");
		}
		if (doc.getEmployee() == null) {
			throw new InvalidOperationException("Document is not linked to any employee. Please re-upload.");
		}

		if (request.isApproved()) {
			doc.setStatus("APPROVED");
			doc.setRejectionReason(null);
			// Send approval email
			emailService.sendDocumentStatusEmail(emp, doc.getFileType(), "APPROVED", null);
		} else {
			doc.setStatus("REJECTED");
			doc.setRejectionReason(request.getRejectionReason());
			emp.setStatus("REJECTED");
			// Send rejection email
			emailService.sendDocumentStatusEmail(emp, doc.getFileType(), "REJECTED", request.getRejectionReason());
		}

		documentRepository.save(doc);
		employeeRepository.save(emp);

		return VerificationResponseDto.builder().employeeId(emp.getEmployeeId())
				.message(request.isApproved() ? "Document approved" : "Document rejected")
				.currentStatus(emp.getStatus()).build();
	}

	@Override
	public VerificationResponseDto verifyEmployeeBankDetails(Long employeeId, BankVerificationRequestDto request) {
		EmployeeEntity emp = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new NotFoundException("Employee not found"));
		EmployeeBankDetailsEntity bank = emp.getBankDetails();

		if (bank == null) {
			throw new NotFoundException("Bank details not found for employee");
		}

		if (request.isApproved()) {
			bank.setVerificationStatus("APPROVED");
			bank.setReviewerComments(null);
			// Send bank approval email
			emailService.sendBankStatusEmail(emp, "APPROVED", null);
		} else {
			bank.setVerificationStatus("REJECTED");
			bank.setReviewerComments(request.getRejectionReason());
			emp.setStatus("REJECTED");
			// Send bank rejection email
			emailService.sendBankStatusEmail(emp, "REJECTED", request.getRejectionReason());
		}

		bankDetailsRepository.save(bank);
		employeeRepository.save(emp);

		return VerificationResponseDto.builder().employeeId(emp.getEmployeeId())
				.message(request.isApproved() ? "Bank details approved" : "Bank details rejected")
				.currentStatus(emp.getStatus()).build();
	}

	@Override
	public VerificationResponseDto completeEmployeeOnboarding(Long employeeId) {
		EmployeeEntity emp = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new NotFoundException("Employee not found"));

		boolean allDocsApproved = RequiredDocuments.REQUIRED_DOC_TYPES.stream()
				.allMatch(type -> documentRepository.existsByEmployeeAndFileTypeAndStatus(emp, type, "APPROVED"));

		boolean bankApproved = emp.getBankDetails() != null
				&& "APPROVED".equalsIgnoreCase(emp.getBankDetails().getVerificationStatus());

		if (allDocsApproved && bankApproved) {
			emp.setStatus("ACTIVE");
			employeeRepository.save(emp);
			// Send onboarding completion email
			emailService.sendEmployeeActivationEmail(emp);

			return VerificationResponseDto.builder().employeeId(emp.getEmployeeId())
					.message("Employee activated successfully").currentStatus("ACTIVE").build();
		} else {
			throw new InvalidOperationException("Cannot complete onboarding — pending or rejected items remain.");
		}
	}

}
