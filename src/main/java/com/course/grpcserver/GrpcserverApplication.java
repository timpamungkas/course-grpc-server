package com.course.grpcserver;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.course.central.proto.resiliency.ResiliencyMessage.ResiliencyRequest;
import com.course.grpcserver.grpc.client.service.ClientResiliencyService;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import lombok.extern.slf4j.Slf4j;

@EnableScheduling
@Slf4j
@SpringBootApplication
public class GrpcserverApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(GrpcserverApplication.class, args);
	}

	private ClientResiliencyService clientResiliencyService;

	public GrpcserverApplication(@Autowired ClientResiliencyService clientResiliencyService) {
		this.clientResiliencyService = clientResiliencyService;
	}

	private void printExceptionStatus(String caller, Throwable e) {
		var status = Status.fromThrowable(e);
		log.error("{} catch {}: {}", caller, e.getClass().getSimpleName(), status);

		var statusDetail = StatusProto.fromThrowable(e);
		if (statusDetail != null) {
			log.error("{} statusDetail: status code={}, message={}", caller, statusDetail.getCode(),
					statusDetail.getMessage());
			statusDetail.getDetailsList()
					.forEach(
							detail -> log.error("  - {} error detail type={}, value={}",
									caller, detail.getTypeUrl(), detail));
		}
	}

	@Override
	public void run(String... args) throws Exception {
		TimeUnit.SECONDS.sleep(5);

		demoUnaryResiliency();
		// demoServerStreamingResiliency();
		// demoClientStreamingResiliency();
		// demoBidirectionalStreamingResiliency();
	}

	private void demoUnaryResiliency() {
		try {
			var minDelaySecond = 0;
			var maxDelaySecond = 1;
			var statusCodes = List.of(0);
			var timeout = Duration.ofSeconds(5);

			var request = ResiliencyRequest.newBuilder()
					.setMinDelaySecond(minDelaySecond)
					.setMaxDelaySecond(maxDelaySecond)
					.addAllStatusCodes(statusCodes)
					.build();

			var res = clientResiliencyService.callUnaryResiliency(request, timeout);

			log.info("unaryResiliency: {}", res.getDummyString());
		} catch (StatusException | StatusRuntimeException e) {
			printExceptionStatus("unaryResiliency", e);
		} catch (Exception e) {
			log.error("unaryResiliency catch exception: {}", e.getMessage(), e);
		}
	}

	private void demoServerStreamingResiliency() {
		try {
			var minDelaySecond = 0;
			var maxDelaySecond = 1;
			var statusCodes = List.of(0);
			var timeout = Duration.ofSeconds(17);

			var request = ResiliencyRequest.newBuilder()
					.setMinDelaySecond(minDelaySecond)
					.setMaxDelaySecond(maxDelaySecond)
					.addAllStatusCodes(statusCodes)
					.build();

			clientResiliencyService.callServerStreamingResiliency(request, timeout);
		} catch (StatusException | StatusRuntimeException e) {
			printExceptionStatus("serverStreamingResiliency", e);
		} catch (Exception e) {
			log.error("serverStreamingResiliency catch exception: {}", e.getMessage(), e);
		}
	}

	private void demoClientStreamingResiliency() {
		try {
			var minDelaySecond = 0;
			var maxDelaySecond = 1;
			var statusCodes = List.of(0);
			var timeout = Duration.ofSeconds(20);

			var request1 = ResiliencyRequest.newBuilder()
					.setMinDelaySecond(minDelaySecond)
					.setMaxDelaySecond(maxDelaySecond)
					.addAllStatusCodes(statusCodes)
					.build();

			var request2 = ResiliencyRequest.newBuilder()
					.setMinDelaySecond(minDelaySecond)
					.setMaxDelaySecond(maxDelaySecond)
					.addAllStatusCodes(statusCodes)
					.build();

			var request3 = ResiliencyRequest.newBuilder()
					.setMinDelaySecond(minDelaySecond)
					.setMaxDelaySecond(maxDelaySecond)
					.addAllStatusCodes(statusCodes)
					.build();

			var requests = List.of(request1, request2, request3);

			var response = clientResiliencyService.callClientStreamingResiliency(requests, timeout);

			log.info("clientStreamingResiliency: {}", response.getDummyString());
		} catch (StatusException | StatusRuntimeException e) {
			printExceptionStatus("clientStreamingResiliency", e);
		} catch (Exception e) {
			log.error("clientStreamingResiliency catch exception: {}", e.getMessage(), e);
		}
	}

	private void demoBidirectionalStreamingResiliency() {
		try {
			var minDelaySecond = 0;
			var maxDelaySecond = 1;
			var statusCodes = List.of(0);
			var timeout = Duration.ofSeconds(20);

			var request1 = ResiliencyRequest.newBuilder()
					.setMinDelaySecond(minDelaySecond)
					.setMaxDelaySecond(maxDelaySecond)
					.addAllStatusCodes(statusCodes)
					.build();

			var request2 = ResiliencyRequest.newBuilder()
					.setMinDelaySecond(minDelaySecond)
					.setMaxDelaySecond(maxDelaySecond)
					.addAllStatusCodes(statusCodes)
					.build();

			var request3 = ResiliencyRequest.newBuilder()
					.setMinDelaySecond(minDelaySecond)
					.setMaxDelaySecond(maxDelaySecond)
					.addAllStatusCodes(statusCodes)
					.build();

			var requests = List.of(request1, request2, request3);

			clientResiliencyService.callBidirectionalStreamingResiliency(requests, timeout);
		} catch (StatusException | StatusRuntimeException e) {
			printExceptionStatus("bidirectionalResiliency", e);
		} catch (Exception e) {
			log.error("bidirectionalResiliency catch exception: {}", e.getMessage(), e);
		}
	}

}
