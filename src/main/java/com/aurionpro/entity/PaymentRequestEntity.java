package com.aurionpro.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
	@JoinColumn(name = "vendorId", nullable = false)
	private VendorEntity vendor;

	@Positive
	private Double amount;

	@Column(columnDefinition = "TEXT")
	private String description;

	@NotBlank
	private String status;

	private LocalDateTime requestDate;
	private LocalDateTime approvalDate;
	private String paymentRefNo;
}
