package com.heng.ku.lib.encry;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;

/**
 * aes加密混淆工具类
 *
 * @author baoxing.wen
 */
public class AesUtils {
    private static final String AES = "AES";
    private final static String HEX = "0123456789ABCDEF";
    private static final String CRYPT_KEY = "y2W89L6BkRAFljhN";
    private static final String IV_STRING = "dMitHORyqbeYVE0o";
    private static final int STR_LENGTH = 16;

    /**
     * 加密
     *
     * @param key     密钥
     * @param content 明文
     * @return 密文
     */
    public static String encrypt(String key, String content) {
        byte[] encryptedBytes = new byte[0];
        try {
            byte[] byteContent = content.getBytes("UTF-8");
            // 注意，为了能与 iOS 统一
            // 这里的 key 不可以使用 KeyGenerator、SecureRandom、SecretKey 生成
            byte[] enCodeFormat = key.getBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, AES);
            byte[] initParam = IV_STRING.getBytes();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
            // 指定加密的算法、工作模式和填充方式
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            encryptedBytes = cipher.doFinal(byteContent);
            // 同样对加密后数据进行 base64 编码
        } catch (Exception e) {
            e.printStackTrace();
        }

        return toHex(encryptedBytes);
    }

    /**
     * 二进制转字符,转成了16进制
     *
     * @param buf 二进制数组
     * @return 16进制字符串
     */
    public static String toHex(byte[] buf) {
        if (buf == null) {
            return "";
        }
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (byte aBuf : buf) {
            appendHex(result, aBuf);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }

    /**
     * 解密
     *
     * @param content 密文
     * @return 明文
     */
    public static String decrypt(String key, String content) {
        try {
            byte[] encryptedBytes = toByte(content);
            byte[] enCodeFormat = key.getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(enCodeFormat, AES);
            byte[] initParam = IV_STRING.getBytes();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            byte[] result = cipher.doFinal(encryptedBytes);

            return new String(result, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 把16进制转化为字节数组
     *
     * @param hexString 16进制字符串
     * @return 二进制数组
     */
    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        }
        return result;
    }

    /**
     * 对文件进行AES加密
     *
     * @param sourceFile 文件
     * @param pathName   A pathname string
     * @param key        密钥
     * @return 加密后文件
     */
    public static File encryptFile(File sourceFile, String pathName, String key) {
        // 新建临时加密文件
        File encrypfile = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(sourceFile);
            encrypfile = new File(pathName);
            outputStream = new FileOutputStream(encrypfile);
            byte[] enCodeFormat = toMakekey(key, STR_LENGTH, IV_STRING).getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(enCodeFormat, AES);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            // 以加密流写入文件
            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
            byte[] cache = new byte[1024];
            int nRead;
            while ((nRead = cipherInputStream.read(cache)) != -1) {
                outputStream.write(cache, 0, nRead);
                outputStream.flush();
            }
            cipherInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
        return encrypfile;
    }

    /**
     * AES方式解密文件
     *
     * @param sourceFile  要被解密的文件
     * @param decryptFile 解密完后的文件
     * @param key         解密秘钥
     * @return 解密后的文件
     */
    public static File decryptFile(File sourceFile, File decryptFile, String key) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            byte[] enCodeFormat = toMakekey(key, STR_LENGTH, IV_STRING).getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(enCodeFormat, AES);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            inputStream = new FileInputStream(sourceFile);
            outputStream = new FileOutputStream(decryptFile);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = inputStream.read(buffer)) >= 0) {
                cipherOutputStream.write(buffer, 0, r);
            }
            cipherOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();  // To change body of catch statement use File |
            // Settings | File Templates.
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
        return decryptFile;
    }

    /**
     * 密钥key ,默认补的数字，补全16位数，以保证安全补全至少16位长度,android和ios对接通过
     *
     * @param str       密钥key
     * @param strLength 补全16位数
     * @param val       默认补的数字
     * @return 补全后的字符串
     */
    private static String toMakekey(String str, int strLength, String val) {

        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(str).append(val);
                str = buffer.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    public static void main(String[] args) {
/*        System.out.println(AesUtils.encrypt(CRYPT_KEY,"要加密的字符串"));
        System.out.println(AesUtils.decrypt(CRYPT_KEY,"171DF7BAE29CB97CC58A44F8A1BC364E7D507F428180EC79E50EC0DC8D3B1F6F"));*/
//        String a = AesUtils.encryptFile(new File("e:/idcard.jpg"), "e:/", "a.jpg", "y2W89L6BkRAFljhN").getPath();
//        File file = AesUtils.decryptFile(new File("e:/a.jpg"), new File("e:/aaa.jpg"), "y2W89L6BkRAFljhN");
    }
}