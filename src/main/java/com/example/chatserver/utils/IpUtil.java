package com.example.chatserver.utils;


import jakarta.servlet.http.HttpServletRequest;

/**
 * IP 地址工具类
 * 用于获取 HTTP 请求的客户端真实 IP
 */
public class IpUtil {

    private static final String LOCAL_IP = "127.0.0.1";

    /**
     * 获取客户端真实 IP 地址
     *
     * @param request HTTP 请求对象
     * @return 客户端 IP 地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        // 1. 空值判断
        if (request == null) {
            return "unknown";
        }

        // 2. 从各种请求头中获取 IP（按优先级）

        //最常用（Nginx、Apache）
        String ip = request.getHeader("x-forwarded-for");
        //Apache 代理
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        //标准代理头（大写）
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        //WebLogic
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        //Nginx 常用
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        // 3. 降级：直接获取连接 IP
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 4. IPv6 本地地址转 IPv4
        return "0:0:0:0:0:0:0:1".equals(ip) ? LOCAL_IP : ip;
    }
}
