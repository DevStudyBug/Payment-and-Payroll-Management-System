package com.aurionpro.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.constants.PaymentRequestType;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.entity.PaymentRequestEntity;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequestEntity, Long> {

    // Fetch all payment requests by organization
    List<PaymentRequestEntity> findByOrganization_OrgId(Long orgId);

    // Fetch all payroll requests (PAYROLL type)
    List<PaymentRequestEntity> findByRequestType(String requestType);

    //Fetch by requestType and status (for BankAdmin)
    List<PaymentRequestEntity> findByRequestTypeAndStatus(String requestType, String status);

    //Fetch all PENDING payroll requests
    List<PaymentRequestEntity> findByRequestTypeAndStatusOrderByRequestDateDesc(String requestType, String status);
    
    List<PaymentRequestEntity> findByOrganization(OrganizationEntity organization);
    
    @Query("""
    	    SELECT p FROM PaymentRequestEntity p
    	    WHERE (:status IS NULL OR LOWER(p.status) = LOWER(:status))
    	      AND (:requestType IS NULL OR LOWER(p.requestType) = LOWER(:requestType))
    	      AND (:orgId IS NULL OR p.organization.orgId = :orgId)
    	      AND (:startDate IS NULL OR p.requestDate >= :startDate)
    	      AND (:endDate IS NULL OR p.requestDate <= :endDate)
    	""")
    	Page<PaymentRequestEntity> findFilteredPaymentRequests(
    	        @Param("status") String status,
    	        @Param("requestType") String requestType,
    	        @Param("orgId") Long orgId,
    	        @Param("startDate") LocalDateTime startDate,
    	        @Param("endDate") LocalDateTime endDate,
    	        Pageable pageable);
    
    List<PaymentRequestEntity> findByOrganizationAndRequestType(OrganizationEntity organization, PaymentRequestType requestType);
}
