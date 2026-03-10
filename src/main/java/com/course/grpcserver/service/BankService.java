package com.course.grpcserver.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.course.central.proto.bank.TransactionMessage.TransactionType;
import com.course.grpcserver.entity.BankExchangeRate;

public interface BankService {

    double findCurrentBalance(String accountNumber);

    BankExchangeRate saveExchangeRate(String fromCurrency, String toCurrency, 
        double rate, OffsetDateTime validFromTimestamp, OffsetDateTime validToTimestamp);

    double findExchangeRateAtTimeStamp(String fromCurrency, String toCurrency, OffsetDateTime timestamp);

    void createTransaction(String accountNumber, TransactionType type, double amount, String notes);

    UUID createTransfer(String fromAccountNumber, String toAccountNumber, String currency, double amount);

    void createTransactionPair(String fromAccountNumber, String toAccountNumber, double amount, String notes);

    int updateTransferStatus(String transferUuid, boolean isSuccess);

}
