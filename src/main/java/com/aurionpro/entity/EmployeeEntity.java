package com.aurionpro.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employees")
public class EmployeeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long employeeId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId", nullable = false, unique = true)
	private UserEntity user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orgId", nullable = false)
	private OrganizationEntity organization;

	@NotBlank(message = "First name is required.")
	@Column(name = "first_name", nullable = false)
	private String firstName;

	@NotBlank(message = "Last name is required.")
	@Column(name = "last_name", nullable = false)
	private String lastName;
	@Past
	private LocalDate dob;

	@NotBlank
	private String department;

	@NotBlank
	private String designation;

	@NotBlank
	private String status;// NEW, UNDER_REVIEW, REUPLOAD_REQUIRED, VERIFIED

	@OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
	private List<DocumentEntity> documents = new ArrayList<>();

	@OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private EmployeeBankDetailsEntity bankDetails;

	@OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private EmployeeSalaryEntity employeeSalary;

	@OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
	private List<SalaryDisbursementEntity> disbursements = new ArrayList<>();

	@OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
	private List<ConcernEntity> concerns = new ArrayList<>();
}
