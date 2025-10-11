package com.aurionpro.config;

import java.util.HashSet;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.aurionpro.entity.UserEntity;
import com.aurionpro.entity.UserRoleEntity;
import com.aurionpro.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepo.findByUsername("bankAdmin").isEmpty()) {
            UserEntity bankAdmin = UserEntity.builder()
                    .username("bankAdmin")
                    .password(passwordEncoder.encode("admin1234"))
                    .email("bankadmin@example.com")
                    .status("ACTIVE")
                    .roles(new HashSet<>()) // IMPORTANT: initialize roles here
                    .build();

            UserRoleEntity role = new UserRoleEntity();
            role.setRole("BANK_ADMIN"); 
            role.setUser(bankAdmin);
            bankAdmin.getRoles().add(role);


            userRepo.save(bankAdmin);

            System.out.println("Bank Admin user created: username=bankAdmin, password=admin1234");
        }
    
    }
}

