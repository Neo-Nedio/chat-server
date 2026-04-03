package com.example.chatserver.exception;


import cn.hutool.json.JSONUtil;
import com.example.chatserver.utils.ResultUtil;
import lombok.Getter;

import java.util.HashMap;

public class BaseException extends RuntimeException {

    @Getter
    private final int code;
    private final String message;
    private HashMap<String, Object> param;

    public BaseException(String message) {
        this.code = ResultUtil.ResponseEnum.FAIL.getType();
        this.message = message;
    }

    /***
     * 添加异常信息 键值对
     */
    public BaseException param(String key, Object value) {
        if (null == this.param) {
            this.param = new HashMap<>();
        }
        param.put(key, value);
        return this;
    }

    /***
     * 置空param
     */
    public BaseException empty() {
        this.param = new HashMap<>();
        return this;
    }

    public BaseException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String paramToString() {
        if (null == this.param || this.param.size() <= 0)
            return null;
        return JSONUtil.toJsonStr(this.param);
    }
}
