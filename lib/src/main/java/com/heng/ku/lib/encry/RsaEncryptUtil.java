package com.heng.ku.lib.encry;



import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


/**
 * RSA非对称加密解密工具类
 *
 * @author kokjuis 189155278@qq.com
 */
public class RsaEncryptUtil {

    /**
     * 加密算法RSA RSA/NONE/NoPadding,RSA/NONE/PKCS1Padding
     */
    private static final String KEY_ALGORITHM = "RSA";

    /**
     * String to hold name of the encryption padding. RSA/NONE/NoPadding
     */
    private static final String PADDING = "RSA/NONE/PKCS1Padding";

    /**
     * String to hold name of the security provider.
     */
    private static final String PROVIDER = "BC";

    /**
     * 签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * 获取公钥的key
     */
    private static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * 获取私钥的key
     */
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    static {
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 公钥加密
     *
     * @param str 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String encryptByPublicKey(String str, String pubKey) throws Exception {

        Cipher cipher = Cipher.getInstance(PADDING, PROVIDER);
        // 获得公钥
        Key publicKey = getPublicKey(pubKey);

        // 用公钥加密
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        // 读数据源
        byte[] data = str.getBytes("UTF-8");

        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();

        //TODO
        return Base64Util.encode(encryptedData);
//        return AesUtils.toHex(encryptedData);
    }

    /**
     * 私钥加密
     *
     * @param str 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String encryptByPrivateKey(String str, String pubKey) throws Exception {

        Cipher cipher = Cipher.getInstance(PADDING, PROVIDER);
        // 获得私钥
        Key privateKey = getPrivateKey(pubKey);

        // 用私钥加密
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        // 读数据源
        byte[] data = str.getBytes("UTF-8");

        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();

        //TODO
        return Base64Util.encode(encryptedData);
//        return AesUtils.toHex(encryptedData);
    }

    /**
     * 公钥解密
     *
     * @param str 密文
     * @return 明文
     */
    public static String decryptByPublicKey(String str, String pubKey) throws Exception {
        Cipher cipher = Cipher.getInstance(PADDING, PROVIDER);

        // 获得公钥
        Key publicKey = getPublicKey(pubKey);

        // 用公钥解密
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        //TODO  读数据源
        byte[] encryptedData = Base64Util.decode(str);
//        byte[] encryptedData = AesUtils.toByte(str);

        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher
                        .doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher
                        .doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();

        return new String(decryptedData, "UTF-8");
    }

    /**
     * 读取公钥
     *
     * @return key
     */
    private static Key getPublicKey(String publicKey) throws Exception {

        byte[] keyBytes;
        //TODO
        keyBytes = Base64Util.decode(publicKey);
//        keyBytes = AesUtils.toByte(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 读取私钥
     *
     * @return key
     */
    private static Key getPrivateKey(String privateKey) throws Exception {

        byte[] keyBytes;
        //TODO
        keyBytes = Base64Util.decode(privateKey);
//        keyBytes = AesUtils.toByte(publicKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);

    }


}
