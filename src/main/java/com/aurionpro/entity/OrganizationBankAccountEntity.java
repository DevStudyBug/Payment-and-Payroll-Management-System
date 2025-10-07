package com.aurionpro.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
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
@Table(name = "organization_bank_accounts")
public class OrganizationBankAccountEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "account_id")
	private Long accountId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", nullable = false, unique = true)
	private OrganizationEntity organization;

	@NotBlank(message = "Account holder name is required.")
	@Column(name = "account_holder_name", nullable = false, length = 150)
	private String accountHolderName;

	@NotBlank(message = "Account number is required.")
	@Pattern(regexp = "^[0-9]{9,18}$", message = "Account number must be between 9 and 18 digits.")
	@Column(name = "account_number", nullable = false, unique = true, length = 20)
	private String accountNumber;

	@NotBlank(message = "IFSC code is required.")
	@Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format.")
	@Column(name = "ifsc_code", nullable = false, length = 15)
	private String ifscCode;

	@NotBlank(message = "Bank name is required.")
	@Column(name = "bank_name", nullable = false, length = 100)
	private String bankName;

	@PositiveOrZero(message = "Balance cannot be negative.")
	@Column(name = "balance", nullable = false)
	private Double balance = 0.0;

	@Column(name = "verification_status", nullable = false, length = 20)
	private String verificationStatus = "PENDING"; // PENDING, APPROVED, REJECTED

	@Column(name = "verified_by", length = 100)
	private String verifiedBy; // Bank admin email

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "verified_at")
	private LocalDateTime verifiedAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}
