package cn.iocoder.yudao.module.digital.util;

import com.antherd.smcrypto.sm4.Sm4;

/**
 * @program: intellect-callcenter
 * @description: 加密解密类
 * @author: huangyx
 * @create: 2024-03-11 15:39
 **/

public class EncryptionAndDecryptionUtils {

    /**
     * 密钥
     */
    private final static String KEY = "4373a1c1bd451099380abb42c51b2ad4";

    /**
     * 加密
     *
     * @param pliantext
     * @return
     */
    public static String encrypt(String pliantext)
    {
        return encryptSM4(pliantext, KEY);
    }

    /**
     * 加密
     *
     * @param pliantext
     * @param key
     * @return
     */
    public static String encrypt(String pliantext, String key)
    {
        return encryptSM4(pliantext, key);
    }

    /**
     * 解密
     *
     * @param ciphertext
     * @return
     */
    public static String decrypt(String ciphertext)
    {
        return decryptSM4(ciphertext, KEY);
    }

    /**
     * 解密
     *
     * @param ciphertext
     * @param key
     * @return
     */
    public static String decrypt(String ciphertext, String key)
    {
        return decryptSM4(ciphertext, key);
    }

    /**
     * 加密
     *
     * @param data
     * @param key
     * @return
     */
    public static String encryptSM4(String data, String key)
    {
        return Sm4.encrypt(data, key);
    }

    /**
     * 解密
     *
     * @param data
     * @param key
     * @return
     */
    public static String decryptSM4(String data, String key)
    {
        return Sm4.decrypt(data, key);
    }

    /**
     * 测试入口
     *
     * @param args
     */
    public static void main(String[] args)
    {
        String str = "123123123";
        String encryptString = null;
        try {
            encryptString = EncryptUtil.encrypt(str);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(encryptString);

        try {
            System.err.println(EncryptUtil.decrypt(encryptString));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
