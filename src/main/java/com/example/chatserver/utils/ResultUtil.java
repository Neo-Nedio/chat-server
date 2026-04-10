package com.example.chatserver.utils;


import cn.hutool.json.JSONObject;
import lombok.Getter;

public class ResultUtil {

    @Getter
    public enum ResponseEnum {
        SUCCEED(0),
        FAIL(1),
        TOKEN_INVALID(-1), //token失效
        FORBIDDEN(-2); //没有权限


        //类型后的数字
        private final int type;

        ResponseEnum(int type) {
            this.type = type;
        }

    }

    public static String CODE = "code";
    public static String MSG = "msg";
    public static String DATA = "data";

    /**
     * 根据条件返回
     */
    public static JSONObject ResultByFlag(boolean flag) {
        if (flag) {
            return Succeed();
        } else {
            return Fail();
        }
    }

    /**
     * 根据条件返回
     */
    public static JSONObject ResultByFlag(boolean flag, String msg, Object data) {
        if (flag) {
            return Succeed();
        } else {
            return Fail(msg);
        }
    }


    /**
     * 成功没有返回数据
     */
    public static JSONObject Succeed() {
        JSONObject result = new JSONObject();
        result.set(CODE, ResponseEnum.SUCCEED.getType());
        result.set(MSG, "操作成功");
        return result;
    }

    /**
     * 成功有返回数据
     */
    public static JSONObject Succeed(Object data) {
        JSONObject result = new JSONObject();
        result.set(CODE, ResponseEnum.SUCCEED.getType());
        result.set(DATA, data);
        return result;
    }

    /**
     * 成功有返回消息和返回数据和提示
     */
    public static JSONObject Succeed(String msg, Object data) {
        JSONObject result = new JSONObject();
        result.set(CODE, ResponseEnum.SUCCEED.getType());
        result.set(MSG, msg);
        result.set(DATA, data);
        return result;
    }

    /**
     * 失败没有返回数据
     */
    public static JSONObject Fail() {
        JSONObject result = new JSONObject();
        result.set(CODE, ResponseEnum.FAIL.getType());
        result.set(MSG, "操作失败");
        return result;
    }

    /**
     * 失败有返回数据
     */
    public static JSONObject Fail(String msg) {
        JSONObject result = new JSONObject();
        result.set(CODE, ResponseEnum.FAIL.getType());
        result.set(MSG, msg);
        return result;
    }

    /**
     * 自定义的返回
     */
    public static JSONObject Result(int code, String msg, Object data) {
        JSONObject result = Result(code, msg);
        result.set(DATA, data);
        return result;
    }

    /**
     * 自定义的返回
     */
    public static JSONObject Result(int code, String msg) {
        JSONObject result = new JSONObject();
        result.set(CODE, code);
        result.set(MSG, msg);
        return result;
    }

    /**
     * 失败有返回消息和返回数据
     */
    public static JSONObject Fail(String msg, Object data) {
        JSONObject result = Fail(msg);
        result.set(DATA, data);
        return result;
    }

    /**
     * 失败有返回消息和返回数据
     */
    public static JSONObject TokenInvalid() {
        JSONObject result = new JSONObject();
        result.set(CODE, ResponseEnum.TOKEN_INVALID.getType());
        result.set(MSG, "认证失效,请重新登录~");
        return result;
    }

    /**
     * 失败有返回消息和返回数据
     */
    public static JSONObject Forbidden() {
        JSONObject result = new JSONObject();
        result.set(CODE, ResponseEnum.FORBIDDEN.getType());
        result.set(MSG, "该用户没有权限~");
        return result;
    }
}
