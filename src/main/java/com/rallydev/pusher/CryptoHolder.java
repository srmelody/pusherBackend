package com.rallydev.pusher;

import com.google.common.io.BaseEncoding;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;
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
        byte[] bytes = "SecretPassphrase".getBytes();
        this.secretKey = new SecretKeySpec(bytes, "AES");//keyGen.generateKey();


        byte[] ivBytes = "SecretPassphrase".getBytes();
//        new Random().nextBytes(ivBytes);
        ivSpec = new IvParameterSpec(ivBytes);

    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();

    }
    }
    public String getKey() {
        return BaseEncoding.base64().encode(secretKey.getEncoded());
    }
    public String decrypt(String cipherText) {
        try {
            byte[] input = BaseEncoding.base64().decode(cipherText);
            Cipher cipher = null;

            cipher = getInstance("AES/CBC/PKCS5Padding", "SunJCE");

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] decrypted = cipher.doFinal(input);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
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

    public String getIV() {
        return BaseEncoding.base64().encode(ivSpec.getIV());
    }
}
