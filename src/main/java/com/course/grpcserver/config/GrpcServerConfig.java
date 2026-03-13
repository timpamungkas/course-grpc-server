package com.course.grpcserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;

import com.course.grpcserver.exception.TransferDestinationAccountNotFoundException;
import com.course.grpcserver.exception.TransferRecordFailedException;
import com.course.grpcserver.exception.TransferSourceAccountNotFoundException;
import com.course.grpcserver.exception.TransferTransactionPairException;
import com.google.protobuf.Any;
import com.google.rpc.ErrorInfo;
import com.google.rpc.Help;
import com.google.rpc.PreconditionFailure;
import io.grpc.Status;
import io.grpc.protobuf.StatusProto;

@Configuration
public class GrpcServerConfig {

    /*
    @Bean
    GrpcExceptionHandler grpcExceptionHandler() {
        return exception -> {
com.google.rpc.Status errorStatus;

            if (exception instanceof TransferSourceAccountNotFoundException) {
                var details = PreconditionFailure.newBuilder()
                        .addViolations(PreconditionFailure.Violation.newBuilder()
                            .setType("INVALID_ACCOUNT")
                            .setSubject("Source account not found")
                            .setDescription("source account (from " + request.getFromAccountNumber() + ") not found")
                            .build())
                    .build();

            errorStatus = com.google.rpc.Status.newBuilder()
                    .setCode(Status.FAILED_PRECONDITION.getCode().value())
                    .setMessage(exception.getMessage())
                    .addDetails(Any.pack(details))
                    .build();
        } else if (exception instanceof TransferDestinationAccountNotFoundException) {
            var details = PreconditionFailure.newBuilder()
                    .addViolations(PreconditionFailure.Violation.newBuilder()
                            .setType("INVALID_ACCOUNT")
                            .setSubject("Destination account not found")
                            .setDescription("destination account (to " + request.getToAccountNumber() + ") not found")
                            .build())
                    .build();

            errorStatus = com.google.rpc.Status.newBuilder()
                    .setCode(Status.FAILED_PRECONDITION.getCode().value())
                    .setMessage(exception.getMessage())
                    .addDetails(Any.pack(details))
                    .build();
        } else if (exception instanceof TransferRecordFailedException) {
            var details = Help.newBuilder()
                    .addLinks(Help.Link.newBuilder()
                            .setUrl("my-bank-website.com/faq")
                            .setDescription("Bank FAQ")
                            .build())
                    .build();

            errorStatus = com.google.rpc.Status.newBuilder()
                    .setCode(Status.INTERNAL.getCode().value())
                    .setMessage(exception.getMessage())
                    .addDetails(Any.pack(details))
                    .build();
        } else if (exception instanceof TransferTransactionPairException) {
            var details = ErrorInfo.newBuilder()
                    .setDomain("my-bank-website.com")
                    .setReason("TRANSACTION_PAIR_FAILED")
                    .putMetadata("from_account", request.getFromAccountNumber())
                    .putMetadata("to_account", request.getToAccountNumber())
                    .putMetadata("currency", request.getCurrency())
                    .putMetadata("amount", String.valueOf(request.getAmount()))
                    .build();

            errorStatus = com.google.rpc.Status.newBuilder()
                    .setCode(Status.INVALID_ARGUMENT.getCode().value())
                    .setMessage(e.getMessage())
                    .addDetails(Any.pack(details))
                    .build();
        } else {
            errorStatus = com.google.rpc.Status.newBuilder()
                    .setCode(Status.UNKNOWN.getCode().value())
                    .setMessage(e.getMessage())
                    .build();
        }

        return StatusProto.toStatusRuntimeException(errorStatus);
        };
    }
        */

}
