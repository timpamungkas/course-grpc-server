package com.course.grpcserver.grpc.server;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;

import com.course.central.proto.bank.AccountMessage.GetCurrentBalanceRequest;
import com.course.central.proto.bank.AccountMessage.GetCurrentBalanceResponse;
import com.course.central.proto.bank.BankServiceGrpc;
import com.course.central.proto.bank.ExchangeRateMessage.FetchExchangeRateRequest;
import com.course.central.proto.bank.ExchangeRateMessage.FetchExchangeRateResponse;
import com.course.grpcserver.service.BankService;
import com.google.type.Date;

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
    }

    @Override
    public void fetchExchangeRates(FetchExchangeRateRequest request,
            StreamObserver<FetchExchangeRateResponse> responseObserver) {
        var fromCurrency = request.getFromCurrency();
        var toCurrency = request.getToCurrency();
        var serverCallObserver = (ServerCallStreamObserver<FetchExchangeRateResponse>) responseObserver;

        while (!serverCallObserver.isCancelled()) {
            var now = OffsetDateTime.now(ZoneOffset.UTC);
            var exchangeRate = bankService.findExchangeRateAtTimeStamp(fromCurrency, toCurrency, now);

            var response = FetchExchangeRateResponse.newBuilder()
                    .setFromCurrency(fromCurrency)
                    .setToCurrency(toCurrency)
                    .setRate(exchangeRate)
                    .setTimestamp(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();

            responseObserver.onNext(response);

            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (serverCallObserver.isCancelled()) {
            log.info("Client cancelled the request");
        }
    }

}
