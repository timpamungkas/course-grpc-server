package com.course.grpcserver.grpc.client.service;

import java.time.Duration;
import java.util.List;

import com.course.central.proto.resiliency.ResiliencyMessage.ResiliencyRequest;
import com.course.central.proto.resiliency.ResiliencyMessage.ResiliencyResponse;

public interface ClientResiliencyService {

    public ResiliencyResponse callUnaryResiliency(ResiliencyRequest request, Duration timeout) throws Exception;

    public void callServerStreamingResiliency(ResiliencyRequest request, Duration timeout) throws Exception;

    public ResiliencyResponse callClientStreamingResiliency(List<ResiliencyRequest> requests, Duration timeout)
            throws Exception;

    public void callBidirectionalStreamingResiliency(List<ResiliencyRequest> requests, Duration timeout)
            throws Exception;

}
