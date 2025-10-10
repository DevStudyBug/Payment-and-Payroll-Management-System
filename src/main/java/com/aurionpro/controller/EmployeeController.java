package com.aurionpro.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dto.request.EmployeeBankDetailsRequestDto;
import com.aurionpro.dto.response.DocumentUploadResponseDto;
import com.aurionpro.dto.response.EmployeeBankDetailsResponseDto;
import com.aurionpro.dto.response.EmployeeOnboardingStatusDto;
import com.aurionpro.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController {
	private final EmployeeService employeeService;

	@PreAuthorize("hasRole('EMPLOYEE')")
	@PostMapping("/document/uploads")

	public ResponseEntity<?> uploadDocuments(@RequestParam("files") MultipartFile[] files,
			@RequestParam("docTypes") String[] docTypes, Authentication authentication) {

		try {
			String username = authentication.getName();

			// Single or multiple upload based on file count
			DocumentUploadResponseDto response;
			if (files.length == 1) {
				response = employeeService.uploadDocument(username, files[0], docTypes[0]);
			} else {
				response = employeeService.uploadDocuments(username, files, docTypes);
			}

			return ResponseEntity.ok(response);

		} catch (Exception e) {

			return ResponseEntity.badRequest()
					.body(Map.of("error", e.getMessage(), "timestamp", java.time.LocalDateTime.now().toString()));
		}
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@PostMapping("/add-bank-details")
	public ResponseEntity<?> addBankDetails(@Valid @RequestBody EmployeeBankDetailsRequestDto request,
			Authentication authentication) {
		try {
			EmployeeBankDetailsResponseDto response = employeeService.addBankDetails(authentication.getName(), request);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@GetMapping("/onboarding-status")
	public ResponseEntity<?> getStatus(Authentication authentication) {
		EmployeeOnboardingStatusDto status = employeeService.getOnboardingStatus(authentication.getName());
		return ResponseEntity.ok(status);
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@PutMapping("/reupload/bank-details")
	public ResponseEntity<?> reuploadBankDetails(@Valid @RequestBody EmployeeBankDetailsRequestDto request,
			Authentication authentication) {
		try {
			EmployeeBankDetailsResponseDto response = employeeService
					.reuploadRejectedBankDetails(authentication.getName(), request);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@PostMapping("/reupload-document/{documentId}")
	public ResponseEntity<?> reuploadDocument(@PathVariable Long documentId, @RequestParam("file") MultipartFile file,
			Authentication authentication) {
		return ResponseEntity.ok(employeeService.reuploadRejectedDocument(authentication.getName(), documentId, file));
	}
}
