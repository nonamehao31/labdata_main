package com.example.labdata.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private Boolean success;
    private String message;
    private T data;
    
    public ApiResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }
}
