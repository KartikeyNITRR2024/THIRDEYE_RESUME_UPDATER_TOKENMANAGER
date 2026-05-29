package com.thirdeye30.resumehelper.tokenmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TokenmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TokenmanagerApplication.class, args);
	}

}
