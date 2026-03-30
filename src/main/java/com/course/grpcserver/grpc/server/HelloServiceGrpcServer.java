package com.course.grpcserver.grpc.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;

import com.course.central.proto.hello.Hello.SayHelloRequest;
import com.course.central.proto.hello.Hello.SayHelloResponse;
import com.course.central.proto.hello.HelloServiceGrpc;
import com.course.grpcserver.service.HelloService;

import io.grpc.stub.StreamObserver;

@GrpcService
public class HelloServiceGrpcServer extends HelloServiceGrpc.HelloServiceImplBase {

    private HelloService helloService;

    public HelloServiceGrpcServer(HelloService helloService) {
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
            responseObserver.onError(e);
        }
    }

    @Override
    public StreamObserver<SayHelloRequest> sayHelloToEveryone(StreamObserver<SayHelloResponse> responseObserver) {
        List<String> names = new ArrayList<>();

        return new StreamObserver<SayHelloRequest>() {

            @Override
            public void onNext(SayHelloRequest request) {
                names.add(request.getName());
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                var mergedNames = String.join(", ", names);
                var message = helloService.generateHello(mergedNames);

                var response = SayHelloResponse.newBuilder()
                        .setGreet(message)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<SayHelloRequest> sayHelloContinuous(StreamObserver<SayHelloResponse> responseObserver) {
        return new StreamObserver<SayHelloRequest>() {

            @Override
            public void onNext(SayHelloRequest request) {
                var name = request.getName();
                var message = helloService.generateHello(name);

                var response = SayHelloResponse.newBuilder()
                        .setGreet(message)
                        .build();

                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

}