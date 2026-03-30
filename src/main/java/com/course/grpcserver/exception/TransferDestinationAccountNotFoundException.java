package com.course.grpcserver.exception;

public class TransferDestinationAccountNotFoundException extends RuntimeException {

    public TransferDestinationAccountNotFoundException(String accountNumber) {
        super("Destination account not found: " + accountNumber);
    }

}
