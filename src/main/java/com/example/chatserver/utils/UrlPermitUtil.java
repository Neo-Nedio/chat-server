package com.example.chatserver.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//URL 免验证工具类，用于判断某个请求路径是否需要放行
@Component
public class UrlPermitUtil {
    // 免验证Url
    private final List<String> urls = new ArrayList<>();

    // 需要验证角色的url资源
    private Map<String, List<String>> roleUrl = new HashMap<>();

    {
        urls.add("/ws/**");
    }

    public boolean verifyUrl(String permitUrl, List<String> urlArr) {
        for (String url : urlArr) {
            for (int index = 0; index < url.length(); index++) {
                if (url.charAt(index) == '*') {
                    return true;
                }
                if (permitUrl.length() == index + 1 && url.length() == index + 1) {
                    return true;
                }
                if (index == permitUrl.length() || permitUrl.charAt(index) != url.charAt(index)) {
                    break;
                }
            }
        }
        return false;
    }

    // 判断是否需要免验证
    public boolean isPermitUrl(String url) {
        return verifyUrl(url, urls);
    }


    public List<String> getPermitAllUrl() {
        return urls;
    }

    //动态添加
    public void addUrls(List<String> urls) {
        this.urls.addAll(urls);
    }

/*
    roleUrl (Map<String, List<String>>)
    │
            ├── "/api/user/delete" ──→ ["ADMIN"]
            │
            ├── "/api/group/remove" ──→ ["ADMIN", "GROUP_OWNER"]
            │
            ├── "/api/chat/send" ──→ ["USER", "VIP"]
            │
            └── "/api/public/key" ──→ null (不存在)*/
    public void addRoleUrl(String role, String url) {
        // 1. 根据 URL 作为 key，获取当前已有的角色列表
        List<String> roles = roleUrl.get(url);

        // 2. 如果列表不存在（第一次添加该 URL）
        if (roles == null) {
            // 创建一个新的空列表
            roles = new ArrayList<>();
            // 将 URL 和 新列表 放入 Map
            roleUrl.put(url, roles);
        }

        // 3. 将角色添加到列表中
        roles.add(role);
    }

    public boolean isRoleUrl(String role, String url) {
        List<String> roles = roleUrl.get(url);
        if (roles == null) return true;
        for (String r : roles) {
            if (r.equals(role)) {
                return true;
            }
        }
        return false;
    }
}
