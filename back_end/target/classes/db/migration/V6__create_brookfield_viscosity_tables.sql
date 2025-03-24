-- 创建布鲁克菲尔德旋转黏度实验主表
CREATE TABLE IF NOT EXISTS brookfield_viscosity_test (
    id SERIAL PRIMARY KEY,
    task_id VARCHAR(255) NOT NULL, -- 使用varchar类型避免超长数值问题
    experimenter VARCHAR(100), -- 实验人员
    test_date BIGINT, -- 测试日期（时间戳）
    device_id VARCHAR(100), -- 设备ID
    device_name VARCHAR(100), -- 设备名称
    device_manufacturer VARCHAR(100), -- 设备制造商
    device_model VARCHAR(100), -- 设备型号
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

-- 创建温度点表
CREATE TABLE IF NOT EXISTS brookfield_viscosity_temperature_point (
    id SERIAL PRIMARY KEY,
    test_id INTEGER NOT NULL,
    point_id VARCHAR(50) NOT NULL, -- 存储前端生成的点ID
    temperature VARCHAR(20) NOT NULL, -- 温度值，使用VARCHAR以便支持多种格式
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_temperature_test FOREIGN KEY (test_id) REFERENCES brookfield_viscosity_test(id) ON DELETE CASCADE
);

-- 创建粘度测量值表
CREATE TABLE IF NOT EXISTS brookfield_viscosity_measurement (
    id SERIAL PRIMARY KEY,
    temperature_point_id INTEGER NOT NULL,
    measurement_id VARCHAR(50) NOT NULL, -- 存储前端生成的测量值ID
    spindle_type VARCHAR(100), -- 转子型号
    rotation_speed VARCHAR(20), -- 转速
    viscosity VARCHAR(20) NOT NULL, -- 粘度值
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_measurement_temperature FOREIGN KEY (temperature_point_id) REFERENCES brookfield_viscosity_temperature_point(id) ON DELETE CASCADE
);

-- 创建索引以提高查询效率
CREATE INDEX IF NOT EXISTS idx_brookfield_task_id ON brookfield_viscosity_test(task_id);
CREATE INDEX IF NOT EXISTS idx_temperature_point_test_id ON brookfield_viscosity_temperature_point(test_id);
CREATE INDEX IF NOT EXISTS idx_viscosity_measurement_temperature_id ON brookfield_viscosity_measurement(temperature_point_id);
