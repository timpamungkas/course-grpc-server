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
@Table("bank_transfers")
public class BankTransfer implements Persistable<UUID> {

    @Id
    private UUID transferUuid;

    private UUID fromAccountUuid;

    private UUID toAccountUuid;

    private String currency;

    private BigDecimal amount;

    private OffsetDateTime transferTimestamp;

    private boolean transferSuccess;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return transferUuid;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

}
