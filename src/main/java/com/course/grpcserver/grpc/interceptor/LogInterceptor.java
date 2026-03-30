package com.course.grpcserver.grpc.interceptor;

import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;

@Component
// @GlobalServerInterceptor
@Order(100)
@Slf4j
public class LogInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        log.info("[LogInterceptor] receiving request for method: {}",
                serverCall.getMethodDescriptor().getFullMethodName());

        return next.startCall(serverCall, headers);
    }

}
