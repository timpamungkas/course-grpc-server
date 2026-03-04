package com.course.grpcserver.grpc.client.service.impl;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.course.central.proto.hello.Hello.SayHelloRequest;
import com.course.central.proto.hello.Hello.SayHelloResponse;
import com.course.central.proto.hello.HelloServiceGrpc;
import com.course.grpcserver.grpc.client.service.ClientHelloService;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClientHelloServiceImpl implements ClientHelloService {

    private HelloServiceGrpc.HelloServiceBlockingV2Stub helloServiceBlockingStub;
    private HelloServiceGrpc.HelloServiceStub helloServiceAsyncStub;

    public ClientHelloServiceImpl(
            @Autowired HelloServiceGrpc.HelloServiceBlockingV2Stub helloServiceBlockingStub,
            @Autowired HelloServiceGrpc.HelloServiceStub helloServiceAsyncStub) {
        this.helloServiceBlockingStub = helloServiceBlockingStub;
        this.helloServiceAsyncStub = helloServiceAsyncStub;
    }

    @Override
    public String sayHello(String name) {
        try {
            var request = SayHelloRequest.newBuilder().setName(name).build();
            var response = helloServiceBlockingStub.sayHello(request);

            return response.getGreet();
        } catch (Exception e) {
            throw new RuntimeException("Error while calling gRPC service", e);
        }
    }

    @Override
    public void sayServerStreamingHello(String name) throws Exception {
        try {
            var request = SayHelloRequest.newBuilder().setName(name).build();
            var responseStream = helloServiceBlockingStub.sayManyHellos(request);

            while (responseStream.hasNext()) {
                var response = responseStream.read();
                log.info(response.getGreet());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while calling gRPC service", e);
        }
    }

    @Override
    public SayHelloResponse sayClientStreamingHello(List<String> names) throws Exception {
        var latch = new CountDownLatch(1);
        var responseHolder = new AtomicReference<SayHelloResponse>();

        StreamObserver<SayHelloRequest> requestObserver = helloServiceAsyncStub.sayHelloToEveryone(
                new StreamObserver<>() {

                    @Override
                    public void onNext(SayHelloResponse response) {
                        responseHolder.set(response);
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("[sayHelloToEveryone] error occurred", t);
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        latch.countDown();
                    }
                });

        for (var name : names) {
            var request = SayHelloRequest.newBuilder().setName(name).build();
            requestObserver.onNext(request);
            TimeUnit.MILLISECONDS.sleep(500);
        }

        requestObserver.onCompleted();
        latch.await(10, TimeUnit.SECONDS);

        return responseHolder.get();
    }

}
