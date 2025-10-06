package com.aurionpro.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	// Service responsible for parsing/validating JWTs
	private final JwtService jwtService;
	// Spring's UserDetailsService to load user info (authorities, account status)
	private final UserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// Read the Authorization header from the incoming HTTP request
		final String authHeader = request.getHeader("Authorization");

		// If header exists and starts with "Bearer ", proceed to extract token
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			// Remove the "Bearer " prefix to get the raw JWT
			final String jwtToken = authHeader.substring(7);

			// Extract username from the token (this may throw JwtValidationException
			// which is expected to be handled by a global exception handler)
			final String userName = jwtService.extractUserName(jwtToken);

			// If we successfully extracted a username and no authentication is present yet
			if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				// Load UserDetails
				UserDetails userDetails = this.userDetailsService.loadUserByUsername(userName);

				// Validate the token against the loaded user (checks subject, expiration,
				// signature, etc.)

				if (jwtService.isTokenValid(jwtToken, userDetails)) {

					// When your JwtAuthenticationFilter successfully validates a JWT token, you
					// want to tell Spring Security:
					// “This user is authenticated, here are their details and roles.”
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
							null, // credentials (not needed here)
							userDetails.getAuthorities() // authorities/roles
					);

					// Stores the authentication in the SecurityContext
					// Makes Spring Security treat the user as logged in for this request
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
				

			}

		}
		filterChain.doFilter(request, response);
	}
}
