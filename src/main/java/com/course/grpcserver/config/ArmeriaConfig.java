package com.course.grpcserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.course.grpcserver.grpc.server.HelloServiceGrpcServer;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;

/**
 * Configures Armeria to expose gRPC services as REST/JSON over HTTP.
 *
 * <p>Each unary gRPC method is reachable via HTTP POST at:
 * {@code POST /<package>.<ServiceName>/<MethodName>}
 * with Content-Type {@code application/json; charset=utf-8}.
 *
 * <p>Example:
 * <pre>
 *   POST http://localhost:8080/hello.v1.HelloService/SayHello
 *   Content-Type: application/json
 *
 *   {"name": "World"}
 * </pre>
 */
@Configuration
public class ArmeriaConfig {

    /**
     * Registers gRPC services on the Armeria server with unframed (REST/JSON) request support enabled.
     *
     * <p>Enabled serialization formats:
     * <ul>
     *   <li>Native gRPC (binary protobuf)</li>
     *   <li>gRPC-Web</li>
     *   <li>Unframed JSON — plain HTTP POST with a JSON body (REST-like)</li>
     *   <li>Unframed protobuf — plain HTTP POST with a binary protobuf body</li>
     * </ul>
     */
    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator(HelloServiceGrpcServer helloServiceGrpcServer) {
        return builder -> builder.service(
                GrpcService.builder()
                        .addService(helloServiceGrpcServer)
                        // Enables plain HTTP POST access (no gRPC framing).
                        // Unary methods become REST-like endpoints accepting JSON or binary protobuf.
                        .enableUnframedRequests(true)
                        // Run service methods in a blocking executor (safe for sync implementations).
                        .useBlockingTaskExecutor(true)
                        .build()
        );
    }

}
