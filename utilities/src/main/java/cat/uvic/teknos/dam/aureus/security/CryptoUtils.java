package cat.uvic.teknos.dam.aureus.security;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class for hashing, asymmetric and symmetric operations used by the server
 * and clients. Configuration is read from crypto.properties on the classpath and
 * some values (keystore password) can be overridden via environment variable
 * AUREUS_KS_PASSWORD.
 */
public class CryptoUtils {

    private static final String DEFAULT_RESOURCE = "crypto.properties";
    private static final String RESOURCE_PROP = "crypto.properties.resource";

    private static volatile String algorithm;
    private static volatile byte[] saltBytes;
    private static volatile boolean initialized = false;

    private static volatile String asymmetricTransformation;
    private static volatile String symmetricTransformation;
    private static volatile String keystoreType;
    private static volatile String keystorePassword;
    // alias -> resource mapping (optional)
    private static final Map<String, String> keystoreResources = new HashMap<>();

    private static volatile byte[] symmetricKeyBytes;
    private static volatile byte[] symmetricIvBytes;

    private CryptoUtils() {}

    public static String hash(String plainText) {
        if (plainText == null) return null;
        return hash(plainText.getBytes(StandardCharsets.UTF_8));
    }

    public static String hash(byte[] bytes) {
        if (bytes == null) return null;
        ensureInitialized();
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            if (saltBytes != null && saltBytes.length > 0) md.update(saltBytes);
            md.update(bytes);
            byte[] digest = md.digest();
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid hash algorithm configured: " + algorithm, e);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return null;
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit((b & 0xF), 16));
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String hex) {
        if (hex == null) return null;
        String s = hex.trim();
        if (s.length() % 2 != 0) throw new IllegalArgumentException("Invalid hex string length");
        int len = s.length() / 2;
        byte[] out = new byte[len];
        for (int i = 0; i < len; i++) {
            int hi = Character.digit(s.charAt(i * 2), 16);
            int lo = Character.digit(s.charAt(i * 2 + 1), 16);
            if (hi == -1 || lo == -1) throw new IllegalArgumentException("Invalid hex char");
            out[i] = (byte) ((hi << 4) + lo);
        }
        return out;
    }

    private static synchronized void ensureInitialized() {
        if (initialized) return;
        String resource = System.getProperty(RESOURCE_PROP, DEFAULT_RESOURCE);
        Properties props = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (in != null) props.load(in);
        } catch (IOException ignored) {}

        algorithm = props.getProperty("crypto.algorithm", "SHA-256");
        String salt = props.getProperty("crypto.salt", "");
        saltBytes = salt.getBytes(StandardCharsets.UTF_8);

        asymmetricTransformation = props.getProperty("crypto.asymmetric.transformation", "RSA/ECB/PKCS1Padding");
        symmetricTransformation = props.getProperty("crypto.symmetric.transformation", "AES/CBC/PKCS5Padding");
        keystoreType = props.getProperty("crypto.keystore.type", "PKCS12");

        String envKsPwd = System.getenv("AUREUS_KS_PASSWORD");
        if (envKsPwd != null && !envKsPwd.isEmpty()) keystorePassword = envKsPwd.trim();
        else keystorePassword = props.getProperty("crypto.keystore.password", "");

        // CORRECCIÓN CRÍTICA: Lógica para leer los mapeos de recursos del Keystore
        keystoreResources.clear();
        props.forEach((k, v) -> {
            String key = (String) k;
            if (key.startsWith("crypto.keystore.resource.")) {
                String alias = key.substring("crypto.keystore.resource.".length());
                keystoreResources.put(alias, (String) v);
            }
        });

        // La lógica para parsear una sola línea de mapeo de comas (crypto.keystore.resources)
        // es redundante con la corrección, pero se deja para no modificar la estructura original.
        String mapping = props.getProperty("crypto.keystore.resources", "").trim();
        if (!mapping.isEmpty()) {
            String[] entries = mapping.split(",");
            for (String e : entries) {
                String pair = e.trim();
                if (pair.isEmpty()) continue;
                int eq = pair.indexOf('=');
                if (eq > 0) {
                    String alias = pair.substring(0, eq).trim();
                    String res = pair.substring(eq + 1).trim();
                    if (!alias.isEmpty() && !res.isEmpty()) keystoreResources.put(alias, res);
                }
            }
        }

        // --- Inicialización de la clave/IV simétricos (HEX!!) para la Fase 3 ---
        String symKeyHex = props.getProperty("crypto.symmetric.key", "").trim();
        if (!symKeyHex.isEmpty()) {
            symmetricKeyBytes = hexToBytes(symKeyHex);
        } else {
            symmetricKeyBytes = null;
        }

        String symIvHex = props.getProperty("crypto.symmetric.iv", "").trim();
        if (!symIvHex.isEmpty()) {
            symmetricIvBytes = hexToBytes(symIvHex);
        } else {
            symmetricIvBytes = null;
        }

        initialized = true;
    }

    static void resetForTests() {
        initialized = false;
        algorithm = null;
        saltBytes = null;
        asymmetricTransformation = null;
        symmetricTransformation = null;
        keystoreType = null;
        keystorePassword = null;
        keystoreResources.clear();
        // Limpiar campos simétricos añadidos
        symmetricKeyBytes = null;
        symmetricIvBytes = null;
    }

    public static PublicKey loadPublicKeyFromCertificateResource(String resourceName) throws IOException, CertificateException {
        // NOTA: Esta función no se usa directamente en el Paso 8, sino que se usa getPublicKey(alias)
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) throw new IOException("Certificate resource not found: " + resourceName);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(in);
            return cert.getPublicKey();
        }
    }

    public static PrivateKey loadFirstPrivateKeyFromPKCS12(String resourceName) throws Exception {
        // NOTA: Esta función NO se usa en el Paso 8, se usa getPrivateKey(alias)
        ensureInitialized();
        // Usamos el alias para buscar la ruta del archivo, o asumimos que resourceName es la ruta si no está mapeado.
        String resolved = keystoreResources.getOrDefault(resourceName, resourceName);

        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resolved)) {
            if (in == null) throw new IOException("Keystore resource not found for alias: " + resourceName + " (looked for " + resolved + ")");
            KeyStore ks = KeyStore.getInstance(keystoreType);
            char[] pwd = keystorePassword == null ? new char[0] : keystorePassword.toCharArray();
            ks.load(in, pwd);
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (ks.isKeyEntry(alias)) {
                    Key key = ks.getKey(alias, pwd);
                    if (key instanceof PrivateKey) return (PrivateKey) key;
                }
            }
            throw new IOException("No private key entry found in keystore: " + resolved);
        }
    }

    public static byte[] generateRandomBytes(int length) {
        byte[] b = new byte[length];
        new java.security.SecureRandom().nextBytes(b);
        return b;
    }

    public static byte[] asymmetricEncrypt(byte[] data, PublicKey pub) throws Exception {
        Cipher cipher = Cipher.getInstance(asymmetricTransformation);
        cipher.init(Cipher.ENCRYPT_MODE, pub);
        return cipher.doFinal(data);
    }

    public static byte[] asymmetricDecrypt(byte[] data, PrivateKey priv) throws Exception {
        Cipher cipher = Cipher.getInstance(asymmetricTransformation);
        cipher.init(Cipher.DECRYPT_MODE, priv);
        return cipher.doFinal(data);
    }

    public static byte[] aesEncrypt(byte[] plaintext, byte[] keyBytes, byte[] ivBytes) throws Exception {
        SecretKeySpec sk = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance(symmetricTransformation);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, sk, iv);
        return cipher.doFinal(plaintext);
    }

    public static byte[] aesDecrypt(byte[] ciphertext, byte[] keyBytes, byte[] ivBytes) throws Exception {
        SecretKeySpec sk = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance(symmetricTransformation);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, sk, iv);
        return cipher.doFinal(ciphertext);
    }

    public static String decrypt(String keyHex, String ivHex, String bodyHex) throws Exception {
        byte[] key = hexToBytes(keyHex);
        byte[] iv = hexToBytes(ivHex);
        byte[] ct = hexToBytes(bodyHex);
        byte[] plain = aesDecrypt(ct, key, iv);
        return new String(plain, StandardCharsets.UTF_8);
    }

    public static String[] encryptWithKeyHex(String keyHex, String plainText) throws Exception {
        byte[] key = hexToBytes(keyHex);
        byte[] iv = generateRandomBytes(16);
        byte[] ct = aesEncrypt(plainText.getBytes(StandardCharsets.UTF_8), key, iv);
        return new String[]{bytesToHex(iv), bytesToHex(ct)};
    }

    // --- FASE 3: Cifrado simétrico estático ---
    /**
     * Cifra `plainText` usando la clave e IV estáticos configurados en crypto.properties
     * (campos: crypto.symmetric.key y crypto.symmetric.iv) y devuelve el ciphertext en HEX.
     * Requiere que `ensureInitialized()` haya cargado la clave/IV en formato HEX.
     */
    public static String crypt(String plainText) throws Exception {
        ensureInitialized();
        if (plainText == null) return null;
        if (symmetricKeyBytes == null || symmetricIvBytes == null) {
            throw new IllegalStateException("Symmetric key/IV not configured in crypto.properties (expected hex in crypto.symmetric.key and crypto.symmetric.iv)");
        }
        byte[] ct = aesEncrypt(plainText.getBytes(StandardCharsets.UTF_8), symmetricKeyBytes, symmetricIvBytes);
        return bytesToHex(ct);
    }

    /**
     * Descifra un ciphertext en HEX usando la clave e IV estáticos configurados en crypto.properties
     * y devuelve el texto plano.
     */
    public static String decrypt(String hexCipherText) throws Exception {
        ensureInitialized();
        if (hexCipherText == null) return null;
        if (symmetricKeyBytes == null || symmetricIvBytes == null) {
            throw new IllegalStateException("Symmetric key/IV not configured in crypto.properties (expected hex in crypto.symmetric.key and crypto.symmetric.iv)");
        }
        byte[] ct = hexToBytes(hexCipherText);
        byte[] plain = aesDecrypt(ct, symmetricKeyBytes, symmetricIvBytes);
        return new String(plain, StandardCharsets.UTF_8);
    }

    // --- Métodos de acceso público ---

    public static String asymmetricEncrypt(String certificateKeystoreAlias, String plainText) throws Exception {
        ensureInitialized();
        String resource = keystoreResources.get(certificateKeystoreAlias);
        if (resource == null) {
            throw new IOException("Keystore resource not found for alias: " + certificateKeystoreAlias);
        }

        PublicKey pubKey = null;
        Exception lastEx = null;
        // Try heuristics based on resource extension
        try {
            if (resource.endsWith(".p12") || resource.endsWith(".pfx") || resource.endsWith(".jks")) {
                pubKey = getPublicKey(certificateKeystoreAlias);
            } else if (resource.endsWith(".cer") || resource.endsWith(".crt") || resource.endsWith(".pem")) {
                pubKey = loadPublicKeyFromCertificateResource(resource);
            } else {
                // Unknown extension: try keystore first then certificate
                try {
                    pubKey = getPublicKey(certificateKeystoreAlias);
                } catch (Exception e) {
                    lastEx = e;
                    pubKey = loadPublicKeyFromCertificateResource(resource);
                }
            }
        } catch (Exception e) {
            lastEx = e;
            // Fallback: try to load as certificate resource
            try {
                pubKey = loadPublicKeyFromCertificateResource(resource);
            } catch (Exception e2) {
                // If both attempts fail, throw the first meaningful exception
                if (lastEx != null) throw lastEx;
                else throw e2;
            }
        }

        byte[] cipherText = asymmetricEncrypt(plainText.getBytes(StandardCharsets.UTF_8), pubKey);
        return bytesToHex(cipherText);
    }

    public static String asymmetricDecrypt(String privateKeyStoreAlias, String hexCipherText) throws Exception {
        ensureInitialized();
        PrivateKey privateKey = getPrivateKey(privateKeyStoreAlias);
        byte[] cipherTextBytes = hexToBytes(hexCipherText);
        byte[] decryptedBytes = asymmetricDecrypt(cipherTextBytes, privateKey);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    // --- Métodos de Keystore ---

    private static PublicKey getPublicKey(String alias) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        // Se llama internamente desde asymmetricEncrypt(String alias, String text)
        String ksResource = keystoreResources.get(alias);
        if (ksResource == null) {
            throw new KeyStoreException("Keystore resource not found for alias: " + alias);
        }
        KeyStore ks = loadKeystore(ksResource);
        Certificate certificate = ks.getCertificate(alias);
        if (certificate == null) {
            throw new KeyStoreException("Certificate not found in keystore '" + ksResource + "' for alias: " + alias);
        }
        return certificate.getPublicKey();
    }

    private static PrivateKey getPrivateKey(String alias) throws KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException {
        // Se llama internamente desde asymmetricDecrypt(String alias, String text)
        String ksResource = keystoreResources.get(alias);
        if (ksResource == null) {
            throw new KeyStoreException("Keystore resource not found for alias: " + alias);
        }
        KeyStore ks = loadKeystore(ksResource);
        Key key = ks.getKey(alias, keystorePassword.toCharArray());
        if (key instanceof PrivateKey) {
            return (PrivateKey) key;
        } else {
            throw new UnrecoverableKeyException("Key found under alias '" + alias + "' is not a PrivateKey.");
        }
    }

    private static KeyStore loadKeystore(String resourcePath) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks = KeyStore.getInstance(keystoreType);
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Keystore file not found in classpath: " + resourcePath);
            }
            ks.load(is, keystorePassword.toCharArray());
        }
        return ks;
    }
}
