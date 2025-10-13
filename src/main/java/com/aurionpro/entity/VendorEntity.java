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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "vendors", uniqueConstraints = { @UniqueConstraint(columnNames = { "email" }) })
public class VendorEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long vendorId;

	@Column(nullable = false)
	private String name;

	private String contactPerson;

	private String email;

	private String phoneNumber;

	private String address;

	private String bankName;
	private String bankAccountNumber;
	private String ifscCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orgId")
	private OrganizationEntity organization;
	@OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL)
	private List<PaymentRequestEntity> paymentRequests = new ArrayList<>();
	
	@Column(nullable = false)
    private boolean deleted = false;
}
