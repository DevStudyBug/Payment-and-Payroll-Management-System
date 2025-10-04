package com.aurionpro.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_bank_details")
public class EmployeeBankDetailsEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long bankDetailsId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employeeId", nullable = false, unique = true)
	private EmployeeEntity employee;

	@NotBlank
	private String accountNumber;

	@NotBlank
	private String ifscCode;

	@NotBlank
	private String bankName;

	

}
