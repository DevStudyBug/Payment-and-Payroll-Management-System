package com.aurionpro.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

import com.aurionpro.dto.request.ConcernRequestDto;
import com.aurionpro.dto.request.EmployeeBankDetailsRequestDto;
import com.aurionpro.dto.response.ConcernResponseDto;
import com.aurionpro.dto.response.DocumentUploadResponseDto;
import com.aurionpro.dto.response.EmployeeBankDetailsFullResponseDto;
import com.aurionpro.dto.response.EmployeeBankDetailsResponseDto;
import com.aurionpro.dto.response.EmployeeOnboardingStatusDto;
import com.aurionpro.dto.response.SalarySlipResponseDto;
import com.aurionpro.service.EmployeeDashboardService;
import com.aurionpro.service.EmployeeService;
import com.aurionpro.serviceImplementation.EmployeeConcernServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {
	private final EmployeeService employeeService;
	private final EmployeeDashboardService employeeDashboardService;
	private final EmployeeConcernServiceImpl concernService;
	private final ObjectMapper objectMapper;

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

	@PreAuthorize("hasRole('EMPLOYEE')")
	@GetMapping("/salary-slip/{month}")
	public ResponseEntity<SalarySlipResponseDto> getSalarySlip(Authentication authentication,
			@PathVariable String month) {

		SalarySlipResponseDto response = employeeDashboardService.viewSalarySlip(authentication.getName(), month);
		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@GetMapping("/salary-slip/{month}/pdf")
	public ResponseEntity<byte[]> downloadSalarySlip(Authentication authentication, @PathVariable String month) {

		byte[] pdf = employeeDashboardService.downloadSalarySlip(authentication.getName(), month);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=salary-slip-" + month + ".pdf")
				.contentType(MediaType.APPLICATION_PDF).body(pdf);
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@GetMapping("/bank-details")
	public ResponseEntity<EmployeeBankDetailsFullResponseDto> getBankDetails(Authentication authentication) {
		return ResponseEntity.ok(employeeDashboardService.getBankDetails(authentication.getName()));
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@PutMapping("/bank-details/update")
	public ResponseEntity<EmployeeBankDetailsResponseDto> updateBankDetails(Authentication authentication,
			@RequestBody EmployeeBankDetailsRequestDto request) {
		return ResponseEntity.ok(employeeDashboardService.updateBankDetails(authentication.getName(), request));
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@PostMapping("/concerns")
	public ResponseEntity<ConcernResponseDto> raiseConcern(Authentication authentication,
			@RequestPart("data") String requestData,
			@RequestPart(value = "file", required = false) MultipartFile file) {
		try {

			ConcernRequestDto request = objectMapper.readValue(requestData, ConcernRequestDto.class);

			return ResponseEntity.ok(concernService.raiseConcern(authentication.getName(), request, file));
		} catch (Exception e) {
			throw new RuntimeException("Error parsing request data: " + e.getMessage(), e);
		}

	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@GetMapping("/concerns")
	public ResponseEntity<List<ConcernResponseDto>> getMyConcerns(Authentication authentication) {
		return ResponseEntity.ok(concernService.getMyConcerns(authentication.getName()));
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@GetMapping("/concerns/{ticketNumber}")
	public ResponseEntity<ConcernResponseDto> getConcernByTicket(Authentication authentication,
			@PathVariable String ticketNumber) {
		return ResponseEntity.ok(concernService.getConcernByTicket(authentication.getName(), ticketNumber));
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@PutMapping("/concerns/{ticketNumber}/acknowledge")
	public ResponseEntity<ConcernResponseDto> closeConcern(Authentication authentication,
			@PathVariable String ticketNumber) {
		return ResponseEntity.ok(concernService.acknowledgeConcern(authentication.getName(), ticketNumber));
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@PutMapping("/concerns/{ticketNumber}/reopen")
	public ResponseEntity<ConcernResponseDto> reopenConcern(Authentication authentication,
			@PathVariable String ticketNumber, @RequestBody String reason) {
		return ResponseEntity.ok(concernService.reopenConcern(authentication.getName(), ticketNumber, reason));
	}

}