package com.course.grpcserver.repository;

import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import com.course.grpcserver.entity.BankTransfer;

public interface BankTransferRepository extends ListCrudRepository<BankTransfer, UUID> {

    @Modifying
    @Query("""
        UPDATE bank_transfers
           SET transfer_success = :isSuccess, updated_at = current_timestamp
         WHERE transfer_uuid = :transferUuid
    """)
    int updateTransferStatus(@Param("transferUuid") UUID transferUuid, @Param("isSuccess") boolean isSuccess);

}
