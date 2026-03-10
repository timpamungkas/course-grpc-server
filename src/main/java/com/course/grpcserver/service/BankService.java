package com.course.grpcserver.service;

import java.time.OffsetDateTime;

import com.course.central.proto.bank.TransactionMessage.TransactionType;
import com.course.grpcserver.entity.BankExchangeRate;

public interface BankService {

    double findCurrentBalance(String accountNumber);

    BankExchangeRate saveExchangeRate(String fromCurrency, String toCurrency, 
        double rate, OffsetDateTime validFromTimestamp, OffsetDateTime validToTimestamp);

    double findExchangeRateAtTimeStamp(String fromCurrency, String toCurrency, OffsetDateTime timestamp);

    void createTransaction(String accountNumber, TransactionType type, double amount, String notes);

}
