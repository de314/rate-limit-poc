package com.bettercloud.platform.ratelimitpoc.demo;

import org.springframework.stereotype.Service;

@Service
public class SlowFibonacciService implements FibonacciService {

    @Override
    public long fib(int i) {
        if (i <= 1) {
            return 1;
        }
        return fib(i - 1) + fib(i - 2);
    }
}
