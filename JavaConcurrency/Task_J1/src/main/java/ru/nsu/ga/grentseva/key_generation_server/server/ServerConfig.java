package ru.nsu.ga.grentseva.key_generation_server.server;

public class ServerConfig {

    private final int port;
    private final int generatorThreads;
    private final String issuer;
    private final String signingKeyPath;

    public ServerConfig(int port, int generatorThreads, String issuer, String signingKeyPath) {
        this.port = port;
        this.generatorThreads = generatorThreads;
        this.issuer = issuer;
        this.signingKeyPath = signingKeyPath;
    }

    public int getPort() {
        return port;
    }

    public int getGeneratorThreads() {
        return generatorThreads;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getSigningKeyPath() {
        return signingKeyPath;
    }
}