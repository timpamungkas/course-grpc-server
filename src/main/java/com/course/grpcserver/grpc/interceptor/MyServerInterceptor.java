package com.course.grpcserver.grpc.interceptor;

import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

import com.course.grpcserver.grpc.context.GrpcContextKeyConstants;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

@GlobalServerInterceptor
@Component
@Slf4j
public class MyServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        var gammaMetadata = new Metadata();
        var newContext = Context.current()
                .withValue(GrpcContextKeyConstants.CONTEXT_KEY_GAMMA, gammaMetadata);

        var wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void sendHeaders(Metadata responseHeaders) {
                responseHeaders.merge(gammaMetadata);
                super.sendHeaders(responseHeaders);
            }
        };

        return Contexts.interceptCall(newContext, wrappedCall, headers, next);
    }

}
