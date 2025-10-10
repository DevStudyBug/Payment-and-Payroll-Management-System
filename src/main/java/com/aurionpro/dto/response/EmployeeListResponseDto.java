package com.aurionpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeListResponseDto {
	private Long employeeId;
	private String employeeName;
	private String email;
	private String status;
	private boolean allDocumentsApproved;
	private boolean bankApproved;
	private int documentCompletion;

}
