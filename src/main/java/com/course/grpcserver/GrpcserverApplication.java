package com.course.grpcserver;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.course.grpcserver.grpc.client.service.ClientHelloService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class GrpcserverApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(GrpcserverApplication.class, args);
	}

	private ClientHelloService clientHelloService;

	public GrpcserverApplication(@Autowired ClientHelloService clientHelloService) {
		this.clientHelloService = clientHelloService;
	}

	@Override
	public void run(String... args) throws Exception {
		TimeUnit.SECONDS.sleep(5);

		var randomName = "Carter Hall " + ThreadLocalRandom.current().nextInt(1000);

		clientHelloService.sayServerStreamingHello(randomName);
	}

}
