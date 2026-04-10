package com.example.chatserver.utils;

import com.example.chatserver.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

//安全工具类，提供密码加密验证和 RSA 解密功能
@Slf4j
public final class SecurityUtil {

    //RSA 密钥对（公钥+私钥）
    private static final KeyPair keyPair;
    //Spring Security 提供的密码编码器
    private static final BCryptPasswordEncoder passwordEncoder;
    private static final String AesKey = "chatChatChatChat";

    static {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");//获取 RSA 算法生成器
            keyGen.initialize(1024); //初始化密钥长度为 1024 位
            keyPair = keyGen.generateKeyPair(); //生成公钥和私钥
            passwordEncoder = new BCryptPasswordEncoder();
        } catch (NoSuchAlgorithmException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    //获取公钥
    public static String getPublicKey() {
        return "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) +
                "\n-----END PUBLIC KEY-----";
    }

    //RSA 解密
    public static String decryptPassword(String encryptedPassword) {
        try {
            Cipher cipher = Cipher.getInstance("RSA"); // 获取 RSA 解密器
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate()); //初始化解密模式
            //解密得到明文字节
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new BaseException("密码解析失败~");
        }
    }

    //验证密码
    public static boolean verifyPassword(String password, String passwordHash) {
        return passwordEncoder.matches(password, passwordHash);
    }

    //哈希密码
    public static String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 获取 AES 密钥规范对象
     *
     * @return SecretKeySpec 对象
     */
    private static SecretKeySpec getSecretAesKeySpec() {
        // AesKey 是一个静态字符串密钥
        // "AES" 表示算法名称
        return new SecretKeySpec(AesKey.getBytes(), "AES");
    }

    /**
     * AES 加密
     *
     * @param data 明文数据
     * @return Base64 编码的密文
     */
    public static String aesEncrypt(String data) {
        try {
            // 1. 获取 AES 加密器
            Cipher cipher = Cipher.getInstance("AES");

            // 2. 初始化为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, getSecretAesKeySpec());

            // 3. 执行加密
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // 4. 返回 Base64 编码的密文
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            log.error("AES加密失败：", e);
            throw new BaseException("生成失败~");
        }
    }

    /**
     * AES 解密
     *
     * @param encryptedData Base64 编码的密文
     * @return 明文数据
     */
    public static String aesDecrypt(String encryptedData) {
        try {
            // 1. 获取 AES 解密器
            Cipher cipher = Cipher.getInstance("AES");

            // 2. 初始化解密模式（使用同一个密钥）
            cipher.init(Cipher.DECRYPT_MODE, getSecretAesKeySpec());

            // 3. Base64 解码
            byte[] decoded = Base64.getDecoder().decode(encryptedData);

            // 4. 执行解密
            byte[] decrypted = cipher.doFinal(decoded);

            // 5. 返回明文字符串
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("AES解密失败：", e);
            throw new BaseException("解析失败~");
        }
    }
}