package ru.nsu.ga.grentseva.key_generation_server.generation;

import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class CertificateGenerator {

    private final PrivateKey signingKey;
    private final String issuer;

    public CertificateGenerator(PrivateKey signingKey, String issuer) {
        this.signingKey = signingKey;
        this.issuer = issuer;
    }

    public byte[] generate(String name, PublicKey publicKey) {
        try {
            Date notBefore = new Date();
            Date notAfter = new Date(notBefore.getTime() + 1L * 24 * 60 * 60 * 1000);

            BigInteger serialNumber = new BigInteger(100, new SecureRandom());

            X500Name issuerName = new X500Name(issuer);
            X500Name subjectName = new X500Name("CN=" + name);

            X509v3CertificateBuilder certificateBuilder =
                    new JcaX509v3CertificateBuilder(issuerName, serialNumber, notBefore,
                            notAfter, subjectName, publicKey);

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(signingKey);

            X509CertificateHolder certificateHolder = certificateBuilder.build(signer);

            return certificateHolder.getEncoded();
        } catch (OperatorCreationException | IOException exception) {
            throw new IllegalStateException("Failed to generate X.509 certificate", exception);
        }
    }
}