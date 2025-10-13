package com.aurionpro.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.entity.VendorEntity;

public interface VendorRepository extends JpaRepository<VendorEntity, Long>  {
	  List<VendorEntity> findByOrganizationAndDeletedFalse(OrganizationEntity organization);

	    Optional<VendorEntity> findByVendorIdAndOrganizationAndDeletedFalse(Long vendorId, OrganizationEntity organization);
}
