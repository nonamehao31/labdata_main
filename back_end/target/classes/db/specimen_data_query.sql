-- 根据任务ID前缀查询specimen数据的SQL脚本
-- 用法:
-- 1. 在PostgreSQL中调用：SELECT * FROM get_specimen_data('任务ID前缀');
-- 2. 创建函数:

CREATE OR REPLACE FUNCTION get_specimen_data(task_id_prefix VARCHAR)
RETURNS TABLE (
    specimen_id BIGINT,
    mixing_temperature FLOAT,
    mixing_speed FLOAT,
    mixing_time INTEGER,
    compaction_method VARCHAR,
    device_id VARCHAR,
    manufacturer VARCHAR,
    device_type VARCHAR
) AS $$
BEGIN
    -- 返回所有与任务相关的试件信息和设备信息
    RETURN QUERY
    SELECT 
        s.id AS specimen_id,
        s.mixing_temperature,
        s.mixing_speed,
        s.mixing_time,
        s.compaction_method,
        CASE 
            WHEN mt.assigned_mixing_equipment IS NOT NULL THEN mt.assigned_mixing_equipment
            ELSE mt.assigned_forming_equipment
        END AS device_id,
        CASE 
            WHEN mt.assigned_mixing_equipment IS NOT NULL THEN mt.mixing_equipment_manufacturer
            ELSE mt.forming_equipment_manufacturer
        END AS manufacturer,
        CASE 
            WHEN mt.assigned_mixing_equipment IS NOT NULL THEN 'mixing'
            ELSE 'forming'
        END AS device_type
    FROM 
        mixture_task mt
    JOIN 
        specimens s ON mt.specimen_id = s.id
    WHERE 
        mt.task_id LIKE CONCAT(task_id_prefix, '%')
    ORDER BY 
        s.id, device_type;
END;
$$ LANGUAGE plpgsql;
