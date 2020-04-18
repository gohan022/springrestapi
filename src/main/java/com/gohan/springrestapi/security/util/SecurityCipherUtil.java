package com.gohan.springrestapi.security.util;

import com.gohan.springrestapi.security.AuthenticationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class SecurityCipherUtil {

    private final static Logger logger = LoggerFactory.getLogger(SecurityCipherUtil.class);
    private static final String KEYVALUE = "mySecretKey";
    private static SecretKeySpec secretKey;
    private static byte[] key;

    private SecurityCipherUtil() {
        throw new AssertionError("Static!");
    }

    public static void setKey() {
        MessageDigest sha;
        try {
            key = KEYVALUE.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            //e.printStackTrace();
            logger.warn("Algorithm generate error!");
        }
    }

    public static String encrypt(String strToEncrypt) {
        if (strToEncrypt == null) return null;

        try {
            setKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            //e.printStackTrace();
            logger.warn("Cannot encode the given string.");
        }
        return null;
    }


    public static String decrypt(String strToDecrypt) {
        if (strToDecrypt == null) return null;

        try {
            setKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
           // e.printStackTrace();
            logger.warn("Cannot decrypt the given token.");
        }
        return null;
    }
}
