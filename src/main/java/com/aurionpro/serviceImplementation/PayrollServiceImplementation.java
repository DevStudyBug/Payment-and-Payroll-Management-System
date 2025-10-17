package com.aurionpro.serviceImplementation;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.aurionpro.app.exception.NotFoundException;
import com.aurionpro.constants.PaymentRequestType;
import com.aurionpro.dto.request.PaymentRequestListDto;
import com.aurionpro.dto.request.PaymentRequestSummaryDto;
import com.aurionpro.dto.request.RejectRequestDto;
import com.aurionpro.dto.response.PaymentRequestDetailDto;
import com.aurionpro.dto.response.PaymentRequestPageResponseDto;
import com.aurionpro.dto.response.PayrollActionResponseDto;
import com.aurionpro.dto.response.PayrollGenerateResponseDto;
import com.aurionpro.dto.response.PayrollSubmitResponseDto;
import com.aurionpro.dto.response.SalaryDisbursementDto;
import com.aurionpro.dto.response.VendorPaymentRequestResponseDto;
import com.aurionpro.entity.EmployeeEntity;
import com.aurionpro.entity.EmployeeSalaryEntity;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.entity.PaymentRequestEntity;
import com.aurionpro.entity.SalaryDisbursementEntity;
import com.aurionpro.entity.SalaryTemplateEntity;
import com.aurionpro.entity.UserEntity;
import com.aurionpro.entity.VendorEntity;
import com.aurionpro.repo.EmployeeRepository;
import com.aurionpro.repo.EmployeeSalaryRepository;
import com.aurionpro.repo.OrganizationRepository;
import com.aurionpro.repo.PaymentRequestRepository;
import com.aurionpro.repo.SalaryDisbursementRepository;
import com.aurionpro.repo.UserRepository;
import com.aurionpro.service.EmailService;
import com.aurionpro.service.PayrollService;
import com.aurionpro.service.PdfGeneratorService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollServiceImplementation implements PayrollService {

	private final EmployeeRepository employeeRepo;
	private final EmployeeSalaryRepository employeeSalaryRepo;
	private final SalaryDisbursementRepository salaryDisbursementRepo;
	private final PaymentRequestRepository paymentRequestRepo;
	private final OrganizationRepository organizationRepo;
	private final UserRepository userRepository;
	private final EmailService emailService;
	private final PdfGeneratorService pdfGenerator;

	// GENERATE PAYROLL (Org Admin)
	// -----------------------------------------------------------------
	@Override
	public PayrollGenerateResponseDto generatePayroll(Authentication authentication, String salaryMonth) {
		UserEntity user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = user.getOrganization();
		if (org == null)
			throw new NotFoundException("User is not associated with any organization.");

		YearMonth requestedMonth;
		try {
			requestedMonth = YearMonth.parse(salaryMonth); // Expected format: "YYYY-MM"
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Invalid salary month format. Expected format: YYYY-MM (e.g., 2025-10)");
		}

		YearMonth currentMonth = YearMonth.now();
		if (!requestedMonth.equals(currentMonth)) {
			throw new IllegalArgumentException("Payroll can only be generated for the current month. Current month: "
					+ currentMonth + ", Requested month: " + requestedMonth);
		}

		// Check for existing payroll
		List<SalaryDisbursementEntity> existing = salaryDisbursementRepo
				.findByOrganization_OrgIdAndSalaryMonth(org.getOrgId(), salaryMonth);

		boolean hasActivePayroll = existing.stream().anyMatch(
				d -> !d.getStatus().equalsIgnoreCase("REJECTED") && !d.getStatus().equalsIgnoreCase("CANCELLED"));
		if (hasActivePayroll)
			throw new IllegalStateException("Payroll for " + salaryMonth + " already exists.");

		// Fetch all employees
		List<EmployeeEntity> employees = employeeRepo.findByOrganization_OrgId(org.getOrgId());
		if (employees.isEmpty())
			throw new NotFoundException("No employees found for organization " + org.getOrgName());

		// ✅ New Rule: All employees must be ACTIVE
		boolean allActive = employees.stream()
				.allMatch(e -> e.getStatus() != null && e.getStatus().equalsIgnoreCase("ACTIVE"));

		if (!allActive) {
			// Find non-active employees to give a clear message
			List<String> nonActiveEmployees = employees.stream().filter(e -> !e.getStatus().equalsIgnoreCase("ACTIVE"))
					.map(e -> e.getFirstName() + " " + e.getLastName() + " (" + e.getStatus() + ")").toList();

			throw new IllegalStateException("Payroll generation blocked. The following employees are not ACTIVE: "
					+ String.join(", ", nonActiveEmployees));
		}

		// Proceed only if all employees are ACTIVE
		List<SalaryDisbursementEntity> batch = new ArrayList<>();
		int batchSize = 10;

		for (EmployeeEntity emp : employees) {
			try {
				EmployeeSalaryEntity empSalary = employeeSalaryRepo.findByEmployee(emp).orElseThrow(
						() -> new NotFoundException("Salary info missing for employee: " + emp.getFirstName()));

				SalaryTemplateEntity template = empSalary.getTemplate();

				double basic = template.getBasicSalary().doubleValue();
				double hra = template.getHra().doubleValue();
				double da = template.getDa().doubleValue();
				double pf = template.getPf().doubleValue();
				double other = template.getOtherAllowances().doubleValue();

				double gross = basic + hra + da + other;
				double net = gross - pf;

				SalaryDisbursementEntity dis = new SalaryDisbursementEntity();
				dis.setOrganization(org);
				dis.setEmployee(emp);
				dis.setSalaryMonth(salaryMonth);
				dis.setBasicSalary(basic);
				dis.setHra(hra);
				dis.setAllowances(da + other);
				dis.setDeductions(pf);
				dis.setNetSalary(net);
				dis.setStatus("GENERATED");
				dis.setRemark("Auto-generated from employee salary template.");

				batch.add(dis);

				if (batch.size() == batchSize) {
					salaryDisbursementRepo.saveAll(batch);
					batch.clear();
				}

			} catch (Exception ex) {
				System.err.println("Skipping employee " + emp.getFirstName() + ": " + ex.getMessage());
			}
		}

		if (!batch.isEmpty())
			salaryDisbursementRepo.saveAll(batch);

		return new PayrollGenerateResponseDto(
				"✅ Payroll generated successfully for " + salaryMonth + " (" + employees.size() + " employees)",
				salaryMonth, employees.size());
	}

	// SUBMIT PAYROLL TO BANK (Org Admin)
	// -----------------------------------------------------------------
	@Override
	public PayrollSubmitResponseDto submitPayrollToBank(Authentication authentication, String salaryMonth) {
	    UserEntity user = userRepository.findByUsername(authentication.getName())
	            .orElseThrow(() -> new NotFoundException("User not found"));

	    OrganizationEntity org = user.getOrganization();
	    if (org == null)
	        throw new NotFoundException("User is not associated with any organization.");

	    //  Fetch disbursements for that specific month
	    List<SalaryDisbursementEntity> disbursements = salaryDisbursementRepo
	            .findByOrganization_OrgIdAndSalaryMonthAndStatusIgnoreCase(org.getOrgId(), salaryMonth, "GENERATED");

	    if (disbursements.isEmpty()) {
	        throw new NotFoundException(
	                "No generated payrolls found for organization " + org.getOrgName() +
	                " for the month " + salaryMonth + ".");
	    }

	    //  Calculate total amount
	    double totalAmount = disbursements.stream()
	            .mapToDouble(SalaryDisbursementEntity::getNetSalary)
	            .sum();

	    // Create payment request entry
	    PaymentRequestEntity paymentRequest = new PaymentRequestEntity();
	    paymentRequest.setOrganization(org);
	    paymentRequest.setAmount(totalAmount);
	    paymentRequest.setDescription("Monthly Payroll for " + salaryMonth);
	    paymentRequest.setRequestType(PaymentRequestType.PAYROLL);
	    paymentRequest.setStatus("PENDING");
	    paymentRequest.setRequestDate(LocalDateTime.now());
	    paymentRequestRepo.save(paymentRequest);

	    // Update disbursements to UNDER_REVIEW and link to payment request
	    disbursements.forEach(d -> {
	        d.setStatus("UNDER_REVIEW");
	        d.setPaymentRequest(paymentRequest);
	    });
	    salaryDisbursementRepo.saveAll(disbursements);

	    return new PayrollSubmitResponseDto(
	            "Payroll for " + salaryMonth + " submitted to bank successfully.",
	            paymentRequest.getPaymentId(),
	            paymentRequest.getStatus());
	}


	@Override
	public Object approvePaymentRequest(Long paymentRequestId) {
		PaymentRequestEntity req = paymentRequestRepo.findById(paymentRequestId)
				.orElseThrow(() -> new NotFoundException("Payment request not found."));

		if (req.getRequestType() == PaymentRequestType.PAYROLL) {
			return approvePayrollRequest(paymentRequestId);
		} else if (req.getRequestType() == PaymentRequestType.VENDOR) {
			return approveVendorRequest(paymentRequestId);
		} else {
			throw new IllegalArgumentException("Unknown request type for approval.");
		}
	}

	@Override
	public Object rejectPaymentRequest(Long paymentRequestId, RejectRequestDto dto) {
		PaymentRequestEntity req = paymentRequestRepo.findById(paymentRequestId)
				.orElseThrow(() -> new NotFoundException("Payment request not found."));

		if (req.getRequestType() == PaymentRequestType.PAYROLL) {
			return rejectPayrollRequest(paymentRequestId, dto);
		} else if (req.getRequestType() == PaymentRequestType.VENDOR) {
			return rejectVendorRequest(paymentRequestId, dto);
		} else {
			throw new IllegalArgumentException("Unsupported payment request type.");
		}
	}

	// APPROVE PAYROLL (Bank Admin)
	// -----------------------------------------------------------------
	@Override
	public PayrollActionResponseDto approvePayrollRequest(Long paymentRequestId) {
		PaymentRequestEntity req = paymentRequestRepo.findById(paymentRequestId)
				.orElseThrow(() -> new NotFoundException("Payment request not found."));

		if (!"PENDING".equalsIgnoreCase(req.getStatus()))
			throw new IllegalStateException("Only PENDING payroll requests can be approved.");

		List<SalaryDisbursementEntity> dis = salaryDisbursementRepo.findByPaymentRequest_PaymentId(paymentRequestId);
		if (dis.isEmpty())
			throw new NotFoundException("No salary disbursements found for this payroll request.");

		boolean anyInvalid = false;
		StringBuilder reasons = new StringBuilder();
		int rejectedCount = 0;
		int approvedCount = 0;

		for (SalaryDisbursementEntity s : dis) {
			EmployeeEntity emp = s.getEmployee();

			boolean docsOk = emp.getDocuments() != null && !emp.getDocuments().isEmpty()
					&& emp.getDocuments().stream().allMatch(d -> "APPROVED".equalsIgnoreCase(d.getStatus()));
			boolean bankOk = emp.getBankDetails() != null
					&& "APPROVED".equalsIgnoreCase(emp.getBankDetails().getVerificationStatus());
			boolean active = "ACTIVE".equalsIgnoreCase(emp.getStatus());

			if (!docsOk || !bankOk || !active) {
				anyInvalid = true;
				rejectedCount++;

				StringBuilder empReason = new StringBuilder();
				empReason.append("Employee: ").append(emp.getFirstName()).append(" ").append(emp.getLastName())
						.append(" - ");

				if (!active)
					empReason.append("Inactive employee status. ");
				if (!docsOk)
					empReason.append("Document verification pending or rejected. ");
				if (!bankOk)
					empReason.append("Bank details not verified. ");

				s.setStatus("REJECTED");
				s.setRemark(empReason.toString().trim());
				reasons.append(empReason).append("\n");
			} else {
				approvedCount++;
				s.setStatus("APPROVED");
				s.setRemark("Validated successfully for disbursement.");
			}
		}

		salaryDisbursementRepo.saveAll(dis);

		// If any employee was invalid
		if (anyInvalid) {
			req.setStatus("REJECTED");
			req.setApprovalDate(LocalDateTime.now());

			String finalRemark = String.format("Payroll  rejected. %d employee(s) invalid out of %d.\nReasons:\n%s",
					rejectedCount, dis.size(), reasons.toString().trim());

			req.setRemark(finalRemark);
			paymentRequestRepo.save(req);

			emailService.sendPayrollRejectionEmail(req.getOrganization().getEmail(), req.getPaymentId(), finalRemark);

			return new PayrollActionResponseDto(req.getPaymentId(), "REJECTED",
					"Payroll rejected due to invalid employee data.", finalRemark, req.getAmount(),
					req.getApprovalDate());
		}

		// If all valid
		req.setStatus("APPROVED");
		req.setApprovalDate(LocalDateTime.now());
		String successRemark = String.format("All %d employee disbursements validated successfully.", approvedCount);
		req.setRemark(successRemark);
		paymentRequestRepo.save(req);

		emailService.sendPayrollApprovalEmail(req.getOrganization().getEmail(), req.getPaymentId(),
				req.getDescription());

		return new PayrollActionResponseDto(req.getPaymentId(), "APPROVED",
				"Payroll approved successfully and ready for disbursement.", successRemark, req.getAmount(),
				req.getApprovalDate());
	}

	@Override
	public Object disbursePayment(Long paymentRequestId) {
		PaymentRequestEntity req = paymentRequestRepo.findById(paymentRequestId)
				.orElseThrow(() -> new NotFoundException("Payment request not found."));

		if (req.getRequestType() == PaymentRequestType.PAYROLL) {
			return disbursePayroll(paymentRequestId);
		} else if (req.getRequestType() == PaymentRequestType.VENDOR) {
			return disburseVendorPayment(paymentRequestId);
		} else {
			throw new IllegalArgumentException("Unsupported payment request type for disbursement.");
		}
	}

	// DISBURSE PAYROLL (Bank Admin)
	// -----------------------------------------------------------------
	@Override
	public PayrollActionResponseDto disbursePayroll(Long paymentRequestId) {
		PaymentRequestEntity req = paymentRequestRepo.findById(paymentRequestId)
				.orElseThrow(() -> new NotFoundException("Payment request not found"));

		if (!"APPROVED".equalsIgnoreCase(req.getStatus()))
			throw new IllegalStateException("Only APPROVED payroll requests can be disbursed.");

		List<SalaryDisbursementEntity> disbursements = salaryDisbursementRepo
				.findByPaymentRequest_PaymentId(paymentRequestId);

		if (disbursements.isEmpty())
			throw new NotFoundException("No disbursements found for this request.");

		for (SalaryDisbursementEntity s : disbursements) {
			s.setStatus("PAID");
			s.setTransactionDate(LocalDateTime.now());
			s.setPaymentRefNo("TXN-" + UUID.randomUUID());
			s.setRemark("Salary credited successfully.");
		}

		salaryDisbursementRepo.saveAll(disbursements);

		req.setStatus("PAID");
		req.setApprovalDate(LocalDateTime.now());
		req.setPaymentRefNo("PR-" + UUID.randomUUID());
		req.setRemark("Payroll disbursed and salary slips are being emailed.");
		paymentRequestRepo.save(req);

		// Send emails asynchronously
		for (SalaryDisbursementEntity s : disbursements) {
			try {
				EmployeeEntity emp = s.getEmployee();
				byte[] pdf = pdfGenerator.generateSalarySlip(emp, s);
				emailService.sendSalarySlipEmail(emp,
						emp.getEmployeeSalary().getTemplate().getNetSalary().doubleValue(), pdf, s.getSalaryMonth());
			} catch (Exception e) {
				System.err.println(
						"❌ Error preparing salary slip for " + s.getEmployee().getFirstName() + ": " + e.getMessage());
			}
		}

		emailService.sendPayrollDisbursedEmail(req.getOrganization().getEmail(), req.getOrganization().getOrgName(),
				req.getPaymentId(), req.getAmount(), req.getStatus(), req.getApprovalDate());

		return new PayrollActionResponseDto(req.getPaymentId(), req.getStatus(),
				"Payroll disbursed successfully. Salary slips are being sent via email.", req.getRemark(),
				req.getAmount(), req.getApprovalDate());
	}

	// DISBURSE PAYMENT (Bank Admin)
	@Override
	public VendorPaymentRequestResponseDto disburseVendorPayment(Long paymentRequestId) {
		PaymentRequestEntity req = paymentRequestRepo.findById(paymentRequestId)
				.orElseThrow(() -> new NotFoundException("Payment request not found."));

		if (req.getRequestType() != PaymentRequestType.VENDOR) {
			throw new IllegalArgumentException("This payment request is not of type VENDOR.");
		}

		if (!"APPROVED".equalsIgnoreCase(req.getStatus())) {
			throw new IllegalStateException("Only APPROVED vendor payment requests can be disbursed.");
		}

		VendorEntity vendor = req.getVendor();
		if (vendor == null) {
			throw new NotFoundException("No vendor linked to this payment request.");
		}

		req.setStatus("PAID");
		req.setApprovalDate(LocalDateTime.now());
		req.setPaymentRefNo("VN-" + UUID.randomUUID());
		req.setRemark("Vendor payment disbursed successfully. Reference: " + req.getPaymentRefNo());
		paymentRequestRepo.save(req);

		return VendorPaymentRequestResponseDto.builder().paymentId(req.getPaymentId())
				.organizationName(req.getOrganization().getOrgName()).vendorName(vendor.getName())
				.amount(req.getAmount()).description(req.getDescription()).requestType(req.getRequestType())
				.status(req.getStatus()).remark(req.getRemark()).requestDate(req.getRequestDate())
				.approvalDate(req.getApprovalDate()).paymentRefNo(req.getPaymentRefNo()).build();
	}

	@Override
	public PaymentRequestPageResponseDto getFilteredPaymentRequests(String status, String requestType, Long orgId,
			LocalDateTime startDate, LocalDateTime endDate, int page, int size, String sortBy, String sortDir) {

		Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

		Pageable pageable = PageRequest.of(page, size, sort);

		Page<PaymentRequestEntity> requestPage = paymentRequestRepo.findFilteredPaymentRequests(status, requestType,
				orgId, startDate, endDate, pageable);

		List<PaymentRequestListDto> content = requestPage.getContent().stream()
				.map(req -> new PaymentRequestListDto(req.getPaymentId(),
						req.getOrganization() != null ? req.getOrganization().getOrgName() : "N/A",
						req.getRequestType(), req.getStatus(), req.getAmount(), req.getDescription(),
						req.getRequestDate(), req.getApprovalDate()))
				.toList();

		List<PaymentRequestEntity> allForSummary = paymentRequestRepo
				.findFilteredPaymentRequests(null, requestType, orgId, startDate, endDate, Pageable.unpaged())
				.getContent();

		PaymentRequestSummaryDto summary = new PaymentRequestSummaryDto();
		summary.setTotalRequests(allForSummary.size());
		summary.setTotalPending(allForSummary.stream().filter(r -> r.getStatus().equalsIgnoreCase("PENDING")).count());
		summary.setTotalApproved(
				allForSummary.stream().filter(r -> r.getStatus().equalsIgnoreCase("APPROVED")).count());
		summary.setTotalRejected(
				allForSummary.stream().filter(r -> r.getStatus().equalsIgnoreCase("REJECTED")).count());
		summary.setTotalPaid(allForSummary.stream().filter(r -> r.getStatus().equalsIgnoreCase("PAID")).count());

		summary.setTotalAmount(allForSummary.stream().mapToDouble(PaymentRequestEntity::getAmount).sum());
		summary.setTotalPaidAmount(allForSummary.stream().filter(r -> r.getStatus().equalsIgnoreCase("PAID"))
				.mapToDouble(PaymentRequestEntity::getAmount).sum());
		summary.setTotalPendingAmount(allForSummary.stream().filter(r -> r.getStatus().equalsIgnoreCase("PENDING"))
				.mapToDouble(PaymentRequestEntity::getAmount).sum());

		return new PaymentRequestPageResponseDto(content, summary, requestPage.getNumber(), requestPage.getSize(),
				requestPage.getTotalElements(), requestPage.getTotalPages(), requestPage.isLast());
	}

	@Override
	public PaymentRequestDetailDto getPaymentRequestDetail(Long paymentRequestId) {
		PaymentRequestEntity p = paymentRequestRepo.findById(paymentRequestId)
				.orElseThrow(() -> new NotFoundException("Payment request not found"));

		List<SalaryDisbursementDto> disList = salaryDisbursementRepo.findByPaymentRequest_PaymentId(paymentRequestId)
				.stream().map(d -> {
					String bankStatus = d.getEmployee().getBankDetails() != null
							? d.getEmployee().getBankDetails().getVerificationStatus()
							: "N/A";
					return new SalaryDisbursementDto(d.getDisbursementId(), d.getEmployee().getEmployeeId(),
							d.getEmployee().getFirstName() + " " + d.getEmployee().getLastName(), d.getNetSalary(),
							d.getStatus(), d.getRemark(), bankStatus);
				}).toList();

		return PaymentRequestDetailDto.builder().paymentId(p.getPaymentId())
				.orgId(p.getOrganization() != null ? p.getOrganization().getOrgId() : null)
				.orgName(p.getOrganization() != null ? p.getOrganization().getOrgName() : null)
				.requestType(p.getRequestType()).status(p.getStatus()).amount(p.getAmount())
				.description(p.getDescription()).requestDate(p.getRequestDate()).approvalDate(p.getApprovalDate())
				.remark(p.getRemark()).disbursements(disList).build();
	}

	@Override
	public PayrollActionResponseDto rejectPayrollRequest(Long paymentRequestId, RejectRequestDto dto) {
		PaymentRequestEntity req = paymentRequestRepo.findById(paymentRequestId)
				.orElseThrow(() -> new NotFoundException("Payment request not found"));

		if (!"PENDING".equalsIgnoreCase(req.getStatus()) && !"APPROVED".equalsIgnoreCase(req.getStatus())) {
			throw new IllegalStateException("Only PENDING or APPROVED payroll requests can be rejected.");
		}

		List<SalaryDisbursementEntity> dis = salaryDisbursementRepo.findByPaymentRequest_PaymentId(paymentRequestId);
		dis.forEach(d -> {
			d.setStatus("REJECTED");
			d.setRemark(dto.getReason());
		});
		salaryDisbursementRepo.saveAll(dis);

		req.setStatus("REJECTED");
		req.setApprovalDate(LocalDateTime.now());
		req.setRemark(dto.getReason());
		paymentRequestRepo.save(req);

		emailService.sendPayrollRejectionEmail(req.getOrganization().getEmail(), req.getPaymentId(), dto.getReason());

		return new PayrollActionResponseDto(req.getPaymentId(), req.getStatus(), "Payroll request rejected.",
				req.getRemark(), req.getAmount(), req.getApprovalDate());
	}

	// approve the paymentrequest of vendor
	@Override
	public VendorPaymentRequestResponseDto approveVendorRequest(Long paymentRequestId) {
		PaymentRequestEntity req = paymentRequestRepo.findById(paymentRequestId)
				.orElseThrow(() -> new NotFoundException("Payment request not found."));

		if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
			throw new IllegalStateException("Only PENDING vendor requests can be approved.");
		}

		if (req.getRequestType() != PaymentRequestType.VENDOR) {
			throw new IllegalArgumentException("This payment request is not of type VENDOR.");
		}

		VendorEntity vendor = req.getVendor();
		if (vendor == null) {
			throw new NotFoundException("No vendor associated with this payment request.");
		}

		req.setStatus("APPROVED");
		req.setApprovalDate(LocalDateTime.now());
		req.setRemark("Vendor payment request approved. Awaiting disbursement.");

		paymentRequestRepo.save(req);

		return VendorPaymentRequestResponseDto.builder().paymentId(req.getPaymentId())
				.organizationName(req.getOrganization().getOrgName()).vendorName(vendor.getName())
				.amount(req.getAmount()).description(req.getDescription()).requestType(req.getRequestType())
				.status(req.getStatus()).remark(req.getRemark()).requestDate(req.getRequestDate())
				.approvalDate(req.getApprovalDate()).paymentRefNo(req.getPaymentRefNo()).build();
	}

	// Reject the paymentrequest of vendor
	@Override
	public VendorPaymentRequestResponseDto rejectVendorRequest(Long paymentRequestId, RejectRequestDto dto) {
		PaymentRequestEntity req = paymentRequestRepo.findById(paymentRequestId)
				.orElseThrow(() -> new NotFoundException("Payment request not found."));

		if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
			throw new IllegalStateException("Only PENDING  vendor payment requests can be rejected.");
		}

		if (req.getRequestType() != PaymentRequestType.VENDOR) {
			throw new IllegalArgumentException("This payment request is not of type VENDOR.");
		}

		VendorEntity vendor = req.getVendor();
		if (vendor == null) {
			throw new NotFoundException("No vendor associated with this payment request.");
		}

		req.setStatus("REJECTED");
		req.setApprovalDate(LocalDateTime.now());
		req.setRemark(dto.getReason());

		paymentRequestRepo.save(req);

		return VendorPaymentRequestResponseDto.builder().paymentId(req.getPaymentId())
				.organizationName(req.getOrganization().getOrgName()).vendorName(vendor.getName())
				.amount(req.getAmount()).description(req.getDescription()).requestType(req.getRequestType())
				.status(req.getStatus()).remark(req.getRemark()).requestDate(req.getRequestDate())
				.approvalDate(req.getApprovalDate()).paymentRefNo(req.getPaymentRefNo()).build();
	}

}
