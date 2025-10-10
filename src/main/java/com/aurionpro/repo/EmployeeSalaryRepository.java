package com.aurionpro.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.EmployeeEntity;
import com.aurionpro.entity.EmployeeSalaryEntity;

@Repository
public interface EmployeeSalaryRepository extends JpaRepository<EmployeeSalaryEntity, Long> {
    Optional<EmployeeSalaryEntity> findByEmployee(EmployeeEntity employee);
}

