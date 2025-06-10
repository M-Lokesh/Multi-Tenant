package com.i2i.tenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@SpringBootApplication
public class TenantApplication {

	public static void main(String[] args) {
		SpringApplication.run(TenantApplication.class, args);
		System.out.println("Application started successfully");
	}

}
