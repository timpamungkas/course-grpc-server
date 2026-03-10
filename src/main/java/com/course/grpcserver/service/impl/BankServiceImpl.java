package com.course.grpcserver.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.course.central.proto.bank.TransactionMessage.TransactionType;
import com.course.grpcserver.entity.BankExchangeRate;
import com.course.grpcserver.entity.BankTransaction;
import com.course.grpcserver.repository.BankAccountRepository;
import com.course.grpcserver.repository.BankExchangeRateRepository;
import com.course.grpcserver.repository.BankTransactionRepository;
import com.course.grpcserver.service.BankService;

@Service
public class BankServiceImpl implements BankService {

    private BankAccountRepository bankAccountRepository;
    private BankExchangeRateRepository bankExchangeRateRepository;
    private BankTransactionRepository bankTransactionRepository;

    public BankServiceImpl(@Autowired BankAccountRepository bankAccountRepository,
            @Autowired BankExchangeRateRepository bankExchangeRateRepository,
            @Autowired BankTransactionRepository bankTransactionRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankExchangeRateRepository = bankExchangeRateRepository;
        this.bankTransactionRepository = bankTransactionRepository;
    }

    @Override
    public double findCurrentBalance(String accountNumber) {
        var account = bankAccountRepository.findByAccountNumber(accountNumber);

        return account != null ? account.getCurrentBalance().doubleValue() : 0.0;
    }

    @Override
    public BankExchangeRate saveExchangeRate(String fromCurrency, String toCurrency,
            double rate, OffsetDateTime validFromTimestamp, OffsetDateTime validToTimestamp) {
        var now = OffsetDateTime.now();

        var dummyExchangeRate = BankExchangeRate.builder()
                .exchangeRateUuid(UUID.randomUUID())
                .createdAt(now)
                .updatedAt(now)
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .rate(new BigDecimal(rate))
                .validFromTimestamp(validFromTimestamp)
                .validToTimestamp(validToTimestamp)
                .build();

        return bankExchangeRateRepository.save(dummyExchangeRate);
    }

    @Override
    public double findExchangeRateAtTimeStamp(String fromCurrency, String toCurrency, OffsetDateTime timestamp) {
        var exchangeRate = bankExchangeRateRepository.findExchangeRateAtTimeStamp(fromCurrency, toCurrency, timestamp);

        return exchangeRate != null ? exchangeRate.getRate().doubleValue() : 0.0;
    }

    @Override
    @Transactional
    public void createTransaction(String accountNumber, TransactionType type, double amount, String notes) {
        var account = bankAccountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }

        var now = OffsetDateTime.now();
        var transaction = BankTransaction.builder()
                .transactionUuid(UUID.randomUUID())
                .accountUuid(account.getAccountUuid())
                .transactionTimestamp(now)
                .amount(new BigDecimal(amount))
                .transactionType(type.name())
                .createdAt(now)
                .updatedAt(now)
                .notes(notes)
                .build();

        bankTransactionRepository.save(transaction);

        var adjustedAmount = new BigDecimal(amount);
        if (type == TransactionType.TRANSACTION_TYPE_OUT) {
            adjustedAmount = adjustedAmount.negate();
        }

        var newBalance = account.getCurrentBalance().add(adjustedAmount);
        bankAccountRepository.updateCurrentBalance(account.getAccountUuid(), newBalance, now);
    }

}
