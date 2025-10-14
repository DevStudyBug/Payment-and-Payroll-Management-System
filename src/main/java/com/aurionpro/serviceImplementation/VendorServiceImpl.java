package com.aurionpro.serviceImplementation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.aurionpro.app.exception.NotFoundException;
import com.aurionpro.dto.request.VendorCreateRequestDto;
import com.aurionpro.dto.request.VendorUpdateRequestDto;
import com.aurionpro.dto.response.VendorDetailResponseDto;
import com.aurionpro.dto.response.VendorResponseDto;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.entity.UserEntity;
import com.aurionpro.entity.VendorEntity;
import com.aurionpro.repo.OrganizationRepository;
import com.aurionpro.repo.UserRepository;
import com.aurionpro.repo.VendorRepository;
import com.aurionpro.service.VendorService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorServiceImpl implements VendorService {

	private final VendorRepository vendorRepository;
	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;

	@Override
	public VendorResponseDto createVendor(Authentication authentication, VendorCreateRequestDto dto) {
		OrganizationEntity org = getOrganizationFromAuth(authentication);

		VendorEntity entity = VendorEntity.builder().name(dto.getName()).contactPerson(dto.getContactPerson())
				.email(dto.getEmail()).phoneNumber(dto.getPhoneNumber()).address(dto.getAddress())
				.bankName(dto.getBankName()).bankAccountNumber(dto.getBankAccountNumber()).ifscCode(dto.getIfscCode())
				.organization(org).deleted(false).build();

		VendorEntity saved = vendorRepository.save(entity);
		return toResponseDto(saved);
	}

	@Override
	public List<VendorResponseDto> getAllVendors(Authentication authentication) {
		OrganizationEntity org = getOrganizationFromAuth(authentication);
		return vendorRepository.findByOrganizationAndDeletedFalse(org).stream().map(this::toResponseDto)
				.collect(Collectors.toList());
	}

	@Override
	public VendorDetailResponseDto getVendorById(Authentication authentication, Long vendorId) {
		OrganizationEntity org = getOrganizationFromAuth(authentication);
		VendorEntity entity = vendorRepository.findByVendorIdAndOrganizationAndDeletedFalse(vendorId, org)
				.orElseThrow(() -> new NotFoundException("Vendor not found with id: " + vendorId));
		return toDetailDto(entity);
	}

	@Override
	public VendorResponseDto updateVendor(Authentication authentication, Long vendorId, VendorUpdateRequestDto dto) {
		OrganizationEntity org = getOrganizationFromAuth(authentication);

		VendorEntity entity = vendorRepository.findByVendorIdAndOrganizationAndDeletedFalse(vendorId, org)
				.orElseThrow(() -> new NotFoundException("Vendor not found with id: " + vendorId));

		if (dto.getName() != null && !dto.getName().isBlank()) {
			entity.setName(dto.getName());
		}
		if (dto.getContactPerson() != null && !dto.getContactPerson().isBlank()) {
			entity.setContactPerson(dto.getContactPerson());
		}
		if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
			entity.setEmail(dto.getEmail());
		}
		if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
			entity.setPhoneNumber(dto.getPhoneNumber());
		}
		if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
			entity.setAddress(dto.getAddress());
		}
		if (dto.getBankName() != null && !dto.getBankName().isBlank()) {
			entity.setBankName(dto.getBankName());
		}
		if (dto.getBankAccountNumber() != null && !dto.getBankAccountNumber().isBlank()) {
			entity.setBankAccountNumber(dto.getBankAccountNumber());
		}
		if (dto.getIfscCode() != null && !dto.getIfscCode().isBlank()) {
			entity.setIfscCode(dto.getIfscCode());
		}

		VendorEntity updated = vendorRepository.save(entity);
		return toResponseDto(updated);
	}

	@Override
	public void deleteVendor(Authentication authentication, Long vendorId) {
		OrganizationEntity org = getOrganizationFromAuth(authentication);
		VendorEntity entity = vendorRepository.findByVendorIdAndOrganizationAndDeletedFalse(vendorId, org)
				.orElseThrow(() -> new NotFoundException("Vendor not found with id: " + vendorId));

		entity.setDeleted(true);
		vendorRepository.save(entity);
	}

	private OrganizationEntity getOrganizationFromAuth(Authentication authentication) {
		UserEntity user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = user.getOrganization();
		if (org == null) {
			throw new NotFoundException("User is not associated with any organization.");
		}
		return org;
	}

	private VendorResponseDto toResponseDto(VendorEntity entity) {
		return VendorResponseDto.builder().id(entity.getVendorId()).name(entity.getName())
				.contactPerson(entity.getContactPerson()).email(entity.getEmail()).phoneNumber(entity.getPhoneNumber())
				.address(entity.getAddress()).build();
	}

	private VendorDetailResponseDto toDetailDto(VendorEntity entity) {
		return VendorDetailResponseDto.builder().id(entity.getVendorId()).name(entity.getName())
				.contactPerson(entity.getContactPerson()).email(entity.getEmail()).phoneNumber(entity.getPhoneNumber())
				.address(entity.getAddress()).bankName(entity.getBankName())
				.bankAccountNumber(entity.getBankAccountNumber()).ifscCode(entity.getIfscCode()).build();
	}

}
