package com.course.grpcserver.exception;

public class InvalidExchangeRateException extends RuntimeException {

    public InvalidExchangeRateException(String fromCurrency, String toCurrency) {
        super("Invalid exchange rate for " + fromCurrency + " to " + toCurrency);
    }

}
