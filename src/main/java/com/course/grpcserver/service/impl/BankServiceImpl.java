package com.course.grpcserver.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.course.grpcserver.entity.BankExchangeRate;
import com.course.grpcserver.repository.BankAccountRepository;
import com.course.grpcserver.repository.BankExchangeRateRepository;
import com.course.grpcserver.service.BankService;

@Service
public class BankServiceImpl implements BankService {

    private BankAccountRepository bankAccountRepository;
    private BankExchangeRateRepository bankExchangeRateRepository;

    public BankServiceImpl(@Autowired BankAccountRepository bankAccountRepository,
            @Autowired BankExchangeRateRepository bankExchangeRateRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankExchangeRateRepository = bankExchangeRateRepository;
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

}
