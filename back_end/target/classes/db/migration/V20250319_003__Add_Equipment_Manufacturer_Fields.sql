-- 为mixture_task表添加设备厂家字段
ALTER TABLE mixture_task 
ADD COLUMN IF NOT EXISTS mixing_equipment_manufacturer VARCHAR(100),
ADD COLUMN IF NOT EXISTS forming_equipment_manufacturer VARCHAR(100),
ADD COLUMN IF NOT EXISTS testing_equipment_manufacturer VARCHAR(100);
