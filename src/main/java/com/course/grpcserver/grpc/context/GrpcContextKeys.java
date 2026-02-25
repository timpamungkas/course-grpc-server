package com.course.grpcserver.grpc.context;

import io.grpc.Context;

public class GrpcContextKeys {

    public static final Context.Key<String> BABI_METADATA_VALUE = Context.key("babi-metadata-value");

    private GrpcContextKeys() {
    }

}
