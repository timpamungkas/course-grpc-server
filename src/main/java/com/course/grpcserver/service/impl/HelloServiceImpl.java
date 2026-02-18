package com.course.grpcserver.service.impl;

import java.time.LocalTime;

import org.springframework.stereotype.Service;

import com.course.grpcserver.service.HelloService;

@Service
public class HelloServiceImpl implements HelloService {

    @Override
    public String generateHello(String name) {
        return "Hello, " + name + "! Now is " + LocalTime.now();
    }

}
