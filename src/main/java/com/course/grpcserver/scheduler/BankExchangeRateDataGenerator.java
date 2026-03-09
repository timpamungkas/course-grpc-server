package com.course.grpcserver.scheduler;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.course.grpcserver.entity.BankExchangeRate;
import com.course.grpcserver.repository.BankExchangeRateRepository;
import com.course.grpcserver.service.BankService;

@Service
public class BankExchangeRateDataGenerator {

    private BankService bankService;

    public BankExchangeRateDataGenerator(@Autowired BankService bankService) {
        this.bankService = bankService;
    }

    @Scheduled(fixedRate = 5000)
    void generateDummyData() {
        var random = 17000 + Math.random() * 1500;
        var rounded = Math.round(random * 100.0) / 100.0;
        var now = OffsetDateTime.now();
        var validFromTimestamp = now.plusSeconds(1).truncatedTo(ChronoUnit.SECONDS);
        var validToTimestamp = validFromTimestamp.plusSeconds(5).minusNanos(1);

        bankService.saveExchangeRate("USD", "IDR", rounded, validFromTimestamp, validToTimestamp);
    }

}
