CREATE TABLE IF NOT EXISTS ductility_test (
    id SERIAL PRIMARY KEY,
    task_id VARCHAR(255) NOT NULL,
    temperature VARCHAR(50) NOT NULL,
    displacement VARCHAR(50) NOT NULL,
    experimenter VARCHAR(100),
    test_date BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    device_id VARCHAR(100),
    device_name VARCHAR(255),
    device_manufacturer VARCHAR(255),
    device_model VARCHAR(255)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_ductility_task_id ON ductility_test(task_id);
