package com.course.grpcserver.grpc.server;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;

import com.course.central.proto.bank.AccountMessage.GetCurrentBalanceRequest;
import com.course.central.proto.bank.AccountMessage.GetCurrentBalanceResponse;
import com.course.central.proto.bank.BankServiceGrpc;
import com.course.central.proto.bank.ExchangeRateMessage.FetchExchangeRateRequest;
import com.course.central.proto.bank.ExchangeRateMessage.FetchExchangeRateResponse;
import com.course.central.proto.bank.TransactionMessage.TransactionRequest;
import com.course.central.proto.bank.TransactionMessage.TransactionSummaryResponse;
import com.course.central.proto.bank.TransactionMessage.TransactionType;
import com.course.central.proto.bank.TransferMessage.TransferRequest;
import com.course.central.proto.bank.TransferMessage.TransferResponse;
import com.course.central.proto.bank.TransferMessage.TransferStatus;
import com.course.grpcserver.exception.AccountNotFoundException;
import com.course.grpcserver.exception.InvalidExchangeRateException;
import com.course.grpcserver.service.BankService;
import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.rpc.BadRequest;
import com.google.type.Date;
import com.google.type.DateTime;

import io.grpc.Status;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GrpcService
public class BankServiceGrpcServer extends BankServiceGrpc.BankServiceImplBase {

    private BankService bankService;

    public BankServiceGrpcServer(@Autowired BankService bankService) {
        this.bankService = bankService;
    }

    @Override
    public void getCurrentBalance(GetCurrentBalanceRequest request,
            StreamObserver<GetCurrentBalanceResponse> responseObserver) {
        try {
            var accountNumber = request.getAccountNumber();
            var balance = bankService.findCurrentBalance(accountNumber);
            var today = LocalDate.now();
            var todayProto = Date.newBuilder()
                    .setYear(today.getYear())
                    .setMonth(today.getMonthValue())
                    .setDay(today.getDayOfMonth())
                    .build();

            var response = GetCurrentBalanceResponse.newBuilder()
                    .setAmount(balance)
                    .setCurrentDate(todayProto)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (AccountNotFoundException e) {
            var errorResponse = Status.FAILED_PRECONDITION.withDescription(e.getMessage());
            responseObserver.onError(errorResponse.asRuntimeException());
        }
    }

    @Override
    public void fetchExchangeRates(FetchExchangeRateRequest request,
            StreamObserver<FetchExchangeRateResponse> responseObserver) {
        var fromCurrency = request.getFromCurrency();
        var toCurrency = request.getToCurrency();
        var serverCallObserver = (ServerCallStreamObserver<FetchExchangeRateResponse>) responseObserver;

        while (!serverCallObserver.isCancelled()) {
            try {
                var now = OffsetDateTime.now(ZoneOffset.UTC);
                var exchangeRate = bankService.findExchangeRateAtTimeStamp(fromCurrency, toCurrency, now);

                var response = FetchExchangeRateResponse.newBuilder()
                        .setFromCurrency(fromCurrency)
                        .setToCurrency(toCurrency)
                        .setRate(exchangeRate)
                        .setTimestamp(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .build();

                responseObserver.onNext(response);

                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (InvalidExchangeRateException e) {
                var badRequest = BadRequest.newBuilder()
                        .addFieldViolations(BadRequest.FieldViolation.newBuilder()
                                .setField("exchange_rate")
                                .setDescription(e.getMessage())
                                .build())
                        .build();

                var errorStatus = com.google.rpc.Status.newBuilder()
                        .setCode(Status.NOT_FOUND.getCode().value())
                        .setMessage("Exchange rate not found")
                        .addDetails(Any.pack(badRequest))
                        .build();

                responseObserver.onError(StatusProto.toStatusRuntimeException(errorStatus));
            }
        }

        if (serverCallObserver.isCancelled()) {
            log.info("Client cancelled the request");
        }
    }

    @Override
    public StreamObserver<TransactionRequest> summarizeTransactions(
            StreamObserver<TransactionSummaryResponse> responseObserver) {
        return new StreamObserver<TransactionRequest>() {

            private String accountNumber;
            private double sumAmountIn;
            private double sumAmountOut;

            @Override
            public void onNext(TransactionRequest request) {
                accountNumber = request.getAccountNumber();
                var type = request.getType();
                var amount = request.getAmount();
                var notes = request.getNotes();

                try {
                    bankService.createTransaction(accountNumber, type, amount, notes);

                    if (type == TransactionType.TRANSACTION_TYPE_IN) {
                        sumAmountIn += amount;
                    } else if (type == TransactionType.TRANSACTION_TYPE_OUT) {
                        sumAmountOut += amount;
                    }
                } catch (Exception e) {
                    log.error("Failed to create transaction: {}", e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error while reading from client: {}", t.getMessage());
            }

            @Override
            public void onCompleted() {
                var now = LocalDate.now();

                var today = Date.newBuilder()
                        .setYear(now.getYear())
                        .setMonth(now.getMonthValue())
                        .setDay(now.getDayOfMonth())
                        .build();

                var response = TransactionSummaryResponse.newBuilder()
                        .setAccountNumber(accountNumber)
                        .setSumAmountIn(sumAmountIn)
                        .setSumAmountOut(sumAmountOut)
                        .setSumTotal(sumAmountIn - sumAmountOut)
                        .setTransactionDate(today)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<TransferRequest> transferMultiple(StreamObserver<TransferResponse> responseObserver) {
        var serverCallObserver = (ServerCallStreamObserver<TransferResponse>) responseObserver;

        return new StreamObserver<TransferRequest>() {

            @Override
            public void onNext(TransferRequest request) {
                if (serverCallObserver.isCancelled()) {
                    log.info("Client cancelled the transfer stream");
                    return;
                }

                var fromAccountNumber = request.getFromAccountNumber();
                var toAccountNumber = request.getToAccountNumber();
                var currency = request.getCurrency();
                var amount = request.getAmount();

                UUID transferUuid = null;
                var transferSuccess = false;

                try {
                    transferUuid = bankService.createTransfer(fromAccountNumber, toAccountNumber, currency, amount);
                    bankService.createTransactionPair(fromAccountNumber, toAccountNumber, amount, "Transfer");
                    transferSuccess = true;
                } catch (Exception e) {
                    log.error("Transfer failed: {}", e.getMessage());
                }

                if (transferUuid != null) {
                    bankService.updateTransferStatus(transferUuid, transferSuccess);
                }

                var status = transferSuccess
                        ? TransferStatus.TRANSFER_STATUS_SUCCESS
                        : TransferStatus.TRANSFER_STATUS_FAILED;

                var response = TransferResponse.newBuilder()
                        .setFromAccountNumber(fromAccountNumber)
                        .setToAccountNumber(toAccountNumber)
                        .setCurrency(currency)
                        .setAmount(amount)
                        .setStatus(status)
                        .setTimestamp(currentDatetime())
                        .build();

                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error while reading from client: {}", t.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    private DateTime currentDatetime() {
        var now = OffsetDateTime.now(ZoneOffset.UTC);

        return DateTime.newBuilder()
                .setYear(now.getYear())
                .setMonth(now.getMonthValue())
                .setDay(now.getDayOfMonth())
                .setHours(now.getHour())
                .setMinutes(now.getMinute())
                .setSeconds(now.getSecond())
                .setNanos(now.getNano())
                .setUtcOffset(Duration.getDefaultInstance())
                .build();
    }

}
