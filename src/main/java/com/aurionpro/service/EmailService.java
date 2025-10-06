package com.aurionpro.service;

public interface EmailService {
	public void sendVerificationEmail(String toEmail, String token);
}
