package com.aurionpro.serviceImplementation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.aurionpro.entity.EmployeeEntity;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.service.EmailService;

import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImplementation implements EmailService {

	private final JavaMailSender mailSender;
	private static final String FROM_EMAIL = "nihalsingh9363@gmail.com";

	@Override
	@Async("emailTaskExecutor")
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

	@Override
	public void sendBankAdminVerificationEmail(OrganizationEntity org) {
		String subject = "Organization Verified - Payment & Payroll Portal";
		String body = """
				Dear %s,

				Congratulations! Your organization "%s" has been successfully verified by the bank admin.

				You can now access all features of the Payment & Payroll Portal.

				Regards,
				Payment & Payroll Team
				""".formatted(org.getOrgName(), org.getOrgName());

		sendGenericEmail(org.getEmail(), subject, body);
	}

	@Override
	@Async("emailTaskExecutor")
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
	@Async("emailTaskExecutor")
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
	@Async("emailTaskExecutor")
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
	@Async("emailTaskExecutor")
	public void sendEmployeeActivationEmail(EmployeeEntity employee) {
		String subject = "🎉 Onboarding Complete: Welcome Aboard!";
		String body = String.format("Dear %s,\n\n"
				+ "Congratulations! Your onboarding process has been successfully completed.\n\n"
				+ "Your account is now active, and you can access all employee features, including payroll and attendance.\n\n"
				+ "We’re thrilled to have you on the team!\n\n" + "Warm regards,\nHR Team", employee.getFirstName());

		sendGenericEmail(employee.getUser().getEmail(), subject, body);
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendPayrollApprovalEmail(String toEmail, Long payrollId, String description) {
		String subject = "✅ Payroll Approved - ID " + payrollId;
		String body = """
				Dear Organization,

				Your payroll request (ID: %d) has been successfully reviewed and approved.

				Description:
				%s

				Your payroll is now ready for disbursement. Once disbursed, employees will receive their salary slips automatically.

				Thank you for your cooperation.

				Best regards,
				Bank Admin Team
				"""
				.formatted(payrollId, description);

		sendGenericEmail(toEmail, subject, body);
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendPayrollRejectionEmail(String toEmail, Long payrollId, String reason) {
		String subject = "⚠️ Payroll Rejected - ID " + payrollId;
		String body = """
				Dear Organization,

				Your payroll request (ID: %d) has been rejected.

				Reason:
				%s

				Please review the remarks and resubmit once corrections are made.

				Regards,
				Bank Admin Team
				""".formatted(payrollId, reason);

		sendGenericEmail(toEmail, subject, body);
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendPayrollDisbursedEmail(String toEmail, String orgName, Long payrollId, double totalAmount,
			String status, LocalDateTime approvalDate) {

		String subject = "💰 Payroll Disbursed Successfully - ID " + payrollId;

		String body = String.format(
				"""
						Dear %s Team,

						We're pleased to inform you that your payroll request (ID: %d) has been successfully disbursed.

						📅 Disbursement Date: %s
						💰 Total Amount: ₹%.2f
						🧾 Status: %s

						Salary slips have been generated and emailed to all respective employees automatically.

						You can view payroll details, transaction records, and disbursement history anytime in your organization dashboard.

						If you notice any discrepancies or missing payments, please contact the payroll support team immediately.

						Best regards,
						AurionPro Payroll & Banking Team
						""",
				orgName, payrollId, approvalDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")), totalAmount,
				status);

		try {
			sendGenericEmail(toEmail, subject, body);
			System.out.println("✅ Payroll email sent to: " + toEmail);
		} catch (Exception e) {
			System.err.println("❌ Failed to send payroll email to " + toEmail + ": " + e.getMessage());
		}
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendSalarySlipEmail(EmployeeEntity employee, double netSalary, byte[] pdf, String month) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setFrom(FROM_EMAIL);
			helper.setTo(employee.getUser().getEmail());
			helper.setSubject("💼 Salary Slip - " + month);

			helper.setText(String.format("""
					Dear %s,

					Your salary for %s has been successfully credited to your registered bank account.

					Please find attached your official salary slip for the month.

					💰 Net Salary: ₹%.2f

					For any queries, kindly contact HR.

					Regards,
					Payroll Department
					""", employee.getFirstName(), month, netSalary));

			DataSource dataSource = new ByteArrayDataSource(pdf, "application/pdf");
			helper.addAttachment("SalarySlip_" + month + ".pdf", dataSource);

			mailSender.send(mimeMessage);

			System.out.println("✅ Salary slip sent to: " + employee.getUser().getEmail());

		} catch (Exception e) {
			System.err.println(
					"⚠️ Failed to send salary slip email to " + employee.getUser().getEmail() + ": " + e.getMessage());
		}
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendConcernSubmissionEmail(EmployeeEntity employee, String ticketNumber, String category,
			String priority, String description) {
		String subject = "📝 Concern Submitted - Ticket #" + ticketNumber;
		String body = String.format("""
				Dear %s,

				Your concern has been successfully submitted.

				📋 Ticket ID: %s
				🏷 Category: %s
				⚡ Priority: %s

				Concern Description:
				%s

				Our HR team will review your concern and get back to you soon.

				Regards,
				AurionPro HR Support
				""", employee.getFirstName(), ticketNumber, category, priority, description);

		sendGenericEmail(employee.getUser().getEmail(), subject, body);
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendConcernNotificationToHR(String hrEmail, EmployeeEntity employee, String ticketNumber,
			String category, String priority, String description) {
		String subject = "📨 New Concern Raised - Ticket #" + ticketNumber;
		String body = String.format("""
				Hello HR Team,

				A new concern has been raised by an employee.

				👤 Employee: %s %s
				🧾 Ticket: %s
				🏷 Category: %s
				⚡ Priority: %s

				Description:
				%s

				Please review and respond in the Admin Concern Dashboard.

				Regards,
				AurionPro System
				""", employee.getFirstName(), employee.getLastName(), ticketNumber, category, priority, description);

		sendGenericEmail(hrEmail, subject, body);
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendConcernStatusUpdateEmail(EmployeeEntity employee, String ticketNumber, String newStatus,
			String adminResponse) {
		String subject = "🔔 Concern Status Update - Ticket #" + ticketNumber;
		String body = String.format("""
				Dear %s,

				Your concern (Ticket ID: %s) status has been updated.

				🧾 New Status: %s
				💬 Admin Response: %s

				You can view full details in your Employee Portal under 'My Concerns'.

				Regards,
				AurionPro HR Team
				""", employee.getFirstName(), ticketNumber, newStatus,
				adminResponse != null ? adminResponse : "No additional comments provided.");

		sendGenericEmail(employee.getUser().getEmail(), subject, body);
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendBankDetailsVerifiedEmail(OrganizationEntity org) {
		String subject = "Bank Details Verified - Payment & Payroll Portal";
		String body = """
				Dear %s,

				Your bank details have been successfully verified by the bank admin.

				Regards,
				Payment & Payroll Team
				""".formatted(org.getOrgName());

		sendGenericEmail(org.getEmail(), subject, body);
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendBankAdminRejectionEmail(OrganizationEntity org, String remarks) {
		String subject = "Organization Rejected - Payment & Payroll Portal";
		String body = """
				Dear %s,

				We regret to inform you that your organization "%s" has been rejected by the bank admin.

				Reason for rejection:
				%s

				Regards,
				Payment & Payroll Team
				""".formatted(org.getOrgName(), org.getOrgName(), remarks);

		sendGenericEmail(org.getEmail(), subject, body);
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendDocumentVerifiedEmail(OrganizationEntity org, String fileName) {
		String subject = "Document Verified - Payment & Payroll Portal";
		String body = """
				Dear %s,

				Your document "%s" has been verified successfully by the bank admin.

				Regards,
				Payment & Payroll Team
				""".formatted(org.getOrgName(), fileName);

		sendGenericEmail(org.getEmail(), subject, body);
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendBankDetailsRejectedEmail(OrganizationEntity org, String reason) {
		String subject = "Bank Details Rejected - Payment & Payroll Portal";
		String body = """
				Dear %s,

				Your bank details have been rejected by the bank admin.

				Reason:
				%s

				Please update the bank details and submit again for verification.

				Regards,
				Payment & Payroll Team
				""".formatted(org.getOrgName(), reason);

		sendGenericEmail(org.getEmail(), subject, body);
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendDocumentRejectedEmail(OrganizationEntity org, String fileName, String reason) {
		String subject = "Document Rejected - Payment & Payroll Portal";
		String body = """
				Dear %s,

				Your document "%s" has been rejected by the bank admin.

				Reason:
				%s

				Please re-upload the corrected document for verification.

				Regards,
				Payment & Payroll Team
				""".formatted(org.getOrgName(), fileName, reason);

		sendGenericEmail(org.getEmail(), subject, body);
	}

	@Override
	@Async("emailTaskExecutor")
	public void sendConcernReopenedNotification(String email, EmployeeEntity emp, String ticketNumber, String reason) {
		String subject = "Concern Reopened - Ticket " + ticketNumber;
		String body = String.format("""
				Dear HR Team,

				The concern #%s raised by %s has been reopened by the employee.

				Reason: %s

				Please review the issue again.

				Regards,
				Payroll & Concern Management System
				""", ticketNumber, emp.getFirstName() + " " + emp.getLastName(), reason);

		sendGenericEmail(email, subject, body);

	}

	@Override
	@Async("emailTaskExecutor")
	public void sendConcernClosedNotification(String email, EmployeeEntity emp, String ticketNumber) {
		String subject = "Concern Closed - Ticket " + ticketNumber;
		String body = String.format("""
				Dear HR Team,

				The concern #%s raised by %s has been marked as CLOSED by the employee.

				This issue is now finalized in the system.

				Regards,
				Payroll & Concern Management System
				""", ticketNumber, emp.getFirstName() + " " + emp.getLastName());

		sendGenericEmail(email, subject, body);

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
