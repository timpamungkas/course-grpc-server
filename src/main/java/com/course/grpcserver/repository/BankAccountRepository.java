package com.course.grpcserver.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import com.course.grpcserver.entity.BankAccount;

public interface BankAccountRepository extends ListCrudRepository<BankAccount, UUID> {

    BankAccount findByAccountNumber(String accountNumber);

    @Modifying
    @Query("""
        UPDATE bank_accounts 
           SET current_balance = :balance, updated_at = current_timestamp 
         WHERE account_uuid = :id
    """)
    int updateCurrentBalance(@Param("id") UUID id, @Param("balance") BigDecimal balance);

}
