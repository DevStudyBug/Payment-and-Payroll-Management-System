package com.aurionpro.service;

import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dto.request.ConcernRequestDto;
import com.aurionpro.dto.response.ConcernResponseDto;

public interface EmployeeConcernService {
	public ConcernResponseDto raiseConcern(String username, ConcernRequestDto request, MultipartFile file);
}
