package com.travel.farecalc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TravelFareCalculatingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelFareCalculatingServiceApplication.class, args);
	}

}
