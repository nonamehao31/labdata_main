package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

/**
 * 配比和压实方法信息响应
 */
public class MixratioAndCompactionResponse {
    @SerializedName("mixName")
    private String mixName;

    @SerializedName("compactionMethod")
    private String compactionMethod;

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    public String getMixName() {
        return mixName;
    }

    public void setMixName(String mixName) {
        this.mixName = mixName;
    }

    public String getCompactionMethod() {
        return compactionMethod;
    }

    public void setCompactionMethod(String compactionMethod) {
        this.compactionMethod = compactionMethod;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}