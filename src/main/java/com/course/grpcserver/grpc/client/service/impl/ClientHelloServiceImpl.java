package com.course.grpcserver.grpc.client.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.course.central.proto.hello.Hello.SayHelloRequest;
import com.course.central.proto.hello.HelloServiceGrpc;
import com.course.grpcserver.grpc.client.service.ClientHelloService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClientHelloServiceImpl implements ClientHelloService {

    private HelloServiceGrpc.HelloServiceBlockingV2Stub helloServiceBlockingStub;

    public ClientHelloServiceImpl(@Autowired HelloServiceGrpc.HelloServiceBlockingV2Stub helloServiceBlockingStub) {
        this.helloServiceBlockingStub = helloServiceBlockingStub;
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

}
