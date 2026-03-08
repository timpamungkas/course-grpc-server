package com.course.grpcserver.repository;

import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

import com.course.grpcserver.entity.BankAccount;

public interface BankAccountRepository extends ListCrudRepository<BankAccount, UUID> {

    BankAccount findByAccountNumber(String accountNumber);

}
