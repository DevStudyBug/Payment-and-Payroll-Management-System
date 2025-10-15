package com.aurionpro.repo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aurionpro.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
	Optional<UserEntity> findByEmail(String email);
	Optional<UserEntity> findByUsername(String username);
	boolean existsByUsername(String username);
	@Query("SELECT u FROM UserEntity u WHERE u.email IN :emails")
	List<UserEntity> findAllByEmailIn(@Param("emails") Set<String> emails);

	
    
    @Query("SELECT u FROM UserEntity u WHERE u.username IN :usernames")
    List<UserEntity> findAllByUsernameIn(@Param("usernames") Set<String> usernames);

}
