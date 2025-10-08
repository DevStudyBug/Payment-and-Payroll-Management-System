package com.aurionpro.serviceImplementation;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.aurionpro.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImplementation implements EmailService {
	private final JavaMailSender mailSender;
	private static final String FROM_EMAIL = "nihalsingh9363@gmail.com";

	@Override
	public void sendVerificationEmail(String toEmail, String token) {
		String subject = "Verify Your Email - Payment & Payroll Portal";
		String verifyUrl = "http://localhost:8080/api/v1/auth/verify-email?token=" + token;

		String body = """
				Dear Organization,

				Thank you for registering on our Portal.
				Please click the link below to verify your email address:

				%s

				This link will expire in 24 hours.

				Regards,
				Payment & Payroll Team
				""".formatted(verifyUrl);

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(FROM_EMAIL);
		message.setTo(toEmail);
		message.setSubject(subject);
		message.setText(body);

		mailSender.send(message);
	}

	public void sendGenericEmail(String toEmail, String subject, String body) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(FROM_EMAIL);
		message.setTo(toEmail);
		message.setSubject(subject);
		message.setText(body);
		mailSender.send(message);
	}

}
