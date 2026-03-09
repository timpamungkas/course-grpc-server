package com.course.grpcserver.service;

import java.time.OffsetDateTime;

import com.course.grpcserver.entity.BankExchangeRate;

public interface BankService {

    double findCurrentBalance(String accountNumber);

    BankExchangeRate saveExchangeRate(String fromCurrency, String toCurrency, 
        double rate, OffsetDateTime validFromTimestamp, OffsetDateTime validToTimestamp);

    double findExchangeRateAtTimeStamp(String fromCurrency, String toCurrency, OffsetDateTime timestamp);

}
