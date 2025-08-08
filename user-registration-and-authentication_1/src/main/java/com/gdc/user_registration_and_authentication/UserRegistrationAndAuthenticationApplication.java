package com.gdc.user_registration_and_authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableDiscoveryClient
public class  UserRegistrationAndAuthenticationApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserRegistrationAndAuthenticationApplication.class, args);
	}
}