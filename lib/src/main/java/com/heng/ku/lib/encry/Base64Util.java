package com.heng.ku.lib.encry;


import java.io.*;

/**
 * BASE64编码解码工具包，依赖javabase64-1.3.1.jar
 *
 * @author kokjuis 189155278@qq.com
 */
public class Base64Util {

    /**
     * 文件读取缓冲区大小
     */
    private static final int CACHE_SIZE = 1024;

    /**
     * BASE64字符串解码为二进制数据
     *
     * @param base64 BASE64字符串
     * @return 二进制数据
     * @throws Exception e
     */
    static byte[] decode(String base64) throws Exception {
//        return Base64.getDecoder().decode(base64.getBytes());
        return Base64.getMimeDecoder().decode(base64.getBytes());
    }

    /**
     * 二进制数据编码为BASE64字符串
     *
     * @param bytes 二进制数据
     * @return BASE64字符串
     * @throws Exception e
     */
    static String encode(byte[] bytes) throws Exception {
        return new String(Base64.getEncoder().encode(bytes));
    }

    /**
     * 将文件编码为BASE64字符串，大文件慎用，可能会导致内存溢出
     *
     * @param filePath 文件绝对路径
     * @return BASE64字符串
     * @throws Exception e
     */
    public static String encodeFile(String filePath) throws Exception {
        byte[] bytes = fileToByte(filePath);
        return encode(bytes);
    }

    /**
     * BASE64字符串转回文件
     *
     * @param filePath 文件绝对路径
     * @param base64   编码字符串
     * @throws Exception e
     */
    public static void decodeToFile(String filePath, String base64)
            throws Exception {
        byte[] bytes = decode(base64);
        byteArrayToFile(bytes, filePath);
    }

    /**
     * 文件转换为二进制数组
     *
     * @param filePath 文件路径
     * @return 二进制数组
     * @throws Exception e
     */
    private static byte[] fileToByte(String filePath) throws Exception {
        byte[] data = new byte[0];
        File file = new File(filePath);
        if (file.exists()) {
            FileInputStream in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
            byte[] cache = new byte[CACHE_SIZE];
            int nRead;
            while ((nRead = in.read(cache)) != -1) {
                out.write(cache, 0, nRead);
                out.flush();
            }
            out.close();
            in.close();
            data = out.toByteArray();
        }
        return data;
    }

    /**
     * 二进制数据写文件
     *
     * @param bytes    二进制数据
     * @param filePath 文件生成目录
     * @throws Exception e
     */
    private static void byteArrayToFile(byte[] bytes, String filePath)
            throws Exception {
        InputStream in = new ByteArrayInputStream(bytes);
        File destFile = new File(filePath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        destFile.createNewFile();
        OutputStream out = new FileOutputStream(destFile);
        byte[] cache = new byte[CACHE_SIZE];
        int nRead;
        while ((nRead = in.read(cache)) != -1) {
            out.write(cache, 0, nRead);
            out.flush();
        }
        out.close();
        in.close();
    }

}

