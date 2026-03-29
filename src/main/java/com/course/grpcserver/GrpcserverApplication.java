package com.course.grpcserver;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@EnableResilientMethods
@EnableScheduling
@Slf4j
@SpringBootApplication
public class GrpcserverApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(GrpcserverApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
	}
	
}
