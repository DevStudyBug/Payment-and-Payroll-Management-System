package com.aurionpro.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dto.request.RejectRequestDto;
import com.aurionpro.dto.response.BankOrgRegisterResponseDto;
import com.aurionpro.dto.response.OrgRegisterResponseDto;
import com.aurionpro.dto.response.PaymentRequestDetailDto;
import com.aurionpro.dto.response.PaymentRequestPageResponseDto;
import com.aurionpro.dto.response.PayrollActionResponseDto;
import com.aurionpro.service.BankAdminService;
import com.aurionpro.service.PayrollService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/bank-admin")
@RequiredArgsConstructor
public class BankAdminController {

	private final BankAdminService bankAdminService;
	private final PayrollService payrollService;

	// ---------------------------
	// GET: Pending Organizations
	// ---------------------------
	@PreAuthorize("hasRole('ORG_ADMIN')")
	@GetMapping("/organizations/pending")
	public ResponseEntity<List<OrgRegisterResponseDto>> getPendingOrganizations() {
		return ResponseEntity.ok(bankAdminService.getPendingOrganizations());
	}

	// ---------------------------
	// GET: Under Review Organizations
	// ---------------------------
	@PreAuthorize("hasRole('ORG_ADMIN')")
	@GetMapping("/organizations/under-review")
	public ResponseEntity<List<BankOrgRegisterResponseDto>> getUnderReviewOrganizations() {
		List<BankOrgRegisterResponseDto> underReviewOrgs = bankAdminService.getUnderReviewOrganizations();
		return ResponseEntity.ok(underReviewOrgs);
	}

	// ---------------------------
	// POST: Verify Organization
	// ---------------------------
	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PostMapping("/organizations/{orgId}/verify")
	public ResponseEntity<Map<String, Object>> verifyOrganization(@PathVariable Long orgId) {
		String message = bankAdminService.verifyOrganization(orgId);

		// Determine actual status and emailSent flag
		String orgStatus = message.contains("successfully") ? "ACTIVE" : "PENDING";
		boolean emailSent = message.contains("successfully");

		Map<String, Object> response = new HashMap<>();
		response.put("orgId", orgId);
		response.put("status", orgStatus);
		response.put("message", message);
		response.put("emailSent", emailSent);

		return ResponseEntity.ok(response);
	}

	// ---------------------------
	// POST: Reject Organization
	// ---------------------------
	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PostMapping("/organizations/{orgId}/reject")
	public ResponseEntity<Map<String, Object>> rejectOrganization(@PathVariable Long orgId,
			@RequestBody Map<String, String> request) {

		String reason = request.get("reason");
		String message = bankAdminService.rejectOrganization(orgId, reason);

		Map<String, Object> response = new HashMap<>();
		response.put("orgId", orgId);
		response.put("status", "REJECTED");
		response.put("message", message);
		response.put("reason", reason);
		response.put("emailSent", true);

		return ResponseEntity.ok(response);
	}

	// ---------------------------
	// POST: Verify Document
	// ---------------------------
	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PostMapping("/organizations/{orgId}/documents/{docId}/verify")
	public ResponseEntity<Map<String, Object>> verifyDocument(@PathVariable Long orgId, @PathVariable Long docId) {

		String message = bankAdminService.verifyDocument(orgId, docId);

		Map<String, Object> response = new HashMap<>();
		response.put("orgId", orgId);
		response.put("docId", docId);
		response.put("status", "APPROVED");
		response.put("message", message);
		response.put("emailSent", true);

		return ResponseEntity.ok(response);
	}

	// ---------------------------
	// POST: Reject Document
	// ---------------------------
	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PostMapping("/organizations/{orgId}/documents/{docId}/reject")
	public ResponseEntity<Map<String, Object>> rejectDocument(@PathVariable Long orgId, @PathVariable Long docId,
			@RequestBody Map<String, String> request) {

		String reason = request.get("reason");
		String message = bankAdminService.rejectDocument(orgId, docId, reason);

		Map<String, Object> response = new HashMap<>();
		response.put("orgId", orgId);
		response.put("docId", docId);
		response.put("status", "REJECTED");
		response.put("message", message);
		response.put("reason", reason);
		response.put("emailSent", true);

		return ResponseEntity.ok(response);
	}

	// ---------------------------
	// POST: Verify Bank Details
	// ---------------------------
	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PostMapping("/organizations/{orgId}/bank/verify")
	public ResponseEntity<Map<String, Object>> verifyBankDetails(@PathVariable Long orgId) {
		String message = bankAdminService.verifyBankDetails(orgId);

		Map<String, Object> response = new HashMap<>();
		response.put("orgId", orgId);
		response.put("status", "APPROVED");
		response.put("message", message);
		response.put("emailSent", true);

		return ResponseEntity.ok(response);
	}

	// ---------------------------
	// POST: Reject Bank Details
	// ---------------------------
	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PostMapping("/organizations/{orgId}/bank/reject")
	public ResponseEntity<Map<String, Object>> rejectBankDetails(@PathVariable Long orgId,
			@RequestBody Map<String, String> request) {

		String reason = request.get("reason");
		String message = bankAdminService.rejectBankDetails(orgId, reason);

		Map<String, Object> response = new HashMap<>();
		response.put("orgId", orgId);
		response.put("status", "REJECTED");
		response.put("message", message);
		response.put("reason", reason);
		response.put("emailSent", true);

		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@GetMapping("/requests")
	public ResponseEntity<PaymentRequestPageResponseDto> listPaymentRequests(
			@RequestParam(required = false) String status, @RequestParam(required = false) String type,
			@RequestParam(required = false) Long orgId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "requestDate") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir) {
		return ResponseEntity.ok(payrollService.getFilteredPaymentRequests(status, type, orgId, startDate, endDate,
				page, size, sortBy, sortDir));
	}

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@GetMapping("/requests/{paymentRequestId}")
	public ResponseEntity<PaymentRequestDetailDto> getPaymentRequestDetail(@PathVariable Long paymentRequestId) {
		return ResponseEntity.ok(payrollService.getPaymentRequestDetail(paymentRequestId));
	}

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PutMapping("/approve/{paymentRequestId}")
	public ResponseEntity<PayrollActionResponseDto> approvePayrollRequest(@PathVariable Long paymentRequestId) {

		PayrollActionResponseDto response = payrollService.approvePayrollRequest(paymentRequestId);
		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PutMapping("/reject/{paymentRequestId}")
	public ResponseEntity<PayrollActionResponseDto> rejectPayrollRequest(@PathVariable Long paymentRequestId,
			@RequestBody RejectRequestDto dto) {

		PayrollActionResponseDto response = payrollService.rejectPayrollRequest(paymentRequestId, dto);
		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PutMapping("/disburse/{paymentRequestId}")
	public ResponseEntity<PayrollActionResponseDto> disbursePayroll(@PathVariable Long paymentRequestId) {

		PayrollActionResponseDto response = payrollService.disbursePayroll(paymentRequestId);
		return ResponseEntity.ok(response);
	}
}
