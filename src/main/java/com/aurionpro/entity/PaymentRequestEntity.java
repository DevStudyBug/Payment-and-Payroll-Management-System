package com.aurionpro.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.aurionpro.constants.PaymentRequestType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_requests")
public class PaymentRequestEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orgId", nullable = false)
	private OrganizationEntity organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendorId", nullable = true)
	private VendorEntity vendor;

	@Positive
	private Double amount;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentRequestType requestType;
	// VENDOR or PAYROLL

	@NotBlank
	private String status;
	// PENDING, APPROVED, REJECTED, PAID

	private LocalDateTime requestDate;
	private LocalDateTime approvalDate;
	private String paymentRefNo;

	@OneToMany(mappedBy = "paymentRequest", cascade = CascadeType.ALL)
	private List<SalaryDisbursementEntity> disbursements = new ArrayList<>();
}
