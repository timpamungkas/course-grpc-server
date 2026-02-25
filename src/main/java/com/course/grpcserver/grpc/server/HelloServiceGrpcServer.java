package com.course.grpcserver.grpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.course.central.proto.hello.Hello.SayHelloRequest;
import com.course.central.proto.hello.Hello.SayHelloResponse;
import com.course.central.proto.hello.HelloServiceGrpc;
import com.course.grpcserver.grpc.context.GrpcContextKeys;
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

        String babiValue = GrpcContextKeys.BABI_METADATA_VALUE.get();
        if (babiValue != null) {
            log.info("[sayHello] babi metadata value (modified): {}", babiValue);
            message = message + " [babi: " + babiValue + "]";
        }

        var response = SayHelloResponse.newBuilder()
                .setGreet(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
