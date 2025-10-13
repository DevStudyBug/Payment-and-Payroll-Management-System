package com.aurionpro.service;

import java.util.List;

import com.aurionpro.dto.response.BankOrgRegisterResponseDto;
import com.aurionpro.dto.response.OrgRegisterResponseDto;

public interface BankAdminService {
	List<OrgRegisterResponseDto> getPendingOrganizations();

    List<BankOrgRegisterResponseDto> getUnderReviewOrganizations();

    String verifyOrganization(Long orgId);

    String rejectOrganization(Long orgId, String remarks);

    String verifyDocument(Long orgId, Long docId);

    String rejectDocument(Long orgId, Long docId, String reason);

    String verifyBankDetails(Long orgId);

    String rejectBankDetails(Long orgId, String reason);

	List<OrgRegisterResponseDto> getAllOrganizations();

	List<OrgRegisterResponseDto> getOrganizationsByStatus(String status);
}
