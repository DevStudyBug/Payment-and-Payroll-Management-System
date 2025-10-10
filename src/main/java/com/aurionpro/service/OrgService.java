package com.aurionpro.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.aurionpro.dto.request.BankVerificationRequestDto;
import com.aurionpro.dto.request.DocumentVerificationRequestDto;
import com.aurionpro.dto.response.EmployeeDetailResponseDto;
import com.aurionpro.dto.response.EmployeeListResponseDto;
import com.aurionpro.dto.response.VerificationResponseDto;

public interface OrgService {
	List<EmployeeListResponseDto> getEmployeesByStatus(Authentication authentication, String status);

	EmployeeDetailResponseDto getEmployeeDetails(Long employeeId);

	VerificationResponseDto verifyEmployeeDocument(Long employeeId, Long documentId,
			DocumentVerificationRequestDto request);

	VerificationResponseDto verifyEmployeeBankDetails(Long employeeId, BankVerificationRequestDto request);

	VerificationResponseDto completeEmployeeOnboarding(Long employeeId);
}
