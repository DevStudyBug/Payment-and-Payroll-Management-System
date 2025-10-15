package com.aurionpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PaymentAndPayrollManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentAndPayrollManagementSystemApplication.class, args);
	}
	
}
