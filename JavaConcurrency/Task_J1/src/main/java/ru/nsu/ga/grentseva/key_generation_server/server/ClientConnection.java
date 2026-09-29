package ru.nsu.ga.grentseva.key_generation_server.server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientConnection {

    private final SocketChannel channel;
    private final ByteBuffer inputBuffer = ByteBuffer.allocate(1024);
    private final StringBuilder name = new StringBuilder();

    public ClientConnection(SocketChannel channel) {
        this.channel = channel;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public ByteBuffer getInputBuffer() {
        return inputBuffer;
    }

    public StringBuilder getName() {
        return name;
    }

    public void clearName() {
        name.setLength(0);
    }
}