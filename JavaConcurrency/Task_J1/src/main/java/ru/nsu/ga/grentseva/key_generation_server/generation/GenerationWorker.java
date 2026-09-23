package ru.nsu.ga.grentseva.key_generation_server.generation;

import java.security.KeyPair;
import java.util.concurrent.BlockingQueue;

public class GenerationWorker implements Runnable {

    private final BlockingQueue<GenerationRequest> queue;
    private final KeyGenerator keyGenerator;
    private final CertificateGenerator certificateGenerator;

    public GenerationWorker(BlockingQueue<GenerationRequest> queue, KeyGenerator keyGenerator,
                            CertificateGenerator certificateGenerator) {
        this.queue = queue;
        this.keyGenerator = keyGenerator;
        this.certificateGenerator = certificateGenerator;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                GenerationRequest request = queue.take();
                generate(request);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void generate(GenerationRequest request) {
        try {
            KeyPair keyPair = keyGenerator.generate();

            byte[] certificate = certificateGenerator.generate(
                    request.getName(), keyPair.getPublic());

            GenerationResult result = new GenerationResult(keyPair.getPrivate(),
                    keyPair.getPublic(), certificate);

            request.getFuture().complete(result);
        } catch (RuntimeException exception) {
            request.getFuture().completeExceptionally(exception);
        }
    }
}