package com.aurionpro.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aurionpro.repo.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrgOnboardingEnforcementFilter extends OncePerRequestFilter {

	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		String path = req.getRequestURI();

		// Allow authentication and onboarding-related endpoints to pass through
		if (path.startsWith("/api/auth") || path.contains("/upload-document") || path.contains("/add-bank-details")
				|| path.contains("/test-upload") || path.contains("/onboarding-status")||path.contains("/documents/**")||path.contains("/bank/reupload") ) {
			chain.doFilter(req, res);
			return;
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth != null && auth.isAuthenticated()) {
			String username = auth.getName();
			var user = userRepository.findByUsername(username).orElse(null);

			if (user != null && user.getOrganization() != null) {
				String status = user.getOrganization().getStatus();

				if ("UNDER_REVIEW".equalsIgnoreCase(status)) {
					res.setStatus(HttpServletResponse.SC_FORBIDDEN);
					res.setContentType("application/json");
					res.getWriter().write(
							"{\"error\":\"Your documents and bank details are under review. Please wait for bank admin approval.\"}");
					return;
				}

				if (!"ACTIVE".equalsIgnoreCase(status)) {
					res.setStatus(HttpServletResponse.SC_FORBIDDEN);
					res.setContentType("application/json");
					res.getWriter()
							.write("{\"error\":\"Please complete onboarding and wait for bank admin approval.\"}");
					return;
				}
			}
		}

		chain.doFilter(req, res);
	}
}
