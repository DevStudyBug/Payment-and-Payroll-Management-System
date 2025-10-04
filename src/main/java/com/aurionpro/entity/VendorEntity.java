package com.aurionpro.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

@Table(name = "vendors", uniqueConstraints = { @UniqueConstraint(columnNames = { "email" }) })
public class VendorEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long vendorId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orgId", nullable = false)
	private OrganizationEntity organization;

	@NotBlank
	private String name;

	@NotBlank
	private String type; // CLIENT / VENDOR

	private String bankAccountNo;
	private String ifscCode;
	private String contactNo;

	@Email
	private String email;

	@OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL)
	private List<PaymentRequestEntity> paymentRequests = new ArrayList<>();
}
