package com.aurionpro.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeDetailResponseDto {
	private Long employeeId;
	private String name;
	private String email;
	private String username;
	private String department;
	private String designation;
	private LocalDate dateOfBirth;
	private String status;
	private List<DocumentReviewDto> documents;
	private BankReviewDto bankDetails;
}
