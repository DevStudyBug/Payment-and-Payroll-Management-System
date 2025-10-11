package com.aurionpro.dto.response;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalarySlipResponseDto {

    private String employeeName;
    private String department;
    private String designation;
    private String organizationName;

    private String salaryMonth;
    private Double basicSalary;
    private Double hra;
    private Double allowances;
    private Double deductions;
    private Double netSalary;

    private String status;
    private String remark;
    private LocalDateTime transactionDate;
    private String paymentRefNo;
}
