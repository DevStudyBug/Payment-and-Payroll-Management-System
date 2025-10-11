package com.aurionpro.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.EmployeeEntity;
import com.aurionpro.entity.SalaryDisbursementEntity;

@Repository
public interface SalaryDisbursementRepository extends JpaRepository<SalaryDisbursementEntity, Long> {

	List<SalaryDisbursementEntity> findByOrganization_OrgIdAndSalaryMonth(Long orgId, String salaryMonth);

	List<SalaryDisbursementEntity> findByPaymentRequest_PaymentId(Long paymentRequestId);

	Optional<SalaryDisbursementEntity> findByEmployee_EmployeeIdAndSalaryMonth(Long employeeId, String salaryMonth);

	// find all pending or under review

	@Query("SELECT s FROM SalaryDisbursementEntity s WHERE s.organization.orgId = :orgId AND s.status IN ('GENERATED', 'UNDER_REVIEW')")
	List<SalaryDisbursementEntity> getPendingDisbursementsByOrg(Long orgId);

	Optional<SalaryDisbursementEntity> findByEmployeeAndSalaryMonthAndStatus(EmployeeEntity employee,
			String salaryMonth, String status);

}
