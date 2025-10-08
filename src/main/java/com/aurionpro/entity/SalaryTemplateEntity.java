package com.aurionpro.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "salary_templates")
public class SalaryTemplateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long salaryTemplateId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "org_id", nullable = false)
	@NotNull(message = "Organization reference is required")
	private OrganizationEntity organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "designation_id", nullable = false)
	private DesignationEntity designation;

	@NotNull
	@DecimalMin("0.0")
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal basicSalary;

	@NotNull
	@DecimalMin("0.0")
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal hra;

	@NotNull
	@DecimalMin("0.0")
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal da;

	@NotNull
	@DecimalMin("0.0")
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal pf;

	@NotNull
	@DecimalMin("0.0")
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal otherAllowances;

	@Column(precision = 10, scale = 2)
	private BigDecimal grossSalary;

	@Column(precision = 10, scale = 2)
	private BigDecimal netSalary;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@PrePersist
	@PreUpdate
	private void calculateSalaries() {
		BigDecimal safeBasic = basicSalary != null ? basicSalary : BigDecimal.ZERO;
		BigDecimal safeHra = hra != null ? hra : BigDecimal.ZERO;
		BigDecimal safeDa = da != null ? da : BigDecimal.ZERO;
		BigDecimal safeOther = otherAllowances != null ? otherAllowances : BigDecimal.ZERO;
		BigDecimal safePf = pf != null ? pf : BigDecimal.ZERO;

		this.grossSalary = safeBasic.add(safeHra).add(safeDa).add(safeOther);
		this.netSalary = this.grossSalary.subtract(safePf);
	}

	@OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
	private List<EmployeeSalaryEntity> employeeSalaries = new ArrayList<>();
}
