package com.example.chatserver.constant;

public class CallType {
    public static final String Audio = "audio";
    public static final String Video = "video";

    public static boolean isValid(String callType) {
        return Audio.equals(callType) || Video.equals(callType);
    }
}
