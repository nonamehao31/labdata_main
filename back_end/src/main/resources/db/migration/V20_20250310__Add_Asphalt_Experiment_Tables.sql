-- 添加实验任务类别字段
ALTER TABLE experiment_tasks ADD COLUMN IF NOT EXISTS category VARCHAR(50);

-- 创建沥青信息表
CREATE TABLE IF NOT EXISTS asphalt_info (
    id BIGSERIAL PRIMARY KEY,
    grade VARCHAR(100) NOT NULL,
    type VARCHAR(100) NOT NULL,
    supplier VARCHAR(200) NOT NULL,
    expiry_date DATE,
    experiment_task_id BIGINT,
    client_id BIGINT,
    synced BOOLEAN DEFAULT FALSE,
    sync_status VARCHAR(50) DEFAULT 'NEW',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_asphalt_experiment_task FOREIGN KEY (experiment_task_id) REFERENCES experiment_tasks(id) ON DELETE CASCADE
);

-- 创建沥青实验分配表
CREATE TABLE IF NOT EXISTS asphalt_experiment_assignments (
    id BIGSERIAL PRIMARY KEY,
    asphalt_info_id BIGINT NOT NULL,
    experiment_type_id BIGINT NOT NULL,
    experiment_task_id BIGINT NOT NULL,
    client_id BIGINT,
    synced BOOLEAN DEFAULT FALSE,
    sync_status VARCHAR(50) DEFAULT 'NEW',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_assignment_asphalt_info FOREIGN KEY (asphalt_info_id) REFERENCES asphalt_info(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_experiment_type FOREIGN KEY (experiment_type_id) REFERENCES experiment_types(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_experiment_task FOREIGN KEY (experiment_task_id) REFERENCES experiment_tasks(id) ON DELETE CASCADE
);

-- 添加索引提升查询性能
CREATE INDEX IF NOT EXISTS idx_asphalt_info_experiment_task_id ON asphalt_info(experiment_task_id);
CREATE INDEX IF NOT EXISTS idx_asphalt_info_client_id ON asphalt_info(client_id);
CREATE INDEX IF NOT EXISTS idx_asphalt_experiment_assignments_asphalt_info_id ON asphalt_experiment_assignments(asphalt_info_id);
CREATE INDEX IF NOT EXISTS idx_asphalt_experiment_assignments_experiment_type_id ON asphalt_experiment_assignments(experiment_type_id);
CREATE INDEX IF NOT EXISTS idx_asphalt_experiment_assignments_experiment_task_id ON asphalt_experiment_assignments(experiment_task_id);
CREATE INDEX IF NOT EXISTS idx_asphalt_experiment_assignments_client_id ON asphalt_experiment_assignments(client_id);
