package com.aurionpro.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aurionpro.entity.OrganizationEntity;

public interface OrganizationRepository  extends JpaRepository<OrganizationEntity, Long>{
	Optional<OrganizationEntity> findByRegistrationNo(String registrationNo);
}
