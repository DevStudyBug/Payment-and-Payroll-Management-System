package com.aurionpro.service;

import org.springframework.data.domain.Page;

import com.aurionpro.dto.response.ConcernResponseDto;

public interface OrgAdminConcernService {
	Page<ConcernResponseDto> getAllConcerns(Long orgId, String status, String priority, int page, int size);

	ConcernResponseDto getConcernByTicket(String ticketNumber);

	ConcernResponseDto respondToConcern(String ticketNumber, String response);

	ConcernResponseDto resolveConcern(String ticketNumber, String response);

	ConcernResponseDto rejectConcern(String ticketNumber, String response);
}
