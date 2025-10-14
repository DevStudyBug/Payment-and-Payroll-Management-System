package com.aurionpro.serviceImplementation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.aurionpro.app.exception.NotFoundException;
import com.aurionpro.constants.PaymentRequestType;
import com.aurionpro.dto.request.VendorPaymentRequestCreateDto;
import com.aurionpro.dto.response.VendorPaymentRequestResponseDto;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.entity.PaymentRequestEntity;
import com.aurionpro.entity.UserEntity;
import com.aurionpro.entity.VendorEntity;
import com.aurionpro.repo.PaymentRequestRepository;
import com.aurionpro.repo.UserRepository;
import com.aurionpro.repo.VendorRepository;
import com.aurionpro.service.VendorPaymentRequestService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorPaymentRequestServiceImpl implements VendorPaymentRequestService {

	private final PaymentRequestRepository paymentRequestRepo;
	private final VendorRepository vendorRepo;
	private final UserRepository userRepo;

	private OrganizationEntity getOrganizationFromAuth(Authentication authentication) {
		UserEntity user = userRepo.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = user.getOrganization();
		if (org == null) {
			throw new NotFoundException("User is not associated with any organization.");
		}
		return org;
	}

	@Override
	public VendorPaymentRequestResponseDto createPaymentRequest(Authentication authentication,
			VendorPaymentRequestCreateDto dto) {

		OrganizationEntity org = getOrganizationFromAuth(authentication);

		if (dto.getRequestType() == null) {
			throw new IllegalArgumentException("Request type is required. Valid values: VENDOR or PAYROLL");
		}

		VendorEntity vendor = null;

		if (dto.getRequestType() == PaymentRequestType.VENDOR) {

			if (dto.getVendorId() == null) {
				throw new IllegalArgumentException("Vendor ID is required for VENDOR payment requests.");
			}

			vendor = vendorRepo.findByVendorIdAndOrganizationAndDeletedFalse(dto.getVendorId(), org).orElseThrow(
					() -> new NotFoundException("Vendor not found or does not belong to your organization (ID: "
							+ dto.getVendorId() + ")"));
		}

		else if (dto.getRequestType() == PaymentRequestType.PAYROLL) {
			if (dto.getVendorId() != null) {
				throw new IllegalArgumentException("Vendor ID should not be provided for PAYROLL payment requests.");
			}
		}

		else {
			throw new IllegalArgumentException("Invalid request type. Allowed values: VENDOR or PAYROLL");
		}

		PaymentRequestEntity entity = new PaymentRequestEntity();
		entity.setOrganization(org);
		entity.setVendor(vendor);
		entity.setAmount(dto.getAmount());
		entity.setDescription(dto.getDescription());
		entity.setRequestType(dto.getRequestType());
		entity.setStatus("PENDING");
		entity.setRequestDate(LocalDateTime.now());

		PaymentRequestEntity saved = paymentRequestRepo.save(entity);
		return toDto(saved);
	}

	@Override
	public List<VendorPaymentRequestResponseDto> getOrgPaymentRequests(Authentication authentication) {
		OrganizationEntity org = getOrganizationFromAuth(authentication);

		return paymentRequestRepo.findByOrganizationAndRequestType(org, PaymentRequestType.VENDOR).stream()
				.map(this::toDto).collect(Collectors.toList());
	}

	@Override
	public VendorPaymentRequestResponseDto getPaymentRequestById(Authentication authentication, Long paymentId) {
		OrganizationEntity org = getOrganizationFromAuth(authentication);

		PaymentRequestEntity entity = paymentRequestRepo.findById(paymentId)
				.filter(req -> req.getOrganization().equals(org) && req.getRequestType() == PaymentRequestType.VENDOR)
				.orElseThrow(() -> new NotFoundException("Vendor payment request not found or unauthorized access."));

		return toDto(entity);
	}

	@Override
	public List<VendorPaymentRequestResponseDto> getAllPaymentRequestsForAdmin() {
		return paymentRequestRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
	}

	@Override
	public VendorPaymentRequestResponseDto approvePaymentRequest(Long id) {
		PaymentRequestEntity entity = paymentRequestRepo.findById(id)
				.orElseThrow(() -> new NotFoundException("Payment request not found"));

		entity.setStatus("PAID");
		entity.setApprovalDate(LocalDateTime.now());
		entity.setPaymentRefNo(generateRefNo(id));

		paymentRequestRepo.save(entity);
		return toDto(entity);
	}

	@Override
	public VendorPaymentRequestResponseDto rejectPaymentRequest(Long id, String remark) {
		PaymentRequestEntity entity = paymentRequestRepo.findById(id)
				.orElseThrow(() -> new NotFoundException("Payment request not found"));

		if (remark == null || remark.isBlank()) {
			throw new IllegalArgumentException("Rejection reason must be provided");
		}

		entity.setStatus("REJECTED");
		entity.setRemark(remark);
		entity.setApprovalDate(LocalDateTime.now());

		paymentRequestRepo.save(entity);
		return toDto(entity);
	}

	private String generateRefNo(Long id) {
		return "PAY-" + id + "-" + System.currentTimeMillis();
	}

	private VendorPaymentRequestResponseDto toDto(PaymentRequestEntity e) {
		return VendorPaymentRequestResponseDto.builder().paymentId(e.getPaymentId())
				.organizationName(e.getOrganization().getOrgName())
				.vendorName(e.getVendor() != null ? e.getVendor().getName() : null).amount(e.getAmount())
				.description(e.getDescription()).requestType(e.getRequestType()).status(e.getStatus())
				.remark(e.getRemark()).requestDate(e.getRequestDate()).approvalDate(e.getApprovalDate())
				.paymentRefNo(e.getPaymentRefNo()).build();
	}
}
