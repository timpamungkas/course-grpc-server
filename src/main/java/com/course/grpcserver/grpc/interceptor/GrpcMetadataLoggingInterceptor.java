package com.course.grpcserver.grpc.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

import com.course.grpcserver.grpc.context.GrpcContextKeys;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

@Component
@GlobalServerInterceptor
public class GrpcMetadataLoggingInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GrpcMetadataLoggingInterceptor.class);
    private static final Metadata.Key<String> BABI_KEY = Metadata.Key.of("babi", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        log.info("=== Incoming gRPC Metadata for [{}] ===", call.getMethodDescriptor().getFullMethodName());

        headers.keys().forEach(key -> {
            if (key.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                Metadata.Key<byte[]> binaryKey = Metadata.Key.of(key, Metadata.BINARY_BYTE_MARSHALLER);
                byte[] value = headers.get(binaryKey);
                log.info("  [binary] {} = {}", key, java.util.Arrays.toString(value));
            } else {
                Metadata.Key<String> asciiKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
                String value = headers.get(asciiKey);
                log.info("  {} = {}", key, value);
            }
        });

        log.info("==========================================");

        String babiValue = headers.get(BABI_KEY);
        if (babiValue != null) {
            String modifiedBabiValue = babiValue + "babi";
            log.info("  [babi] metadata found, modified value: {}", modifiedBabiValue);
            Context ctx = Context.current().withValue(GrpcContextKeys.BABI_METADATA_VALUE, modifiedBabiValue);
            return Contexts.interceptCall(ctx, call, headers, next);
        }

        return next.startCall(call, headers);
    }

}
