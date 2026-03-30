package com.course.grpcserver.grpc.client.interceptor;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.core.annotation.Order;
import org.springframework.grpc.client.GlobalClientInterceptor;
import org.springframework.stereotype.Component;

import com.course.grpcserver.grpc.client.constant.ClientGrpcKeyConstants;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

@Component
@GlobalClientInterceptor
@Order(200)
@Slf4j
public class ClientMetadataInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(ClientGrpcKeyConstants.METADATA_KEY_CLIENT_ID,
                        "my-resiliency-client-" + ThreadLocalRandom.current().nextInt(1000));
                headers.put(ClientGrpcKeyConstants.METADATA_KEY_CORRELATION_ID, UUID.randomUUID().toString());
                headers.put(ClientGrpcKeyConstants.METADATA_KEY_REQUEST_TIMESTAMP,
                        Long.toString(Instant.now().toEpochMilli()));

                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
                    @Override
                    public void onHeaders(Metadata responseHeaders) {
                        logResponseMetadata(
                                "[ClientMetadataInterceptor] [" + method.getBareMethodName() + "] response headers",
                                responseHeaders);
                        super.onHeaders(responseHeaders);
                    }

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        logResponseMetadata(
                                "[ClientMetadataInterceptor] [" + method.getBareMethodName() + "] response trailers",
                                trailers);
                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }

    private void logResponseMetadata(String label, Metadata metadata) {
        if (metadata == null || metadata.keys() == null || metadata.keys().isEmpty()) {
            log.info("{} has no metadata", label);
            return;
        }

        log.info("Response metadata for {}:", label);

        metadata.keys().forEach(key -> {
            var value = metadata.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
            log.info("  - {}={}", key, value);
        });
    }

}
