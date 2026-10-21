package ru.nsu.ga.grentseva.async_http_crawler;

import java.util.List;

public record PageResponse(
        String message,
        List<String> successors
) {
}

