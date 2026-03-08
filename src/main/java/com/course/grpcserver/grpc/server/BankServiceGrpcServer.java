package com.course.grpcserver.grpc.server;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;

import com.course.central.proto.bank.AccountMessage.GetCurrentBalanceRequest;
import com.course.central.proto.bank.AccountMessage.GetCurrentBalanceResponse;
import com.course.central.proto.bank.BankServiceGrpc;
import com.course.grpcserver.service.BankService;
import com.google.type.Date;

import io.grpc.stub.StreamObserver;

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

}
