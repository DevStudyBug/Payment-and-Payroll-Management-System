package com.aurionpro.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import com.aurionpro.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
	Optional<UserEntity> findByEmail(String email);
	Optional<UserEntity> findByUsername(String username);
	boolean existsByUsername(String username);

}
