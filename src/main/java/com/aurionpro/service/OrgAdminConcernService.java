package com.aurionpro.service;

import org.springframework.data.domain.Page;

import com.aurionpro.dto.response.ConcernResponseDto;

public interface OrgAdminConcernService {
	Page<ConcernResponseDto> getAllConcerns(Long orgId, String status, String priority, int page, int size);

	ConcernResponseDto getConcernByTicket(Long orgId, String ticketNumber);
	ConcernResponseDto respondToConcern(Long orgId, String ticketNumber, String response);
	ConcernResponseDto resolveConcern(Long orgId, String ticketNumber, String response);
	ConcernResponseDto rejectConcern(Long orgId, String ticketNumber, String response);
}
