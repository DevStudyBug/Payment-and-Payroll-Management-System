package com.aurionpro.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.ConcernEntity;
@Repository
public interface ConcernRepository extends JpaRepository<ConcernEntity, Long> {

	List<ConcernEntity> findByEmployee_EmployeeId(Long employeeId);

//	Optional<ConcernEntity> findByTicketNumber(String ticketNumber);
	@Query("SELECT c FROM ConcernEntity c JOIN FETCH c.employee WHERE c.ticketNumber = :ticketNumber")
	Optional<ConcernEntity> findByTicketNumber(@Param("ticketNumber") String ticketNumber);


	@Query("SELECT c FROM ConcernEntity c " + "JOIN FETCH c.employee e " + "JOIN FETCH c.organization o "
			+ "WHERE e.employeeId = :employeeId")
	List<ConcernEntity> findByEmployeeWithDetails(@Param("employeeId") Long employeeId);

	@Query("SELECT c FROM ConcernEntity c " + "JOIN FETCH c.employee e " + "JOIN FETCH c.organization o "
			+ "WHERE (:orgId IS NULL OR o.orgId = :orgId) " + "AND (:status IS NULL OR c.status = :status) "
			+ "AND (:priority IS NULL OR c.priority = :priority) " + "ORDER BY c.createdAt DESC")
	Page<ConcernEntity> findAllByFilters(@Param("orgId") Long orgId, @Param("status") String status,
			@Param("priority") String priority, Pageable pageable);

}
