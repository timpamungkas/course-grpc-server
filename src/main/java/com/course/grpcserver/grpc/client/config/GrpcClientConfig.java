package com.course.grpcserver.grpc.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

import com.course.central.proto.hello.HelloServiceGrpc;

@Configuration
public class GrpcClientConfig {

    @Bean
    HelloServiceGrpc.HelloServiceBlockingV2Stub helloServiceBlockingStub(GrpcChannelFactory cf) {
        return HelloServiceGrpc.newBlockingV2Stub(cf.createChannel("default-channel"));
    }

    @Bean
    HelloServiceGrpc.HelloServiceStub helloServiceAsyncStub(GrpcChannelFactory cf) {
        return HelloServiceGrpc.newStub(cf.createChannel("default-channel"));
    }

}
