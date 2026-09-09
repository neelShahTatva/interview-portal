package com.tatvasoft.interview_portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InterviewPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(InterviewPortalApplication.class, args);
	}

}
