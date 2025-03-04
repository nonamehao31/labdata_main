package com.example.labdata.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体变更消息，当实体被创建、更新或删除时发送
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityChangeMessage {
    // 变更类型：CREATE, UPDATE, DELETE
    private String changeType;
    // 实体类型：Material, MixRatio, Specimen等
    private String entityType;
    // 实体ID
    private Long entityId;
    // 客户端ID，用于离线同步
    private Long clientId;
    // 用户ID，指示谁做了此更改
    private Long userId;
    
    // 简化构造函数
    public EntityChangeMessage(String changeType, String entityType, Long entityId) {
        this.changeType = changeType;
        this.entityType = entityType;
        this.entityId = entityId;
    }
}
