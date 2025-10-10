package com.aurionpro.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRegisterResponseDto {
	private Long employeeId;
	private String username;
	private String temporaryPassword;
	private String status;
}
