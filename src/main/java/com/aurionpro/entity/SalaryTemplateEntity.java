package com.aurionpro.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
    private Long templateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orgId", nullable = false)
    private OrganizationEntity organization;

    @NotBlank
    private String templateName;

    @DecimalMin("0.0")
    private Double basicPercentage;

    @DecimalMin("0.0")
    private Double hraPercentage;

    @DecimalMin("0.0")
    private Double daPercentage;

    @DecimalMin("0.0")
    private Double pfPercentage;

    @DecimalMin("0.0")
    private Double allowancesPercentage;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
    private List<EmployeeSalaryEntity> employeeSalaries = new ArrayList<>();
}
