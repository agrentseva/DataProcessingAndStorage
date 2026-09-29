package ru.nsu.ga.grentseva.key_generation_server.storage;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import ru.nsu.ga.grentseva.key_generation_server.generation.GenerationResult;

public class KeyStorage {

    private final Map<String, CompletableFuture<GenerationResult>> entries =
            new ConcurrentHashMap<>();

    public CompletableFuture<GenerationResult> get(String name) {
        return entries.get(name);
    }

    public void put(String name, CompletableFuture<GenerationResult> future) {
        entries.put(name, future);
    }

    public CompletableFuture<GenerationResult> putIfAbsent(
            String name, CompletableFuture<GenerationResult> future) {
        return entries.putIfAbsent(name, future);
    }
}
