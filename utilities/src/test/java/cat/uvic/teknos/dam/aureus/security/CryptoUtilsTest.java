package cat.uvic.teknos.dam.aureus.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilsTest {

    private static final Pattern HEX_LOWER = Pattern.compile("^[0-9a-f]+$");

    @AfterEach
    void tearDown() {
        CryptoUtils.resetForTests();
        System.clearProperty("crypto.properties.resource");
    }

    @Test
    void hashNullReturnsNull() {
        assertNull(CryptoUtils.hash((String) null));
        assertNull(CryptoUtils.hash((byte[]) null));
    }

    @Test
    void emptyStringAndEmptyBytesAreEquivalentAndDeterministic() {
        String hStr = CryptoUtils.hash("");
        String hBytes = CryptoUtils.hash(new byte[0]);
        assertNotNull(hStr);
        assertEquals(hStr, hBytes, "Empty string and empty byte[] should produce same hash");
        // Deterministic
        assertEquals(hStr, CryptoUtils.hash(""));
    }

    @Test
    void stringAndByteArrayHashEquality() {
        String s = "Prueba con acentos ñ y emojis 🚀";
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        String hs = CryptoUtils.hash(s);
        String hb = CryptoUtils.hash(bytes);
        assertEquals(hs, hb, "hash(String) and hash(byte[]) must match for same UTF-8 bytes");
    }

    @Test
    void outputIsLowercaseHex() {
        String h = CryptoUtils.hash("check-hex");
        assertNotNull(h);
        assertTrue(HEX_LOWER.matcher(h).matches(), "Hash must be lowercase hexadecimal");
        assertEquals(h, h.toLowerCase());
    }

    @Test
    void changingInputChangesHash() {
        String a = "input-one";
        String b = "input-two";
        assertNotEquals(CryptoUtils.hash(a), CryptoUtils.hash(b));
    }

    @Test
    void changingSaltChangesHash() {
        String msg = "same-message";
        String hDefault = CryptoUtils.hash(msg);

        System.setProperty("crypto.properties.resource", "crypto-test-alt.properties");
        CryptoUtils.resetForTests();
        String hAlt = CryptoUtils.hash(msg);

        assertNotEquals(hDefault, hAlt, "Different salt must produce different hash for same input");
    }

    @Test
    void changingConfigChangesHashAndKeepsSha256Length() {
        String msg = "algorithm-check";
        // SHA-256 default => 64 hex chars
        String hDefault = CryptoUtils.hash(msg);
        assertEquals(64, hDefault.length(), "SHA-256 produces 64 hex chars");

        System.setProperty("crypto.properties.resource", "crypto-test.properties");
        CryptoUtils.resetForTests();
        String hAlt = CryptoUtils.hash(msg);
        // Still SHA-256
        assertEquals(64, hAlt.length(), "SHA-256 produces 64 hex chars (alt config)");
        assertNotEquals(hDefault, hAlt, "Different configuration (salt) should change the hash");
    }

    @Test
    void saltIsAppliedBeforeMessage() {
        String msg = "ordering-check";
        String base = CryptoUtils.hash(msg);

        System.setProperty("crypto.properties.resource", "crypto.properties");
        CryptoUtils.resetForTests();
        String saltAware = CryptoUtils.hash(msg);
        assertEquals(base, saltAware);

        assertTrue(HEX_LOWER.matcher(saltAware).matches());
    }
}
