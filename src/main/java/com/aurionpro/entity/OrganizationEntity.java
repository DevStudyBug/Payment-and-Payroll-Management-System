package com.aurionpro.entity;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

@Table(name = "organizations", uniqueConstraints = { @UniqueConstraint(columnNames = { "registrationNo" }),
		@UniqueConstraint(columnNames = { "email" }), @UniqueConstraint(columnNames = { "orgName" }) })
public class OrganizationEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orgId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId", nullable = false, unique = true)
	private UserEntity user;

	@NotBlank
	@Column(nullable = false)
	private String orgName;

	@NotBlank
	@Column(nullable = false)
	private String registrationNo;

	@NotBlank
	@Column(nullable = false)
	private String address;

	@NotBlank
	@Column(nullable = false)
	private String contactNo;

	private boolean documentUploaded = false;
	private boolean bankDetailsProvided = false;
	@Email
	private String email;

	@NotBlank
	private String status; // PENDING / VERIFIED / REJECTED

	@OneToOne(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private OrganizationBankAccountEntity bankAccount;

	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
	private List<DocumentEntity> documents = new ArrayList<>();

	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
	private List<EmployeeEntity> employees = new ArrayList<>();

	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
	private List<VendorEntity> vendors = new ArrayList<>();

	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SalaryTemplateEntity> salaryTemplates = new ArrayList<>();

	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
	private List<SalaryDisbursementEntity> disbursements = new ArrayList<>();

	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
	private List<PaymentRequestEntity> paymentRequests = new ArrayList<>();

	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ConcernEntity> concerns = new ArrayList<>();

}
