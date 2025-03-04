package com.example.labdata.controller;

import com.example.labdata.payload.WebSocketMessage;
import com.example.labdata.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 处理客户端加入连接
     */
    @MessageMapping("/connect")
    @SendTo("/topic/public")
    public WebSocketMessage addUser(@Payload WebSocketMessage message, 
                                    SimpMessageHeaderAccessor headerAccessor,
                                    Principal principal) {
        
        // 获取用户名，可以是实际用户名或生成的会话ID
        String username = principal != null ? principal.getName() : "匿名用户";
        
        // 将用户名添加到WebSocket会话
        headerAccessor.getSessionAttributes().put("username", username);
        
        logger.info("用户已连接: {}", username);
        
        // 构建连接响应
        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("message", "已连接到WebSocket服务");
        response.put("timestamp", Instant.now());
        
        return new WebSocketMessage("CONNECT", response);
    }
    
    /**
     * 客户端可以发送ping来保持连接活跃
     */
    @MessageMapping("/ping")
    public void handlePing(Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            
            // 发送私人pong响应
            messagingTemplate.convertAndSendToUser(
                username, 
                "/queue/pong", 
                new WebSocketMessage("PONG", Instant.now())
            );
            
            logger.debug("收到来自{}的ping，已响应pong", username);
        }
    }
    
    /**
     * 客户端可以发送自定义消息
     */
    @MessageMapping("/message")
    @SendTo("/topic/public")
    public WebSocketMessage handleMessage(@Payload WebSocketMessage message) {
        logger.info("收到消息: {}", message);
        return message;
    }
}
