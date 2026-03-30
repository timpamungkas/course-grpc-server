package com.course.grpcserver.grpc.interceptor;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.course.central.proto.hello.Hello.SayHelloRequest;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

@Component
@Order(200)
public class ModifyRequestInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        var listener = next.startCall(serverCall, headers);

        return new SimpleForwardingServerCallListener<ReqT>(listener) {
            @SuppressWarnings("unchecked")
            @Override
            public void onMessage(ReqT message) {
                if (message instanceof SayHelloRequest request) {
                    message = (ReqT) request.toBuilder()
                            .setName("[MODIFIED BY SERVER INTERCEPTOR - 1]" + request.getName())
                            .build();
                }
                super.onMessage(message);
            }
        };
    }

}
