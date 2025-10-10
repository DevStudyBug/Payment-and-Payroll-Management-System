package com.aurionpro.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.DocumentEntity;
import com.aurionpro.entity.EmployeeEntity;
import com.aurionpro.entity.OrganizationEntity;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

	List<DocumentEntity> findByOrganization(OrganizationEntity organization);

	Optional<DocumentEntity> findByEmployeeAndFileType(EmployeeEntity employee, String fileType);

	boolean existsByEmployeeAndFileType(EmployeeEntity employee, String fileType);

	List<DocumentEntity> findByEmployee(EmployeeEntity employee);

	long countByEmployee(EmployeeEntity employee);

	long countByEmployeeAndStatus(EmployeeEntity employee, String status);

	boolean existsByEmployeeAndFileTypeAndStatus(EmployeeEntity employee, String fileType, String status);

	boolean existsByEmployeeAndStatus(EmployeeEntity employee, String status);
}