package ru.nsu.ga.grentseva.async_http_crawler;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpCrawlerIntegrationTest {
    private HttpServer server;
    private Map<String, AtomicInteger> requests;
    private CountDownLatch parallelRequests;

    @BeforeEach
    void startServer() throws IOException {
        requests = new ConcurrentHashMap<>();
        parallelRequests = new CountDownLatch(2);

        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            requests.computeIfAbsent(path, key -> new AtomicInteger()).incrementAndGet();

            if (path.equals("/cat") || path.equals("/dog")) {
                parallelRequests.countDown();

                try {
                    if (!parallelRequests.await(5, TimeUnit.SECONDS)) {
                        exchange.sendResponseHeaders(500, -1);
                        return;
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    exchange.close();
                    return;
                }
            }

            String json = switch (path) {
                case "/" -> "{\"message\":\"zebra\",\"successors\":[\"/cat\",\"/dog\"]}";
                case "/cat" -> "{\"message\":\"apple\",\"successors\":[\"/fox\",\"/\"]}";
                case "/dog" -> "{\"message\":\"banana\",\"successors\":[\"/fox\"]}";
                case "/fox" -> "{\"message\":\"cat\",\"successors\":[]}";
                default -> "{\"message\":\"unknown\",\"successors\":[]}";
            };

            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            try {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } finally {
                exchange.close();
            }
        });

        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void shouldCrawlAllPagesWithoutDuplicatesAndSortMessages() throws Exception {
        HttpCrawler crawler = new HttpCrawler(new CrawlerConfig("localhost", server.getAddress().getPort()));

        long start = System.nanoTime();
        List<String> messages = crawler.crawl();
        double elapsed = (System.nanoTime() - start) / 1_000_000_000.0;

        assertEquals(List.of("apple", "banana", "cat", "zebra"), messages);
        assertEquals(Map.of("/", 1, "/cat", 1, "/dog", 1, "/fox", 1),
                requests.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey, entry -> entry.getValue().get())));
        assertEquals(0, parallelRequests.getCount(), "Both requests must start before either can finish");
        assertTrue(elapsed < 2.8, "Crawl took " + elapsed + " seconds");
    }
}