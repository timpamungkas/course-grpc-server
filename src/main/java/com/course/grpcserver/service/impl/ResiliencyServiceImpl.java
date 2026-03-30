package com.course.grpcserver.service.impl;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.course.grpcserver.domain.GenerateResiliencyResultPlaceholder;
import com.course.grpcserver.service.ResiliencyService;

@Service
public class ResiliencyServiceImpl implements ResiliencyService {

    @Override
    public GenerateResiliencyResultPlaceholder generateResiliencyResponse(int minDelaySecond, int maxDelaySecond,
            List<Integer> possibleStatusCodes) {
        var delay = ThreadLocalRandom.current().nextInt(minDelaySecond, maxDelaySecond + 1);

        try {
            TimeUnit.SECONDS.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        var statusCodeIndex = ThreadLocalRandom.current().nextInt(possibleStatusCodes.size());
        var statusCode = possibleStatusCodes.get(statusCodeIndex);
        var dummyString = String.format(
                "The time now is %s, execution delayed for %d seconds",
                LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME),
                delay);

        return GenerateResiliencyResultPlaceholder.builder()
                .dummyString(dummyString)
                .statusCode(statusCode)
                .build();
    }

}
