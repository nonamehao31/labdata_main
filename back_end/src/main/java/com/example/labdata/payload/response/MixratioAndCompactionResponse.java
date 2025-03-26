package com.example.labdata.payload.response;

/**
 * 配比和压实方法信息响应
 */
public class MixratioAndCompactionResponse {
    private String mixName;
    private String compactionMethod;
    private int code;
    private String message;

    public MixratioAndCompactionResponse() {
    }

    public MixratioAndCompactionResponse(String mixName, String compactionMethod) {
        this.mixName = mixName;
        this.compactionMethod = compactionMethod;
        this.code = 200;
        this.message = "success";
    }

    public MixratioAndCompactionResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

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