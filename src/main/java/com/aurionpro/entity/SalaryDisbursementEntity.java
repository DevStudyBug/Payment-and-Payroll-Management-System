package com.aurionpro.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "salary_disbursements")
public class SalaryDisbursementEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long disbursementId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orgId", nullable = false)
	private OrganizationEntity organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employeeId", nullable = false)
	private EmployeeEntity employee;

	@NotBlank
	@Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$")
	private String salaryMonth;

	@PositiveOrZero
	private Double netSalary;

	@NotBlank
	private String status; // "GENERATED", "UNDER_REVIEW", "APPROVED", "PAID", "REJECTED"

	private LocalDateTime transactionDate;

	private String paymentRefNo;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "paymentRequestId")
	private PaymentRequestEntity paymentRequest;

}
