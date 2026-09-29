package ru.nsu.ga.grentseva.key_generation_server.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import ru.nsu.ga.grentseva.key_generation_server.generation.CertificateGenerator;
import ru.nsu.ga.grentseva.key_generation_server.generation.GenerationRequest;
import ru.nsu.ga.grentseva.key_generation_server.generation.GenerationResult;
import ru.nsu.ga.grentseva.key_generation_server.generation.GenerationWorker;
import ru.nsu.ga.grentseva.key_generation_server.generation.KeyGenerator;
import ru.nsu.ga.grentseva.key_generation_server.sending.ResponseSender;
import ru.nsu.ga.grentseva.key_generation_server.sending.SendTask;
import ru.nsu.ga.grentseva.key_generation_server.signing.SigningKeyLoader;
import ru.nsu.ga.grentseva.key_generation_server.storage.KeyStorage;

public class Server {

    private final ServerConfig config;
    private final Selector selector;
    private final ServerSocketChannel serverChannel;

    private final BlockingQueue<GenerationRequest> generationQueue;
    private final KeyStorage keyStorage;
    private final ExecutorService generationExecutor;

    private final ResponseSender responseSender;
    private final Thread responseSenderThread;

    private final KeyGenerator keyGenerator;
    private final CertificateGenerator certificateGenerator;

    public Server(ServerConfig config) throws IOException {
        this.config = config;

        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();

        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(config.getPort()));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        this.generationQueue = new LinkedBlockingQueue<>();
        this.keyStorage = new KeyStorage();
        this.generationExecutor = Executors.newFixedThreadPool(config.getGeneratorThreads());

        SigningKeyLoader signingKeyLoader = new SigningKeyLoader();
        PrivateKey signingKey = signingKeyLoader.load(Path.of(config.getSigningKeyPath()));

        this.keyGenerator = new KeyGenerator();
        this.certificateGenerator = new CertificateGenerator(signingKey, config.getIssuer());

        this.responseSender = new ResponseSender();
        this.responseSenderThread = new Thread(responseSender, "response-sender");
    }

    public void start() throws IOException {
        startGenerationWorkers();
        responseSenderThread.start();

        System.out.println("Server started on port " + config.getPort());

        try {
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();

                var iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        acceptClient();
                    }

                    if (key.isReadable()) {
                        readClient(key);
                    }
                }
            }
        } finally {
            shutdown();
        }
    }

    private void startGenerationWorkers() {
        for (int i = 0; i < config.getGeneratorThreads(); i++) {
            GenerationWorker worker = new GenerationWorker(
                    generationQueue, keyGenerator, certificateGenerator);

            generationExecutor.submit(worker);
        }
    }

    private void acceptClient() throws IOException {
        SocketChannel clientChannel = serverChannel.accept();

        if (clientChannel == null) {
            return;
        }

        clientChannel.configureBlocking(false);

        ClientConnection connection = new ClientConnection(clientChannel);

        SelectionKey key = clientChannel.register(selector, SelectionKey.OP_READ);
        key.attach(connection);

        System.out.println("Client connected: " + clientChannel.getRemoteAddress());
    }

    private void readClient(SelectionKey key) throws IOException {
        ClientConnection connection = (ClientConnection) key.attachment();
        SocketChannel channel = connection.getChannel();
        ByteBuffer buffer = connection.getInputBuffer();

        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            closeClient(key);
            return;
        }

        buffer.flip();

        try {
            boolean nameReceived = Protocol.readName(buffer, connection);

            if (nameReceived) {
                handleName(connection);

                buffer.clear();
                connection.clearName();
            } else {
                buffer.compact();
            }
        } catch (IllegalArgumentException exception) {
            closeClient(key);
        }
    }

    private void handleName(ClientConnection connection) {
        String name = Protocol.getName(connection);

        System.out.println("Received name: " + name);

        CompletableFuture<GenerationResult> future = keyStorage.get(name);
        boolean newGeneration = false;

        if (future == null) {
            CompletableFuture<GenerationResult> newFuture = new CompletableFuture<>();
            CompletableFuture<GenerationResult> existingFuture =
                    keyStorage.putIfAbsent(name, newFuture);

            if (existingFuture == null) {
                future = newFuture;
                newGeneration = true;
            } else {
                future = existingFuture;
            }
        }

        registerResponse(connection, future);

        if (newGeneration) {
            generationQueue.offer(new GenerationRequest(name, future));
        }
    }

    private void registerResponse(ClientConnection connection,
                                  CompletableFuture<GenerationResult> future) {

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                try {
                    connection.getChannel().close();
                } catch (IOException ignored) {
                }
                return;
            }

            responseSender.submit(new SendTask(connection, result));
        });
    }

    private void closeClient(SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();

        System.out.println("Client disconnected");
    }

    private void shutdown() {
        generationExecutor.shutdownNow();
        responseSenderThread.interrupt();

        try {
            responseSenderThread.join(1000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }

        try {
            for (SelectionKey key : selector.keys()) {
                key.channel().close();
            }
        } catch (IOException ignored) {
        }

        try {
            selector.close();
        } catch (IOException ignored) {
        }

        try {
            serverChannel.close();
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.err.println(
                    "Usage: Server <port> <generatorThreads> <issuer> <signingKeyPath>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        int generatorThreads = Integer.parseInt(args[1]);
        String issuer = args[2];
        String signingKeyPath = args[3];

        ServerConfig config = new ServerConfig(
                port, generatorThreads, issuer, signingKeyPath);

        Server server = new Server(config);
        server.start();
    }
}