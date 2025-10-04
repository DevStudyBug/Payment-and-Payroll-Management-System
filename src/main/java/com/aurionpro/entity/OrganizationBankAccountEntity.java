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
    private Long accountId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orgId", nullable = false, unique = true)
    private OrganizationEntity organization;
    
    @NotBlank
    private String accountNumber;

    @NotBlank
    private String ifscCode;

    @NotBlank
    private String bankName;

    @PositiveOrZero
    private Double balance = 0.0;

}
