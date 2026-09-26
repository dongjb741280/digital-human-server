package cn.iocoder.yudao.module.digital.util;

import org.json.JSONException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = ALGORITHM + "/CBC/PKCS5Padding";

    // 自定义密钥（必须是16、24或32字节，对应AES-128、AES-192、AES-256）
    private static final byte[] customKey = "3paEzMaFMjvVbZLY".getBytes();  // 示例密钥，需要确保长度正确
    private static final byte[] iv = "TouRUSSgrl1gh1Tp".getBytes();  // 自定义初始化向量，通常为16字节


    private static final String ADD = "/add/";

    // 加密函数
    public static String encrypt(String plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKeySpec keySpec = new SecretKeySpec(customKey, ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));
        String data = Base64.getEncoder().encodeToString(encryptedBytes);
        return data.replace("\\+",ADD);
    }

    // 解密函数
    public static String decrypt(String ciphertextB64) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKeySpec keySpec = new SecretKeySpec(customKey, ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] encryptedBytes = Base64.getDecoder().decode(ciphertextB64);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, "UTF-8");
    }

    public static void main(String[] args) throws Exception {
//        System.out.println(encrypt("maoyr"));
        System.out.println("name："+decrypt("nPPbPTy+RGTNS7zCp71WEg=="));
        System.out.println("password："+decrypt("08nFSmG8Sai9JD8u/RFr7g=="));
    }

}
