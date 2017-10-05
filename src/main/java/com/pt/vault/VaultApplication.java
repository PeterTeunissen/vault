package com.pt.vault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class VaultApplication {

	public static void main(String[] args) {
		System.setProperty("bucketSize", "100");
		SpringApplication.run(VaultApplication.class, args);
	}

}
