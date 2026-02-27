package com.course.grpcserver.grpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.course.central.proto.hello.Hello.SayHelloRequest;
import com.course.central.proto.hello.Hello.SayHelloResponse;
import com.course.central.proto.hello.HelloServiceGrpc;
import com.course.grpcserver.grpc.context.GrpcContextKeyConstants;
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
    public void sayHelloUnaryOne(SayHelloRequest request, StreamObserver<SayHelloResponse> responseObserver) {
        var name = request.getName();
        log.info("[sayHelloUnaryOne] received name {}", name);

        var message = helloService.generateHello(name);
        var response = SayHelloResponse.newBuilder()
                .setGreet(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void sayHelloUnaryTwo(SayHelloRequest request, StreamObserver<SayHelloResponse> responseObserver) {
        var name = request.getName();

        log.info("[sayHelloUnaryTwo] received name {}", name);

        var thisIsForGamma = "This is for gamma";

        // Expose thisIsForGamma as response metadata via the interceptor's Metadata holder
        var gammaMetadata = GrpcContextKeyConstants.CONTEXT_KEY_GAMMA_HOLDER.get();
        if (gammaMetadata != null) {
            gammaMetadata.put(GrpcContextKeyConstants.METADATA_KEY_GAMMA_CONTEXT, thisIsForGamma);
        }

        var message = helloService.generateHello(name);
        var response = SayHelloResponse.newBuilder()
                .setGreet(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void sayHelloServerStreamOne(SayHelloRequest request, StreamObserver<SayHelloResponse> responseObserver) {
        var name = request.getName();

        log.info("[sayHelloServerStreamOne] received name {}", name);

        for (int i = 1; i <= 3; i++) {
            var message = helloService.generateHello(name + " (stream " + i + ")");
            var response = SayHelloResponse.newBuilder()
                    .setGreet(message)
                    .build();
            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

    @Override
    public void sayHelloServerStreamTwo(SayHelloRequest request, StreamObserver<SayHelloResponse> responseObserver) {
        var name = request.getName();

        log.info("[sayHelloServerStreamTwo] received name {}", name);

        for (int i = 1; i <= 3; i++) {
            var message = helloService.generateHello(name + " (stream " + i + ")");
            var response = SayHelloResponse.newBuilder()
                    .setGreet(message)
                    .build();
            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<SayHelloRequest> sayHelloClientStreamOne(StreamObserver<SayHelloResponse> responseObserver) {
        log.info("[sayHelloClientStreamOne] client stream started");

        return new StreamObserver<SayHelloRequest>() {
            private final StringBuilder names = new StringBuilder();

            @Override
            public void onNext(SayHelloRequest request) {
                log.info("[sayHelloClientStreamOne] received name {}", request.getName());
                if (!names.isEmpty())
                    names.append(", ");
                names.append(request.getName());
            }

            @Override
            public void onError(Throwable t) {
                log.error("[sayHelloClientStreamOne] error", t);
            }

            @Override
            public void onCompleted() {
                var message = helloService.generateHello(names.toString());
                var response = SayHelloResponse.newBuilder()
                        .setGreet(message)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<SayHelloRequest> sayHelloClientStreamTwo(StreamObserver<SayHelloResponse> responseObserver) {
        log.info("[sayHelloClientStreamTwo] client stream started");

        return new StreamObserver<SayHelloRequest>() {
            private final StringBuilder names = new StringBuilder();

            @Override
            public void onNext(SayHelloRequest request) {
                log.info("[sayHelloClientStreamTwo] received name {}", request.getName());
                if (!names.isEmpty())
                    names.append(", ");
                names.append(request.getName());
            }

            @Override
            public void onError(Throwable t) {
                log.error("[sayHelloClientStreamTwo] error", t);
            }

            @Override
            public void onCompleted() {
                var message = helloService.generateHello(names.toString());
                var response = SayHelloResponse.newBuilder()
                        .setGreet(message)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<SayHelloRequest> sayHelloBidirectionalStreamOne(
            StreamObserver<SayHelloResponse> responseObserver) {
        log.info("[sayHelloBidirectionalStreamOne] bidirectional stream started");

        return new StreamObserver<SayHelloRequest>() {
            @Override
            public void onNext(SayHelloRequest request) {
                var name = request.getName();
                log.info("[sayHelloBidirectionalStreamOne] received name {}", name);

                var message = helloService.generateHello(name);
                var response = SayHelloResponse.newBuilder()
                        .setGreet(message)
                        .build();
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                log.error("[sayHelloBidirectionalStreamOne] error", t);
            }

            @Override
            public void onCompleted() {
                log.info("[sayHelloBidirectionalStreamOne] stream completed");
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<SayHelloRequest> sayHelloBidirectionalStreamTwo(
            StreamObserver<SayHelloResponse> responseObserver) {
        log.info("[sayHelloBidirectionalStreamTwo] bidirectional stream started");

        return new StreamObserver<SayHelloRequest>() {
            @Override
            public void onNext(SayHelloRequest request) {
                var name = request.getName();
                log.info("[sayHelloBidirectionalStreamTwo] received name {}", name);

                var message = helloService.generateHello(name);
                var response = SayHelloResponse.newBuilder()
                        .setGreet(message)
                        .build();
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                log.error("[sayHelloBidirectionalStreamTwo] error", t);
            }

            @Override
            public void onCompleted() {
                log.info("[sayHelloBidirectionalStreamTwo] stream completed");
                responseObserver.onCompleted();
            }
        };
    }

}
