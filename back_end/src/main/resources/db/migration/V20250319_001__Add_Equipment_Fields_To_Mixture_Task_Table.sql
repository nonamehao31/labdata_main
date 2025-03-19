-- 为mixture_task表添加设备字段
ALTER TABLE mixture_task
ADD COLUMN assigned_mixing_equipment VARCHAR(100) NULL COMMENT '指派的拌合设备',
ADD COLUMN assigned_forming_equipment VARCHAR(100) NULL COMMENT '指派的制件设备',
ADD COLUMN assigned_testing_equipment VARCHAR(100) NULL COMMENT '指派的测试设备';
