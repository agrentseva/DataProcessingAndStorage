package ru.nsu.ga.grentseva.key_generation_server.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class ServerTest {

    private static final int GENERATOR_THREADS = 4;
    private static final String ISSUER = "CN=KeyServer";

    @Test
    void shouldHandleSeveralClients() throws Exception {
        int port = findFreePort();
        Server server = createServer(port);

        CompletableFuture<Void> serverThread = startServer(server);

        try {
            Thread.sleep(500);

            SocketChannel client1 = connect(port);
            SocketChannel client2 = connect(port);
            SocketChannel client3 = connect(port);

            sendName(client1, "Alice");
            sendName(client2, "Bob");
            sendName(client3, "Charlie");

            byte[] alicePrivateKey = readData(client1);
            byte[] bobPrivateKey = readData(client2);
            byte[] charliePrivateKey = readData(client3);

            assertNotNull(alicePrivateKey);
            assertNotNull(bobPrivateKey);
            assertNotNull(charliePrivateKey);

            assertFalse(alicePrivateKey.length == 0);
            assertFalse(bobPrivateKey.length == 0);
            assertFalse(charliePrivateKey.length == 0);

            client1.close();
            client2.close();
            client3.close();
        } finally {
            serverThread.cancel(true);
        }
    }

    @Test
    void shouldHandleSlowClient() throws Exception {
        int port = findFreePort();
        Server server = createServer(port);

        CompletableFuture<Void> serverThread = startServer(server);

        try {
            Thread.sleep(500);

            SocketChannel slowClient = connect(port);
            SocketChannel normalClient = connect(port);

            sendName(slowClient, "SlowClient");
            sendName(normalClient, "NormalClient");

            Thread.sleep(5000);

            byte[] normalPrivateKey = readData(normalClient);
            byte[] normalPublicKey = readData(normalClient);
            byte[] normalCertificate = readData(normalClient);

            assertFalse(normalPrivateKey.length == 0);
            assertFalse(normalPublicKey.length == 0);
            assertFalse(normalCertificate.length == 0);

            byte[] slowPrivateKey = readData(slowClient);
            byte[] slowPublicKey = readData(slowClient);
            byte[] slowCertificate = readData(slowClient);

            assertFalse(slowPrivateKey.length == 0);
            assertFalse(slowPublicKey.length == 0);
            assertFalse(slowCertificate.length == 0);

            slowClient.close();
            normalClient.close();
        } finally {
            serverThread.cancel(true);
        }
    }

    @Test
    void shouldHandleClientThatAbortsAfterRequest() throws Exception {
        int port = findFreePort();
        Server server = createServer(port);

        CompletableFuture<Void> serverThread = startServer(server);

        try {
            Thread.sleep(500);

            SocketChannel abortClient = connect(port);
            SocketChannel normalClient = connect(port);

            sendName(abortClient, "AbortClient");

            abortClient.close();

            sendName(normalClient, "NormalClient");

            byte[] privateKey = readData(normalClient);
            byte[] publicKey = readData(normalClient);
            byte[] certificate = readData(normalClient);

            assertFalse(privateKey.length == 0);
            assertFalse(publicKey.length == 0);
            assertFalse(certificate.length == 0);

            normalClient.close();
        } finally {
            serverThread.cancel(true);
        }
    }

    @Test
    void shouldReturnSameResultForRepeatedName() throws Exception {
        int port = findFreePort();
        Server server = createServer(port);

        CompletableFuture<Void> serverThread = startServer(server);

        try {
            Thread.sleep(500);

            SocketChannel client1 = connect(port);
            SocketChannel client2 = connect(port);

            sendName(client1, "SameClient");
            sendName(client2, "SameClient");

            byte[] firstPrivateKey = readData(client1);
            byte[] firstPublicKey = readData(client1);
            byte[] firstCertificate = readData(client1);

            byte[] secondPrivateKey = readData(client2);
            byte[] secondPublicKey = readData(client2);
            byte[] secondCertificate = readData(client2);

            assertNotNull(firstPrivateKey);
            assertNotNull(secondPrivateKey);

            org.junit.jupiter.api.Assertions.assertArrayEquals(
                    firstPrivateKey, secondPrivateKey);

            org.junit.jupiter.api.Assertions.assertArrayEquals(
                    firstPublicKey, secondPublicKey);

            org.junit.jupiter.api.Assertions.assertArrayEquals(
                    firstCertificate, secondCertificate);

            client1.close();
            client2.close();
        } finally {
            serverThread.cancel(true);
        }
    }

    private static Server createServer(int port) throws IOException {
        Path signingKey = Path.of(
                System.getProperty("user.dir"),
                "signing-key.pem");

        ServerConfig config = new ServerConfig(
                port,
                GENERATOR_THREADS,
                ISSUER,
                signingKey.toString());

        return new Server(config);
    }

    private static CompletableFuture<Void> startServer(Server server) {
        return CompletableFuture.runAsync(() -> {
            try {
                server.start();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    private static SocketChannel connect(int port) throws IOException {
        SocketChannel client = SocketChannel.open();
        client.connect(new InetSocketAddress("localhost", port));
        return client;
    }

    private static void sendName(SocketChannel client, String name)
            throws IOException {
        byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);
        ByteBuffer buffer = ByteBuffer.allocate(nameBytes.length + 1);

        buffer.put(nameBytes);
        buffer.put((byte) 0);
        buffer.flip();

        while (buffer.hasRemaining()) {
            client.write(buffer);
        }
    }

    private static byte[] readData(SocketChannel client) throws IOException {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);

        readFully(client, lengthBuffer);
        lengthBuffer.flip();

        int length = lengthBuffer.getInt();

        if (length < 0) {
            throw new IOException("Invalid data length: " + length);
        }

        ByteBuffer data = ByteBuffer.allocate(length);

        readFully(client, data);

        return data.array();
    }

    private static void readFully(SocketChannel client, ByteBuffer buffer)
            throws IOException {
        while (buffer.hasRemaining()) {
            int bytesRead = client.read(buffer);

            if (bytesRead == -1) {
                throw new IOException("Server closed connection");
            }
        }
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}