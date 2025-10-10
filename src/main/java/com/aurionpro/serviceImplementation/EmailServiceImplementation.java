package com.aurionpro.serviceImplementation;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.aurionpro.entity.EmployeeEntity;
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

				Thank you for registering on our portal.
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

	public void sendVerificationEmail(String toEmail, String username, String tempPassword, String verificationLink) {
		String subject = "Verify Your Account - Employee Portal";
		String body = """
				Dear Employee,

				Welcome aboard! Please verify your email to activate your account.

				Username: %s
				Temporary Password: %s

				Click the link below to verify your account:
				%s

				Regards,
				AurionPro HR Team
				""".formatted(username, tempPassword, verificationLink);

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(FROM_EMAIL);
		message.setTo(toEmail);
		message.setSubject(subject);
		message.setText(body);
		mailSender.send(message);
	}

	// DOCUMENT STATUS EMAILS (APPROVED / REJECTED)

	@Override
	public void sendDocumentStatusEmail(EmployeeEntity employee, String documentType, String status, String reason) {
		String subject;
		String body;

		if ("REJECTED".equalsIgnoreCase(status)) {
			subject = "Action Required: " + documentType + " Rejected";
			body = String.format(
					"Dear %s,\n\n" + "Your uploaded document (%s) has been rejected during HR verification.\n\n"
							+ "Reason: %s\n\n"
							+ "Please log in to your onboarding portal and re-upload the corrected document.\n\n"
							+ "Thank you,\nHR Team",
					employee.getFirstName(), documentType, reason != null ? reason : "Not specified");
		} else {
			subject = "Document Approved: " + documentType;
			body = String.format(
					"Dear %s,\n\n" + "Good news! Your uploaded document (%s) has been approved by HR.\n\n"
							+ "You can continue your onboarding process normally.\n\n" + "Thank you,\nHR Team",
					employee.getFirstName(), documentType);
		}

		sendGenericEmail(employee.getUser().getEmail(), subject, body);
	}

	// BANK STATUS EMAILS (APPROVED / REJECTED / UNDER REVIEW)

	@Override
	public void sendBankStatusEmail(EmployeeEntity employee, String status, String reason) {
		String subject;
		String body;

		if ("REJECTED".equalsIgnoreCase(status)) {
			subject = "Action Required: Bank Details Rejected";
			body = String.format(
					"Dear %s,\n\n" + "Your submitted bank details were rejected during HR verification.\n\n"
							+ "Reason: %s\n\n" + "Please review and update your bank information in the portal.\n\n"
							+ "Thank you,\nHR Team",
					employee.getFirstName(), reason != null ? reason : "Not specified");
		} else if ("APPROVED".equalsIgnoreCase(status)) {
			subject = "Bank Details Approved";
			body = String.format(
					"Dear %s,\n\n" + "Your bank details have been approved successfully.\n\n"
							+ "You are one step closer to completing onboarding.\n\n" + "Thank you,\nHR Team",
					employee.getFirstName());
		} else if ("UNDER_REVIEW".equalsIgnoreCase(status)) {
			subject = "Bank Details Under Review";
			body = String.format("Dear %s,\n\n"
					+ "Your bank details have been submitted successfully and are now under review by HR.\n\n"
					+ "You will be notified once they are approved or if any changes are required.\n\n"
					+ "Thank you,\nHR Team", employee.getFirstName());
		} else {
			return; // Skip unknown status
		}

		sendGenericEmail(employee.getUser().getEmail(), subject, body);
	}

	// EMPLOYEE ACTIVATION EMAIL

	@Override
	public void sendEmployeeActivationEmail(EmployeeEntity employee) {
		String subject = "🎉 Onboarding Complete: Welcome Aboard!";
		String body = String.format("Dear %s,\n\n"
				+ "Congratulations! Your onboarding process has been successfully completed.\n\n"
				+ "Your account is now active, and you can access all employee features, including payroll and attendance.\n\n"
				+ "We’re thrilled to have you on the team!\n\n" + "Warm regards,\nHR Team", employee.getFirstName());

		sendGenericEmail(employee.getUser().getEmail(), subject, body);
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
