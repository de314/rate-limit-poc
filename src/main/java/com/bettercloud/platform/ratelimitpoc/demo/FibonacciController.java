package com.bettercloud.platform.ratelimitpoc.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/fib", "/rl/fib"})
public class FibonacciController {

	private final FibonacciService fibonacciService;

    FibonacciController(FibonacciService fibonacciService) {
        this.fibonacciService = fibonacciService;
    }


    @GetMapping("/calc/{i}")
	public FibResult calc(@PathVariable("i") int i) {
        long startTime = System.currentTimeMillis();
        long result = fibonacciService.fib(i);
        long duration = System.currentTimeMillis() - startTime;
        return FibResult.builder()
                .result(result)
                .startTime(startTime)
                .duration(duration)
                .build();
	}
}
