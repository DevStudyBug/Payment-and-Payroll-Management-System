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

    // ✅ Fetch all templates for an organization
    List<SalaryTemplateEntity> findAllByOrganization(OrganizationEntity organization);
    List<SalaryTemplateEntity> findByOrganization(OrganizationEntity organization);

    // ✅ Check by organization and designation name (for string comparison)
    boolean existsByOrganizationAndDesignationNameIgnoreCase(OrganizationEntity organization, String designationName);

    // ✅ Get one template by organization and designation entity (used in registerEmployee)
    Optional<SalaryTemplateEntity> findByOrganizationAndDesignation(
            OrganizationEntity organization,
            DesignationEntity designation);

    // ✅ For admin dashboard pagination
    Page<SalaryTemplateEntity> findByOrganization(OrganizationEntity organization, Pageable pageable);
}
