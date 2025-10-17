package com.aurionpro.serviceImplementation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.exception.NotFoundException;
import com.aurionpro.dto.request.EmployeeBankDetailsRequestDto;
import com.aurionpro.dto.response.EmployeeBankDetailsFullResponseDto;
import com.aurionpro.dto.response.EmployeeBankDetailsResponseDto;
import com.aurionpro.dto.response.SalarySlipResponseDto;
import com.aurionpro.entity.EmployeeBankDetailsEntity;
import com.aurionpro.entity.EmployeeEntity;
import com.aurionpro.entity.SalaryDisbursementEntity;
import com.aurionpro.repo.EmployeeBankDetailsRepository;
import com.aurionpro.repo.EmployeeRepository;
import com.aurionpro.repo.SalaryDisbursementRepository;
import com.aurionpro.service.EmployeeDashboardService;
import com.aurionpro.service.PdfGeneratorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeDashboardServiceImplementation implements EmployeeDashboardService {
	private final EmployeeRepository employeeRepo;
	private final SalaryDisbursementRepository disbursementRepo;
	private final PdfGeneratorService pdfGeneratorService;
	private final EmployeeBankDetailsRepository employeeBankDetailsRepository;

	@Override
	public SalarySlipResponseDto viewSalarySlip(String username, String salaryMonth) {
		EmployeeEntity emp = employeeRepo.findByUser_Username(username)
				.orElseThrow(() -> new NotFoundException("Employee not found"));

		SalaryDisbursementEntity disb = disbursementRepo.findByEmployeeAndSalaryMonthAndStatus(emp, salaryMonth, "PAID")
				.orElseThrow(() -> new NotFoundException("Paid salary slip not found for " + salaryMonth));

		return new SalarySlipResponseDto(emp.getFirstName() + " " + emp.getLastName(), emp.getDepartment(),
				emp.getDesignation(), emp.getOrganization().getOrgName(), disb.getSalaryMonth(), disb.getBasicSalary(),
				disb.getHra(), disb.getAllowances(), disb.getDeductions(), disb.getNetSalary(), disb.getStatus(),
				disb.getRemark(), disb.getTransactionDate(), disb.getPaymentRefNo());
	}

	@Override
	public byte[] downloadSalarySlip(String username, String salaryMonth) {
		EmployeeEntity emp = employeeRepo.findByUser_Username(username)
				.orElseThrow(() -> new NotFoundException("Employee not found"));

		SalaryDisbursementEntity disb = disbursementRepo.findByEmployeeAndSalaryMonthAndStatus(emp, salaryMonth, "PAID")
				.orElseThrow(() -> new NotFoundException("Paid salary slip not found for " + salaryMonth));

		return pdfGeneratorService.generateSalarySlip(emp, disb);
	}
	@Override
	public EmployeeBankDetailsFullResponseDto getBankDetails(String username) {
	    EmployeeEntity emp = employeeRepo.findByUser_Username(username)
	            .orElseThrow(() -> new NotFoundException("Employee not found"));

	    EmployeeBankDetailsEntity bank = emp.getBankDetails();
	    if (bank == null) throw new NotFoundException("No bank details found.");

	    return EmployeeBankDetailsFullResponseDto.builder()
	            .employeeId(emp.getEmployeeId())
	            .accountHolderName(bank.getAccountHolderName())
	            .accountNumber(bank.getAccountNumber())
	            .ifscCode(bank.getIfscCode())
	            .bankName(bank.getBankName())
	            .branchName(bank.getBranchName())
	            .accountType(bank.getAccountType())
	            .verificationStatus(bank.getVerificationStatus())
	            .reviewerComments(bank.getReviewerComments())
	            .build();
	}
	@Transactional(readOnly = false)
	@Override
	public EmployeeBankDetailsResponseDto updateBankDetails(String username, EmployeeBankDetailsRequestDto request) {
		EmployeeEntity emp = employeeRepo.findByUser_Username(username)
				.orElseThrow(() -> new NotFoundException("Employee not found"));

		EmployeeBankDetailsEntity bank = emp.getBankDetails();
		if (bank == null) {
			bank = new EmployeeBankDetailsEntity();
			bank.setEmployee(emp);
		}

		boolean changed = false;

		if (request.getAccountHolderName() != null && !request.getAccountHolderName().isBlank()) {
			bank.setAccountHolderName(request.getAccountHolderName());
			changed = true;
		}
		if (request.getAccountNumber() != null && !request.getAccountNumber().isBlank()) {
			bank.setAccountNumber(request.getAccountNumber());
			changed = true;
		}
		if (request.getIfscCode() != null && !request.getIfscCode().isBlank()) {
			bank.setIfscCode(request.getIfscCode());
			changed = true;
		}
		if (request.getBankName() != null && !request.getBankName().isBlank()) {
			bank.setBankName(request.getBankName());
			changed = true;
		}
		if (request.getBranchName() != null && !request.getBranchName().isBlank()) {
			bank.setBranchName(request.getBranchName());
			changed = true;
		}
		if (request.getAccountType() != null && !request.getAccountType().isBlank()) {
			bank.setAccountType(request.getAccountType());
			changed = true;
		}

		if (changed) {
			bank.setVerificationStatus("PENDING");
			bank.setReviewerComments(null);

		}

		employeeBankDetailsRepository.save(bank);
		employeeRepo.save(emp);

		return EmployeeBankDetailsResponseDto.builder().employeeId(emp.getEmployeeId())
				.accountHolderName(bank.getAccountHolderName()).bankName(bank.getBankName())
				.verificationStatus(bank.getVerificationStatus())
				.message("Bank details updated successfully and sent for verification.").build();
	}

}
