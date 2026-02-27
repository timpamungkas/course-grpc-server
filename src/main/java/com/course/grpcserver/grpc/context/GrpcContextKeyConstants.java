package com.course.grpcserver.grpc.context;

import io.grpc.Context;
import io.grpc.Metadata;

public class GrpcContextKeyConstants {

    public static final Metadata.Key<String> METADATA_KEY_GAMMA = Metadata.Key.of("my-gamma-metadata",
            Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> CONTEXT_KEY_GAMMA = Context.key("my-gamma-context");

    // Mutable Metadata holder so the RPC handler can populate headers that the interceptor merges when sending response headers
    public static final Context.Key<Metadata> CONTEXT_KEY_GAMMA_HOLDER = Context.key("my-gamma-context-holder");

    public static final Metadata.Key<String> METADATA_KEY_GAMMA_CONTEXT = Metadata.Key.of("my-gamma-context",
            Metadata.ASCII_STRING_MARSHALLER);

}
