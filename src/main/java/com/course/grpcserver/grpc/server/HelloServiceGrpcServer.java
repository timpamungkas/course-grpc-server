package com.course.grpcserver.grpc.server;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.course.central.proto.hello.Hello.SayHelloRequest;
import com.course.central.proto.hello.Hello.SayHelloResponse;
import com.course.central.proto.hello.HelloServiceGrpc;
import com.course.grpcserver.service.HelloService;

import io.grpc.stub.StreamObserver;

@Service
public class HelloServiceGrpcServer extends HelloServiceGrpc.HelloServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(HelloServiceGrpcServer.class);

    private HelloService helloService;

    public HelloServiceGrpcServer(@Autowired HelloService helloService) {
        this.helloService = helloService;
    }

    @Override
    public void sayHello(SayHelloRequest request, StreamObserver<SayHelloResponse> responseObserver) {
        var name = request.getName();
        var message = helloService.generateHello(name);

        var response = SayHelloResponse.newBuilder()
                .setGreet(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void sayManyHellos(SayHelloRequest request, StreamObserver<SayHelloResponse> responseObserver) {
        try {
            var name = request.getName();

            for (int i = 0; i < 10; i++) {
                var message = helloService.generateHello(name + " " + i);
                var response = SayHelloResponse.newBuilder()
                        .setGreet(message)
                        .build();

                responseObserver.onNext(response);

                TimeUnit.MILLISECONDS.sleep(500);
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[sayManyHellos] error occurred", e);
            responseObserver.onError(e);
        }
    }

}