package com.course.grpcserver.exception;

public class InvalidBillerException extends RuntimeException {

    public InvalidBillerException(String billerCode) {
        super("Invalid biller code: " + billerCode);
    }

}
