package com.i2i.tenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@SpringBootApplication
@EnableWebSecurity(debug = true)
public class TenantApplication {

	public static void main(String[] args) {
		SpringApplication.run(TenantApplication.class, args);
		System.out.println("Application Started---------------------");
	}

}
