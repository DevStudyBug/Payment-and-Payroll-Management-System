package com.aurionpro.entity;

import java.time.LocalDateTime;

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
@Table(name = "salary_disbursements")
public class SalaryDisbursementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long disbursementId;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orgId", nullable = false)
    private OrganizationEntity organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeId", nullable = false)
    private EmployeeEntity employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paymentRequestId")
    private PaymentRequestEntity paymentRequest;

    
    @NotBlank
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Salary month must be in YYYY-MM format")
    private String salaryMonth;

    @PositiveOrZero
    private Double basicSalary;

    @PositiveOrZero
    private Double hra;

    @PositiveOrZero
    private Double allowances; // includes DA + other allowances

    @PositiveOrZero
    private Double deductions; // includes PF or other deductions

    @PositiveOrZero
    private Double netSalary;

   
    @NotBlank
    private String status; // "GENERATED", "UNDER_REVIEW", "APPROVED", "PAID", "REJECTED"

    @Column(columnDefinition = "TEXT")
    private String remark; // Notes or rejection reasons

    private LocalDateTime transactionDate;

    private String paymentRefNo;
}
