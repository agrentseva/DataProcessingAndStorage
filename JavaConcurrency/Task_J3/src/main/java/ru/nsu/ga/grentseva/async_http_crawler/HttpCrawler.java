package ru.nsu.ga.grentseva.async_http_crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

public class HttpCrawler {
    private final CrawlerConfig config;
    private final HttpPageFetcher pageFetcher;

    public HttpCrawler(CrawlerConfig config) {
        this.config = config;
        this.pageFetcher = new HttpPageFetcher();
    }

    public List<String> crawl() throws InterruptedException, IOException {
        Set<String> visitedPaths = ConcurrentHashMap.newKeySet();
        List<String> messages = new ArrayList<>();
        ConcurrentLinkedQueue<IOException> errors = new ConcurrentLinkedQueue<>();
        Phaser phaser = new Phaser(1);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            schedulePath("/", executor, phaser, visitedPaths, messages, errors);
            phaser.arriveAndAwaitAdvance();
        }

        if (!errors.isEmpty()) {
            IOException exception = new IOException("Some pages could not be fetched");
            errors.forEach(exception::addSuppressed);
            throw exception;
        }

        messages.sort(String::compareTo);
        return messages;
    }

    private void schedulePath(
            String path,
            ExecutorService executor,
            Phaser phaser,
            Set<String> visitedPaths,
            List<String> messages,
            ConcurrentLinkedQueue<IOException> errors
    ) {
        if (!visitedPaths.add(path)) {
            return;
        }

        phaser.register();

        executor.submit(() -> {
            try {
                PageResponse response = pageFetcher.fetch(config.buildUri(path));
                synchronized (messages) {
                    messages.add(response.message());
                }

                for (String successor : response.successors()) {
                    schedulePath(
                            successor,
                            executor,
                            phaser,
                            visitedPaths,
                            messages,
                            errors
                    );
                }
            } catch (IOException exception) {
                errors.add(new IOException("Failed to fetch path: " + path, exception));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                errors.add(new IOException("Interrupted while fetching path: " + path, exception));
            } finally {
                phaser.arriveAndDeregister();
            }
        });
    }
}