package com.course.grpcserver.grpc.constant;

import io.grpc.Context;
import io.grpc.Metadata;

public final class GrpcKeyConstants {

    public static final Context.Key<Metadata> CONTEXT_KEY_REQUEST_METADATA = Context
        .key("request-metadata-context");

    public static final Context.Key<Metadata> CONTEXT_KEY_RESPONSE_METADATA = Context
        .key("response-metadata-context");

    public static final Metadata.Key<String> METADATA_KEY_SERVER_VERSION = Metadata.Key.of("x-server-version",
        Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<String> METADATA_KEY_SERVER_TRACE_ID = Metadata.Key.of("x-server-trace-id",
        Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<String> METADATA_KEY_SERVER_TIMESTAMP = Metadata.Key.of("x-server-timestamp",
        Metadata.ASCII_STRING_MARSHALLER);

}
