package com.course.grpcserver.grpc.interceptor;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.course.grpcserver.grpc.constant.GrpcKeyConstants;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

@Component
@Order(400)
public class MetadataInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        var responseMetadata = new Metadata();
        var newContext = Context.current()
                .withValue(GrpcKeyConstants.CONTEXT_KEY_REQUEST_METADATA, headers)
                .withValue(GrpcKeyConstants.CONTEXT_KEY_RESPONSE_METADATA, responseMetadata);

        var wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void sendHeaders(Metadata responseHeaders) {
                responseHeaders.merge(responseMetadata);
                super.sendHeaders(responseHeaders);
            }
        };

        return Contexts.interceptCall(newContext, wrappedCall, headers, next);
    }

}
