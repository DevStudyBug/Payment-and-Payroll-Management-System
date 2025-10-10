package com.aurionpro.service;

import java.time.LocalDateTime;

import com.aurionpro.entity.EmployeeEntity;

public interface EmailService {
	public void sendVerificationEmail(String toEmail, String token);

	public void sendVerificationEmail(String toEmail, String username, String tempPassword, String verificationLink);

//	void sendEmail(String to, String subject, String body);

	public void sendDocumentStatusEmail(EmployeeEntity employee, String documentType, String status, String reason);

	public void sendBankStatusEmail(EmployeeEntity employee, String status, String reason);

	public void sendEmployeeActivationEmail(EmployeeEntity employee);

	public void sendPayrollApprovalEmail(String toEmail, Long payrollId, String description);

	public void sendPayrollRejectionEmail(String toEmail, Long payrollId, String reason);

	public void sendPayrollDisbursedEmail(String toEmail, String orgName, Long payrollId, double totalAmount,
			String status, LocalDateTime approvalDate);

	public void sendSalarySlipEmail(EmployeeEntity employee, double netSalary, byte[] pdf, String month);

}
