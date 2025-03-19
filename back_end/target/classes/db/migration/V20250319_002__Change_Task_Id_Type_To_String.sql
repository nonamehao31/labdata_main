-- 创建新表结构，使用VARCHAR类型的task_id和project_id
CREATE TABLE IF NOT EXISTS mixture_task (
    task_id VARCHAR(255) PRIMARY KEY,
    task_name VARCHAR(255) NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    project_id VARCHAR(255),
    prepare_status VARCHAR(50) DEFAULT 'unfinished',
    making_status VARCHAR(50) DEFAULT 'unfinished',
    testing_status VARCHAR(50) DEFAULT 'unfinished',
    assigned_mixing_equipment VARCHAR(100),
    assigned_forming_equipment VARCHAR(100),
    assigned_testing_equipment VARCHAR(100)
);

-- 转移旧表数据到新表（如果有必要）
-- 注意：这里我们将整数ID转换为字符串
INSERT INTO mixture_task (
    task_id, task_name, task_type, project_id, prepare_status, making_status, testing_status
)
SELECT 
    CAST(task_id AS VARCHAR), task_name, task_type, 
    CAST(project_id AS VARCHAR),
    'unfinished' as prepare_status, 'unfinished' as making_status, 'unfinished' as testing_status
FROM support_mixture_task
ON CONFLICT (task_id) DO NOTHING;
