package com.course.grpcserver.grpc.client.interceptor;

import org.springframework.core.annotation.Order;
import org.springframework.grpc.client.GlobalClientInterceptor;
import org.springframework.stereotype.Component;

import com.course.central.proto.hello.Hello.SayHelloRequest;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

@Component
@GlobalClientInterceptor
@Order(200)
public class ClientModifyRequestMessage implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        var clientCall = next.newCall(method, callOptions);

        return new ForwardingClientCall.SimpleForwardingClientCall<>(clientCall) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(Metadata.Key.of("my-request-metadata-key-1", Metadata.ASCII_STRING_MARSHALLER),
                        "my-request-metadata-value-1");
                headers.put(Metadata.Key.of("my-request-metadata-key-2", Metadata.ASCII_STRING_MARSHALLER),
                        "my-request-metadata-value-2");
                super.start(responseListener, headers);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void sendMessage(ReqT message) {
                if (message instanceof SayHelloRequest request) {
                    message = (ReqT) request.toBuilder()
                            .setName("[MODIFIED BY CLIENT INTERCEPTOR - 1]" + request.getName())
                            .build();
                }
                super.sendMessage(message);
            }
        };
    }

}
