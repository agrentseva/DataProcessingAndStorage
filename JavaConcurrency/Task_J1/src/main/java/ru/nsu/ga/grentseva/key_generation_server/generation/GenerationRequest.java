package ru.nsu.ga.grentseva.key_generation_server.generation;

import java.util.concurrent.CompletableFuture;

public class GenerationRequest {

    private final String name;
    private final CompletableFuture<GenerationResult> future;

    public GenerationRequest(String name, CompletableFuture<GenerationResult> future) {
        this.name = name;
        this.future = future;
    }

    public String getName() {
        return name;
    }

    public CompletableFuture<GenerationResult> getFuture() {
        return future;
    }
}
