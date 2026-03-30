package com.course.grpcserver.exception;

public class AccountBlockedException extends RuntimeException {

    public AccountBlockedException(String accountNumber) {
        super("Account number " + accountNumber + " is blocked");
    }

}
