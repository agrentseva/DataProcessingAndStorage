package ru.nsu.ga.grentseva.key_generation_server.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.channels.SocketChannel;

public class Client {

    private final ClientConfig config;

    public Client(ClientConfig config) {
        this.config = config;
    }

    public void start() throws IOException, InterruptedException {
        try (SocketChannel channel = SocketChannel.open()) {
            connect(channel);
            sendName(channel);

            if (config.isAbort()) {
                System.out.println("Client terminated without reading the response.");
                return;
            }

            waitBeforeReading();
            readResponse(channel);
        }
    }

    private void connect(SocketChannel channel) throws IOException {
        InetSocketAddress address = new InetSocketAddress(config.getHost(), config.getPort());

        channel.connect(address);

        System.out.println("Connected to " + config.getHost() + ":" + config.getPort());
    }

    private void sendName(SocketChannel channel) throws IOException {
        if (!StandardCharsets.US_ASCII.newEncoder().canEncode(config.getName())) {
            throw new IOException("Client name must contain only ASCII characters");
        }

        byte[] nameBytes = config.getName().getBytes(StandardCharsets.US_ASCII);
        ByteBuffer buffer = ByteBuffer.allocate(nameBytes.length + 1);

        buffer.put(nameBytes);
        buffer.put((byte) 0);
        buffer.flip();

        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }

        System.out.println("Request sent for name: " + config.getName());
    }

    private void waitBeforeReading() throws InterruptedException {
        if (config.getDelaySeconds() > 0) {
            System.out.println(
                    "Waiting " + config.getDelaySeconds() + " seconds before reading...");
            Thread.sleep(config.getDelaySeconds() * 1000L);
        }
    }

    private void readResponse(SocketChannel channel) throws IOException {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);

        byte[] privateKey = readData(channel, lengthBuffer);
        byte[] publicKey = readData(channel, lengthBuffer);
        byte[] certificate = readData(channel, lengthBuffer);

        System.out.println("Received private key: " + privateKey.length + " bytes.");
        System.out.println("Received public key: " + publicKey.length + " bytes.");
        System.out.println("Received certificate: " + certificate.length + " bytes.");

        KeyFileWriter.writeKey(config.getName(), privateKey, publicKey);
        KeyFileWriter.writeCertificate(config.getName(), certificate);

        System.out.println("Key pair saved to " + config.getName() + ".key");
        System.out.println("Certificate saved to " + config.getName() + ".crt");
    }

    private byte[] readData(SocketChannel channel, ByteBuffer lengthBuffer) throws IOException {
        lengthBuffer.clear();
        readFully(channel, lengthBuffer);
        lengthBuffer.flip();

        int length = lengthBuffer.getInt();

        if (length < 0) {
            throw new IOException("Invalid response data length: " + length);
        }

        ByteBuffer dataBuffer = ByteBuffer.allocate(length);
        readFully(channel, dataBuffer);

        return dataBuffer.array();
    }

    private void readFully(SocketChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            int bytesRead = channel.read(buffer);

            if (bytesRead == -1) {
                throw new IOException(
                        "Server closed the connection before the response was received");
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ClientConfig config = parseArguments(args);
        new Client(config).start();
    }

    private static ClientConfig parseArguments(String[] args) {
        if (args.length < 3) {
            throw new IllegalArgumentException(
                    "Usage: Client <name> <host> <port> [--delay <seconds>] [--abort]");
        }

        String name = args[0];
        String host = args[1];
        int port = Integer.parseInt(args[2]);

        int delaySeconds = 0;
        boolean abort = false;
        int index = 3;

        while (index < args.length) {
            switch (args[index]) {
                case "--delay":
                    if (index + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for --delay");
                    }

                    delaySeconds = Integer.parseInt(args[index + 1]);

                    if (delaySeconds < 0) {
                        throw new IllegalArgumentException("Delay cannot be negative");
                    }

                    index += 2;
                    break;

                case "--abort":
                    abort = true;
                    index++;
                    break;

                default:
                    throw new IllegalArgumentException("Unknown argument: " + args[index]);
            }
        }

        if (abort && delaySeconds > 0) {
            throw new IllegalArgumentException("--delay and --abort cannot be used together");
        }

        return new ClientConfig(name, host, port, delaySeconds, abort);
    }
}