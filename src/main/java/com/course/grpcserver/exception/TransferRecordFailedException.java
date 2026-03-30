package com.course.grpcserver.exception;

public class TransferRecordFailedException extends RuntimeException {

    public TransferRecordFailedException() {
        super("Can't create transfer record");
    }

}
