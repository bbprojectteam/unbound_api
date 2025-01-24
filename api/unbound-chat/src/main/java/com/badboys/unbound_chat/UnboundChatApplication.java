package com.badboys.unbound_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UnboundChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnboundChatApplication.class, args);
	}

}
