package com.joblink.joblink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JoblinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(JoblinkApplication.class, args);
	}

}
