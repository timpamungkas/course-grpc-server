package com.course.grpcserver.grpc.interceptor;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.course.central.proto.hello.Hello.SayHelloResponse;
import com.course.central.proto.resiliency.ResiliencyMessage.ResiliencyResponse;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(300)
public class ModifyResponseInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> METADATA_KEY_1 = Metadata.Key.of("my-response-metadata-key-1",
            Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> METADATA_KEY_2 = Metadata.Key.of("my-response-metadata-key-2",
            Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        var wrappedCall = new SimpleForwardingServerCall<ReqT, RespT>(serverCall) {
            @Override
            public void sendHeaders(Metadata responseHeaders) {
                responseHeaders.put(METADATA_KEY_1, "my-response-metadata-value-1");
                responseHeaders.put(METADATA_KEY_2, "my-response-metadata-value-2");
                super.sendHeaders(responseHeaders);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void sendMessage(RespT message) {
                if (message instanceof SayHelloResponse response) {
                    message = (RespT) response.toBuilder()
                            .setGreet("[MODIFIED BY SERVER INTERCEPTOR - 2]" + response.getGreet())
                            .build();
                } else if (message instanceof ResiliencyResponse response) {
                    message = (RespT) response.toBuilder()
                            .setDummyString("[MODIFIED BY SERVER INTERCEPTOR - 3]" + response.getDummyString())
                            .build();
                }
                super.sendMessage(message);
            }
        };

        return next.startCall(wrappedCall, headers);
    }

}
