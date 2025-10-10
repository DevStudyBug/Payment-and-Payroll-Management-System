package com.aurionpro.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.DesignationEntity;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.entity.SalaryTemplateEntity;

@Repository
public interface SalaryTemplateRepository extends JpaRepository<SalaryTemplateEntity, Long> {
	List<SalaryTemplateEntity> findByOrganization(OrganizationEntity organization);

	Page<SalaryTemplateEntity> findByOrganization(OrganizationEntity organization, Pageable pageable);
	Optional<SalaryTemplateEntity> findByOrganizationAndDesignation(OrganizationEntity organization, DesignationEntity designation);
}
