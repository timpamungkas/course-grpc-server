package com.course.grpcserver.grpc.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

import com.course.central.proto.bank.BankServiceGrpc;
import com.course.central.proto.hello.HelloServiceGrpc;
import com.course.central.proto.resiliency.ResiliencyServiceGrpc;

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

    @Bean
    BankServiceGrpc.BankServiceBlockingV2Stub bankServiceBlockingStub(GrpcChannelFactory cf) {
        return BankServiceGrpc.newBlockingV2Stub(cf.createChannel("default-channel"));
    }

    @Bean
    BankServiceGrpc.BankServiceStub bankServiceAsyncStub(GrpcChannelFactory cf) {
        return BankServiceGrpc.newStub(cf.createChannel("default-channel"));
    }

    @Bean
    ResiliencyServiceGrpc.ResiliencyServiceBlockingV2Stub resiliencyServiceBlockingStub(GrpcChannelFactory cf) {
        return ResiliencyServiceGrpc.newBlockingV2Stub(cf.createChannel("default-channel"));
    }

    @Bean
    ResiliencyServiceGrpc.ResiliencyServiceStub resiliencyServiceAsyncStub(GrpcChannelFactory cf) {
        return ResiliencyServiceGrpc.newStub(cf.createChannel("default-channel"));
    }

}
