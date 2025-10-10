package com.aurionpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class EmployeeBankDetailsResponseDto {
	private Long employeeId;
	private String accountHolderName;
	private String bankName;
	private String verificationStatus;
	private String message;
}