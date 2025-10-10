package com.aurionpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SalaryDisbursementDto {
    private Long disbursementId;
    private Long employeeId;
    private String employeeName;
    private Double netSalary;
    private String status;
    private String remark;
    private String bankStatus;
}
