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
import com.course.grpcserver.exception.InsufficientFundException;
import com.course.grpcserver.exception.InvalidExchangeRateException;
import com.course.grpcserver.exception.TransferDestinationAccountNotFoundException;
import com.course.grpcserver.exception.TransferRecordFailedException;
import com.course.grpcserver.exception.TransferSourceAccountNotFoundException;
import com.course.grpcserver.exception.TransferTransactionPairException;
import com.course.grpcserver.service.BankService;
import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.rpc.BadRequest;
import com.google.rpc.ErrorInfo;
import com.google.rpc.Help;
import com.google.rpc.PreconditionFailure;
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
                break;
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
            private boolean hasError = false;

            @Override
            public void onNext(TransactionRequest request) {
                if (hasError) return;
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

                    var badRequest = switch (e) {
                        case AccountNotFoundException ae -> BadRequest.newBuilder()
                                .addFieldViolations(BadRequest.FieldViolation.newBuilder()
                                        .setField("account_number")
                                        .setDescription(ae.getMessage())
                                        .build())
                                .build();
                        case InsufficientFundException ife -> BadRequest.newBuilder()
                                .addFieldViolations(BadRequest.FieldViolation.newBuilder()
                                        .setField("amount")
                                        .setDescription(ife.getMessage())
                                        .build())
                                .build();
                        default -> BadRequest.newBuilder()
                                .addFieldViolations(BadRequest.FieldViolation.newBuilder()
                                        .setField("transaction")
                                        .setDescription(e.getMessage())
                                        .build())
                                .build();
                    };

                    var errorStatus = com.google.rpc.Status.newBuilder()
                            .setCode(Status.FAILED_PRECONDITION.getCode().value())
                            .setMessage("Transaction failed")
                            .addDetails(Any.pack(badRequest))
                            .build();

                    responseObserver.onError(StatusProto.toStatusRuntimeException(errorStatus));
                    hasError = true;
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error while reading from client: {}", t.getMessage());
            }

            @Override
            public void onCompleted() {
                if (hasError) return;

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
                try {
                    transferUuid = bankService.createTransfer(fromAccountNumber, toAccountNumber, currency, amount);
                    bankService.createTransactionPair(fromAccountNumber, toAccountNumber, amount, "Transfer");
                    bankService.updateTransferStatus(transferUuid, true);

                    var response = TransferResponse.newBuilder()
                            .setFromAccountNumber(fromAccountNumber)
                            .setToAccountNumber(toAccountNumber)
                            .setCurrency(currency)
                            .setAmount(amount)
                            .setStatus(TransferStatus.TRANSFER_STATUS_SUCCESS)
                            .setTimestamp(currentDatetime())
                            .build();

                    responseObserver.onNext(response);
                } catch (Exception e) {
                    log.error("Transfer failed: {}", e.getMessage());
                    if (transferUuid != null) {
                        bankService.updateTransferStatus(transferUuid, false);
                    }

                    var errorResponse = buildTransferErrorResponse(e, request);
                    responseObserver.onError(errorResponse);
                }
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

    
    private Throwable buildTransferErrorResponse(Exception e, TransferRequest request) {
        com.google.rpc.Status errorStatus;

        if (e instanceof TransferSourceAccountNotFoundException) {
            var details = PreconditionFailure.newBuilder()
                    .addViolations(PreconditionFailure.Violation.newBuilder()
                            .setType("INVALID_ACCOUNT")
                            .setSubject("Source account not found")
                            .setDescription("source account (from " + request.getFromAccountNumber() + ") not found")
                            .build())
                    .build();

            errorStatus = com.google.rpc.Status.newBuilder()
                    .setCode(Status.FAILED_PRECONDITION.getCode().value())
                    .setMessage(e.getMessage())
                    .addDetails(Any.pack(details))
                    .build();
        } else if (e instanceof TransferDestinationAccountNotFoundException) {
            var details = PreconditionFailure.newBuilder()
                    .addViolations(PreconditionFailure.Violation.newBuilder()
                            .setType("INVALID_ACCOUNT")
                            .setSubject("Destination account not found")
                            .setDescription("destination account (to " + request.getToAccountNumber() + ") not found")
                            .build())
                    .build();

            errorStatus = com.google.rpc.Status.newBuilder()
                    .setCode(Status.FAILED_PRECONDITION.getCode().value())
                    .setMessage(e.getMessage())
                    .addDetails(Any.pack(details))
                    .build();
        } else if (e instanceof TransferRecordFailedException) {
            var details = Help.newBuilder()
                    .addLinks(Help.Link.newBuilder()
                            .setUrl("my-bank-website.com/faq")
                            .setDescription("Bank FAQ")
                            .build())
                    .build();

            errorStatus = com.google.rpc.Status.newBuilder()
                    .setCode(Status.INTERNAL.getCode().value())
                    .setMessage(e.getMessage())
                    .addDetails(Any.pack(details))
                    .build();
        } else if (e instanceof TransferTransactionPairException) {
            var details = ErrorInfo.newBuilder()
                    .setDomain("my-bank-website.com")
                    .setReason("TRANSACTION_PAIR_FAILED")
                    .putMetadata("from_account", request.getFromAccountNumber())
                    .putMetadata("to_account", request.getToAccountNumber())
                    .putMetadata("currency", request.getCurrency())
                    .putMetadata("amount", String.valueOf(request.getAmount()))
                    .build();

            errorStatus = com.google.rpc.Status.newBuilder()
                    .setCode(Status.INVALID_ARGUMENT.getCode().value())
                    .setMessage(e.getMessage())
                    .addDetails(Any.pack(details))
                    .build();
        } else {
            errorStatus = com.google.rpc.Status.newBuilder()
                    .setCode(Status.UNKNOWN.getCode().value())
                    .setMessage(e.getMessage())
                    .build();
        }

        return StatusProto.toStatusRuntimeException(errorStatus);
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
