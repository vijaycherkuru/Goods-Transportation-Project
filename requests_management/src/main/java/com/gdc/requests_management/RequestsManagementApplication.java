package com.gdc.requests_management;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RequestsManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(RequestsManagementApplication.class, args);
	}
}