package com.course.grpcserver.grpc.client.service;

public interface ClientHelloService {

    public String sayHello(String name) throws Exception;

    public void sayServerStreamingHello(String name) throws Exception;

}
