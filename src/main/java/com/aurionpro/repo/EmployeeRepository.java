package com.aurionpro.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.EmployeeEntity;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
	List<EmployeeEntity> findByOrganization_OrgIdAndStatusIgnoreCase(Long orgId, String status);

	 List<EmployeeEntity> findByOrganization_OrgId(Long orgId);
	 
	 Optional<EmployeeEntity> findByUser_Username(String username);
	 

}
