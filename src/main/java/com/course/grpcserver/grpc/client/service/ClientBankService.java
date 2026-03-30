package com.course.grpcserver.grpc.client.service;

import java.util.List;

import com.course.central.proto.bank.AccountMessage.GetCurrentBalanceResponse;
import com.course.central.proto.bank.TransactionMessage.TransactionRequest;
import com.course.central.proto.bank.TransactionMessage.TransactionSummaryResponse;
import com.course.central.proto.bank.TransferMessage.TransferRequest;

public interface ClientBankService {

    GetCurrentBalanceResponse callGetCurrentBalance(String accountNumber) throws Exception;

    void callFetchExchangeRates(String fromCurrency, String toCurrency) throws Exception;

    TransactionSummaryResponse callSummarizeTransactions(List<TransactionRequest> requests) throws Exception;

    void callTransferMultiple(List<TransferRequest> requests) throws Exception;

}
