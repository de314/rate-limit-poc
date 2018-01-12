package com.bettercloud.platform.ratelimitpoc.client.models;

import lombok.Data;

import java.io.IOException;
import java.util.List;

@Data
public class CompositeException extends IOException {
    private List<Throwable> causes;

    public CompositeException(String message, List<Throwable> causes) {
        super(message);
        this.causes = causes;
    }
}
