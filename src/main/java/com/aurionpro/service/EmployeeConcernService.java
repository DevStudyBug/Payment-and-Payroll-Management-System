package com.aurionpro.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dto.request.ConcernRequestDto;
import com.aurionpro.dto.response.ConcernResponseDto;

public interface EmployeeConcernService {
	public ConcernResponseDto raiseConcern(String username, ConcernRequestDto request, MultipartFile file);
	public ConcernResponseDto reopenConcern(String username, String ticketNumber, String reason);
	public List<ConcernResponseDto> getMyConcerns(String username);
	public ConcernResponseDto getConcernByTicket(String username, String ticketNumber);
	public ConcernResponseDto acknowledgeConcern(String username, String ticketNumber);
	
}
