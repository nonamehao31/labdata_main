-- 创建制件方法表
CREATE TABLE IF NOT EXISTS compaction_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization VARCHAR(255) NOT NULL,
    method_name VARCHAR(255) NOT NULL,
    mixing_temperature FLOAT,
    mixing_speed FLOAT,
    mixing_time FLOAT,
    compaction_method VARCHAR(255) NOT NULL,
    specimen_type INT,
    is_default BOOLEAN DEFAULT FALSE,
    created_by BIGINT,
    creation_time BIGINT,
    
    -- 添加索引以提高查询性能
    INDEX idx_organization (organization),
    INDEX idx_created_by (created_by)
);

-- 插入一些默认的制件方法数据作为示例
INSERT INTO compaction_methods (organization, method_name, mixing_temperature, mixing_speed, mixing_time, 
                               compaction_method, specimen_type, is_default, created_by, creation_time)
VALUES 
('公路建设部门', '标准震动压实法', 150.0, 450.0, 5.0, '震动压实', 1, TRUE, 1, UNIX_TIMESTAMP() * 1000),
('公路建设部门', '马歇尔击实法', 160.0, 500.0, 5.5, '马歇尔击实', 2, FALSE, 1, UNIX_TIMESTAMP() * 1000),
('桥梁工程单位', '旋转压实法', 155.0, 480.0, 6.0, '旋转压实', 2, TRUE, 2, UNIX_TIMESTAMP() * 1000),
('桥梁工程单位', '静压成型法', 145.0, 400.0, 4.5, '静压成型', 1, FALSE, 2, UNIX_TIMESTAMP() * 1000),
('城市道路管理处', '超声波振动法', 140.0, 420.0, 5.0, '超声波振动', 3, TRUE, 3, UNIX_TIMESTAMP() * 1000);

-- 注意: 这些示例数据在实际应用中应根据真实单位情况进行调整
