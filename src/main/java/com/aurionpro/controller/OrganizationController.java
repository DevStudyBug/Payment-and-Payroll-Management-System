package com.aurionpro.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dto.request.BankDetailsRequestDto;
import com.aurionpro.dto.request.DocumentUploadRequestDto;
import com.aurionpro.dto.request.SalaryTemplateRequestDto;
import com.aurionpro.dto.response.DesignationResponseDto;
import com.aurionpro.dto.response.OrganizationOnboardingResponseDto;
import com.aurionpro.dto.response.OrganizationOnboardingStatusResponseDto;
import com.aurionpro.dto.response.PagedResponse;
import com.aurionpro.dto.response.SalaryTemplateDetailResponseDto;
import com.aurionpro.dto.response.SalaryTemplateResponseDto;
import com.aurionpro.dto.response.SalaryTemplateSummaryResponseDto;
import com.aurionpro.entity.DesignationEntity;
import com.aurionpro.service.DesignationService;
import com.aurionpro.service.OrganizationOnboardingService;
import com.aurionpro.service.SalaryTemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/org")
@RequiredArgsConstructor
public class OrganizationController {

	private final OrganizationOnboardingService onboardingService;
	private final SalaryTemplateService salaryTemplateService;
	private final DesignationService designationService;

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PostMapping(value = "/upload-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<OrganizationOnboardingResponseDto> uploadDocument(Authentication authentication,
			@RequestPart("file") List<MultipartFile> files, @Valid @RequestPart("meta") String metaJson)
			throws JsonProcessingException {

		// Service handles JSON parsing & upload
		OrganizationOnboardingResponseDto response = onboardingService.uploadDocument(authentication, files, metaJson);

		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PostMapping("/add-bank-details")
	public ResponseEntity<OrganizationOnboardingResponseDto> addBankDetails(Authentication authentication,
			@Valid @RequestBody BankDetailsRequestDto request) {

		OrganizationOnboardingResponseDto response = onboardingService.addBankDetails(authentication, request);

		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PutMapping("/bank/reupload")
	public ResponseEntity<OrganizationOnboardingResponseDto> reuploadBankDetails(Authentication authentication,
			@Valid @RequestBody BankDetailsRequestDto req) {

		return ResponseEntity.ok(onboardingService.reuploadBankDetails(authentication, req));
	}

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PutMapping("/documents/{documentId}/reupload")
	public ResponseEntity<OrganizationOnboardingResponseDto> reuploadRejectedDocument(Authentication authentication,
			@PathVariable Long documentId, @RequestPart("file") MultipartFile file,
			@RequestPart("meta") String metaJson) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		DocumentUploadRequestDto meta = mapper.readValue(metaJson, DocumentUploadRequestDto.class);

		return ResponseEntity.ok(onboardingService.reuploadRejectedDocument(authentication, documentId, file, meta));
	}

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PostMapping("/salary-templates")
	public ResponseEntity<SalaryTemplateResponseDto> createSalaryTemplate(Authentication authentication,
			@Valid @RequestBody SalaryTemplateRequestDto request) {
		return ResponseEntity.ok(salaryTemplateService.createTemplate(authentication, request));
	}

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@GetMapping("/salary-templates")
	public ResponseEntity<PagedResponse<SalaryTemplateSummaryResponseDto>> getAllSalaryTemplates(
			Authentication authentication, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestParam(value = "sortBy", defaultValue = "designation") String sortBy,
			@RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {

		PagedResponse<SalaryTemplateSummaryResponseDto> response = salaryTemplateService.getAllTemplates(authentication,
				page, size, sortBy, sortDir);

		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@GetMapping("/salary-templates/{templateId}")
	public ResponseEntity<SalaryTemplateDetailResponseDto> getSalaryTemplateById(Authentication authentication,
			@PathVariable Long templateId) {
		return ResponseEntity.ok(salaryTemplateService.getTemplateById(authentication, templateId));
	}

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@PostMapping("/designations")
	public ResponseEntity<DesignationResponseDto> addDesignation(Authentication authentication,
			@RequestParam String name) {

		DesignationEntity designation = designationService.addDesignation(authentication, name);

		DesignationResponseDto response = new DesignationResponseDto(designation.getDesignationId(),
				designation.getName());

		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@GetMapping("/designations")
	public ResponseEntity<List<DesignationResponseDto>> getAllDesignations(Authentication authentication) {
		List<DesignationEntity> designations = designationService.getAllDesignations(authentication);

		List<DesignationResponseDto> responseList = designations.stream()
				.map(d -> new DesignationResponseDto(d.getDesignationId(), d.getName())).collect(Collectors.toList());

		return ResponseEntity.ok(responseList);

	}

	@PreAuthorize("hasRole('ORG_ADMIN')")
	@GetMapping("/onboarding-status")
	public ResponseEntity<OrganizationOnboardingStatusResponseDto> getOnboardingStatus(Authentication authentication) {
		return ResponseEntity.ok(onboardingService.getOnboardingStatus(authentication));
	}
}
