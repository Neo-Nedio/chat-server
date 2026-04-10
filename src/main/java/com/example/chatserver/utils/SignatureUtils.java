package com.example.chatserver.utils;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class SignatureUtils {
    public static String calculateSignature(String method, String path, String accessKey,
                                             String timestamp, String secretKey) throws Exception {
        /*
         * 计算请求签名
         *
         * @param method     HTTP 方法（GET、POST、PUT、DELETE 等）
         * @param path       请求路径（如 /api/user/info）
         * @param accessKey  访问密钥（公钥，用于标识调用方）
         * @param timestamp  时间戳（防重放攻击）
         * @param secretKey  私钥（用于签名的密钥，双方保密）
         * @return 签名字符串（十六进制）
         * @throws Exception 签名算法异常
         */

        // 1. 拼接待签名字符串
        String stringToSign = method + path + accessKey + timestamp;

        // 2. 使用 HMAC-SHA256 算法计算签名
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        hmacSHA256.init(secretKeySpec);

        // 3. 计算哈希值
        byte[] hash = hmacSHA256.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));

        // 4. 转换为十六进制字符串
        return Hex.encodeHexString(hash);
    }
}
