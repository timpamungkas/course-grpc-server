package com.course.grpcserver.grpc.client.constant;

import io.grpc.Metadata;

public final class ClientGrpcKeyConstants {

    public static final Metadata.Key<String> METADATA_KEY_CLIENT_ID =
        Metadata.Key.of("x-client-id", Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<String> METADATA_KEY_CORRELATION_ID =
        Metadata.Key.of("x-correlation-id", Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<String> METADATA_KEY_REQUEST_TIMESTAMP =
        Metadata.Key.of("x-request-timestamp", Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<String> METADATA_KEY_PER_REQUEST =
        Metadata.Key.of("x-per-request-metadata", Metadata.ASCII_STRING_MARSHALLER);

}
