package com.aurionpro.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aurionpro.entity.EmployeeEntity;
import com.aurionpro.entity.UserEntity;
import com.aurionpro.entity.UserRoleEntity;
import com.aurionpro.repo.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OnboardingEnforcementFilter extends OncePerRequestFilter {

	private final UserRepository userRepository;

	private static final List<String> PUBLIC_PATHS = Arrays.asList("/api/v1/auth", "/test-upload", "/swagger-ui");

	// Employee onboarding paths
	private static final List<String> EMPLOYEE_ONBOARDING_PATHS = Arrays.asList("/api/v1/employee/document/uploads",
			"/api/v1/employee/upload-documents", "/api/v1/employee/add-bank-details",
			"/api/v1/employee/update-bank-details", "/api/v1/employee/reupload/bank-details",
			"/api/v1/employee/reupload-document", "/api/v1/employee/reupload/rejected-items",
			"/api/v1/employee/onboarding-status", "/api/v1/employee/profile", "/api/v1/employee/bank-details");

	// Organization onboarding paths
	private static final List<String> ORG_ONBOARDING_PATHS = Arrays.asList("/api/v1/org/upload-documents",
			"/api/v1/org/add-bank-details", "/api/v1/org/update-bank-details", "/api/v1/org/reupload/bank-details",
			"/api/v1/org/reupload/document", "/api/v1/org/onboarding-status", "/api/v1/org/profile",
			"/api/v1/org/documents", "/api/v1/org/bank-details");

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		String path = req.getRequestURI();
		String method = req.getMethod();

		log.debug("OnboardingEnforcementFilter: Processing {} {}", method, path);

		// Skip filter for public paths
		if (shouldSkipFilter(path)) {
			log.debug("OnboardingEnforcementFilter: Skipping filter for public path: {}", path);
			chain.doFilter(req, res);
			return;
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
			String username = auth.getName();
			UserEntity user = userRepository.findByUsername(username).orElse(null);

			if (user != null) {
				// Check if user has ORG_ADMIN role
				boolean isOrgAdmin = user.getRoles().stream().map(UserRoleEntity::getRole)
						.anyMatch(role -> "ORG_ADMIN".equalsIgnoreCase(role));

				// Check if user has EMPLOYEE role
				boolean isEmployee = user.getRoles().stream().map(UserRoleEntity::getRole)
						.anyMatch(role -> "EMPLOYEE".equalsIgnoreCase(role));

				// Organization Admin Enforcement
				if (isOrgAdmin) {
					if (isOrgOnboardingPath(path)) {
						log.debug("OnboardingEnforcementFilter: Allowing org onboarding path: {}", path);
						chain.doFilter(req, res);
						return;
					}

					if (!enforceOrganizationStatus(user, res)) {
						return;
					}
				}

				// Employee Enforcement
				if (isEmployee) {
					if (isEmployeeOnboardingPath(path)) {
						log.debug("OnboardingEnforcementFilter: Allowing employee onboarding path: {}", path);
						chain.doFilter(req, res);
						return;
					}

					if (!enforceEmployeeStatus(user, res)) {
						return;
					}
				}
			}
		}

		chain.doFilter(req, res);
	}

	private boolean enforceOrganizationStatus(UserEntity user, HttpServletResponse res) throws IOException {
		if (user.getOrganization() == null) {
			log.warn("OnboardingEnforcementFilter: ORG_ADMIN user {} has no organization", user.getUsername());
			sendErrorResponse(res, HttpServletResponse.SC_FORBIDDEN, "Organization not found. Please contact support.");
			return false;
		}

		String orgStatus = user.getOrganization().getStatus();
		log.debug("OnboardingEnforcementFilter: Organization status for {}: {}", user.getUsername(), orgStatus);

		switch (orgStatus.toUpperCase()) {
		case "PENDING":
			sendErrorResponse(res, HttpServletResponse.SC_FORBIDDEN,
					"Please complete organization onboarding by uploading documents and bank details.");
			return false;

		case "DOCUMENTS_UPLOADED":
			sendErrorResponse(res, HttpServletResponse.SC_FORBIDDEN,
					"Please complete bank details submission to proceed.");
			return false;

		case "UNDER_REVIEW":
			sendErrorResponse(res, HttpServletResponse.SC_FORBIDDEN,
					"Your organization's documents and bank details are under review. Please wait for bank admin approval.");
			return false;

		case "REJECTED":
			sendErrorResponse(res, HttpServletResponse.SC_FORBIDDEN,
					"Your organization's onboarding was rejected. Please re-upload the required documents.");
			return false;

		case "ACTIVE":
			log.debug("OnboardingEnforcementFilter: Organization is ACTIVE, allowing access");
			return true;

		default:
			log.warn("OnboardingEnforcementFilter: Unknown organization status: {}", orgStatus);
			sendErrorResponse(res, HttpServletResponse.SC_FORBIDDEN,
					"Unknown organization status. Please contact support.");
			return false;
		}
	}

	private boolean enforceEmployeeStatus(UserEntity user, HttpServletResponse res) throws IOException {
		if (user.getEmployee() == null) {
			log.warn("OnboardingEnforcementFilter: EMPLOYEE user {} has no employee record", user.getUsername());
			sendErrorResponse(res, HttpServletResponse.SC_FORBIDDEN, "Employee record not found. Please contact HR.");
			return false;
		}

		EmployeeEntity employee = user.getEmployee();
		String empStatus = employee.getStatus();
		log.debug("OnboardingEnforcementFilter: Employee status for {}: {}", user.getUsername(), empStatus);

		switch (empStatus.toUpperCase()) {
		case "PENDING":
			sendErrorResponse(res, HttpServletResponse.SC_FORBIDDEN,
					"Please complete your profile by uploading required documents and bank details.");
			return false;

		case "DOCUMENTS_UPLOADED":
			sendErrorResponse(res, HttpServletResponse.SC_FORBIDDEN,
					"Please complete bank details submission to proceed.");
			return false;

		case "BANK_DETAILS_ADDED":
			sendErrorResponse(res, HttpServletResponse.SC_FORBIDDEN,
					"Your documents and bank details have been submitted. Waiting for HR to review and approve.");
			return false;

		case "UNDER_REVIEW":
			sendErrorResponse(res, HttpServletResponse.SC_FORBIDDEN,
					"Your documents and bank details are under review by HR. Please wait for approval.");
			return false;

		case "REJECTED":
			sendErrorResponse(res, HttpServletResponse.SC_FORBIDDEN,
					"Your onboarding was rejected. Please check your onboarding status and re-upload rejected items.");
			return false;

		case "ACTIVE":
			log.debug("OnboardingEnforcementFilter: Employee is ACTIVE, allowing access");
			return true;

		default:
			log.warn("OnboardingEnforcementFilter: Unknown employee status: {}", empStatus);
			sendErrorResponse(res, HttpServletResponse.SC_FORBIDDEN,
					"Your employee account is not active. Please contact HR for assistance.");
			return false;
		}
	}

	private boolean shouldSkipFilter(String path) {
		// Check public paths
		for (String publicPath : PUBLIC_PATHS) {
			if (path.startsWith(publicPath)) {
				return true;
			}
		}
		return false;
	}

	private boolean isEmployeeOnboardingPath(String path) {
		for (String onboardingPath : EMPLOYEE_ONBOARDING_PATHS) {
			// Exact match or starts with the path
			if (path.equals(onboardingPath) || path.startsWith(onboardingPath + "/")) {
				return true;
			}
		}
		return false;
	}

	private boolean isOrgOnboardingPath(String path) {
		for (String onboardingPath : ORG_ONBOARDING_PATHS) {
			// Exact match or starts with the path
			if (path.equals(onboardingPath) || path.startsWith(onboardingPath + "/")) {
				return true;
			}
		}
		return false;
	}

	private boolean matchesPattern(String path, String pattern) {

		String regex = pattern.replaceAll("\\{[^}]+\\}", "[^/]+");
		return Pattern.matches(regex, path);
	}

	private void sendErrorResponse(HttpServletResponse res, int statusCode, String message) throws IOException {
		res.setStatus(statusCode);
		res.setContentType("application/json");
		res.setCharacterEncoding("UTF-8");

		String jsonResponse = String.format("{\"error\":\"%s\",\"status\":%d,\"timestamp\":\"%s\"}",
				message.replace("\"", "\\\""), statusCode, java.time.LocalDateTime.now().toString());

		res.getWriter().write(jsonResponse);
		log.info("OnboardingEnforcementFilter: Blocked request - Status: {}, Message: {}", statusCode, message);
	}
}