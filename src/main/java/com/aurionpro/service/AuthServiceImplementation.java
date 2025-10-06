package com.aurionpro.service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aurionpro.app.exception.DuplicateResourceException;
import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.exception.NotFoundException;
import com.aurionpro.dto.request.LoginRequestDto;
import com.aurionpro.dto.request.OrgRegisterRequestDto;
import com.aurionpro.dto.response.LoginResponseDto;
import com.aurionpro.dto.response.OrgRegisterResponseDto;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.entity.UserEntity;
import com.aurionpro.entity.UserRoleEntity;
import com.aurionpro.entity.VerificationTokenEntity;
import com.aurionpro.repo.OrganizationRepository;
import com.aurionpro.repo.UserRepository;
import com.aurionpro.repo.VerificationTokenRepository;
import com.aurionpro.security.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImplementation implements AuthService {
	private final UserRepository userRepository;
	private final OrganizationRepository organizationRepository;

	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;
	private final VerificationTokenRepository tokenRepo;
	private final ModelMapper modelMapper;
	private final JwtService jwtService;

	@Override
	public OrgRegisterResponseDto registerOrganization(OrgRegisterRequestDto req) {
		if (userRepository.findByEmail(req.getEmail()).isPresent()) {
			throw new DuplicateResourceException("Email '" + req.getEmail() + "' is already registered.");
		}

		if (organizationRepository.findByRegistrationNo(req.getRegistrationNo()).isPresent()) {
			throw new DuplicateResourceException(
					"Organization registration number '" + req.getRegistrationNo() + "' is already in use.");
		}
		OrganizationEntity org = modelMapper.map(req, OrganizationEntity.class);
		org.setStatus("PENDING");

		UserEntity user = new UserEntity();
		user.setUsername(req.getUsername());
		user.setEmail(req.getEmail());
		user.setPassword(passwordEncoder.encode(req.getPassword()));
		user.setStatus("PENDING");

		UserRoleEntity role = new UserRoleEntity();
		role.setRole("ORG_ADMIN");
		role.setUser(user);
		user.getRoles().add(role);

		userRepository.save(user);

		org.setUser(user);
		organizationRepository.save(org);

		// Generate verification token
		String token = UUID.randomUUID().toString();
		VerificationTokenEntity vToken = new VerificationTokenEntity(token, user);
		tokenRepo.save(vToken);

		// Send verification email
		try {
			emailService.sendVerificationEmail(user.getEmail(), token);
		} catch (Exception e) {
			throw new InvalidOperationException(
					"Organization registered but failed to send verification email. Please try again later.");
		}
		OrgRegisterResponseDto response = modelMapper.map(org, OrgRegisterResponseDto.class);
		response.setEmail(user.getEmail());
		response.setMessage("Organization registered successfully. Please verify your email.");

		return response;
	}

	@Override
	public LoginResponseDto loginOrganization(LoginRequestDto req) {
		UserEntity user = userRepository.findByUsername(req.getUserName())
				.orElseThrow(() -> new NotFoundException("No account found with username: " + req.getUserName()));

		if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
			throw new InvalidOperationException("Please verify your email before logging in.");
		}

		if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
			throw new InvalidOperationException("Invalid username or password.");
		}

		// Collect all roles
		Set<String> roleNames = user.getRoles().stream().map(UserRoleEntity::getRole).collect(Collectors.toSet());

		// Generate JWT with roles
		String jwt = jwtService
				.generateToken(
						org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
								.password(user.getPassword()).authorities(roleNames.toArray(new String[0])).build(),
						roleNames);

		// Get organization status (if applicable)
		String orgStatus = (user.getOrganization() != null) ? user.getOrganization().getStatus() : "N/A";

		return LoginResponseDto.builder().token(jwt).userId(user.getUserId()).email(user.getEmail()).roles(roleNames)
				.orgStatus(orgStatus).message("Login successful.").build();
	}

}
