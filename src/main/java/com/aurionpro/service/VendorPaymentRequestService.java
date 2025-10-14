package com.aurionpro.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.aurionpro.dto.request.VendorPaymentRequestCreateDto;
import com.aurionpro.dto.response.VendorPaymentRequestResponseDto;

public interface VendorPaymentRequestService {

	VendorPaymentRequestResponseDto createPaymentRequest(Authentication authentication,
			VendorPaymentRequestCreateDto dto);

	List<VendorPaymentRequestResponseDto> getOrgPaymentRequests(Authentication authentication);

	VendorPaymentRequestResponseDto getPaymentRequestById(Authentication authentication, Long paymentId);

	List<VendorPaymentRequestResponseDto> getAllPaymentRequestsForAdmin();

	VendorPaymentRequestResponseDto approvePaymentRequest(Long id);

	VendorPaymentRequestResponseDto rejectPaymentRequest(Long id, String remark);
}
