package com.course.grpcserver.grpc.client.service.impl;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.course.central.proto.bank.AccountMessage.GetCurrentBalanceRequest;
import com.course.central.proto.bank.AccountMessage.GetCurrentBalanceResponse;
import com.course.central.proto.bank.BankServiceGrpc;
import com.course.central.proto.bank.ExchangeRateMessage.FetchExchangeRateRequest;
import com.course.central.proto.bank.ExchangeRateMessage.FetchExchangeRateResponse;
import com.course.central.proto.bank.TransactionMessage.TransactionRequest;
import com.course.central.proto.bank.TransactionMessage.TransactionSummaryResponse;
import com.course.central.proto.bank.TransferMessage.TransferRequest;
import com.course.central.proto.bank.TransferMessage.TransferResponse;
import com.course.grpcserver.grpc.client.service.ClientBankService;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClientBankServiceImpl implements ClientBankService {

    private BankServiceGrpc.BankServiceBlockingV2Stub bankServiceBlockingStub;
    private BankServiceGrpc.BankServiceStub bankServiceAsyncStub;

    public ClientBankServiceImpl(
            @Autowired BankServiceGrpc.BankServiceBlockingV2Stub bankServiceBlockingStub,
            @Autowired BankServiceGrpc.BankServiceStub bankServiceAsyncStub) {
        this.bankServiceBlockingStub = bankServiceBlockingStub;
        this.bankServiceAsyncStub = bankServiceAsyncStub;
    }

    @Override
    public GetCurrentBalanceResponse callGetCurrentBalance(String accountNumber) throws Exception {
        var request = GetCurrentBalanceRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();
        var response = bankServiceBlockingStub.getCurrentBalance(request);
        log.info("[callGetCurrentBalance] account={}, amount={}", accountNumber, response.getAmount());
        return response;
    }

    @Override
    public void callFetchExchangeRates(String fromCurrency, String toCurrency) throws Exception {
        var request = FetchExchangeRateRequest.newBuilder()
                .setFromCurrency(fromCurrency)
                .setToCurrency(toCurrency)
                .build();
        var responseStream = bankServiceBlockingStub.fetchExchangeRates(request);

        while (responseStream.hasNext()) {
            var response = responseStream.read();
            log.info("[callFetchExchangeRates] {} to {} rate={} at {}",
                    response.getFromCurrency(), response.getToCurrency(),
                    response.getRate(), response.getTimestamp());
        }
    }

    @Override
    public TransactionSummaryResponse callSummarizeTransactions(List<TransactionRequest> requests) throws Exception {
        var latch = new CountDownLatch(1);
        var responseHolder = new AtomicReference<TransactionSummaryResponse>();
        var errorHolder = new AtomicReference<Throwable>();

        var requestObserver = bankServiceAsyncStub.summarizeTransactions(
                new StreamObserver<>() {

                    @Override
                    public void onNext(TransactionSummaryResponse response) {
                        responseHolder.set(response);
                    }

                    @Override
                    public void onError(Throwable t) {
                        errorHolder.set(t);
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        latch.countDown();
                    }
                });

        for (var request : requests) {
            requestObserver.onNext(request);
            TimeUnit.MILLISECONDS.sleep(500);
        }

        requestObserver.onCompleted();
        latch.await(10, TimeUnit.SECONDS);

        var error = errorHolder.get();
        if (error != null) {
            if (error instanceof Exception ex) {
                throw ex;
            }
            throw new RuntimeException(error);
        }

        return responseHolder.get();
    }

    @Override
    public void callTransferMultiple(List<TransferRequest> requests) throws Exception {
        var latch = new CountDownLatch(1);
        var errorHolder = new AtomicReference<Throwable>();

        var requestObserver = bankServiceAsyncStub.transferMultiple(
                new StreamObserver<>() {

                    @Override
                    public void onNext(TransferResponse response) {
                        log.info("[callTransferMultiple] {}→{} {} {} status={}",
                                response.getFromAccountNumber(), response.getToAccountNumber(),
                                response.getAmount(), response.getCurrency(), response.getStatus());
                    }

                    @Override
                    public void onError(Throwable t) {
                        errorHolder.set(t);
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        log.info("[callTransferMultiple] stream completed");
                        latch.countDown();
                    }
                });

        for (var request : requests) {
            requestObserver.onNext(request);
            TimeUnit.MILLISECONDS.sleep(500);
        }

        requestObserver.onCompleted();
        latch.await(10, TimeUnit.SECONDS);

        var error = errorHolder.get();
        if (error != null) {
            if (error instanceof Exception ex) {
                throw ex;
            }
            throw new RuntimeException(error);
        }
    }

}
