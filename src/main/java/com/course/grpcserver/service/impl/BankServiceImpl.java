package com.course.grpcserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.course.grpcserver.repository.BankAccountRepository;
import com.course.grpcserver.service.BankService;

@Service
public class BankServiceImpl implements BankService {

    private BankAccountRepository bankAccountRepository;

    public BankServiceImpl(@Autowired BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    @Override
    public double findCurrentBalance(String accountNumber) {
        var account = bankAccountRepository.findByAccountNumber(accountNumber);

        return account != null ? account.getCurrentBalance().doubleValue() : 0.0;
    }

}
