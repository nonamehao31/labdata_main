package com.example.labdata.model;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 针入度试验实体类
 */
@Entity
@Table(name = "asphalt_penetration_test")
@Data
public class PenetrationTest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "penetration_test_id_seq")
    @SequenceGenerator(name = "penetration_test_id_seq", sequenceName = "penetration_test_id_seq", allocationSize = 1)
    private Long id;
    
    /**
     * 任务ID - 使用字符串类型存储，避免大数值问题
     */
    @Column(name = "task_id", nullable = false, length = 255)
    private String taskId;
    
    /**
     * 温度值
     */
    @Column(name = "temperature", nullable = false)
    private String temperature;
    
    /**
     * 读数值
     */
    @Column(name = "reading", nullable = false)
    private String reading;
    
    /**
     * 试验操作人
     */
    @Column(name = "experimenter")
    private String experimenter;
    
    /**
     * 试验日期 - 使用Long类型存储毫秒时间戳
     */
    @Column(name = "test_date")
    private Long testDate;
    
    /**
     * 设备ID
     */
    @Column(name = "device_id")
    private String deviceId;
    
    /**
     * 设备名称
     */
    @Column(name = "device_name")
    private String deviceName;
    
    /**
     * 设备制造商
     */
    @Column(name = "device_manufacturer")
    private String deviceManufacturer;
    
    /**
     * 设备型号
     */
    @Column(name = "device_model")
    private String deviceModel;
    
    /**
     * 创建时间 - 使用Timestamp类型
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;
    
    /**
     * 更新时间 - 使用Timestamp类型
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
