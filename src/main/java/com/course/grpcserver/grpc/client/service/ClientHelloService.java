package com.course.grpcserver.grpc.client.service;

import java.util.List;

import com.course.central.proto.hello.Hello.SayHelloResponse;

public interface ClientHelloService {

    public String sayHello(String name) throws Exception;

    public void sayServerStreamingHello(String name) throws Exception;

    public SayHelloResponse sayClientStreamingHello(List<String> names) throws Exception;

}
