package com.course.grpcserver.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("bank_exchange_rates")
public class BankExchangeRate implements Persistable<UUID> {

    @Id
    private UUID exchangeRateUuid;

    private String fromCurrency;

    private String toCurrency;

    private BigDecimal rate;

    private OffsetDateTime validFromTimestamp;

    private OffsetDateTime validToTimestamp;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return exchangeRateUuid;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

}
