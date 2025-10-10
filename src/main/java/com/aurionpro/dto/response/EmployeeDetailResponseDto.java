package com.aurionpro.dto.response;

import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeDetailResponseDto {
	private Long employeeId;
	private String name;

	private String status;
	private List<DocumentReviewDto> documents;
	private BankReviewDto bankDetails;
}
