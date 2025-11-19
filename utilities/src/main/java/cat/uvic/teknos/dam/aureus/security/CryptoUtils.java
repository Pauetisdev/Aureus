package cat.uvic.teknos.dam.aureus.security;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * Utility class for hashing strings/bytes using an algorithm and salt configured
 * via a crypto.properties file on the classpath. The resulting digest is
 * returned as a lowercase hexadecimal string.
 */
public class CryptoUtils {

    // Name of the properties resource on the classpath (can be overridden for tests)
    private static final String DEFAULT_RESOURCE = "crypto.properties";
    private static final String RESOURCE_PROP = "crypto.properties.resource";

    private static volatile String algorithm;
    private static volatile byte[] saltBytes;
    private static volatile boolean initialized = false;

    // Prevent instantiation
    private CryptoUtils() {}

    /**
     * Compute the hex-encoded cryptographic hash of the provided text.
     * Returns null if the input is null. Empty string is hashed (with salt).
     */
    public static String hash(String plainText) {
        if (plainText == null) return null; //importante especificar UTF_8
        return hash(plainText.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Compute the hex-encoded cryptographic hash of the provided bytes.
     * Returns null if bytes is null. Empty array is hashed (with salt).
     */
    public static String hash(byte[] bytes) {
        if (bytes == null) return null;
        ensureInitialized();
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            // Prepend salt for hashing
            if (saltBytes != null && saltBytes.length > 0) {
                md.update(saltBytes);
            }
            md.update(bytes);
            byte[] digest = md.digest();
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid hash algorithm configured: " + algorithm, e);
        }
    }

    // Convert byte[] to lowercase hex string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit((b & 0xF), 16));
        }
        return sb.toString();
    }

    // Lazy initialization of algorithm and salt from properties resource
    private static synchronized void ensureInitialized() {
        if (initialized) return;
        String resource = System.getProperty(RESOURCE_PROP, DEFAULT_RESOURCE);
        Properties props = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // ignore and rely on defaults below
        }

        algorithm = props.getProperty("crypto.algorithm", "SHA-256");
        String salt = props.getProperty("crypto.salt", "");
        saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        initialized = true;
    }

    // Package-private helper used by tests to reset cached config
    static void resetForTests() {
        initialized = false;
        algorithm = null;
        saltBytes = null;
    }
}
