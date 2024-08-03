package com.mcm.product_catalog.util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyPairUtil {

    private static final String PRIVATE_KEY_FILE = "src/main/resources/private_key.pem";
    private static final String PUBLIC_KEY_FILE = "src/main/resources/public_key.pem";

    private static void generateAndStoreKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        // Save the keys to files
        saveKeyToFile(PRIVATE_KEY_FILE, privateKey.getEncoded(), "PRIVATE KEY");
        saveKeyToFile(PUBLIC_KEY_FILE, publicKey.getEncoded(), "PUBLIC KEY");
    }

    private static void saveKeyToFile(String filePath, byte[] key, String keyType) throws IOException {
        String encodedKey = Base64.getEncoder().encodeToString(key);
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("-----BEGIN " + keyType + "-----\n");
            writer.write(encodedKey);
            writer.write("\n-----END " + keyType + "-----\n");
        }
    }

    public static PrivateKey loadPrivateKey() throws Exception {
    	//check if the private key file exists
    	if (!Files.exists(Paths.get(PRIVATE_KEY_FILE))) {
    		generateAndStoreKeyPair();
    	}
        byte[] keyBytes = Files.readAllBytes(Paths.get(PRIVATE_KEY_FILE));
        String privateKeyPEM = new String(keyBytes).replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
        byte[] decodedKey = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public static PublicKey loadPublicKey() throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(PUBLIC_KEY_FILE));
        String publicKeyPEM = new String(keyBytes).replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "").replaceAll("\\s", "");
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}
