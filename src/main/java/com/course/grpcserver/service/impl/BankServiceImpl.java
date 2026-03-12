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
import com.course.grpcserver.entity.BankTransfer;
import com.course.grpcserver.exception.AccountNotFoundException;
import com.course.grpcserver.exception.InvalidExchangeRateException;
import com.course.grpcserver.repository.BankAccountRepository;
import com.course.grpcserver.repository.BankExchangeRateRepository;
import com.course.grpcserver.repository.BankTransactionRepository;
import com.course.grpcserver.repository.BankTransferRepository;
import com.course.grpcserver.service.BankService;

@Service
public class BankServiceImpl implements BankService {

    private BankAccountRepository bankAccountRepository;
    private BankExchangeRateRepository bankExchangeRateRepository;
    private BankTransactionRepository bankTransactionRepository;
    private BankTransferRepository bankTransferRepository;

    public BankServiceImpl(@Autowired BankAccountRepository bankAccountRepository,
            @Autowired BankExchangeRateRepository bankExchangeRateRepository,
            @Autowired BankTransactionRepository bankTransactionRepository,
            @Autowired BankTransferRepository bankTransferRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankExchangeRateRepository = bankExchangeRateRepository;
        this.bankTransactionRepository = bankTransactionRepository;
        this.bankTransferRepository = bankTransferRepository;
    }

    @Override
    public double findCurrentBalance(String accountNumber) {
        var account = bankAccountRepository.findByAccountNumber(accountNumber);

        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }

        return account.getCurrentBalance().doubleValue();
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
        if (exchangeRate == null) {
            throw new InvalidExchangeRateException(fromCurrency, toCurrency);
        }

        return exchangeRate.getRate().doubleValue();
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
        var updatedRows = bankAccountRepository.updateCurrentBalance(account.getAccountUuid(), newBalance);

        if (updatedRows != 1) {
            throw new IllegalStateException("Failed to update balance for account: " + accountNumber);
        }
    }

    @Override
    public UUID createTransfer(String fromAccountNumber, String toAccountNumber, String currency, double amount) {
        var fromAccount = bankAccountRepository.findByAccountNumber(fromAccountNumber);
        if (fromAccount == null) {
            throw new IllegalArgumentException("From account not found: " + fromAccountNumber);
        }
        var toAccount = bankAccountRepository.findByAccountNumber(toAccountNumber);
        if (toAccount == null) {
            throw new IllegalArgumentException("To account not found: " + toAccountNumber);
        }

        var now = OffsetDateTime.now();
        var transfer = BankTransfer.builder()
                .transferUuid(UUID.randomUUID())
                .fromAccountUuid(fromAccount.getAccountUuid())
                .toAccountUuid(toAccount.getAccountUuid())
                .currency(currency)
                .amount(new BigDecimal(amount))
                .transferTimestamp(now)
                .transferSuccess(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return bankTransferRepository.save(transfer).getTransferUuid();
    }

    @Override
    @Transactional
    public void createTransactionPair(String fromAccountNumber, String toAccountNumber, double amount, String notes) {
        var fromAccount = bankAccountRepository.findByAccountNumber(fromAccountNumber);
        if (fromAccount == null) {
            throw new IllegalArgumentException("From account not found: " + fromAccountNumber);
        }

        var toAccount = bankAccountRepository.findByAccountNumber(toAccountNumber);
        if (toAccount == null) {
            throw new IllegalArgumentException("To account not found: " + toAccountNumber);
        }

        var now = OffsetDateTime.now();
        var amountDecimal = new BigDecimal(amount);

        var fromTransaction = BankTransaction.builder()
                .transactionUuid(UUID.randomUUID())
                .accountUuid(fromAccount.getAccountUuid())
                .transactionTimestamp(now)
                .amount(amountDecimal)
                .transactionType(TransactionType.TRANSACTION_TYPE_OUT.name())
                .notes(notes)
                .createdAt(now)
                .updatedAt(now)
                .build();
        bankTransactionRepository.save(fromTransaction);

        var toTransaction = BankTransaction.builder()
                .transactionUuid(UUID.randomUUID())
                .accountUuid(toAccount.getAccountUuid())
                .transactionTimestamp(now)
                .amount(amountDecimal)
                .transactionType(TransactionType.TRANSACTION_TYPE_IN.name())
                .notes(notes)
                .createdAt(now)
                .updatedAt(now)
                .build();
        bankTransactionRepository.save(toTransaction);

        var fromNewBalance = fromAccount.getCurrentBalance().subtract(amountDecimal);
        var updatedFromAccountRows = bankAccountRepository.updateCurrentBalance(fromAccount.getAccountUuid(), fromNewBalance);
        if (updatedFromAccountRows != 1) {
            throw new IllegalStateException("Failed to update balance for account: " + fromAccountNumber);
        }

        var toNewBalance = toAccount.getCurrentBalance().add(amountDecimal);
        var updatedToAccountRows = bankAccountRepository.updateCurrentBalance(toAccount.getAccountUuid(), toNewBalance);
        if (updatedToAccountRows != 1) {
            throw new IllegalStateException("Failed to update balance for account: " + toAccountNumber);
        }
    }

    @Override
    public int updateTransferStatus(UUID transferUuid, boolean isSuccess) {
        return bankTransferRepository.updateTransferStatus(transferUuid, isSuccess);
    }

}
