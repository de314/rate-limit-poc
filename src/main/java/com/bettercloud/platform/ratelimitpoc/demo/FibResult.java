package com.bettercloud.platform.ratelimitpoc.demo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FibResult {
    private long result;
    private long startTime;
    private long duration;
}
