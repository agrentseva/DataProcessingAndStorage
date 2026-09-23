package ru.nsu.ga.grentseva.key_generation_server.server;

import java.nio.ByteBuffer;

public final class Protocol {

    public static final byte NAME_TERMINATOR = 0;

    private Protocol() {
    }

    public static boolean readName(ByteBuffer buffer, ClientConnection connection) {
        while (buffer.hasRemaining()) {
            byte value = buffer.get();

            if (value == NAME_TERMINATOR) {
                return true;
            }

            if ((value & 0x80) != 0) {
                throw new IllegalArgumentException("Client name must be ASCII");
            }

            connection.getName().append((char) value);
        }

        return false;
    }

    public static String getName(ClientConnection connection) {
        return connection.getName().toString();
    }
}