package com.aurionpro.controller;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dto.request.RejectRequestDto;
import com.aurionpro.dto.response.PaymentRequestDetailDto;
import com.aurionpro.dto.response.PaymentRequestPageResponseDto;
import com.aurionpro.dto.response.PayrollActionResponseDto;
import com.aurionpro.service.PayrollService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bankadmin")
@RequiredArgsConstructor
public class BankAdminController {
	private final PayrollService payrollService;

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

	@GetMapping("/requests/{paymentRequestId}")
	public ResponseEntity<PaymentRequestDetailDto> getPaymentRequestDetail(@PathVariable Long paymentRequestId) {
		return ResponseEntity.ok(payrollService.getPaymentRequestDetail(paymentRequestId));
	}
	
	@PutMapping("/approve/{paymentRequestId}")
    public ResponseEntity<PayrollActionResponseDto> approvePayrollRequest(
            @PathVariable Long paymentRequestId) {

        PayrollActionResponseDto response = payrollService.approvePayrollRequest(paymentRequestId);
        return ResponseEntity.ok(response);
    }
	
	@PutMapping("/reject/{paymentRequestId}")
    public ResponseEntity<PayrollActionResponseDto> rejectPayrollRequest(
            @PathVariable Long paymentRequestId,
            @RequestBody RejectRequestDto dto) {

        PayrollActionResponseDto response = payrollService.rejectPayrollRequest(paymentRequestId, dto);
        return ResponseEntity.ok(response);
    }
	
	@PutMapping("/disburse/{paymentRequestId}")
    public ResponseEntity<PayrollActionResponseDto> disbursePayroll(
            @PathVariable Long paymentRequestId) {

        PayrollActionResponseDto response = payrollService.disbursePayroll(paymentRequestId);
        return ResponseEntity.ok(response);
    }

}
