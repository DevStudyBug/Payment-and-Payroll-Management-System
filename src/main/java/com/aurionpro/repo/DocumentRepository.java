package com.aurionpro.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.DocumentEntity;
import com.aurionpro.entity.OrganizationEntity;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

	List<DocumentEntity> findByOrganization(OrganizationEntity organization);
}