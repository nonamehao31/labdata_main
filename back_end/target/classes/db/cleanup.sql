-- 创建备份表（如果需要恢复数据，可以从这里恢复）
CREATE SCHEMA IF NOT EXISTS backup;

-- 备份要删除的表到backup schema
DO $$
DECLARE
    table_exists BOOLEAN;
BEGIN
    -- 备份实验数据表
    SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'experiment_data') INTO table_exists;
    IF table_exists THEN
        EXECUTE 'CREATE TABLE IF NOT EXISTS backup.experiment_data AS SELECT * FROM experiment_data';
    END IF;

    -- 备份实验任务表
    SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'experiment_tasks') INTO table_exists;
    IF table_exists THEN
        EXECUTE 'CREATE TABLE IF NOT EXISTS backup.experiment_tasks AS SELECT * FROM experiment_tasks';
    END IF;

    -- 备份实验类型表
    SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'experiment_types') INTO table_exists;
    IF table_exists THEN
        EXECUTE 'CREATE TABLE IF NOT EXISTS backup.experiment_types AS SELECT * FROM experiment_types';
    END IF;

    -- 备份材料表
    SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'materials') INTO table_exists;
    IF table_exists THEN
        EXECUTE 'CREATE TABLE IF NOT EXISTS backup.materials AS SELECT * FROM materials';
    END IF;

    -- 备份材料属性表
    SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'material_properties') INTO table_exists;
    IF table_exists THEN
        EXECUTE 'CREATE TABLE IF NOT EXISTS backup.material_properties AS SELECT * FROM material_properties';
    END IF;

    -- 备份配合比表
    SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'mix_ratios') INTO table_exists;
    IF table_exists THEN
        EXECUTE 'CREATE TABLE IF NOT EXISTS backup.mix_ratios AS SELECT * FROM mix_ratios';
    END IF;

    -- 备份拌合方法表
    SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'mixing_methods') INTO table_exists;
    IF table_exists THEN
        EXECUTE 'CREATE TABLE IF NOT EXISTS backup.mixing_methods AS SELECT * FROM mixing_methods';
    END IF;

    -- 备份项目表
    SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'projects') INTO table_exists;
    IF table_exists THEN
        EXECUTE 'CREATE TABLE IF NOT EXISTS backup.projects AS SELECT * FROM projects';
    END IF;

    -- 备份试件表
    SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'specimens') INTO table_exists;
    IF table_exists THEN
        EXECUTE 'CREATE TABLE IF NOT EXISTS backup.specimens AS SELECT * FROM specimens';
    END IF;
END $$;

-- 删除外键约束
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT tc.table_name, tc.constraint_name
              FROM information_schema.table_constraints tc
              JOIN information_schema.constraint_column_usage ccu 
              ON tc.constraint_name = ccu.constraint_name
              WHERE tc.constraint_type = 'FOREIGN KEY'
              AND (tc.table_name != 'users' AND tc.table_name != 'supported_devices' AND tc.table_name != 'devices')
              AND (ccu.table_name != 'users' AND ccu.table_name != 'supported_devices' AND ccu.table_name != 'devices'))
    LOOP
        EXECUTE 'ALTER TABLE ' || r.table_name || ' DROP CONSTRAINT ' || r.constraint_name;
    END LOOP;
END $$;

-- 删除不需要的表
DROP TABLE IF EXISTS experiment_data CASCADE;
DROP TABLE IF EXISTS experiment_tasks CASCADE;
DROP TABLE IF EXISTS experiment_types CASCADE;
DROP TABLE IF EXISTS materials CASCADE;
DROP TABLE IF EXISTS material_properties CASCADE;
DROP TABLE IF EXISTS mix_ratios CASCADE;
DROP TABLE IF EXISTS mixing_methods CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS specimens CASCADE;

-- 保留的表：users, supported_devices, devices
