package com.aurionpro.serviceImplementation;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.aurionpro.app.exception.DuplicateResourceException;
import com.aurionpro.app.exception.NotFoundException;
import com.aurionpro.dto.request.SalaryTemplateRequestDto;
import com.aurionpro.dto.response.PagedResponse;
import com.aurionpro.dto.response.SalaryTemplateDetailResponseDto;
import com.aurionpro.dto.response.SalaryTemplateResponseDto;
import com.aurionpro.dto.response.SalaryTemplateSummaryResponseDto;
import com.aurionpro.entity.DesignationEntity;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.entity.SalaryTemplateEntity;
import com.aurionpro.entity.UserEntity;
import com.aurionpro.repo.DesignationRepository;
import com.aurionpro.repo.OrganizationRepository;
import com.aurionpro.repo.SalaryTemplateRepository;
import com.aurionpro.repo.UserRepository;
import com.aurionpro.service.SalaryTemplateService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalaryTemplateServiceImplementation implements SalaryTemplateService {

	private final UserRepository userRepository;
	private final SalaryTemplateRepository salaryTemplateRepository;
	private final OrganizationRepository organizationRepository;
	private final DesignationRepository designationRepository;
	private final ModelMapper modelMapper;

	@Override
	public SalaryTemplateResponseDto createTemplate(Authentication authentication, SalaryTemplateRequestDto req) {
		UserEntity user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = user.getOrganization();
		if (org == null)
			throw new NotFoundException("User is not associated with any organization.");

		if (!"ACTIVE".equalsIgnoreCase(org.getStatus()))
			throw new IllegalStateException("Organization is not active. Cannot create salary templates.");

		DesignationEntity designation = designationRepository
				.findByNameIgnoreCaseAndOrganization(req.getDesignation(), org).orElseThrow(() -> new NotFoundException(
						"Designation '" + req.getDesignation() + "' not found in your organization."));

		// Prevent duplicate template for the same designation within this org
		boolean exists = salaryTemplateRepository.findByOrganization(org).stream()
				.anyMatch(t -> t.getDesignation().getDesignationId().equals(designation.getDesignationId()));

		if (exists)
			throw new DuplicateResourceException(
					"Salary template already exists for designation: " + designation.getName());

		SalaryTemplateEntity template = modelMapper.map(req, SalaryTemplateEntity.class);
		template.setOrganization(org);
		template.setDesignation(designation);

		salaryTemplateRepository.save(template);

		return SalaryTemplateResponseDto.builder().salaryTemplateId(template.getSalaryTemplateId())
				.designationId(designation.getDesignationId()).designationName(designation.getName())
				.message("Salary template created successfully for designation: " + designation.getName()).build();
	}

	@Override
	public PagedResponse<SalaryTemplateSummaryResponseDto> getAllTemplates(Authentication authentication, int page,
			int size, String sortBy, String sortDir) {

		UserEntity user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = user.getOrganization();
		if (org == null)
			throw new NotFoundException("User is not associated with any organization.");

		if (!"ACTIVE".equalsIgnoreCase(org.getStatus()))
			throw new IllegalStateException("Organization is not active. Cannot get salary templates.");

		Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
		Pageable pageable = PageRequest.of(page, size, sort);

		Page<SalaryTemplateEntity> templatePage = salaryTemplateRepository.findByOrganization(org, pageable);

		List<SalaryTemplateSummaryResponseDto> content = templatePage.getContent().stream()
				.map(template -> SalaryTemplateSummaryResponseDto.builder().templateId(template.getSalaryTemplateId())
						.designation(template.getDesignation().getName()) // ✅ Fetch name from linked designation
						.grossSalary(template.getGrossSalary()).netSalary(template.getNetSalary()).build())
				.collect(Collectors.toList());

		return new PagedResponse<>(content, templatePage.getNumber(), templatePage.getSize(),
				templatePage.getTotalElements(), templatePage.getTotalPages(), templatePage.isLast());
	}

	@Override
	public SalaryTemplateDetailResponseDto getTemplateById(Authentication authentication, Long templateId) {
		UserEntity user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = user.getOrganization();
		if (org == null)
			throw new NotFoundException("User is not associated with any organization.");

		if (!"ACTIVE".equalsIgnoreCase(org.getStatus()))
			throw new IllegalStateException("Organization is not active. Cannot get salary templates.");

		SalaryTemplateEntity template = salaryTemplateRepository.findById(templateId)
				.orElseThrow(() -> new NotFoundException("Salary template not found."));

		if (!template.getOrganization().getOrgId().equals(org.getOrgId()))
			throw new NotFoundException("This template does not belong to your organization.");

		SalaryTemplateDetailResponseDto dto = SalaryTemplateDetailResponseDto.builder()
				.salaryTemplateId(template.getSalaryTemplateId())
				.designation(template.getDesignation() != null ? template.getDesignation().getName() : null)
				.basicSalary(template.getBasicSalary()).hra(template.getHra()).da(template.getDa()).pf(template.getPf())
				.otherAllowances(template.getOtherAllowances()).grossSalary(template.getGrossSalary())
				.netSalary(template.getNetSalary())
				.createdAt(template.getCreatedAt() != null ? template.getCreatedAt().toString() : null)
				.updatedAt(template.getUpdatedAt() != null ? template.getUpdatedAt().toString() : null).build();

		return dto;
	}

}
