package ru.nsu.ga.grentseva.key_generation_server.sending;

import ru.nsu.ga.grentseva.key_generation_server.generation.GenerationResult;
import ru.nsu.ga.grentseva.key_generation_server.server.ClientConnection;

public class SendTask {

    private final ClientConnection connection;
    private final GenerationResult result;

    public SendTask(ClientConnection connection, GenerationResult result) {
        this.connection = connection;
        this.result = result;
    }

    public ClientConnection getConnection() {
        return connection;
    }

    public GenerationResult getResult() {
        return result;
    }
}