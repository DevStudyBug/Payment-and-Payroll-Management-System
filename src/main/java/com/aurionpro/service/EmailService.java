package com.aurionpro.service;

import com.aurionpro.entity.EmployeeEntity;

public interface EmailService {
	public void sendVerificationEmail(String toEmail, String token);
	public void sendVerificationEmail(String toEmail, String username, String tempPassword, String verificationLink);
	
//	void sendEmail(String to, String subject, String body);

    void sendDocumentStatusEmail(EmployeeEntity employee, String documentType, String status, String reason);

    void sendBankStatusEmail(EmployeeEntity employee, String status, String reason);

    void sendEmployeeActivationEmail(EmployeeEntity employee);
}
