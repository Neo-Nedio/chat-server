package com.example.chatserver.utils;

import com.example.chatserver.exception.BaseException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;

//安全工具类，提供密码加密验证和 RSA 解密功能
public final class SecurityUtil {

    //RSA 密钥对（公钥+私钥）
    private static final KeyPair keyPair;
    //Spring Security 提供的密码编码器
    private static final BCryptPasswordEncoder passwordEncoder;

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
}