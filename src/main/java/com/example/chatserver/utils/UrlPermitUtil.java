package com.example.chatserver.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//URL 免验证工具类，用于判断某个请求路径是否需要放行
@Component
public class UrlPermitUtil {
    // 免验证Url
    private final List<String> urls = new ArrayList<>();

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
}
