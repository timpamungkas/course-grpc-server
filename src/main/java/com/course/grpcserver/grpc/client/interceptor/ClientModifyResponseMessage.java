package com.course.grpcserver.grpc.client.interceptor;

import org.springframework.core.annotation.Order;
import org.springframework.grpc.client.GlobalClientInterceptor;
import org.springframework.stereotype.Component;

import com.course.central.proto.hello.Hello.SayHelloResponse;
import com.course.central.proto.resiliency.ResiliencyMessage.ResiliencyResponse;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

@Component
// @GlobalClientInterceptor
@Order(300)
public class ClientModifyResponseMessage implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        var clientCall = next.newCall(method, callOptions);

        return new ForwardingClientCall.SimpleForwardingClientCall<>(clientCall) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onMessage(RespT message) {
                        if (message instanceof SayHelloResponse response) {
                            message = (RespT) response.toBuilder()
                                    .setGreet("[MODIFIED BY CLIENT INTERCEPTOR - 2]" + response.getGreet())
                                    .build();
                        } else if (message instanceof ResiliencyResponse response) {
                            message = (RespT) response.toBuilder()
                                    .setDummyString("[MODIFIED BY CLIENT INTERCEPTOR - 3]" + response.getDummyString())
                                    .build();
                        }
                        super.onMessage(message);
                    }
                }, headers);
            }
        };
    }

}
