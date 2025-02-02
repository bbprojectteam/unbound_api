package com.badboys.unbound_match;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UnboundMatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnboundMatchApplication.class, args);
	}

}
