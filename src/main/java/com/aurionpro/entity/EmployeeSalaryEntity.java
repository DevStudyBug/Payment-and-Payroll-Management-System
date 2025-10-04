package com.aurionpro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_salaries")
public class EmployeeSalaryEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeSalaryId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeId", nullable = false, unique = true)
    private EmployeeEntity employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "templateId", nullable = false)
    private SalaryTemplateEntity template;

    @Column(columnDefinition = "TEXT")
    private String customAllowances;
}
