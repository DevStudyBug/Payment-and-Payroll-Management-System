package com.aurionpro.service;

import com.aurionpro.dto.request.EmployeeBankDetailsRequestDto;
import com.aurionpro.dto.response.EmployeeBankDetailsFullResponseDto;
import com.aurionpro.dto.response.EmployeeBankDetailsResponseDto;
import com.aurionpro.dto.response.SalarySlipResponseDto;

public interface EmployeeDashboardService {
	SalarySlipResponseDto viewSalarySlip(String username, String salaryMonth);

	// Download as PDF
	public byte[] downloadSalarySlip(String username, String salaryMonth);

	public EmployeeBankDetailsResponseDto updateBankDetails(String username, EmployeeBankDetailsRequestDto request);
	public EmployeeBankDetailsFullResponseDto getBankDetails(String username);

}
