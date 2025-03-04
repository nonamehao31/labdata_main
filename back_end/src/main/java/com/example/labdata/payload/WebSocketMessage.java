package com.example.labdata.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 通用WebSocket消息格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    private String type;
    private Object payload;
    private Instant timestamp = Instant.now();
    
    public WebSocketMessage(String type, Object payload) {
        this.type = type;
        this.payload = payload;
    }
}
