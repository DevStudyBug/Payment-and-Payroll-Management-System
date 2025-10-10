package com.aurionpro.service;

import org.springframework.security.core.Authentication;

import com.aurionpro.dto.request.SalaryTemplateRequestDto;
import com.aurionpro.dto.response.PagedResponse;
import com.aurionpro.dto.response.SalaryTemplateDetailResponseDto;
import com.aurionpro.dto.response.SalaryTemplateResponseDto;
import com.aurionpro.dto.response.SalaryTemplateSummaryResponseDto;

public interface SalaryTemplateService {
	public SalaryTemplateResponseDto createTemplate(Authentication authentication, SalaryTemplateRequestDto req);

	PagedResponse<SalaryTemplateSummaryResponseDto> getAllTemplates(Authentication authentication, int page, int size,
			String sortBy, String sortDir);

	public SalaryTemplateDetailResponseDto getTemplateById(Authentication authentication, Long templateId);
	
}
