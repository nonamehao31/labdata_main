-- 创建软化点实验数据表
CREATE TABLE IF NOT EXISTS asphalt_softening_point_test (
    id SERIAL PRIMARY KEY,
    task_id VARCHAR(255) NOT NULL, -- 使用varchar而非numeric，避免大数值问题
    temperature VARCHAR(20) NOT NULL, -- 初始温度
    softening_temperature VARCHAR(20) NOT NULL, -- 软化温度
    experimenter VARCHAR(100), -- 实验人员
    test_date BIGINT, -- 测试日期（时间戳）
    device_id VARCHAR(100), -- 设备ID
    device_name VARCHAR(100), -- 设备名称
    device_manufacturer VARCHAR(100), -- 设备制造商
    device_model VARCHAR(100), -- 设备型号
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

-- 创建索引以提高查询效率
CREATE INDEX IF NOT EXISTS idx_softening_point_task_id ON asphalt_softening_point_test(task_id);
