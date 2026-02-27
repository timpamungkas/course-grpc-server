package com.course.grpcserver.grpc.interceptor;

import org.springframework.stereotype.Component;

import com.course.central.proto.hello.Hello.SayHelloRequest;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

@Component
public class ServerInterceptorIncoming implements ServerInterceptor {

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        var listener = next.startCall(call, headers);

        return new SimpleForwardingServerCallListener<ReqT>(listener) {
            @Override
            public void onMessage(ReqT message) {
                if (message instanceof SayHelloRequest request) {
                    message = (ReqT) request.toBuilder()
                            .setName(request.getName() + " - intercepted")
                            .build();
                }
                super.onMessage(message);
            }
        };

    }

}
