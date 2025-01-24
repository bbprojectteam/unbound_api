package com.badboys.unbound_auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UnboundAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnboundAuthApplication.class, args);
	}

}
