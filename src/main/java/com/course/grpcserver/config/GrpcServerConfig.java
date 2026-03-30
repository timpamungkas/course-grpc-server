package com.course.grpcserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;

import com.course.grpcserver.exception.AccountBlockedException;
import com.course.grpcserver.exception.InsufficientFundException;
import com.course.grpcserver.exception.InvalidBillerException;
import com.google.protobuf.Any;
import com.google.rpc.ErrorInfo;
import com.google.rpc.PreconditionFailure;

import io.grpc.Status;
import io.grpc.protobuf.StatusProto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class GrpcServerConfig {

        @Bean
        GrpcExceptionHandler grpcExceptionHandler() {
                return exception -> {
                        com.google.rpc.Status errorStatus;

                        if (exception instanceof AccountBlockedException) {
                                log.warn("GrpcExceptionHandler: AccountBlockedException");

                                var details = PreconditionFailure.newBuilder()
                                                .addViolations(PreconditionFailure.Violation.newBuilder()
                                                                .setType("ACCOUNT_BLOCKED")
                                                                .setSubject("Account blocked")
                                                                .setDescription(exception.getMessage())
                                                                .build())
                                                .build();

                                errorStatus = com.google.rpc.Status.newBuilder()
                                                .setCode(Status.FAILED_PRECONDITION.getCode().value())
                                                .setMessage(exception.getMessage())
                                                .addDetails(Any.pack(details))
                                                .build();
                        } else if (exception instanceof InvalidBillerException) {
                                log.warn("GrpcExceptionHandler: InvalidBillerException");

                                var details = ErrorInfo.newBuilder()
                                                .setReason("INVALID_BILLER")
                                                .putMetadata("message", exception.getMessage())
                                                .build();

                                errorStatus = com.google.rpc.Status.newBuilder()
                                                .setCode(Status.INVALID_ARGUMENT.getCode().value())
                                                .setMessage(exception.getMessage())
                                                .addDetails(Any.pack(details))
                                                .build();
                        } else if (exception instanceof InsufficientFundException) {
                                log.warn("GrpcExceptionHandler: InsufficientFundException");

                                var details = PreconditionFailure.newBuilder()
                                                .addViolations(PreconditionFailure.Violation.newBuilder()
                                                                .setType("INSUFFICIENT_FUND")
                                                                .setSubject("Insufficient fund")
                                                                .setDescription(exception.getMessage())
                                                                .build())
                                                .build();

                                errorStatus = com.google.rpc.Status.newBuilder()
                                                .setCode(Status.FAILED_PRECONDITION.getCode().value())
                                                .setMessage(exception.getMessage())
                                                .addDetails(Any.pack(details))
                                                .build();
                        } else {
                                log.error("GrpcExceptionHandler: Generic exception of type {} with message {}",
                                                exception.getClass().getSimpleName(), exception.getMessage());

                                errorStatus = com.google.rpc.Status.newBuilder()
                                                .setCode(Status.UNKNOWN.getCode().value())
                                                .setMessage(exception.getMessage())
                                                .build();
                        }

                        return StatusProto.toStatusException(errorStatus);
                };
        }

}
