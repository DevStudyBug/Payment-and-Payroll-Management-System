package com.aurionpro.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dto.request.BankDetailsRequestDto;
import com.aurionpro.dto.response.OrganizationOnboardingResponseDto;
import com.aurionpro.service.OrganizationOnboardingService;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/org")
@RequiredArgsConstructor
public class OrganizationController {

	private final OrganizationOnboardingService onboardingService;

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
}
