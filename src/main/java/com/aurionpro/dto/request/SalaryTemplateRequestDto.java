package com.aurionpro.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryTemplateRequestDto {

	@NotBlank(message = "Designation is required.")
	private String designation;

	@NotNull(message = "Basic salary is required.")
	@DecimalMin(value = "0.0", message = "Basic salary must be non-negative.")
	private BigDecimal basicSalary;

	@NotNull(message = "HRA is required.")
	@DecimalMin(value = "0.0", message = "HRA must be non-negative.")
	private BigDecimal hra;

	@NotNull(message = "DA is required.")
	@DecimalMin(value = "0.0", message = "DA must be non-negative.")
	private BigDecimal da;

	@NotNull(message = "PF is required.")
	@DecimalMin(value = "0.0", message = "PF must be non-negative.")
	private BigDecimal pf;

	@NotNull(message = "Other allowances are required.")
	@DecimalMin(value = "0.0", message = "Other allowances must be non-negative.")
	private BigDecimal otherAllowances;
}
