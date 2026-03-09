package com.course.grpcserver.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import com.course.grpcserver.entity.BankExchangeRate;

public interface BankExchangeRateRepository extends ListCrudRepository<BankExchangeRate, UUID> {

    @Query("""
          SELECT *
            FROM bank_exchange_rates
           WHERE UPPER(from_currency) = UPPER(:fromCurrency)
                 AND UPPER(to_currency) = UPPER(:toCurrency)
                 AND (:timestamp BETWEEN valid_from_timestamp AND valid_to_timestamp)
        ORDER BY valid_from_timestamp DESC
           LIMIT 1
    """)
    BankExchangeRate findExchangeRateAtTimeStamp(String fromCurrency, String toCurrency, OffsetDateTime timestamp);

}
