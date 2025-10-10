package com.aurionpro.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.EmployeeBankDetailsEntity;
import com.aurionpro.entity.EmployeeEntity;

@Repository
public interface EmployeeBankDetailsRepository extends JpaRepository<EmployeeBankDetailsEntity, Long> {

	boolean existsByEmployee(EmployeeEntity employee);

	EmployeeBankDetailsEntity findByEmployee(EmployeeEntity employee);
}