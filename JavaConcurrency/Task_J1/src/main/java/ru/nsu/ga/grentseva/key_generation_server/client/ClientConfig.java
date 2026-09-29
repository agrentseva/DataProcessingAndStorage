package ru.nsu.ga.grentseva.key_generation_server.client;

public class ClientConfig {

    private final String name;
    private final String host;
    private final int port;
    private final int delaySeconds;
    private final boolean abort;

    public ClientConfig(String name, String host, int port, int delaySeconds, boolean abort) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.delaySeconds = delaySeconds;
        this.abort = abort;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getDelaySeconds() {
        return delaySeconds;
    }

    public boolean isAbort() {
        return abort;
    }
}
