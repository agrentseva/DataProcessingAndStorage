package ru.nsu.ga.grentseva.key_generation_server.generation;

import java.security.PrivateKey;
import java.security.PublicKey;

public class GenerationResult {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final byte[] certificate;

    public GenerationResult(PrivateKey privateKey, PublicKey publicKey, byte[] certificate) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.certificate = certificate;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] getCertificate() {
        return certificate;
    }
}
