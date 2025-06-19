package com.formation.events_batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EventsBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventsBatchApplication.class, args);
	}

}
