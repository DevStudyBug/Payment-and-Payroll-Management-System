package com.aurionpro.service;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;

import com.aurionpro.dto.request.RejectRequestDto;
import com.aurionpro.dto.response.PaymentRequestDetailDto;
import com.aurionpro.dto.response.PaymentRequestPageResponseDto;
import com.aurionpro.dto.response.PayrollActionResponseDto;
import com.aurionpro.dto.response.PayrollGenerateResponseDto;
import com.aurionpro.dto.response.PayrollSubmitResponseDto;

public interface PayrollService {
	// OrgAdmin Actions
	public PayrollGenerateResponseDto generatePayroll(Authentication authentication, String salaryMonth);

	public PayrollSubmitResponseDto submitPayrollToBank(Authentication authentication, String salaryMonth);

	public PaymentRequestPageResponseDto getFilteredPaymentRequests(String status, String requestType, Long orgId,
			LocalDateTime startDate, LocalDateTime endDate, int page, int size, String sortBy, String sortDir);

	public PaymentRequestDetailDto getPaymentRequestDetail(Long paymentRequestId);

	public PayrollActionResponseDto approvePayrollRequest(Long paymentRequestId);

	public PayrollActionResponseDto rejectPayrollRequest(Long paymentRequestId, RejectRequestDto dto);

	public PayrollActionResponseDto disbursePayroll(Long paymentRequestId);
}
