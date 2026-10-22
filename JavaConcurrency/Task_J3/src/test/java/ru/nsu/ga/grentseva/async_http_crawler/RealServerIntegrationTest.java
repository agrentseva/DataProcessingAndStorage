package ru.nsu.ga.grentseva.async_http_crawler;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RealServerIntegrationTest {
    @Test
    void shouldCrawlRealServerCorrectly() throws Exception {
        HttpCrawler crawler = new HttpCrawler(new CrawlerConfig("localhost", 8080));

        long start = System.nanoTime();
        List<String> messages = crawler.crawl();
        double elapsedSeconds = (System.nanoTime() - start) / 1_000_000_000.0;

        assertNotNull(messages, "Crawler result must not be null");
        assertFalse(messages.isEmpty(), "Crawler must collect messages");

        assertEquals(messages.stream().sorted().toList(), messages, "Messages must be sorted lexicographically");

        System.out.printf("Crawl time: %.2f seconds%n", elapsedSeconds);
        System.out.printf("Messages collected: %d%n", messages.size());
    }
}
