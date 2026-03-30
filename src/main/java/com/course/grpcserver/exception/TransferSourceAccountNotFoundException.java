package com.course.grpcserver.exception;

public class TransferSourceAccountNotFoundException extends RuntimeException {

    public TransferSourceAccountNotFoundException(String accountNumber) {
        super("Source account not found: " + accountNumber);
    }

}
