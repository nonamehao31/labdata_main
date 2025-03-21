-- 创建马歇尔试验数据表
CREATE TABLE IF NOT EXISTS marshall_test (
    id SERIAL PRIMARY KEY,
    task_id VARCHAR(255) NOT NULL,
    stability_1 FLOAT,
    stream_value1 FLOAT,
    stability_2 FLOAT,
    stream_value2 FLOAT,
    stability_3 FLOAT,
    stream_value3 FLOAT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 添加索引以提高查询效率
CREATE INDEX idx_marshall_test_task_id ON marshall_test(task_id);

-- 添加注释
COMMENT ON TABLE marshall_test IS '马歇尔试验数据表';
COMMENT ON COLUMN marshall_test.id IS '主键ID';
COMMENT ON COLUMN marshall_test.task_id IS '关联的混合料任务ID';
COMMENT ON COLUMN marshall_test.stability_1 IS '第一次马歇尔稳定度(KN)';
COMMENT ON COLUMN marshall_test.stream_value1 IS '第一次流值(mm)';
COMMENT ON COLUMN marshall_test.stability_2 IS '第二次马歇尔稳定度(KN)';
COMMENT ON COLUMN marshall_test.stream_value2 IS '第二次流值(mm)';
COMMENT ON COLUMN marshall_test.stability_3 IS '第三次马歇尔稳定度(KN)';
COMMENT ON COLUMN marshall_test.stream_value3 IS '第三次流值(mm)';
COMMENT ON COLUMN marshall_test.created_at IS '创建时间';
COMMENT ON COLUMN marshall_test.updated_at IS '更新时间';
