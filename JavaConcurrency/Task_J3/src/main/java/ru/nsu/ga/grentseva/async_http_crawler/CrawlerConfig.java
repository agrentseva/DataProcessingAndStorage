package ru.nsu.ga.grentseva.async_http_crawler;

import java.net.URI;

public record CrawlerConfig(String host, int port) {
    private static final int DEFAULT_PORT = 8080;

    public CrawlerConfig {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Host must not be empty");
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
    }

    public static CrawlerConfig fromArgs(String[] args) {
        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("Usage: <host> [port]");
        }

        String host = args[0];
        int port = DEFAULT_PORT;

        if (args.length == 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Port must be an integer", exception);
            }
        }

        return new CrawlerConfig(host, port);
    }

    public URI buildUri(String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return URI.create("http://" + host + ":" + port + normalizedPath);
    }
}
