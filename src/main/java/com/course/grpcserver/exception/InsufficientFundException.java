package com.course.grpcserver.exception;

public class InsufficientFundException extends RuntimeException {

    public InsufficientFundException(double amount) {
        super("Insufficient fund for amount: " + amount);
    }

}
