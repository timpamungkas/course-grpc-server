package com.course.grpcserver.service.impl;

import org.springframework.stereotype.Service;

import com.course.grpcserver.service.BankService;

@Service
public class BankServiceImpl implements BankService {

    @Override
    public double findCurrentBalance(String accountNumber) {
        return 999.85;
    }

}
