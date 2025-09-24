package Authenticator;

import org.jasypt.util.text.AES256TextEncryptor;

public class Encryptor {

    private static final String KEY = "encryptor001";

    // Static encryptor instance, initialized only once
    private static final AES256TextEncryptor encryptor;

    static {
        encryptor = new AES256TextEncryptor();
        encryptor.setPassword(KEY); // only once here
    }

    // Static methods to use the shared encryptor
    public static String encrypt(String text) {
        return encryptor.encrypt(text);
    }

    public static String decrypt(String text) {
        return encryptor.decrypt(text);
    }
}
