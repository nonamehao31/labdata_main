-- 添加压力级别字段
ALTER TABLE mixture_uniaxial_compression_uts028_data ADD COLUMN pressure_level VARCHAR(10);

-- 更新表结构，添加所需字段并重命名现有字段
-- 注意: 如果字段已存在则会出错，执行时请根据实际情况调整

-- 重命名现有字段 (可选)
-- ALTER TABLE mixture_uniaxial_compression_uts028_data RENAME COLUMN max_force TO max_force_kn;
-- ALTER TABLE mixture_uniaxial_compression_uts028_data RENAME COLUMN min_force TO min_force_n;

-- 添加新字段 (可选)
-- ALTER TABLE mixture_uniaxial_compression_uts028_data ADD COLUMN stress_dev_kpa FLOAT;
-- ALTER TABLE mixture_uniaxial_compression_uts028_data ADD COLUMN displ_resil_mm FLOAT;
-- ALTER TABLE mixture_uniaxial_compression_uts028_data ADD COLUMN strain_resil FLOAT;
-- ALTER TABLE mixture_uniaxial_compression_uts028_data ADD COLUMN resilient_modulus_mpa FLOAT;
