package com.aurionpro.controller;

import java.util.Date;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dto.request.LoginRequestDto;
import com.aurionpro.dto.request.OrgRegisterRequestDto;
import com.aurionpro.dto.response.LoginResponseDto;
import com.aurionpro.dto.response.OrgRegisterResponseDto;
import com.aurionpro.entity.UserEntity;
import com.aurionpro.entity.VerificationTokenEntity;
import com.aurionpro.repo.UserRepository;
import com.aurionpro.repo.VerificationTokenRepository;
import com.aurionpro.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final UserRepository userRepository;
	private final VerificationTokenRepository verificationTokenRepo;
	private final ModelMapper modelMapper;

	@PostMapping("/org-register")
	public ResponseEntity<?> registerOrg(@Valid @RequestBody OrgRegisterRequestDto req) {
		OrgRegisterResponseDto savedOrg = authService.registerOrganization(req);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(Map.of("message", "Organization registered successfully. Please verify your email.", "orgId",
						savedOrg.getOrgId(), "status", savedOrg.getStatus()));
	}

	@GetMapping("/verify-email")
	public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
		VerificationTokenEntity vToken = verificationTokenRepo.findByToken(token)
				.orElseThrow(() -> new RuntimeException("Invalid token"));

		if (vToken.getExpiryDate().before(new Date())) {
			return ResponseEntity.badRequest().body("Token expired");
		}

		UserEntity user = vToken.getUser();
		user.setStatus("ACTIVE");
		userRepository.save(user);

		return ResponseEntity.ok("Email verified successfully. You can now log in.");
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
		LoginResponseDto response = authService.loginOrganization(request);
		return ResponseEntity.ok(response);
	}
}
