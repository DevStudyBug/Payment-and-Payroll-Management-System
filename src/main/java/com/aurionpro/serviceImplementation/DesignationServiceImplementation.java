package com.aurionpro.serviceImplementation;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.aurionpro.app.exception.DuplicateResourceException;
import com.aurionpro.app.exception.NotFoundException;
import com.aurionpro.entity.DesignationEntity;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.entity.UserEntity;
import com.aurionpro.repo.DesignationRepository;
import com.aurionpro.repo.UserRepository;
import com.aurionpro.service.DesignationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DesignationServiceImplementation implements DesignationService {
	private final DesignationRepository designationRepository;
	private final UserRepository userRepository;

	@Override
	public DesignationEntity addDesignation(Authentication authentication, String name) {
		UserEntity user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = user.getOrganization();
		if (org == null) {
			throw new NotFoundException("User is not associated with any organization.");
		}

		designationRepository.findByNameIgnoreCaseAndOrganization(name, org).ifPresent(d -> {
			throw new DuplicateResourceException("Designation already exists in your organization: " + name);
		});

		DesignationEntity designation = new DesignationEntity();
		designation.setName(name);

		designation.setOrganization(org);

		return designationRepository.save(designation);
	}

	public List<DesignationEntity> getAllDesignations(Authentication authentication) {
		UserEntity user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = user.getOrganization();
		if (org == null) {
			throw new NotFoundException("User is not associated with any organization.");
		}

		return designationRepository.findByOrganization(org);
	}

}
