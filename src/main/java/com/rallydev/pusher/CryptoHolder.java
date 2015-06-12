package com.rallydev.pusher;

import com.google.common.io.BaseEncoding;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static javax.crypto.Cipher.getInstance;

/**
 * Created by smelody on 6/12/15.
 */
public class CryptoHolder {
    private SecretKey secretKey = null;
    private IvParameterSpec ivSpec = null;

    public CryptoHolder() {
    try {

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // for example
        this.secretKey = keyGen.generateKey();


        byte[] ivBytes = new byte[16];
        new Random().nextBytes(ivBytes);
        ivSpec = new IvParameterSpec(ivBytes);

    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();

    }
    }
    public String encrypt(String message) {
        try {

            byte[] input = message.getBytes("UTF-8");
            Cipher cipher = getInstance("AES/CBC/PKCS5Padding", "SunJCE");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] encrypted = cipher.doFinal(input);
            return BaseEncoding.base64().encode(encrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
