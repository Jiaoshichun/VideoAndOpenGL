package com.heng.ku.lib.encry;

import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class LicenseEncrypt {
    private final static String PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIgDUU+V2t0TsZZzZbUEQbMB4ih2xISWyXsVnVUz6VPS3bXdZDEtcl+C1xUeyt4S3WJjTZa/7ZoFoIMGIhkALQdnS8i6ECQ8dvu4n2qoKvBj5WDvs4lv+fD6NcIqb3UKQ7D67QYos/GYZ6IrcGxSIaWvh3u1C1tW+64jPgR8i6djAgMBAAECgYAiypZ2ASMnAe6fSY1urFQR8h1jtF24fMm1DmZccRbyNjI4XxVN7w4emF1kLag+/hxbGoOyWA3zJBVW7q6yLnVV23lE2ViShGPpaSjmdAGHvsDQL+tQPlHx1D13QnS29YwiT6N+vMtvBd6Ebr5ckR686gmj6aaTWfo/BjUcUaFmgQJBAM+MXj3AXbde+KAlZkcj/rhghGqrpVafK++reGpWtK4UjBvC5ZYNq/qKk9iFKVPC1N3lh7xnjCQUmzTVrci2Q1MCQQCnw88cY6Eu7eXxSXY5cgbcj3HrDCoTVcIyrsMvZAukJ3MfdSmMM92iDgJ0WZavhcF5ojqIg5pqGPa0MXKDvBmxAkAQWcOW48++QXoey7N6CUjo55mm+azF3TZN5FlRL1F+oVnEWh9SKfORgldRaHQwCBpS/OEro5CzyvfLOsY9GLbHAkEAhQ2mECTwK/Efwkr6Cbtmt7zIo5tFL4p/d4TlZouZM/rZsiRQ7FIiQCmORsra2KBjft1sQOLnL6hW2TiyUsp3MQJBALAqxUhtD8O/6G5/y+mIcEerG/z2jNNXkVsc+U3cKB5gMYWwrJ41sZcXKOyttsvA5flW8lCH2b1Ldcs66jbjAYI=";


    private final static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDPmmAxuuy1U9FjEQ+2MWG4fZGEs/0Jvu3LNSX+3OThBb5bwwxVSxnurzntVSFBWxYOesKTavCRlIQppvhy1Qor3xZevgdP4GPIMo+ck7bGGPdcqdVH2sMCvb1eiWo7RxLA41KZtVvZlGBddptBozvv8I0WV/dL9z8DaVO9CBtwIwIDAQAB";
//    private final static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDlGIu2hM8b60BpR++w5KHP2hf/ozebq3hSENJLZKo6/oyj7s8tHZ4MYssYXoOnoC9Qu4KvFNyipUZK7Umb4hQGdO134xpJlpExP1vpEmABAJSw0jeVw/IYNnUHuJi+ZXExoCSaFyPW0oiteRynONYUjthD112iAUAmHF/z/gzJEwIDAQAB";


    public static void main(String[] args) {
        String packageName = "com.msxf.guotou.sdk.demo";
//        String packageName = "com.msxf.RecordAVSDKForGuotou";
        String type = "0";

//        String startTime = System.currentTimeMillis() + "";
//        String endTime = System.currentTimeMillis() + 1000 * 24 * 60 * 60 * 1000L + "";
        try {
//            String encrypt = encrypt(PRIVATE_KEY, packageName, type, startTime, endTime, "11111", "msxf");
//            String encrypt =
//                    "############################################################\n" +
//                            "SenseTime license\n" +
//                            "License Product: senseid_liveness\n" +
//                            "Expiration: 20191210-20200224\n" +
//                            "License SN: 45d44a36-a887-4081-a96e-616f73c20e9c\n" +
//                            "############################################################\n" +
//                            "Vb+nc4deHDSHp2j6SpcEEKlu/lruf+X36dpWiYRKDlm3VtU8KRv69oV627RG8bc4lkjlTwEhyPEfVEaxWeXVwSUygnq+6VkcqWcwAI0jJkRxB7B74/gnEj6kfVlc1a4pTKHY9wkUs/ORDr5ftlVJOAn89S8pBygfPLgKfb/EGAg7mf6eLZWXsZ2oCE/eBpRAAKZLkolXmFtgw+a2RqEETyo0eTh3Wtj7v0/GjKlgJtGe5dW62f96nGZGtr3C7YLbDJ3MiGgu8Zw0P/9OgyyK7wcW7KxGUq2Y8P2ourLKLOyMIJxRvXOnVVZH5pubxsSGBdEhuIQZvqyIH6GtQqPvfzGg49XpAHxiUWlESqeZi+5OXEQMpglK/zRJdYDv22sK+4gsypVlfXdqywu2cQ2U8rS7ajs3HXqk5obwjDNlatSN9KIpHGFCYV9+skO40lv2lap2YfYUx2JIZsNuFGxo2YrspXf7O3sVqNKZhVsZUWCmAD5WGxgXODj24wIIyJHjKtTF6SwuT+RRRDjS+NGwQUNmDbYzSQkJHroUXlal6FpXA2ihUr9Un0lDEzmlOIOfEnGypHpfI7PksxGFjARpw9zFTbZa7MiQ3S94UYp5TheWtYM8fx00ZLYyqARxvt4ugAapBtxc7QED846O7SIkjTDA4w9OiiYaAt3sK1yl4XJpyCDiKDVDlbl6tnn97TiPZR+4VemjGcaW5K8tYJ3ZJQll16RXgCTHhtmWV0tKw0OSWpZn98Vy3HjjYBjETsK+0p1dKCklR/SizYWX8y3TrbOvfIm+HFOV2n3XWEPZNhzGj8e9cBc9n2LS1cr2AtsUOGAT5QdTRo5hy9E3+2QYxz9+4rXx+MIbDuv946hUj2BFlm3mOflG2+RAZudSDicVK+4NXw38h1QqEDO/ohbWokvyfVN7PA6rE3wH1H07NktSfuqSKa6bqk/FZjaL7Qomi7Xq0rIRwPcpQu+TyKk1tUnKeZiUxQxb/siiJ0oCwiS+QuF0Eo8ula/Lq44UVHVh\n" +
//                            "############################################################\n" +
//                            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCIA1FPldrdE7GWc2W1BEGzAeIodsSElsl7FZ1VM+lT0t213WQxLXJfgtcVHsreEt1iY02Wv+2aBaCDBiIZAC0HZ0vIuhAkPHb7uJ9qqCrwY+Vg77OJb/nw+jXCKm91CkOw+u0GKLPxmGeiK3BsUiGlr4d7tQtbVvuuIz4EfIunYwIDAQAB\n";
//            System.out.println("加密后的:" + encrypt);

//            String[] strings = encrypt.split("############################################################");
//            String s = strings[strings.length - 2].trim();
//            System.out.println("实际的加密串为:" + s);
//            encrypt="Rm7nPg3jsVFvwSezlq4pa9gvapuAzgsQMzMQEM6JyZle3tGMj30LJXY/jKShCEMYpKSL5tJdbmMOUrJmlCPEIgArW5Rg/h3sdjJ6wg16zTTUeQ7EVQFc4g4A3rv7bC/YMRPppT8qBeDU+ETPnl65M8Fvmx7W1E8k0DzkTbvSkR4RRyf7rDfxx0a9XMkSK3+bYHGBXqnvKOmMyoYlDAg+xSfbCN7b811zPlLwLa3c6dQvL1VitBofyOYP28COQrD4E4voWQLpENJy+5Xp8W3E+9gM13Ihu2OPG6mnJIqu0LXTaqVZBgKanFU5H7BOniIpjjU7sDZtEFULqPtjU4wvtAkYwokK4hX4QGFZ+DKoC9rILvodtGOxKm5miE/rOKPfReH8+mATIbf4SV46cOM05IVz2AhAWFL58V4ax4EqKW2KboaB+fA17J1SEvCX9CSyMFcKCFVZ35DAb5+98p5NAHF78EwAsQxt6vp1RrEpqqfTyOW0vWCXdya6H/KPyq0Kr4rDeN3H7DafFOdkTSttdmpsX/oWyZ45OYb0dn2f7CeeAbdh3KkKnXdH1R8EbF/o5Da1xf4Q02mELY4pjObCcs2mCwt4TPMFZ6PODgAwh/8Yl/9L6IfnBgiw9fL4eoiShkulHD6l+24UVrcbg+VGo/I8c8VbP1vFOg4iG4XnkWBkR1923yk9qyTm/SxxtwEyIgmfeJUBSynyFmgBmVUUY046aRPs52AjZC8o2eVSSZsVlRZcQL+dmOm3WI2tieZVDshs49ZB1Q2KPGp++Ck1tcJ1APrvOH17QxwIAWBpo/osuFyrk/74t+MSO5TJZVoV+OPevzKHiNjbWCUXdPomdGx8hUPQM9ca+LNgPqoVPfM9hA82foYIb1Qyxi5zpLvutCBf+8OKNs6qaaTy0p01CSgJ1ptZlexGv4XmJWp6RV+lQDobh2m3MZR3f8QhWUJPFdneVWDh1B52moDyyzDby0QqnXt5OItYYYyzceIoMarIIMK0FQojvrSK0u4GS68f";
           String encrypt="tuT2x1Gd1gW6B5siBX9SSUXLw0f2izBnR2wM5AObhQh146GHIEN7EuVK686yi58s46l34tTa+5kXO4CiXTZ4DaRoQ53oT9QxYf9Fju/7M6630Abtax3xGAhlog2FvTA1GJRX4AlGmrB8AKvHqmYDNcexhzn8yM/Lgl+mbVVWgwUiCXENyUXyC9IQeiiSOwHKtEXdVEFypvZpcNm9OH82mR3LSbJpePLZx/1VqCf/3V5viKkBRlWXx1Le6OAH4KFhvYWlZ/R/eajRWMgGPLWkEjMQBtXRep6tG22rZvfEwb5UVj1t0s8DEcv+4uw6nX2gdVyQGlrHBsEvZOhZg6n1d6NOnJlOdziHLz8gJtvdV1dmpinv5zAqiQXhTdiMbH7JqN0mQDdJODm3AkoL5zXoouR/WrPgLcVrPA0ovKJaXrE+7Xvn0EVqC5UfVqZlHt4kT4EVLn/UohHHRtfBNf0LSZgXu8zrC5KFob0P1XYOPm/Y1P86GvOzYNlSmOx0/2D2AK5zZlLAsXQZFtJqM7lCNx91OGsDp5BAArIYCd72aFU396FxWObeUeQdE04KBusULQUEP4nPDNTEN5E4NF5tyITHZO34uMFqnN/fJWD/Usy0xKvzOY0cUkxrblRPjCPM/eJGLTBjLxbyF8NsX3+byRZRKdFKgtMrRQXqjzkCs3E=";
            String decrypt = decrypt(PUBLIC_KEY, encrypt, packageName, type);
            System.out.println("解密后的:" + decrypt);

            boolean verify = verify(decrypt, packageName, type);
            System.out.println("是否验证通过:" + verify);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static boolean verify(String decryptStr, String packageName, String type) {

        //解析字符串
        String[] split = decryptStr.split("&");
        if (split.length != 7) return false;

        String decryptName = split[0];
        String decryptType = split[1];
        long startTime = Long.valueOf(split[2]);
        long endTime = Long.valueOf(split[3]);
        String licenseSn = split[4];
        String agency = split[5];
        String nameLength = split[6];

        System.out.println("包名:" + decryptName + " 类型:" +
                decryptType + " 开始时间:" + startTime + " 结束时间:" + endTime +
                "  授权序列号:" + licenseSn + "  签发单位:" + agency + "  包名长度:" + nameLength);

        if (!decryptName.equals(encryptPackageName(packageName))) {
            System.out.println("包名不对");
            return false;
        }
        if (Integer.valueOf(nameLength) != encryptNameLength(packageName.length())) {
            System.out.println("包名长度不对");
            return false;
        }
        if (!decryptType.equals(type)) {
            System.out.println("类型不对");
            return false;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis > startTime && currentTimeMillis < endTime) {
            return true;
        }
        System.out.println("已过有效期");
        return false;
    }

    /**
     * 生成RSA秘钥对
     *
     * @param keyLength
     * @return
     */
    public static KeyPair generateRSAKeyPair(int keyLength) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(keyLength);
            return kpg.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解密算法
     *
     * @param publicKey   公钥
     * @param encryptStr  加密的字符串
     * @param packageName 包名
     * @param type        类型
     * @return 签名验证通过的原始字符串
     */
    public static String decrypt(String publicKey,
                                 String encryptStr,
                                 String packageName,
                                 String type) {
        //获取AES KEY
        String aesKey = createAesKey(packageName, type);

        try {
            //使用RSA公钥进行解密
            String aesEncrypt = RsaEncryptUtil.decryptByPublicKey(encryptStr, publicKey);
            System.out.println("Rsa解密后:" + aesEncrypt);

            //使用Aes进行解密
            String str = AesUtils.decrypt(aesKey, aesEncrypt);
            System.out.println("Aes解密后:" + str);

            //获取原始字符串
            String originStr = new String(AesUtils.toByte(str.substring(0, str.length() - 64)));
            System.out.println("解析出来的原始字符串:" + originStr);

            //根据字符串获取签名
            String localSignature = signatureStr(originStr);
            System.out.println("本地算出来的签名:" + localSignature);

            //获取远端签名
            String signatureStr = new String(AesUtils.toByte(str.substring(str.length() - 64)));
            System.out.println("解析出来的签名:" + signatureStr);

            if (localSignature.equals(signatureStr)) {
                return originStr;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 加密算法
     *
     * @param privateKey  私钥
     * @param packageName 应用包名
     * @param type        应用类型 0 android 1 IOS
     * @param startTime   授权开始时间
     * @param endTime     授权结束时间
     * @param licenseSn   授权序列号
     * @param agency      签发单位
     * @return
     */
    public static String encrypt(String privateKey,
                                 String packageName,
                                 String type,
                                 String startTime,
                                 String endTime,
                                 String licenseSn,
                                 String agency) {
        //获取AES KEY
        String aesKey = createAesKey(packageName, type);

        System.out.println("加密 aesKey:" + aesKey);

        //拼接原始信息
        String str = encryptPackageName(packageName) + "&" +
                type + "&" +
                startTime + "&" +
                endTime + "&" +
                licenseSn + "&" +
                agency + "&" +
                encryptNameLength(packageName.length());
        System.out.println("加密 原始信息:" + str);
        try {
            //将原始信息串转为16进制
            String originStr = AesUtils.toHex(str.getBytes("utf-8"));
            System.out.println("加密 原始信息16进制:" + originStr);

            //获取签名字符串 并转为16进制
            String signatureStr = AesUtils.toHex(signatureStr(str).getBytes("utf-8"));
            System.out.println("加密 签名字信息16进制:" + signatureStr);

            //使用Aes进行加密
            String encryptAes = AesUtils.encrypt(aesKey, originStr + signatureStr);
            System.out.println("加密 Aes加密后:" + encryptAes);

            //使用RSA私钥加密
            return RsaEncryptUtil.encryptByPrivateKey(encryptAes, privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 生成AES key
     *
     * @param packageName 包名
     * @param type        类型
     */
    private static String createAesKey(String packageName,
                                       String type) {
        StringBuilder str = new StringBuilder();
        char[] temp = {'m', 's', 'x', 'f'};
        for (int i = 0; i < packageName.length(); i++) {
            str.append(temp[i % 4]).append(packageName.charAt(i));
        }
        str.append(type);
        return Md5Utils.md5(Md5Utils.md5(str.toString())).substring(8, 24).toUpperCase();
    }

    /**
     * 对字符串做签名
     *
     * @param originStr
     * @return
     */
    private static String signatureStr(String originStr) {
        StringBuilder str = new StringBuilder();
        char[] temp = {'m', 's', 'x', 'f'};
        for (int i = originStr.length() - 1; i > -1; i--) {
            str.append(temp[i % 4]).append(originStr.charAt(i));
        }
        System.out.println("signatureStr的字符串：" + str);
        return Md5Utils.md5(str.toString());
    }

    /**
     * 包名长度处理
     *
     * @param length
     * @return
     */
    private static int encryptNameLength(int length) {
        // 3*x立方 + 8* x的平方 +7x+3
        return (int) (3 * Math.pow(length, 3) + 8 * Math.pow(length, 2) + 7 * length + 3);
    }

    /**
     * 加密包名
     *
     * @param packageName
     * @return
     */
    private static String encryptPackageName(String packageName) {
        try {
            return Md5Utils.md5(AesUtils.toHex(("msxf" + packageName + "2020").getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
