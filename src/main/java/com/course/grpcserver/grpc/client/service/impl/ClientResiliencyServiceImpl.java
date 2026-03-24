package com.course.grpcserver.grpc.client.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.course.central.proto.resiliency.ResiliencyMessage.ResiliencyRequest;
import com.course.central.proto.resiliency.ResiliencyMessage.ResiliencyResponse;
import com.course.central.proto.resiliency.ResiliencyServiceGrpc;
import com.course.grpcserver.grpc.client.service.ClientResiliencyService;

import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClientResiliencyServiceImpl implements ClientResiliencyService {

    private ResiliencyServiceGrpc.ResiliencyServiceBlockingV2Stub resiliencyServiceBlockingStub;
    private ResiliencyServiceGrpc.ResiliencyServiceStub resiliencyServiceAsyncStub;

    public ClientResiliencyServiceImpl(
            @Autowired ResiliencyServiceGrpc.ResiliencyServiceBlockingV2Stub resiliencyServiceBlockingStub,
            @Autowired ResiliencyServiceGrpc.ResiliencyServiceStub resiliencyServiceAsyncStub) {
        this.resiliencyServiceBlockingStub = resiliencyServiceBlockingStub;
        this.resiliencyServiceAsyncStub = resiliencyServiceAsyncStub;
    }

    @Retryable(includes = { StatusException.class,
            StatusRuntimeException.class }, maxRetries = 5, delayString = "3s", maxDelayString = "10s", multiplier = 2)
    @Override
    public ResiliencyResponse callUnaryResiliency(ResiliencyRequest request, Duration timeout) throws Exception {
        log.info("[callUnaryResiliency] called");
        var stub = resiliencyServiceBlockingStub;

        if (timeout != null) {
            stub = stub.withDeadlineAfter(timeout);
        }

        return stub.unaryResiliency(request);
    }

    @Retryable(includes = { StatusException.class,
            StatusRuntimeException.class }, maxRetries = 5, delayString = "3s", maxDelayString = "10s", multiplier = 2)
    @Override
    public void callServerStreamingResiliency(ResiliencyRequest request, Duration timeout) throws Exception {
        log.info("[callServerStreamingResiliency] called");
        var stub = resiliencyServiceBlockingStub;

        if (timeout != null) {
            stub = stub.withDeadlineAfter(timeout);
        }

        var responseStream = stub.serverStreamingResiliency(request);

        while (responseStream.hasNext()) {
            var response = responseStream.read();
            log.info("[callServerStreamingResiliency] received a response: {}", response.getDummyString());
        }
    }

    @Retryable(includes = { StatusException.class,
            StatusRuntimeException.class }, maxRetries = 5, delayString = "3s", maxDelayString = "10s", multiplier = 2)
    @Override
    public ResiliencyResponse callClientStreamingResiliency(List<ResiliencyRequest> requests, Duration timeout)
            throws Exception {
        log.info("[callClientStreamingResiliency] called");
        var latch = new CountDownLatch(1);
        var responseHolder = new AtomicReference<ResiliencyResponse>();
        var errorHolder = new AtomicReference<Throwable>();

        var stub = resiliencyServiceAsyncStub;

        if (timeout != null) {
            stub = stub.withDeadlineAfter(timeout);
        }

        var requestObserver = stub.clientStreamingResiliency(
                new StreamObserver<>() {

                    @Override
                    public void onNext(ResiliencyResponse response) {
                        responseHolder.set(response);
                    }

                    @Override
                    public void onError(Throwable t) {
                        errorHolder.set(t);
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        latch.countDown();
                    }
                });

        for (var request : requests) {
            requestObserver.onNext(request);
        }

        requestObserver.onCompleted();

        var latchAwaitTime = timeout == null ? Duration.ofMinutes(2) : timeout.plusSeconds(3);
        latch.await(latchAwaitTime.toSeconds(), TimeUnit.SECONDS);

        boolean completed = latch.await(latchAwaitTime.toSeconds(), TimeUnit.SECONDS);

        var error = errorHolder.get();
        if (error != null) {
            if (error instanceof Exception ex) {
                throw ex;
            }
            throw new RuntimeException(error);
        }

        if (!completed) {
            throw new RuntimeException("Timed out waiting for client streaming response");
        }

        var response = responseHolder.get();
        log.info("[callClientStreamingResiliency] dummyString={}", response.getDummyString());
        return response;
    }

    @Retryable(includes = { StatusException.class,
            StatusRuntimeException.class }, maxRetries = 5, delayString = "3s", maxDelayString = "10s", multiplier = 2)
    @Override
    public void callBidirectionalStreamingResiliency(List<ResiliencyRequest> requests, Duration timeout)
            throws Exception {
        log.info("[callBidirectionalStreamingResiliency] called");
        var latch = new CountDownLatch(1);
        var errorHolder = new AtomicReference<Throwable>();

        var stub = resiliencyServiceAsyncStub;

        if (timeout != null) {
            stub = stub.withDeadlineAfter(timeout);
        }

        var requestObserver = stub.bidirectionalResiliency(
                new StreamObserver<>() {

                    @Override
                    public void onNext(ResiliencyResponse response) {
                        log.info("[callBidirectionalStreamingResiliency] dummyString={}", response.getDummyString());
                    }

                    @Override
                    public void onError(Throwable t) {
                        errorHolder.set(t);
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        log.info("[callBidirectionalStreamingResiliency] stream completed");
                        latch.countDown();
                    }
                });

        for (var request : requests) {
            requestObserver.onNext(request);
        }

        requestObserver.onCompleted();

        var latchAwaitTime = timeout == null ? Duration.ofMinutes(2) : timeout.plusSeconds(3);
        boolean completed = latch.await(latchAwaitTime.toSeconds(), TimeUnit.SECONDS);

        var error = errorHolder.get();
        if (error != null) {
            if (error instanceof Exception ex) {
                throw ex;
            }
            throw new RuntimeException(error);
        }

        if (!completed) {
            throw new RuntimeException("Timed out waiting for bidirectional streaming response");
        }
    }

}
