package com.aurionpro.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.DesignationEntity;
import com.aurionpro.entity.OrganizationEntity;

@Repository
public interface DesignationRepository extends JpaRepository<DesignationEntity, Long> {
	Optional<DesignationEntity> findByNameIgnoreCaseAndOrganization(String name, OrganizationEntity organization);

	List<DesignationEntity> findByOrganization(OrganizationEntity organization);
}
