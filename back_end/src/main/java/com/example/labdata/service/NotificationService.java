package com.example.labdata.service;

import com.example.labdata.model.audit.UserDateAudit;
import com.example.labdata.payload.EntityChangeMessage;
import com.example.labdata.payload.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 提供WebSocket实时通知功能的服务
 */
@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 发送实体变更通知
     * @param changeType 变更类型 (CREATE, UPDATE, DELETE)
     * @param entityType 实体类型 (例如 "material", "specimen")
     * @param entity 变更的实体对象
     */
    public void notifyEntityChange(String changeType, String entityType, Object entity) {
        try {
            Long entityId = null;
            Long clientId = null;
            Long userId = null;
            
            // 尝试从实体中提取ID和clientId
            if (entity instanceof UserDateAudit) {
                UserDateAudit audit = (UserDateAudit) entity;
                
                // 使用反射获取ID，因为不同实体的ID字段可能不同
                try {
                    entityId = (Long) entity.getClass().getMethod("getId").invoke(entity);
                    clientId = (Long) entity.getClass().getMethod("getClientId").invoke(entity);
                    
                    // 将String类型的createdBy转换为Long类型
                    String createdBy = audit.getCreatedBy();
                    if (createdBy != null && !createdBy.isEmpty()) {
                        try {
                            userId = Long.parseLong(createdBy);
                        } catch (NumberFormatException e) {
                            logger.warn("无法将createdBy转换为Long: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    logger.warn("无法从实体获取ID或clientId: " + e.getMessage());
                }
            }
            
            EntityChangeMessage message = new EntityChangeMessage(
                    changeType, 
                    entityType, 
                    entityId,
                    clientId,
                    userId
            );
            
            // 创建通用WebSocket消息
            WebSocketMessage wsMessage = new WebSocketMessage(
                    "ENTITY_CHANGE",
                    message
            );
            
            // 广播消息到特定主题
            // 主题格式: /topic/entity/{entityType}/{changeType}
            String destination = String.format("/topic/entity/%s/%s", entityType.toLowerCase(), changeType.toLowerCase());
            messagingTemplate.convertAndSend(destination, wsMessage);
            
            // 同时发送到全局实体变更主题
            messagingTemplate.convertAndSend("/topic/entity/all", wsMessage);
            
            logger.info("已发送实体变更通知: {} {} ID={}", changeType, entityType, entityId);
        } catch (Exception e) {
            logger.error("发送通知时出错: " + e.getMessage(), e);
        }
    }
    
    /**
     * 发送创建实体的通知
     */
    public void notifyEntityCreated(String entityType, Object entity) {
        notifyEntityChange("CREATE", entityType, entity);
    }
    
    /**
     * 发送更新实体的通知
     */
    public void notifyEntityUpdated(String entityType, Object entity) {
        notifyEntityChange("UPDATE", entityType, entity);
    }
    
    /**
     * 发送删除实体的通知
     */
    public void notifyEntityDeleted(String entityType, Object entity) {
        notifyEntityChange("DELETE", entityType, entity);
    }
    
    /**
     * 发送自定义消息
     */
    public void sendCustomMessage(String topic, String type, Object payload) {
        WebSocketMessage message = new WebSocketMessage(type, payload);
        messagingTemplate.convertAndSend(topic, message);
        logger.info("已发送自定义消息到 {}: {}", topic, type);
    }
}
