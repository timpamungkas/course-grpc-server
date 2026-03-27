package com.course.grpcserver.grpc.client.interceptor;

import org.springframework.core.annotation.Order;
import org.springframework.grpc.client.GlobalClientInterceptor;
import org.springframework.stereotype.Component;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import lombok.extern.slf4j.Slf4j;

@Component
@GlobalClientInterceptor
@Order(100)
@Slf4j
public class ClientLogInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
            CallOptions callOptions, Channel channel) {
        log.info("[ClientLogInterceptor] sending request for method: {}", methodDescriptor.getFullMethodName());
        
        return channel.newCall(methodDescriptor, callOptions);
    }

}
