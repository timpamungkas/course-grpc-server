package com.course.grpcserver.service;

import java.util.List;

import com.course.grpcserver.domain.GenerateResiliencyResultPlaceholder;

public interface ResiliencyService {

    GenerateResiliencyResultPlaceholder generateResiliencyResponse(
            int minDelaySecond, int maxDelaySecond, List<Integer> possibleStatusCodes);

}
