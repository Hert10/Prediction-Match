package org.dice.match_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MatchMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatchMicroserviceApplication.class, args);
	}

}
