package com.aurionpro.service;

import com.aurionpro.entity.EmployeeEntity;
import com.aurionpro.entity.SalaryDisbursementEntity;

public interface PdfGeneratorService {
	 public byte[] generateSalarySlip(EmployeeEntity emp, SalaryDisbursementEntity disb);
}
