package com.course.grpcserver;

import java.time.Duration;
import java.util.List;

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
		// demoUnaryResiliency();
		// demoServerStreamingResiliency();
		// demoClientStreamingResiliency();
		demoBidirectionalStreamingResiliency();
	}

	private void demoUnaryResiliency() {
		try {
			// TODO: change the parameters to trigger different scenarios, e.g.:
			// - set maxDelaySecond to less than timeout to get successful response
			// - set maxDelaySecond to more than timeout to trigger deadline exceeded error
			// - add non-zero value to statusCodes to trigger the corresponding error status

			var minDelaySecond = 0;
			var maxDelaySecond = 3;
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
			// TODO: change the parameters to trigger different scenarios, e.g.:
			// - suppose both minDelaySecond and maxDelaySecond is 3s and the timeout is set to 16s;
			//   we should receive at least 5 responses before the deadline exceeded error occurs
			// - add non-zero value to statusCodes to trigger the corresponding error status returned by server
			
			var minDelaySecond = 3;
			var maxDelaySecond = 3;
			var statusCodes = List.of(0);
			var timeout = Duration.ofSeconds(16);

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
			// TODO: change the parameters to trigger different scenarios, e.g.:
			// Since we have 3 requests, if we set minDelaySecond and maxDelaySecond to 4s, 
			// the total processing time on server side would be around 12s.
			// - set timeout to more than total processing time to get successful response, e.g. 20s
			// - set timeout to less than total processing time to trigger deadline exceeded error, e.g. 7s
			// - add non-zero value to statusCodes to trigger the corresponding error status returned by server

			var minDelaySecond = 4;
			var maxDelaySecond = 4;
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
			// TODO: change the parameters to trigger different scenarios, e.g.:
			// Since we have 3 requests, if we set minDelaySecond and maxDelaySecond to 5s, 
			// the total processing time on server side would be around 15s.
			// - set timeout to more than total processing time to get successful response, e.g. 20s
			// - set timeout to less than total processing time to trigger deadline exceeded error, e.g. 7s
			// - add non-zero value to statusCodes to trigger the corresponding error status returned by server

			var minDelaySecond = 5;
			var maxDelaySecond = 5;
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
