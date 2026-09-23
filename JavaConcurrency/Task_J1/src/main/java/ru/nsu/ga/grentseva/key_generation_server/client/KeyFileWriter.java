package ru.nsu.ga.grentseva.key_generation_server.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class KeyFileWriter {

    private KeyFileWriter() {
    }

    public static void writeKey(String name, byte[] privateKey, byte[] publicKey)
            throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + privateKey.length
                        + Integer.BYTES + publicKey.length);

        buffer.putInt(privateKey.length);
        buffer.put(privateKey);

        buffer.putInt(publicKey.length);
        buffer.put(publicKey);

        Files.write(Path.of(name + ".key"), buffer.array());
    }

    public static void writeCertificate(String name, byte[] certificate)
            throws IOException {
        Files.write(Path.of(name + ".crt"), certificate);
    }
}