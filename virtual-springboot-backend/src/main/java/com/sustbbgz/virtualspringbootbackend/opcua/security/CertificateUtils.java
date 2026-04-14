package com.sustbbgz.virtualspringbootbackend.opcua.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.MessageDigest;

public class CertificateUtils {

    private static final Logger logger = LoggerFactory.getLogger(CertificateUtils.class);
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEYSTORE_PASSWORD = "password";
    private static final String CERTIFICATE_ALIAS = "opcua-server";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyPair generateKeyPair() throws Exception {
        return SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
    }

    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String commonName, String applicationUri) throws Exception {
        SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair)
                .setCommonName(commonName)
                .setOrganization("SUST")
                .setOrganizationalUnit("Digital Twin Department")
                .setLocalityName("Xi'an")
                .setStateName("Shaanxi")
                .setCountryCode("CN")
                .setApplicationUri(applicationUri);

        return builder.build();
    }

    public static String certificateToPem(X509Certificate certificate) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN CERTIFICATE-----\n");
        sb.append(java.util.Base64.getMimeEncoder(64, "\n".getBytes())
                .encodeToString(certificate.getEncoded()));
        sb.append("\n-----END CERTIFICATE-----\n");
        return sb.toString();
    }

    public static String getCertificateThumbprint(X509Certificate certificate) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] der = certificate.getEncoded();
        byte[] digest = md.digest(der);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static KeyPairLoader createKeyPairLoader(Path certificatePath, String applicationUri) {
        return new KeyPairLoader(certificatePath, applicationUri);
    }

    public static class KeyPairLoader {
        private final Path certificatePath;
        private final String applicationUri;
        private KeyPair keyPair;
        private X509Certificate certificate;

        public KeyPairLoader(Path certificatePath, String applicationUri) {
            this.certificatePath = certificatePath;
            this.applicationUri = applicationUri;
        }

        public KeyPair loadOrCreateKeyPair() throws Exception {
            Path keyStorePath = certificatePath.resolve("server.pfx");
            
            if (Files.exists(keyStorePath)) {
                logger.info("Loading existing certificate from: {}", keyStorePath);
                return loadKeyPairFromKeystore(keyStorePath);
            } else {
                logger.info("Generating new self-signed certificate at: {}", keyStorePath);
                return generateAndSaveKeyPair(keyStorePath);
            }
        }

        private KeyPair loadKeyPairFromKeystore(Path keyStorePath) throws Exception {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            try (InputStream is = Files.newInputStream(keyStorePath)) {
                keyStore.load(is, KEYSTORE_PASSWORD.toCharArray());
            }

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(CERTIFICATE_ALIAS, KEYSTORE_PASSWORD.toCharArray());
            certificate = (X509Certificate) keyStore.getCertificate(CERTIFICATE_ALIAS);
            PublicKey publicKey = certificate.getPublicKey();

            keyPair = new KeyPair(publicKey, privateKey);
            return keyPair;
        }

        private KeyPair generateAndSaveKeyPair(Path keyStorePath) throws Exception {
            Files.createDirectories(certificatePath);

            keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);

            String commonName = "DigitalTwinOPCUAServer";
            String organisation = "SUST";
            String organisationalUnit = "Digital Twin Department";
            String locality = "Xi'an";
            String state = "Shaanxi";
            String country = "CN";

            SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair)
                    .setCommonName(commonName)
                    .setOrganization(organisation)
                    .setOrganizationalUnit(organisationalUnit)
                    .setLocalityName(locality)
                    .setStateName(state)
                    .setCountryCode(country)
                    .setApplicationUri(applicationUri);

            certificate = builder.build();

            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(null, null);
            keyStore.setKeyEntry(CERTIFICATE_ALIAS, keyPair.getPrivate(), KEYSTORE_PASSWORD.toCharArray(),
                    new java.security.cert.Certificate[]{certificate});

            try (OutputStream os = Files.newOutputStream(keyStorePath)) {
                keyStore.store(os, KEYSTORE_PASSWORD.toCharArray());
            }

            logger.info("Self-signed certificate generated successfully");
            return keyPair;
        }

        public X509Certificate getCertificate() {
            return certificate;
        }

        public KeyPair getKeyPair() {
            return keyPair;
        }
    }

    public static boolean validateCertificate(X509Certificate certificate) {
        try {
            certificate.checkValidity();
            certificate.verify(certificate.getPublicKey());
            return true;
        } catch (Exception e) {
            logger.error("Certificate validation failed", e);
            return false;
        }
    }
}
