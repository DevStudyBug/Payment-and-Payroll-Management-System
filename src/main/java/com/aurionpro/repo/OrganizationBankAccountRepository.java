package com.aurionpro.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.OrganizationBankAccountEntity;
import com.aurionpro.entity.OrganizationEntity;

@Repository
public interface OrganizationBankAccountRepository extends JpaRepository<OrganizationBankAccountEntity, Long> {

	Optional<OrganizationBankAccountEntity> findByOrganization(OrganizationEntity organization);
}