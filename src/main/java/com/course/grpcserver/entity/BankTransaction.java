package com.course.grpcserver.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("bank_transactions")
public class BankTransaction implements Persistable<UUID> {

    @Id
    private UUID transactionUuid;

    private UUID accountUuid;

    private OffsetDateTime transactionTimestamp;

    private BigDecimal amount;

    private String transactionType;

    private String notes;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return transactionUuid;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

}
