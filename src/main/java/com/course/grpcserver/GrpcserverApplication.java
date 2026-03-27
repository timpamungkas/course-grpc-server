package com.course.grpcserver;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.course.central.proto.resiliency.ResiliencyMessage.ResiliencyRequest;
import com.course.grpcserver.grpc.client.service.ClientHelloService;
import com.course.grpcserver.grpc.client.service.ClientResiliencyService;

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
