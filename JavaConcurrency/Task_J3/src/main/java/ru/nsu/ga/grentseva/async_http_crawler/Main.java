package ru.nsu.ga.grentseva.async_http_crawler;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            CrawlerConfig config = CrawlerConfig.fromArgs(args);
            HttpCrawler crawler = new HttpCrawler(config);

            List<String> messages = crawler.crawl();

            for (String message : messages) {
                System.out.println(message);
            }
        } catch (IllegalArgumentException exception) {
            System.err.println(exception.getMessage());
            System.exit(1);
        } catch (IOException exception) {
            System.err.println("Crawler failed: " + exception.getMessage());
            System.exit(1);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.err.println("Crawler was interrupted");
            System.exit(1);
        }
    }
}