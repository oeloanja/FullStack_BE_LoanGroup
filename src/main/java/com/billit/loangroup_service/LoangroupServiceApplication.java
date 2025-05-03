package com.billit.loangroup_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class LoangroupServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoangroupServiceApplication.class, args);
	}

}
