package com.badboys.unbound_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UnboundGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnboundGatewayApplication.class, args);
	}

}
