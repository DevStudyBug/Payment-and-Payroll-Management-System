package com.aurionpro.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aurionpro.entity.ConcernEntity;

public interface ConcernRepository extends JpaRepository<ConcernEntity, Long> {

    List<ConcernEntity> findByEmployee_EmployeeId(Long employeeId);

    ConcernEntity findByTicketNumber(String ticketNumber);
    
    
    @Query("SELECT c FROM ConcernEntity c " +
    	       "JOIN FETCH c.employee e " +
    	       "JOIN FETCH c.organization o " +
    	       "WHERE e.employeeId = :employeeId")
    	List<ConcernEntity> findByEmployeeWithDetails(@Param("employeeId") Long employeeId);

}
