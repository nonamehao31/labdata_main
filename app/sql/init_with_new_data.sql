/*
 Navicat Premium Dump SQL

 Source Server         : pgsql
 Source Server Type    : PostgreSQL
 Source Server Version : 170004 (170004)
 Source Host           : 25.tcp.cpolar.top:11703
 Source Catalog        : labdata
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 170004 (170004)
 File Encoding         : 65001

 Date: 04/04/2025 14:11:08
*/


-- ----------------------------
-- Sequence structure for asphalt_material_asphalt_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."asphalt_material_asphalt_id_seq";
CREATE SEQUENCE "public"."asphalt_material_asphalt_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for asphalt_mixture_bending_test_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."asphalt_mixture_bending_test_id_seq";
CREATE SEQUENCE "public"."asphalt_mixture_bending_test_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for asphalt_softening_point_test_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."asphalt_softening_point_test_id_seq";
CREATE SEQUENCE "public"."asphalt_softening_point_test_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for asphalt_task_asphalt_experiment_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."asphalt_task_asphalt_experiment_id_seq";
CREATE SEQUENCE "public"."asphalt_task_asphalt_experiment_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for asphalt_task_asphalt_task_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."asphalt_task_asphalt_task_id_seq";
CREATE SEQUENCE "public"."asphalt_task_asphalt_task_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for bbr_test_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."bbr_test_id_seq";
CREATE SEQUENCE "public"."bbr_test_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for brookfield_viscosity_measurement_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."brookfield_viscosity_measurement_id_seq";
CREATE SEQUENCE "public"."brookfield_viscosity_measurement_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for brookfield_viscosity_temperature_point_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."brookfield_viscosity_temperature_point_id_seq";
CREATE SEQUENCE "public"."brookfield_viscosity_temperature_point_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for brookfield_viscosity_test_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."brookfield_viscosity_test_id_seq";
CREATE SEQUENCE "public"."brookfield_viscosity_test_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for compaction_methods_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."compaction_methods_id_seq";
CREATE SEQUENCE "public"."compaction_methods_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for direct_stretching_fatigue_data_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."direct_stretching_fatigue_data_id_seq";
CREATE SEQUENCE "public"."direct_stretching_fatigue_data_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for direct_stretching_fatigue_specimens_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."direct_stretching_fatigue_specimens_id_seq";
CREATE SEQUENCE "public"."direct_stretching_fatigue_specimens_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for direct_stretching_fatigue_test_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."direct_stretching_fatigue_test_id_seq";
CREATE SEQUENCE "public"."direct_stretching_fatigue_test_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for direct_stretching_modulus_data_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."direct_stretching_modulus_data_id_seq";
CREATE SEQUENCE "public"."direct_stretching_modulus_data_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for dsr_measurement_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."dsr_measurement_id_seq";
CREATE SEQUENCE "public"."dsr_measurement_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for dsr_temperature_point_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."dsr_temperature_point_id_seq";
CREATE SEQUENCE "public"."dsr_temperature_point_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for ductility_test_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."ductility_test_id_seq";
CREATE SEQUENCE "public"."ductility_test_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for dynamic_modulus_measurement_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."dynamic_modulus_measurement_id_seq";
CREATE SEQUENCE "public"."dynamic_modulus_measurement_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for dynamic_modulus_specimen_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."dynamic_modulus_specimen_id_seq";
CREATE SEQUENCE "public"."dynamic_modulus_specimen_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for dynamic_modulus_temperature_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."dynamic_modulus_temperature_id_seq";
CREATE SEQUENCE "public"."dynamic_modulus_temperature_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for dynamic_modulus_test_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."dynamic_modulus_test_id_seq";
CREATE SEQUENCE "public"."dynamic_modulus_test_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for dynamic_shear_rheometer_test_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."dynamic_shear_rheometer_test_id_seq";
CREATE SEQUENCE "public"."dynamic_shear_rheometer_test_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for hamburg_rutting_test_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."hamburg_rutting_test_id_seq";
CREATE SEQUENCE "public"."hamburg_rutting_test_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for marshall_test_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."marshall_test_id_seq";
CREATE SEQUENCE "public"."marshall_test_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mixratio_asphalt_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mixratio_asphalt_id_seq";
CREATE SEQUENCE "public"."mixratio_asphalt_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mixratio_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mixratio_id_seq";
CREATE SEQUENCE "public"."mixratio_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mixratio_sand_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mixratio_sand_id_seq";
CREATE SEQUENCE "public"."mixratio_sand_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mixratio_stone_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mixratio_stone_id_seq";
CREATE SEQUENCE "public"."mixratio_stone_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mixture_four_point_bending_result_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mixture_four_point_bending_result_id_seq";
CREATE SEQUENCE "public"."mixture_four_point_bending_result_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mixture_four_point_bending_specimen_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mixture_four_point_bending_specimen_id_seq";
CREATE SEQUENCE "public"."mixture_four_point_bending_specimen_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mixture_four_point_bending_test_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mixture_four_point_bending_test_id_seq";
CREATE SEQUENCE "public"."mixture_four_point_bending_test_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mixture_task_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mixture_task_id_seq";
CREATE SEQUENCE "public"."mixture_task_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mixture_uniaxial_compression_p_values_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mixture_uniaxial_compression_p_values_id_seq";
CREATE SEQUENCE "public"."mixture_uniaxial_compression_p_values_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mixture_uniaxial_compression_specimen_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mixture_uniaxial_compression_specimen_id_seq";
CREATE SEQUENCE "public"."mixture_uniaxial_compression_specimen_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mixture_uniaxial_compression_test_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mixture_uniaxial_compression_test_id_seq";
CREATE SEQUENCE "public"."mixture_uniaxial_compression_test_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mixture_uniaxial_compression_uts028_data_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mixture_uniaxial_compression_uts028_data_id_seq";
CREATE SEQUENCE "public"."mixture_uniaxial_compression_uts028_data_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for molding_methods_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."molding_methods_id_seq";
CREATE SEQUENCE "public"."molding_methods_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for penetration_test_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."penetration_test_id_seq";
CREATE SEQUENCE "public"."penetration_test_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for penetration_tests_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."penetration_tests_id_seq";
CREATE SEQUENCE "public"."penetration_tests_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for projects_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."projects_id_seq";
CREATE SEQUENCE "public"."projects_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for projects_project_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."projects_project_id_seq";
CREATE SEQUENCE "public"."projects_project_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sand_material_sand_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sand_material_sand_id_seq";
CREATE SEQUENCE "public"."sand_material_sand_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for softening_point_test_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."softening_point_test_id_seq";
CREATE SEQUENCE "public"."softening_point_test_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for specimen_devices_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."specimen_devices_id_seq";
CREATE SEQUENCE "public"."specimen_devices_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for specimens_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."specimens_id_seq";
CREATE SEQUENCE "public"."specimens_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for stone_material_stone_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."stone_material_stone_id_seq";
CREATE SEQUENCE "public"."stone_material_stone_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for supported_devices_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."supported_devices_id_seq";
CREATE SEQUENCE "public"."supported_devices_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for test_asphalt_material_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."test_asphalt_material_id_seq";
CREATE SEQUENCE "public"."test_asphalt_material_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for users_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."users_id_seq";
CREATE SEQUENCE "public"."users_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for asphalt_material
-- ----------------------------
DROP TABLE IF EXISTS "public"."asphalt_material";
CREATE TABLE "public"."asphalt_material" (
  "asphalt_id" int8 NOT NULL GENERATED ALWAYS AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1
),
  "asphalt_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "asphalt_grade" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "asphalt_character" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "asphalt_company" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Records of asphalt_material
-- ----------------------------
INSERT INTO "public"."asphalt_material" OVERRIDING SYSTEM VALUE VALUES (6, '班级', 'bju', 'NORMAL', '2259397646222444157');
INSERT INTO "public"."asphalt_material" OVERRIDING SYSTEM VALUE VALUES (7, '示例', '70', 'NORMAL', 'default');
INSERT INTO "public"."asphalt_material" OVERRIDING SYSTEM VALUE VALUES (8, '沥青z', 'alal', 'NORMAL', '1329150149859754791');

-- ----------------------------
-- Table structure for asphalt_mixture_bending_test
-- ----------------------------
DROP TABLE IF EXISTS "public"."asphalt_mixture_bending_test";
CREATE TABLE "public"."asphalt_mixture_bending_test" (
  "id" int8 NOT NULL DEFAULT nextval('asphalt_mixture_bending_test_id_seq'::regclass),
  "task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "mix_ratio_id" int8 NOT NULL,
  "span_length" float4,
  "specimen_count" int4 NOT NULL DEFAULT 3,
  "average_flexural_strength" float4,
  "average_max_strain" float4,
  "average_stiffness_modulus" float4,
  "specimens" json,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;

-- ----------------------------
-- Records of asphalt_mixture_bending_test
-- ----------------------------
INSERT INTO "public"."asphalt_mixture_bending_test" VALUES (19, '52e60002-4437-45c7-99f7-29f5d3584943-2', 9, 1, 3, 0.00012967523, 4.42e+08, 1.2763404e-06, '[{"specimenNumber":1,"deflection":5.0,"maxStrain":9.0E7,"stiffnessModulus":3.7037037E-6,"maxLoad":4.0,"flexuralStrength":3.3333333E-4,"width":2.0,"height":3.0},{"specimenNumber":2,"deflection":9.0,"maxStrain":3.78E8,"stiffnessModulus":1.079797E-7,"maxLoad":8.0,"flexuralStrength":4.0816325E-5,"width":6.0,"height":7.0},{"specimenNumber":3,"deflection":13.0,"maxStrain":8.58E8,"stiffnessModulus":1.7338033E-8,"maxLoad":12.0,"flexuralStrength":1.4876033E-5,"width":10.0,"height":11.0}]', '2025-04-03 01:04:05.977487', '2025-04-03 01:04:05.977487');
INSERT INTO "public"."asphalt_mixture_bending_test" VALUES (20, '62eb46b1-2fc9-40c5-a2ef-ad00901d4450-2', 9, 1, 3, 0.0014999999, 6e+06, 0.00025, '[{"specimenNumber":1,"deflection":1.0,"maxStrain":6000000.0,"stiffnessModulus":2.5E-4,"maxLoad":1.0,"flexuralStrength":0.0015,"width":1.0,"height":1.0},{"specimenNumber":2,"deflection":1.0,"maxStrain":6000000.0,"stiffnessModulus":2.5E-4,"maxLoad":1.0,"flexuralStrength":0.0015,"width":1.0,"height":1.0},{"specimenNumber":3,"deflection":1.0,"maxStrain":6000000.0,"stiffnessModulus":2.5E-4,"maxLoad":1.0,"flexuralStrength":0.0015,"width":1.0,"height":1.0}]', '2025-04-03 10:43:55.244946', '2025-04-03 10:43:55.244946');

-- ----------------------------
-- Table structure for asphalt_penetration_test
-- ----------------------------
DROP TABLE IF EXISTS "public"."asphalt_penetration_test";
CREATE TABLE "public"."asphalt_penetration_test" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "task_id" varchar(255) COLLATE "pg_catalog"."default",
  "device_id" varchar(255) COLLATE "pg_catalog"."default",
  "device_name" varchar(255) COLLATE "pg_catalog"."default",
  "device_manufacturer" varchar(255) COLLATE "pg_catalog"."default",
  "device_model" varchar(255) COLLATE "pg_catalog"."default",
  "temperature" varchar(255) COLLATE "pg_catalog"."default",
  "reading" varchar(255) COLLATE "pg_catalog"."default",
  "experimenter" varchar(255) COLLATE "pg_catalog"."default",
  "test_date" int8,
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;

-- ----------------------------
-- Records of asphalt_penetration_test
-- ----------------------------

-- ----------------------------
-- Table structure for asphalt_softening_point_test
-- ----------------------------
DROP TABLE IF EXISTS "public"."asphalt_softening_point_test";
CREATE TABLE "public"."asphalt_softening_point_test" (
  "id" int8 NOT NULL DEFAULT nextval('asphalt_softening_point_test_id_seq'::regclass),
  "task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "temperature" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "softening_temperature" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "experimenter" varchar(255) COLLATE "pg_catalog"."default",
  "test_date" int8,
  "device_id" varchar(255) COLLATE "pg_catalog"."default",
  "device_name" varchar(255) COLLATE "pg_catalog"."default",
  "device_manufacturer" varchar(255) COLLATE "pg_catalog"."default",
  "device_model" varchar(255) COLLATE "pg_catalog"."default",
  "created_at" timestamp(6) DEFAULT now(),
  "updated_at" timestamp(6) DEFAULT now()
)
;

-- ----------------------------
-- Records of asphalt_softening_point_test
-- ----------------------------

-- ----------------------------
-- Table structure for asphalt_task
-- ----------------------------
DROP TABLE IF EXISTS "public"."asphalt_task";
CREATE TABLE "public"."asphalt_task" (
  "asphalt_task_id" int8 NOT NULL DEFAULT nextval('asphalt_task_asphalt_task_id_seq'::regclass),
  "asphalt_task_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "selected_asphalt_id" int8,
  "asphalt_task_assignment" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "created_at" timestamptz(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) DEFAULT CURRENT_TIMESTAMP,
  "status" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "task_status" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "asphalt_experiment_id" int8 NOT NULL DEFAULT nextval('asphalt_task_asphalt_experiment_id_seq'::regclass),
  "asphalt_experiment_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "asphalt_experiment_type" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "company_id" varchar(255) COLLATE "pg_catalog"."default",
  "due_date" date,
  "asphalt_task_assignment_id" varchar(255) COLLATE "pg_catalog"."default",
  "accept_time" int8,
  "acceptor" varchar(255) COLLATE "pg_catalog"."default",
  "experiment_status" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'unfinished'::character varying,
  "assigned_asphalt_equipment" varchar(255) COLLATE "pg_catalog"."default",
  "assigned_asphalt_equipment_manufacturer" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."asphalt_task"."asphalt_task_id" IS '沥青任务ID，自动生成';
COMMENT ON COLUMN "public"."asphalt_task"."asphalt_task_name" IS '用户输入的沥青任务名称';
COMMENT ON COLUMN "public"."asphalt_task"."selected_asphalt_id" IS '用户选择的沥青ID';
COMMENT ON COLUMN "public"."asphalt_task"."asphalt_task_assignment" IS '用户分配的实验任务（JSON格式）';
COMMENT ON COLUMN "public"."asphalt_task"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."asphalt_task"."updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."asphalt_task"."status" IS '任务状态';
COMMENT ON COLUMN "public"."asphalt_task"."asphalt_task_assignment_id" IS '用于索引的沥青任务id';
COMMENT ON TABLE "public"."asphalt_task" IS '沥青任务表';

-- ----------------------------
-- Records of asphalt_task
-- ----------------------------
INSERT INTO "public"."asphalt_task" VALUES (148, 'ymmm1_隔壁班', 3, '沥青弯曲蠕变劲度试验（弯曲梁流变仪法）', '2025-04-03 12:36:10.649106+08', '2025-04-03 12:36:18.136777+08', 'ONGOING', 'ONGOING', 148, 'ymmm1_隔壁班', 'ASPHALT', '1329150149859754791', '2025-05-25', '7b8b9962-921f-4d28-b9d1-41f39a5ecd7c', 1743654977426, '啊啊啊', 'unfinished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (138, 'ymmm1_全了', 3, '延度试验', '2025-04-03 11:49:43.619049+08', '2025-04-03 11:49:48.650049+08', 'ONGOING', 'ONGOING', 138, 'ymmm1_全了', 'ASPHALT', '1329150149859754791', '2025-05-25', '645c3a5f-2dec-4680-bcf9-e7a461ad0c60', 1743652187790, '啊啊啊', 'unfinished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (140, 'ymmm1_全了', 3, '针入度试验', '2025-04-03 11:49:44.143664+08', '2025-04-03 11:49:49.138031+08', 'ONGOING', 'ONGOING', 140, 'ymmm1_全了', 'ASPHALT', '1329150149859754791', '2025-05-25', '645c3a5f-2dec-4680-bcf9-e7a461ad0c60', 1743652187790, '啊啊啊', 'unfinished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (141, 'ymmm1_全了', 3, '软化点试验（环球法）', '2025-04-03 11:49:44.406697+08', '2025-04-03 11:49:49.382036+08', 'ONGOING', 'ONGOING', 141, 'ymmm1_全了', 'ASPHALT', '1329150149859754791', '2025-05-25', '645c3a5f-2dec-4680-bcf9-e7a461ad0c60', 1743652187790, '啊啊啊', 'unfinished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (136, 'ymmm1_全了', 3, '沥青旋转黏度试验（布鲁克菲尔德黏度计法）', '2025-04-03 11:49:43.06721+08', '2025-04-03 12:11:03.62907+08', 'ONGOING', 'ONGOING', 136, 'ymmm1_全了', 'ASPHALT', '1329150149859754791', '2025-05-25', '645c3a5f-2dec-4680-bcf9-e7a461ad0c60', 1743652187790, '啊啊啊', 'finished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (149, 'ymmm1_隔壁班', 3, '针入度试验', '2025-04-03 12:36:10.883222+08', '2025-04-03 12:36:18.370858+08', 'ONGOING', 'ONGOING', 149, 'ymmm1_隔壁班', 'ASPHALT', '1329150149859754791', '2025-05-25', '7b8b9962-921f-4d28-b9d1-41f39a5ecd7c', 1743654977426, '啊啊啊', 'unfinished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (143, 'ymmm1_任务3', 3, '延度试验', '2025-04-03 12:34:11.45316+08', '2025-04-03 12:34:18.517169+08', 'ONGOING', 'ONGOING', 143, 'ymmm1_任务3', 'ASPHALT', '1329150149859754791', '2025-05-25', '07abb45d-f570-48db-b3be-66fd62e48a2b', 1743654857997, '啊啊啊', 'unfinished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (144, 'ymmm1_任务3', 3, '针入度试验', '2025-04-03 12:34:11.708584+08', '2025-04-03 12:34:18.749953+08', 'ONGOING', 'ONGOING', 144, 'ymmm1_任务3', 'ASPHALT', '1329150149859754791', '2025-05-25', '07abb45d-f570-48db-b3be-66fd62e48a2b', 1743654857997, '啊啊啊', 'unfinished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (145, 'ymmm1_任务3', 3, '软化点试验（环球法）', '2025-04-03 12:34:11.964438+08', '2025-04-03 12:34:18.985191+08', 'ONGOING', 'ONGOING', 145, 'ymmm1_任务3', 'ASPHALT', '1329150149859754791', '2025-05-25', '07abb45d-f570-48db-b3be-66fd62e48a2b', 1743654857997, '啊啊啊', 'unfinished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (142, 'ymmm1_任务3', 3, '沥青旋转黏度试验（布鲁克菲尔德黏度计法）', '2025-04-03 12:34:11.168995+08', '2025-04-03 12:34:47.779432+08', 'ONGOING', 'ONGOING', 142, 'ymmm1_任务3', 'ASPHALT', '1329150149859754791', '2025-05-25', '07abb45d-f570-48db-b3be-66fd62e48a2b', 1743654857997, '啊啊啊', 'finished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (147, 'ymmm1_隔壁班', 3, '延度试验', '2025-04-03 12:36:10.415224+08', '2025-04-03 12:36:17.896885+08', 'ONGOING', 'ONGOING', 147, 'ymmm1_隔壁班', 'ASPHALT', '1329150149859754791', '2025-05-25', '7b8b9962-921f-4d28-b9d1-41f39a5ecd7c', 1743654977426, '啊啊啊', 'unfinished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (150, 'ymmm1_隔壁班', 3, '软化点试验（环球法）', '2025-04-03 12:36:11.117646+08', '2025-04-03 12:36:18.604296+08', 'ONGOING', 'ONGOING', 150, 'ymmm1_隔壁班', 'ASPHALT', '1329150149859754791', '2025-05-25', '7b8b9962-921f-4d28-b9d1-41f39a5ecd7c', 1743654977426, '啊啊啊', 'unfinished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (146, 'ymmm1_隔壁班', 3, '沥青旋转黏度试验（布鲁克菲尔德黏度计法）', '2025-04-03 12:36:10.179891+08', '2025-04-03 12:38:49.107933+08', 'ONGOING', 'ONGOING', 146, 'ymmm1_隔壁班', 'ASPHALT', '1329150149859754791', '2025-05-25', '7b8b9962-921f-4d28-b9d1-41f39a5ecd7c', 1743654977426, '啊啊啊', 'finished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (137, 'ymmm1_全了', 3, '动态剪切流变试验', '2025-04-03 11:49:43.3518+08', '2025-04-03 12:41:13.511117+08', 'ONGOING', 'ONGOING', 137, 'ymmm1_全了', 'ASPHALT', '1329150149859754791', '2025-05-25', '645c3a5f-2dec-4680-bcf9-e7a461ad0c60', 1743652187790, '啊啊啊', 'finished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (139, 'ymmm1_全了', 3, '沥青弯曲蠕变劲度试验（弯曲梁流变仪法）', '2025-04-03 11:49:43.880662+08', '2025-04-03 13:09:47.743669+08', 'ONGOING', 'ONGOING', 139, 'ymmm1_全了', 'ASPHALT', '1329150149859754791', '2025-05-25', '645c3a5f-2dec-4680-bcf9-e7a461ad0c60', 1743652187790, '啊啊啊', 'finished', NULL, NULL);
INSERT INTO "public"."asphalt_task" VALUES (151, 'ymmm1_动态', 3, '动态剪切流变试验', '2025-04-03 13:39:13.763389+08', '2025-04-03 13:39:51.310198+08', 'ONGOING', 'COMPLETED', 151, 'ymmm1_动态', 'ASPHALT', '1329150149859754791', '2025-05-25', '31931f37-4231-4a5e-bb8a-bf312e3414a1', 1743658770624, '啊啊啊', 'finished', NULL, NULL);

-- ----------------------------
-- Table structure for bbr_test
-- ----------------------------
DROP TABLE IF EXISTS "public"."bbr_test";
CREATE TABLE "public"."bbr_test" (
  "id" int8 NOT NULL DEFAULT nextval('bbr_test_id_seq'::regclass),
  "task_id" varchar(255) COLLATE "pg_catalog"."default",
  "operator_id" varchar(255) COLLATE "pg_catalog"."default",
  "test_date" timestamp(6),
  "beam_span" float8,
  "specimen_width" float8,
  "specimen_height" float8,
  "creep_rate" float8,
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "deflection120s" float8,
  "deflection15s" float8,
  "deflection240s" float8,
  "deflection30s" float8,
  "deflection60s" float8,
  "deflection8s" float8,
  "load120s" float8,
  "load15s" float8,
  "load240s" float8,
  "load30s" float8,
  "load60s" float8,
  "load8s" float8,
  "stiffness120s" float8,
  "stiffness15s" float8,
  "stiffness240s" float8,
  "stiffness30s" float8,
  "stiffness60s" float8,
  "stiffness8s" float8,
  "temperature120s" float8,
  "temperature15s" float8,
  "temperature240s" float8,
  "temperature30s" float8,
  "temperature60s" float8,
  "temperature8s" float8,
  "material_type" varchar(255) COLLATE "pg_catalog"."default",
  "remarks" varchar(255) COLLATE "pg_catalog"."default",
  "specimen_id" varchar(255) COLLATE "pg_catalog"."default",
  "specimen_type" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of bbr_test
-- ----------------------------
INSERT INTO "public"."bbr_test" VALUES (8, '645c3a5f-2dec-4680-bcf9-e7a461ad0c60', '啊啊啊', '2025-04-03 13:09:46.925189', 1, 2, 3, 0.036, '2025-04-03 13:09:46.948226', '2025-04-03 13:09:46.948226', 18, 9, 21, 12, 15, 6, 17, 8, 20, 11, 14, 5, 0.004372, 0, 0, 0.004244, 0, 0.003858, 16, 7, 19, 10, 13, 4, '', '', '', '');

-- ----------------------------
-- Table structure for brookfield_viscosity_measurement
-- ----------------------------
DROP TABLE IF EXISTS "public"."brookfield_viscosity_measurement";
CREATE TABLE "public"."brookfield_viscosity_measurement" (
  "id" int8 NOT NULL DEFAULT nextval('brookfield_viscosity_measurement_id_seq'::regclass),
  "temperature_point_id" int8,
  "measurement_id" varchar(255) COLLATE "pg_catalog"."default",
  "spindle_type" varchar(255) COLLATE "pg_catalog"."default",
  "rotation_speed" varchar(255) COLLATE "pg_catalog"."default",
  "viscosity" varchar(255) COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) DEFAULT CURRENT_TIMESTAMP
)
;

-- ----------------------------
-- Records of brookfield_viscosity_measurement
-- ----------------------------
INSERT INTO "public"."brookfield_viscosity_measurement" VALUES (16, 13, '1', '1', '2', '4', '2025-04-03 12:34:46.849654+08');
INSERT INTO "public"."brookfield_viscosity_measurement" VALUES (17, 14, '1', '123456789', '9', '999', '2025-04-03 12:38:48.169782+08');
INSERT INTO "public"."brookfield_viscosity_measurement" VALUES (18, 14, '2', '123456789', '9', '9999', '2025-04-03 12:38:48.295979+08');

-- ----------------------------
-- Table structure for brookfield_viscosity_temperature_point
-- ----------------------------
DROP TABLE IF EXISTS "public"."brookfield_viscosity_temperature_point";
CREATE TABLE "public"."brookfield_viscosity_temperature_point" (
  "id" int8 NOT NULL DEFAULT nextval('brookfield_viscosity_temperature_point_id_seq'::regclass),
  "test_id" int8,
  "point_id" varchar(255) COLLATE "pg_catalog"."default",
  "temperature" varchar(255) COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) DEFAULT CURRENT_TIMESTAMP
)
;

-- ----------------------------
-- Records of brookfield_viscosity_temperature_point
-- ----------------------------
INSERT INTO "public"."brookfield_viscosity_temperature_point" VALUES (13, 10, '1', '3', '2025-04-03 12:34:46.718769+08');
INSERT INTO "public"."brookfield_viscosity_temperature_point" VALUES (14, 11, '1', '99', '2025-04-03 12:38:48.048384+08');

-- ----------------------------
-- Table structure for brookfield_viscosity_test
-- ----------------------------
DROP TABLE IF EXISTS "public"."brookfield_viscosity_test";
CREATE TABLE "public"."brookfield_viscosity_test" (
  "id" int8 NOT NULL DEFAULT nextval('brookfield_viscosity_test_id_seq'::regclass),
  "task_id" varchar(255) COLLATE "pg_catalog"."default",
  "device_code" varchar(100) COLLATE "pg_catalog"."default",
  "device_manufacturer" varchar(255) COLLATE "pg_catalog"."default",
  "device_model" varchar(255) COLLATE "pg_catalog"."default",
  "experimenter" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "created_at" timestamptz(6),
  "device_id" varchar(255) COLLATE "pg_catalog"."default",
  "device_name" varchar(255) COLLATE "pg_catalog"."default",
  "test_date" int8,
  "updated_at" timestamptz(6)
)
;

-- ----------------------------
-- Records of brookfield_viscosity_test
-- ----------------------------
INSERT INTO "public"."brookfield_viscosity_test" VALUES (10, '07abb45d-f570-48db-b3be-66fd62e48a2b', NULL, NULL, NULL, '啊啊啊', '2025-04-03 12:34:46.651757', '2025-04-03 12:34:46.572207+08', NULL, NULL, 1743654886385, '2025-04-03 12:34:46.572207+08');
INSERT INTO "public"."brookfield_viscosity_test" VALUES (11, '7b8b9962-921f-4d28-b9d1-41f39a5ecd7c', NULL, NULL, NULL, '啊啊啊', '2025-04-03 12:38:47.986086', '2025-04-03 12:38:47.931246+08', NULL, NULL, 1743655127778, '2025-04-03 12:38:47.931246+08');

-- ----------------------------
-- Table structure for compaction_methods
-- ----------------------------
DROP TABLE IF EXISTS "public"."compaction_methods";
CREATE TABLE "public"."compaction_methods" (
  "id" int8 NOT NULL DEFAULT nextval('compaction_methods_id_seq'::regclass),
  "created_by" int8,
  "creation_time" int8,
  "description" varchar(255) COLLATE "pg_catalog"."default",
  "method_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "organization_id" int8 NOT NULL,
  "compaction_method" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "is_default" bool DEFAULT false,
  "mixing_speed" float4,
  "mixing_temperature" float4,
  "mixing_time" float4,
  "organization" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "specimen_type" int4
)
;

-- ----------------------------
-- Records of compaction_methods
-- ----------------------------

-- ----------------------------
-- Table structure for devices
-- ----------------------------
DROP TABLE IF EXISTS "public"."devices";
CREATE TABLE "public"."devices" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "company_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "manufacturer" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "model" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "purchase_year" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "user_id" int8 NOT NULL
)
;

-- ----------------------------
-- Records of devices
-- ----------------------------
INSERT INTO "public"."devices" VALUES ('e684eb35-fc90-4067-95df-7e5da786b288', '2259397646222444157', 'Controls', '77-PV0077/C', '2025', 'MIXING', 6);
INSERT INTO "public"."devices" VALUES ('c083ae64-af58-4f90-b001-78e943623cad', '2259397646222444157', 'Controls', '77-PV41A02', '2025', 'FORMING', 6);
INSERT INTO "public"."devices" VALUES ('cb83cc28-9e54-478c-a341-cbc5877ddf95', '2259397646222444157', 'Controls', '76-B3002', '2025', 'TESTING', 6);
INSERT INTO "public"."devices" VALUES ('f7935016-8e09-441a-9198-ba4a43861118', '5371211465312940040', 'Controls', '77-PV0077/C', '2025', 'MIXING', 7);
INSERT INTO "public"."devices" VALUES ('6c508db5-3c92-4ca1-aeb3-77a41c35cd23', '5371211465312940040', 'Controls', '77-PV41A02', '2025', 'FORMING', 7);
INSERT INTO "public"."devices" VALUES ('b08583d7-a5ba-41ef-afbc-2b53aa828a26', '5371211465312940040', 'Controls', '76-B3002', '2025', 'TESTING', 7);
INSERT INTO "public"."devices" VALUES ('d8a056c8-7883-486e-906a-27836519905c', '5371211465312940040', 'Controls', '77-PV0077/C', '2025', 'MIXING', 7);
INSERT INTO "public"."devices" VALUES ('1324f386-e4be-4785-9470-7ee614749efa', '5371211465312940040', 'Controls', '77-PV41A02', '2025', 'FORMING', 7);
INSERT INTO "public"."devices" VALUES ('a82c4192-3b9e-4bb8-be45-bb8a92c60ff9', '5371211465312940040', 'Controls', '76-B3002', '2025', 'TESTING', 7);
INSERT INTO "public"."devices" VALUES ('c661b096-6c35-411e-bdd0-27a19c42f35b', 'default', 'infratest', '20-0160-60', '2025', 'MIXING', 1);
INSERT INTO "public"."devices" VALUES ('0b33a477-58e4-4c1b-8a43-d5908631e6d1', 'default', 'Controls', '77-PV41A02', '2025', 'FORMING', 1);
INSERT INTO "public"."devices" VALUES ('e9c97bde-edff-49f4-901e-2984c7681994', 'default', 'Controls', '76-B3002', '2025', 'TESTING', 1);
INSERT INTO "public"."devices" VALUES ('b1b30174-f97e-40f4-bb9a-48e639d28bdb', '8154373918757111414', 'Controls', '77-PV0077/C', '2025', 'MIXING', 11);
INSERT INTO "public"."devices" VALUES ('371bd561-5568-4529-a02e-b6b2f6608e20', '8154373918757111414', 'Controls', '77-PV41A02', '2025', 'FORMING', 11);
INSERT INTO "public"."devices" VALUES ('a624b1fb-0a34-4e48-9c97-ccf3ee9e8ba2', '8154373918757111414', 'Controls', '76-B3002', '2025', 'TESTING', 11);
INSERT INTO "public"."devices" VALUES ('680d8ca2-02d8-4c4c-9d87-055225c54e3a', '6686834324109702399', 'Controls', '77-PV0077/C', '2025', 'MIXING', 12);
INSERT INTO "public"."devices" VALUES ('fff1f9f3-20d8-4d0e-8cd7-2f47878d4222', '6686834324109702399', 'Controls', '77-PV41A02', '2025', 'FORMING', 12);
INSERT INTO "public"."devices" VALUES ('657513a9-2995-4605-8dbc-d8bff140d5a7', '6686834324109702399', 'Controls', '76-B3002', '2025', 'TESTING', 12);
INSERT INTO "public"."devices" VALUES ('da319196-6270-4cc0-b545-84ca30f5f965', '1301433143261679451', 'Controls', '77-PV0077/C', '2025', 'MIXING', 13);
INSERT INTO "public"."devices" VALUES ('0e434e23-2b1a-415a-96d4-38f79d83fe3f', '1301433143261679451', 'Controls', '77-PV41A02', '2025', 'FORMING', 13);
INSERT INTO "public"."devices" VALUES ('bd359c8b-0a44-4b55-a723-b6d01bb87452', '1301433143261679451', 'Controls', '76-B3002', '2025', 'TESTING', 13);
INSERT INTO "public"."devices" VALUES ('b6e516aa-742f-4bfc-9afc-e47e9d575ca8', '4757752133930273773', 'Controls', '77-PV0077/C', '2025', 'MIXING', 15);
INSERT INTO "public"."devices" VALUES ('786f2af9-438d-496a-9521-14aadb04c71d', '4757752133930273773', 'Controls', '77-PV41A02', '2025', 'FORMING', 15);
INSERT INTO "public"."devices" VALUES ('6b52b253-31ef-444c-b581-cb3a3fa03c45', '4757752133930273773', 'Controls', '76-B3002', '2025', 'TESTING', 15);
INSERT INTO "public"."devices" VALUES ('76e7bedc-d988-4ed7-a863-1fc5edb91027', '2850349200073707383', 'Controls', '77-PV0077/C', '2025', 'MIXING', 17);
INSERT INTO "public"."devices" VALUES ('5f0ad9a2-e938-4e4e-a6c9-36ef55314201', '2850349200073707383', 'Controls', '77-PV41A02', '2025', 'FORMING', 17);
INSERT INTO "public"."devices" VALUES ('aeba0634-9040-4dda-840f-6da6e7228887', '2850349200073707383', 'Controls', '76-B3002', '2025', 'TESTING', 17);
INSERT INTO "public"."devices" VALUES ('44a6e49f-c5be-49ee-91a7-edac3e6a70b2', '3809984879460171779', 'Controls', '77-PV0077/C', '2025', 'MIXING', 18);
INSERT INTO "public"."devices" VALUES ('76852f55-f594-4076-b7a7-844b2a16c65c', '3809984879460171779', 'Controls', '77-PV41A02', '2025', 'FORMING', 18);
INSERT INTO "public"."devices" VALUES ('d741d9fc-0e98-460c-b88b-dc411c6ef5de', '3809984879460171779', 'Controls', '76-B3002', '2025', 'TESTING', 18);
INSERT INTO "public"."devices" VALUES ('48453980-7191-442c-9e8b-c3dc0add5771', '4751356350763481445', 'Controls', '77-PV0077/C', '2025', 'MIXING', 19);
INSERT INTO "public"."devices" VALUES ('6b2d985b-4573-46bb-bd92-90c697698c41', '4751356350763481445', 'Controls', '77-PV41A02', '2025', 'FORMING', 19);
INSERT INTO "public"."devices" VALUES ('6167a06d-7f99-4810-a4b5-24d8f73df1f9', '4751356350763481445', 'Controls', '76-B3002', '2025', 'TESTING', 19);
INSERT INTO "public"."devices" VALUES ('bedb6e70-1787-47e5-b3d0-22f64a589d7d', '4630555664774839271', 'Controls', '77-PV0077/C', '2025', 'MIXING', 21);
INSERT INTO "public"."devices" VALUES ('b22dfde7-34f2-4f59-b964-fb81c7c1535f', '4630555664774839271', 'Controls', '77-PV41A02', '2025', 'FORMING', 21);
INSERT INTO "public"."devices" VALUES ('a44e279d-d928-4888-ab40-e6858f96a69e', '4630555664774839271', 'Controls', '76-B3002', '2025', 'TESTING', 21);
INSERT INTO "public"."devices" VALUES ('b7225ca4-c857-4d40-b11b-207bdc890547', '1329150149859754791', 'Controls', '77-PV0077/C', '2025', 'MIXING', 23);
INSERT INTO "public"."devices" VALUES ('ecd2b31b-ff5d-45d1-a533-5008c557967b', '1329150149859754791', 'Controls', '77-PV41A02', '2025', 'FORMING', 23);
INSERT INTO "public"."devices" VALUES ('6ddd47c9-da2c-4ef7-bb4c-8a591f093a93', '1329150149859754791', 'Controls', '76-B3002', '2025', 'TESTING', 23);
INSERT INTO "public"."devices" VALUES ('ac2585bb-0cd3-4ac6-a71e-45dcfa4575bf', '7393056949553090473', 'Controls', '77-PV0077/C', '2025', 'MIXING', 26);
INSERT INTO "public"."devices" VALUES ('70af759a-524d-404f-9a3c-3b1bf0309a9d', '7393056949553090473', 'Controls', '77-PV41A02', '2025', 'FORMING', 26);
INSERT INTO "public"."devices" VALUES ('19fb9552-c961-460c-870b-3bd6c7c8687c', '7393056949553090473', 'Controls', '76-B3002', '2025', 'TESTING', 26);

-- ----------------------------
-- Table structure for direct_stretching_fatigue_data
-- ----------------------------
DROP TABLE IF EXISTS "public"."direct_stretching_fatigue_data";
CREATE TABLE "public"."direct_stretching_fatigue_data" (
  "id" int8 NOT NULL DEFAULT nextval('direct_stretching_fatigue_data_id_seq'::regclass),
  "specimen_id" int8,
  "stage" varchar(20) COLLATE "pg_catalog"."default",
  "cycle_count" int4,
  "phase_angle" float8,
  "force_level" float8,
  "equilibrium_strain" float8,
  "dynamic_modulus" float8,
  "temperature" float8,
  "created_at" timestamp(6),
  "updated_at" timestamp(6),
  "test_id" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of direct_stretching_fatigue_data
-- ----------------------------
INSERT INTO "public"."direct_stretching_fatigue_data" VALUES (23, 1, 'initial', 4, 6, 7, 8, 5, 10, '2025-04-03 00:58:31.133', NULL, 'be457a40-8829-4157-bf34-c0d30091e9d3');
INSERT INTO "public"."direct_stretching_fatigue_data" VALUES (24, 1, 'final', 4, 6, 7, 8, 5, 10, '2025-04-03 00:58:31.339', NULL, 'be457a40-8829-4157-bf34-c0d30091e9d3');

-- ----------------------------
-- Table structure for direct_stretching_fatigue_specimens
-- ----------------------------
DROP TABLE IF EXISTS "public"."direct_stretching_fatigue_specimens";
CREATE TABLE "public"."direct_stretching_fatigue_specimens" (
  "id" int8 NOT NULL DEFAULT nextval('direct_stretching_fatigue_specimens_id_seq'::regclass),
  "task_id" varchar(255) COLLATE "pg_catalog"."default",
  "specimen_id" int8,
  "diameter" float8,
  "height" float8,
  "test_temperature" float8,
  "created_at" timestamp(6),
  "updated_at" timestamp(6),
  "test_id" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of direct_stretching_fatigue_specimens
-- ----------------------------
INSERT INTO "public"."direct_stretching_fatigue_specimens" VALUES (21, NULL, 1, 1, 2, NULL, '2025-04-03 00:58:30.519', NULL, 'be457a40-8829-4157-bf34-c0d30091e9d3');

-- ----------------------------
-- Table structure for direct_stretching_fatigue_test
-- ----------------------------
DROP TABLE IF EXISTS "public"."direct_stretching_fatigue_test";
CREATE TABLE "public"."direct_stretching_fatigue_test" (
  "id" int8 NOT NULL DEFAULT nextval('direct_stretching_fatigue_test_id_seq'::regclass),
  "task_id" varchar(255) COLLATE "pg_catalog"."default",
  "mix_ratio_id" varchar(255) COLLATE "pg_catalog"."default",
  "created_at" timestamp(6),
  "test_temperature" varchar(255) COLLATE "pg_catalog"."default",
  "test_time" date,
  "operator" varchar(255) COLLATE "pg_catalog"."default",
  "test_equipment" varchar(255) COLLATE "pg_catalog"."default",
  "test_method" varchar(255) COLLATE "pg_catalog"."default",
  "test_standard" varchar(255) COLLATE "pg_catalog"."default",
  "remarks" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" date,
  "update_time" date,
  "test_id" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of direct_stretching_fatigue_test
-- ----------------------------
INSERT INTO "public"."direct_stretching_fatigue_test" VALUES (46, '52e60002-4437-45c7-99f7-29f5d3584943-0', '9', NULL, NULL, '2025-04-03', '', '', '直接拉伸循环疲劳测黏弹损伤试验', '标准试验', '', '2025-04-03', '2025-04-03', 'be457a40-8829-4157-bf34-c0d30091e9d3');

-- ----------------------------
-- Table structure for direct_stretching_modulus_data
-- ----------------------------
DROP TABLE IF EXISTS "public"."direct_stretching_modulus_data";
CREATE TABLE "public"."direct_stretching_modulus_data" (
  "id" int8 NOT NULL DEFAULT nextval('direct_stretching_modulus_data_id_seq'::regclass),
  "specimen_id" int8,
  "stage" varchar(20) COLLATE "pg_catalog"."default",
  "cycle_count" int4,
  "phase_angle" float8,
  "force_level" float8,
  "equilibrium_strain" float8,
  "dynamic_modulus" float8,
  "temperature" float8,
  "created_at" timestamp(6),
  "updated_at" timestamp(6),
  "task_id" varchar(255) COLLATE "pg_catalog"."default",
  "test_id" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of direct_stretching_modulus_data
-- ----------------------------
INSERT INTO "public"."direct_stretching_modulus_data" VALUES (25, 1, 'initial', 4, 6, 7, 8, 5, 10, '2025-04-03 00:58:30.724', NULL, NULL, 'be457a40-8829-4157-bf34-c0d30091e9d3');
INSERT INTO "public"."direct_stretching_modulus_data" VALUES (26, 1, 'final', 4, 6, 7, 8, 5, 10, '2025-04-03 00:58:30.929', NULL, NULL, 'be457a40-8829-4157-bf34-c0d30091e9d3');

-- ----------------------------
-- Table structure for dsr_measurement
-- ----------------------------
DROP TABLE IF EXISTS "public"."dsr_measurement";
CREATE TABLE "public"."dsr_measurement" (
  "id" int8 NOT NULL DEFAULT nextval('dsr_measurement_id_seq'::regclass),
  "temperature_point_id" int8,
  "load_frequency" float8,
  "max_shear_stress" float8,
  "max_shear_strain" float8,
  "phase_angle" float8,
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "complex_shear_modulus" float8,
  "task_id" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of dsr_measurement
-- ----------------------------
INSERT INTO "public"."dsr_measurement" VALUES (7, 7, 3, 5, 7, 9, '2025-04-03 12:11:02.730968', '2025-04-03 12:11:02.730968', NULL, NULL);
INSERT INTO "public"."dsr_measurement" VALUES (8, 8, 654321, 11, 12, 13, '2025-04-03 12:41:12.683961', '2025-04-03 12:41:12.683961', NULL, NULL);
INSERT INTO "public"."dsr_measurement" VALUES (9, 9, 2, 4, 5, 6, '2025-04-03 13:39:50.151596', '2025-04-03 13:39:50.151596', NULL, '31931f37-4231-4a5e-bb8a-bf312e3414a1');

-- ----------------------------
-- Table structure for dsr_temperature_point
-- ----------------------------
DROP TABLE IF EXISTS "public"."dsr_temperature_point";
CREATE TABLE "public"."dsr_temperature_point" (
  "id" int8 NOT NULL DEFAULT nextval('dsr_temperature_point_id_seq'::regclass),
  "test_id" int8,
  "point_number" int4,
  "temperature" float8,
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "task_id" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of dsr_temperature_point
-- ----------------------------
INSERT INTO "public"."dsr_temperature_point" VALUES (7, 8, 1, 1, '2025-04-03 12:11:02.610499', '2025-04-03 12:11:02.610499', NULL);
INSERT INTO "public"."dsr_temperature_point" VALUES (8, 9, 1, 123456, '2025-04-03 12:41:12.562016', '2025-04-03 12:41:12.562016', NULL);
INSERT INTO "public"."dsr_temperature_point" VALUES (9, 10, 1, 1, '2025-04-03 13:39:50.025059', '2025-04-03 13:39:50.025059', '31931f37-4231-4a5e-bb8a-bf312e3414a1');

-- ----------------------------
-- Table structure for ductility_test
-- ----------------------------
DROP TABLE IF EXISTS "public"."ductility_test";
CREATE TABLE "public"."ductility_test" (
  "id" int8 NOT NULL DEFAULT nextval('ductility_test_id_seq'::regclass),
  "task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "temperature" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "displacement" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "experimenter" varchar(255) COLLATE "pg_catalog"."default",
  "test_date" int8,
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "device_id" varchar(255) COLLATE "pg_catalog"."default",
  "device_name" varchar(255) COLLATE "pg_catalog"."default",
  "device_manufacturer" varchar(255) COLLATE "pg_catalog"."default",
  "device_model" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of ductility_test
-- ----------------------------

-- ----------------------------
-- Table structure for dynamic_modulus_measurement
-- ----------------------------
DROP TABLE IF EXISTS "public"."dynamic_modulus_measurement";
CREATE TABLE "public"."dynamic_modulus_measurement" (
  "id" int8 NOT NULL DEFAULT nextval('dynamic_modulus_measurement_id_seq'::regclass),
  "temperature_id" int8,
  "frequency" numeric(38,2),
  "cycle_count" int4,
  "dynamic_modulus" numeric(38,2),
  "phase_angle" numeric(38,2),
  "axial_stress" numeric(38,2),
  "axial_strain" numeric(38,2),
  "permanent_deformation" numeric(38,2),
  "test_date" timestamp(6),
  "is_valid" bool DEFAULT true,
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "test_id" int4
)
;

-- ----------------------------
-- Records of dynamic_modulus_measurement
-- ----------------------------
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (571, 99, 25.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:37.871', 't', '2025-04-03 00:54:37.871', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (572, 99, 10.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:38.076', 't', '2025-04-03 00:54:38.076', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (573, 99, 5.00, 100, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:38.28', 't', '2025-04-03 00:54:38.28', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (574, 99, 1.00, 20, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:38.484', 't', '2025-04-03 00:54:38.484', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (575, 99, 0.50, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:38.689', 't', '2025-04-03 00:54:38.689', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (576, 99, 0.10, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:38.894', 't', '2025-04-03 00:54:38.894', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (577, 100, 25.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:39.099', 't', '2025-04-03 00:54:39.099', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (578, 100, 10.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:39.304', 't', '2025-04-03 00:54:39.304', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (579, 100, 5.00, 100, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:39.509', 't', '2025-04-03 00:54:39.509', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (580, 100, 1.00, 20, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:39.714', 't', '2025-04-03 00:54:39.714', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (581, 100, 0.50, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:39.918', 't', '2025-04-03 00:54:39.918', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (582, 100, 0.10, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:40.124', 't', '2025-04-03 00:54:40.124', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (583, 101, 25.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:40.328', 't', '2025-04-03 00:54:40.328', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (584, 101, 10.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:40.532', 't', '2025-04-03 00:54:40.532', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (585, 101, 5.00, 100, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:40.738', 't', '2025-04-03 00:54:40.738', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (586, 101, 1.00, 20, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:40.943', 't', '2025-04-03 00:54:40.943', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (587, 101, 0.50, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:41.147', 't', '2025-04-03 00:54:41.147', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (588, 101, 0.10, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:41.352', 't', '2025-04-03 00:54:41.352', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (589, 102, 25.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:41.556', 't', '2025-04-03 00:54:41.556', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (590, 102, 10.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:41.761', 't', '2025-04-03 00:54:41.761', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (591, 102, 5.00, 100, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:41.967', 't', '2025-04-03 00:54:41.967', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (592, 102, 1.00, 20, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:42.171', 't', '2025-04-03 00:54:42.171', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (593, 102, 0.50, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:42.376', 't', '2025-04-03 00:54:42.376', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (594, 102, 0.10, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:42.581', 't', '2025-04-03 00:54:42.581', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (595, 103, 25.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:42.786', 't', '2025-04-03 00:54:42.786', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (596, 103, 10.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:42.99', 't', '2025-04-03 00:54:42.99', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (597, 103, 5.00, 100, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:43.196', 't', '2025-04-03 00:54:43.196', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (598, 103, 1.00, 20, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:43.4', 't', '2025-04-03 00:54:43.4', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (599, 103, 0.50, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:43.605', 't', '2025-04-03 00:54:43.605', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (600, 103, 0.10, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:43.81', 't', '2025-04-03 00:54:43.81', 26);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (601, 104, 25.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:47.392', 't', '2025-04-03 00:54:47.392', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (602, 104, 10.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:47.598', 't', '2025-04-03 00:54:47.598', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (603, 104, 5.00, 100, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:47.803', 't', '2025-04-03 00:54:47.803', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (604, 104, 1.00, 20, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:48.007', 't', '2025-04-03 00:54:48.007', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (605, 104, 0.50, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:48.212', 't', '2025-04-03 00:54:48.212', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (606, 104, 0.10, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:48.417', 't', '2025-04-03 00:54:48.418', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (607, 105, 25.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:48.622', 't', '2025-04-03 00:54:48.622', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (608, 105, 10.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:48.83', 't', '2025-04-03 00:54:48.83', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (609, 105, 5.00, 100, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:49.032', 't', '2025-04-03 00:54:49.032', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (610, 105, 1.00, 20, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:49.237', 't', '2025-04-03 00:54:49.237', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (611, 105, 0.50, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:49.441', 't', '2025-04-03 00:54:49.441', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (612, 105, 0.10, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:49.647', 't', '2025-04-03 00:54:49.647', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (613, 106, 25.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:49.851', 't', '2025-04-03 00:54:49.851', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (614, 106, 10.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:50.057', 't', '2025-04-03 00:54:50.057', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (615, 106, 5.00, 100, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:50.261', 't', '2025-04-03 00:54:50.261', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (616, 106, 1.00, 20, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:50.466', 't', '2025-04-03 00:54:50.466', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (617, 106, 0.50, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:50.669', 't', '2025-04-03 00:54:50.669', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (618, 106, 0.10, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:50.876', 't', '2025-04-03 00:54:50.876', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (619, 107, 25.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:51.079', 't', '2025-04-03 00:54:51.079', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (620, 107, 10.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:51.285', 't', '2025-04-03 00:54:51.285', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (621, 107, 5.00, 100, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:51.49', 't', '2025-04-03 00:54:51.49', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (622, 107, 1.00, 20, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:51.697', 't', '2025-04-03 00:54:51.697', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (623, 107, 0.50, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:51.899', 't', '2025-04-03 00:54:51.899', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (624, 107, 0.10, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:52.104', 't', '2025-04-03 00:54:52.104', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (625, 108, 25.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:52.309', 't', '2025-04-03 00:54:52.309', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (626, 108, 10.00, 200, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:52.514', 't', '2025-04-03 00:54:52.514', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (627, 108, 5.00, 100, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:52.718', 't', '2025-04-03 00:54:52.718', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (628, 108, 1.00, 20, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:52.924', 't', '2025-04-03 00:54:52.924', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (629, 108, 0.50, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:53.128', 't', '2025-04-03 00:54:53.128', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (630, 108, 0.10, 15, 3.00, 4.00, 5.00, 6.00, 7.00, '2025-04-03 00:54:53.333', 't', '2025-04-03 00:54:53.333', 27);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (631, 109, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:55.605', 't', '2025-04-03 10:34:55.605', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (632, 109, 10.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:55.721', 't', '2025-04-03 10:34:55.721', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (633, 109, 5.00, 100, 4.00, 2.00, 3.00, 4.00, 2.00, '2025-04-03 10:34:55.835', 't', '2025-04-03 10:34:55.835', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (634, 109, 1.00, 20, 4.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:55.953', 't', '2025-04-03 10:34:55.953', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (635, 109, 0.50, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:56.065', 't', '2025-04-03 10:34:56.065', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (636, 109, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:56.179', 't', '2025-04-03 10:34:56.179', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (637, 110, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:56.292', 't', '2025-04-03 10:34:56.292', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (638, 110, 10.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:56.405', 't', '2025-04-03 10:34:56.405', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (639, 110, 5.00, 100, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:56.517', 't', '2025-04-03 10:34:56.517', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (640, 110, 1.00, 20, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:56.632', 't', '2025-04-03 10:34:56.632', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (641, 110, 0.50, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:56.746', 't', '2025-04-03 10:34:56.746', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (642, 110, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:56.861', 't', '2025-04-03 10:34:56.861', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (643, 111, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:56.974', 't', '2025-04-03 10:34:56.974', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (644, 111, 10.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:57.088', 't', '2025-04-03 10:34:57.088', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (645, 111, 5.00, 100, 1.00, 2.00, 6.00, 4.00, 5.00, '2025-04-03 10:34:57.201', 't', '2025-04-03 10:34:57.201', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (646, 111, 1.00, 20, 1.00, 2.00, 6.00, 4.00, 8.00, '2025-04-03 10:34:57.314', 't', '2025-04-03 10:34:57.314', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (647, 111, 0.50, 15, 1.00, 2.00, 6.00, 4.00, 5.00, '2025-04-03 10:34:57.428', 't', '2025-04-03 10:34:57.428', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (648, 111, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 8.00, '2025-04-03 10:34:57.541', 't', '2025-04-03 10:34:57.541', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (649, 112, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:57.655', 't', '2025-04-03 10:34:57.655', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (650, 112, 10.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:57.768', 't', '2025-04-03 10:34:57.768', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (651, 112, 5.00, 100, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:57.882', 't', '2025-04-03 10:34:57.882', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (652, 112, 1.00, 20, 1.00, 2.00, 33.00, 4.00, 5.00, '2025-04-03 10:34:57.997', 't', '2025-04-03 10:34:57.997', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (653, 112, 0.50, 15, 1.00, 2.00, 3.00, 34.00, 5.00, '2025-04-03 10:34:58.11', 't', '2025-04-03 10:34:58.11', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (654, 112, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 45.00, '2025-04-03 10:34:58.265', 't', '2025-04-03 10:34:58.265', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (655, 113, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:58.382', 't', '2025-04-03 10:34:58.382', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (656, 113, 10.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:58.495', 't', '2025-04-03 10:34:58.495', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (657, 113, 5.00, 100, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:58.608', 't', '2025-04-03 10:34:58.608', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (658, 113, 1.00, 20, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:58.72', 't', '2025-04-03 10:34:58.72', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (659, 113, 0.50, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:58.836', 't', '2025-04-03 10:34:58.836', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (660, 113, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:34:58.949', 't', '2025-04-03 10:34:58.949', 28);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (661, 114, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:01.018', 't', '2025-04-03 10:35:01.018', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (662, 114, 10.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:01.141', 't', '2025-04-03 10:35:01.141', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (663, 114, 5.00, 100, 4.00, 2.00, 3.00, 4.00, 2.00, '2025-04-03 10:35:01.259', 't', '2025-04-03 10:35:01.259', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (664, 114, 1.00, 20, 4.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:01.376', 't', '2025-04-03 10:35:01.376', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (665, 114, 0.50, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:01.525', 't', '2025-04-03 10:35:01.525', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (666, 114, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:01.643', 't', '2025-04-03 10:35:01.643', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (667, 115, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:01.76', 't', '2025-04-03 10:35:01.76', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (668, 115, 10.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:01.879', 't', '2025-04-03 10:35:01.879', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (669, 115, 5.00, 100, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:01.997', 't', '2025-04-03 10:35:01.997', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (670, 115, 1.00, 20, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:02.408', 't', '2025-04-03 10:35:02.408', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (671, 115, 0.50, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:02.533', 't', '2025-04-03 10:35:02.533', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (672, 115, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:02.651', 't', '2025-04-03 10:35:02.651', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (673, 116, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:02.768', 't', '2025-04-03 10:35:02.768', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (674, 116, 10.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:02.887', 't', '2025-04-03 10:35:02.887', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (675, 116, 5.00, 100, 1.00, 2.00, 6.00, 4.00, 5.00, '2025-04-03 10:35:03.005', 't', '2025-04-03 10:35:03.005', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (676, 116, 1.00, 20, 1.00, 2.00, 6.00, 4.00, 8.00, '2025-04-03 10:35:03.124', 't', '2025-04-03 10:35:03.124', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (677, 116, 0.50, 15, 1.00, 2.00, 6.00, 4.00, 5.00, '2025-04-03 10:35:03.242', 't', '2025-04-03 10:35:03.242', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (678, 116, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 8.00, '2025-04-03 10:35:03.36', 't', '2025-04-03 10:35:03.36', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (679, 117, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:03.483', 't', '2025-04-03 10:35:03.483', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (680, 117, 10.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:03.601', 't', '2025-04-03 10:35:03.601', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (681, 117, 5.00, 100, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:03.719', 't', '2025-04-03 10:35:03.719', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (682, 117, 1.00, 20, 1.00, 2.00, 33.00, 4.00, 5.00, '2025-04-03 10:35:03.838', 't', '2025-04-03 10:35:03.838', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (683, 117, 0.50, 15, 1.00, 2.00, 3.00, 34.00, 5.00, '2025-04-03 10:35:03.976', 't', '2025-04-03 10:35:03.976', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (684, 117, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 45.00, '2025-04-03 10:35:04.093', 't', '2025-04-03 10:35:04.093', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (685, 118, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:04.211', 't', '2025-04-03 10:35:04.211', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (686, 118, 10.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:04.329', 't', '2025-04-03 10:35:04.329', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (687, 118, 5.00, 100, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:04.448', 't', '2025-04-03 10:35:04.448', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (688, 118, 1.00, 20, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:04.565', 't', '2025-04-03 10:35:04.565', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (689, 118, 0.50, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:04.684', 't', '2025-04-03 10:35:04.684', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (690, 118, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:35:04.802', 't', '2025-04-03 10:35:04.802', 29);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (691, 119, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:23.388', 't', '2025-04-03 10:41:23.388', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (692, 119, 10.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:23.522', 't', '2025-04-03 10:41:23.522', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (693, 119, 5.00, 100, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:23.648', 't', '2025-04-03 10:41:23.648', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (694, 119, 1.00, 20, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:23.767', 't', '2025-04-03 10:41:23.767', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (695, 119, 0.50, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:23.886', 't', '2025-04-03 10:41:23.886', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (696, 119, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:24.004', 't', '2025-04-03 10:41:24.004', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (697, 120, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:24.13', 't', '2025-04-03 10:41:24.13', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (698, 120, 10.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:24.261', 't', '2025-04-03 10:41:24.261', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (699, 120, 5.00, 100, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:24.387', 't', '2025-04-03 10:41:24.387', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (700, 120, 1.00, 20, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:24.515', 't', '2025-04-03 10:41:24.515', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (701, 120, 0.50, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:24.639', 't', '2025-04-03 10:41:24.639', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (702, 120, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:24.758', 't', '2025-04-03 10:41:24.758', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (703, 121, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 8.00, '2025-04-03 10:41:24.883', 't', '2025-04-03 10:41:24.883', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (704, 121, 10.00, 200, 1.00, 5.00, 3.00, 4.00, 8.00, '2025-04-03 10:41:25.012', 't', '2025-04-03 10:41:25.012', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (705, 121, 5.00, 100, 1.00, 5.00, 3.00, 4.00, 8.00, '2025-04-03 10:41:25.134', 't', '2025-04-03 10:41:25.134', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (706, 121, 1.00, 20, 1.00, 5.00, 3.00, 4.00, 8.00, '2025-04-03 10:41:25.253', 't', '2025-04-03 10:41:25.253', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (707, 121, 0.50, 15, 1.00, 5.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:25.375', 't', '2025-04-03 10:41:25.375', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (708, 121, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:25.5', 't', '2025-04-03 10:41:25.5', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (709, 122, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:25.62', 't', '2025-04-03 10:41:25.62', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (710, 122, 10.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:25.74', 't', '2025-04-03 10:41:25.74', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (711, 122, 5.00, 100, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:25.862', 't', '2025-04-03 10:41:25.862', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (712, 122, 1.00, 20, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:25.978', 't', '2025-04-03 10:41:25.978', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (713, 122, 0.50, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:26.101', 't', '2025-04-03 10:41:26.101', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (714, 122, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:26.223', 't', '2025-04-03 10:41:26.223', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (715, 123, 25.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:26.341', 't', '2025-04-03 10:41:26.341', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (716, 123, 10.00, 200, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:26.46', 't', '2025-04-03 10:41:26.46', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (717, 123, 5.00, 100, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:26.583', 't', '2025-04-03 10:41:26.583', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (718, 123, 1.00, 20, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:26.711', 't', '2025-04-03 10:41:26.711', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (719, 123, 0.50, 15, 1.00, 2.00, 3.00, 4.00, 5.00, '2025-04-03 10:41:26.828', 't', '2025-04-03 10:41:26.828', 30);
INSERT INTO "public"."dynamic_modulus_measurement" VALUES (720, 123, 0.10, 15, 1.00, 2.00, 3.00, 4.00, 55.00, '2025-04-03 10:41:26.947', 't', '2025-04-03 10:41:26.947', 30);

-- ----------------------------
-- Table structure for dynamic_modulus_specimen
-- ----------------------------
DROP TABLE IF EXISTS "public"."dynamic_modulus_specimen";
CREATE TABLE "public"."dynamic_modulus_specimen" (
  "id" int8 NOT NULL DEFAULT nextval('dynamic_modulus_specimen_id_seq'::regclass),
  "test_id" int8,
  "specimen_number" int4,
  "diameter" numeric(38,2),
  "height" numeric(38,2),
  "bulk_density" numeric(38,2),
  "air_void_content" numeric(38,2),
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;

-- ----------------------------
-- Records of dynamic_modulus_specimen
-- ----------------------------
INSERT INTO "public"."dynamic_modulus_specimen" VALUES (24, 26, 1, 1.00, 2.00, NULL, NULL, '2025-04-03 00:54:36.232');
INSERT INTO "public"."dynamic_modulus_specimen" VALUES (25, 27, 1, 1.00, 2.00, NULL, NULL, '2025-04-03 00:54:45.754');
INSERT INTO "public"."dynamic_modulus_specimen" VALUES (26, 28, 1, 1.00, 1.00, NULL, NULL, '2025-04-03 10:34:54.676');
INSERT INTO "public"."dynamic_modulus_specimen" VALUES (27, 29, 1, 1.00, 1.00, NULL, NULL, '2025-04-03 10:35:00.053');
INSERT INTO "public"."dynamic_modulus_specimen" VALUES (28, 30, 1, 1.00, 1.00, NULL, NULL, '2025-04-03 10:41:22.434');

-- ----------------------------
-- Table structure for dynamic_modulus_temperature
-- ----------------------------
DROP TABLE IF EXISTS "public"."dynamic_modulus_temperature";
CREATE TABLE "public"."dynamic_modulus_temperature" (
  "id" int8 NOT NULL DEFAULT nextval('dynamic_modulus_temperature_id_seq'::regclass),
  "test_id" int8,
  "specimen_id" int8,
  "temperature" numeric(38,2),
  "temperature_order" int4,
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;

-- ----------------------------
-- Records of dynamic_modulus_temperature
-- ----------------------------
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (99, 26, NULL, -10.00, 1, '2025-04-03 00:54:36.847');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (100, 26, NULL, 4.40, 2, '2025-04-03 00:54:37.051');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (101, 26, NULL, 21.10, 3, '2025-04-03 00:54:37.256');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (102, 26, NULL, 37.80, 4, '2025-04-03 00:54:37.461');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (103, 26, NULL, 54.00, 5, '2025-04-03 00:54:37.667');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (104, 27, NULL, -10.00, 1, '2025-04-03 00:54:46.369');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (105, 27, NULL, 4.40, 2, '2025-04-03 00:54:46.575');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (106, 27, NULL, 21.10, 3, '2025-04-03 00:54:46.778');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (107, 27, NULL, 37.80, 4, '2025-04-03 00:54:46.985');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (108, 27, NULL, 54.00, 5, '2025-04-03 00:54:47.189');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (109, 28, NULL, -10.00, 1, '2025-04-03 10:34:55.028');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (110, 28, NULL, 4.40, 2, '2025-04-03 10:34:55.146');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (111, 28, NULL, 21.10, 3, '2025-04-03 10:34:55.26');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (112, 28, NULL, 37.80, 4, '2025-04-03 10:34:55.376');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (113, 28, NULL, 54.00, 5, '2025-04-03 10:34:55.492');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (114, 29, NULL, -10.00, 1, '2025-04-03 10:35:00.417');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (115, 29, NULL, 4.40, 2, '2025-04-03 10:35:00.545');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (116, 29, NULL, 21.10, 3, '2025-04-03 10:35:00.663');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (117, 29, NULL, 37.80, 4, '2025-04-03 10:35:00.78');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (118, 29, NULL, 54.00, 5, '2025-04-03 10:35:00.899');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (119, 30, NULL, -10.00, 1, '2025-04-03 10:41:22.79');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (120, 30, NULL, 4.40, 2, '2025-04-03 10:41:22.908');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (121, 30, NULL, 21.10, 3, '2025-04-03 10:41:23.028');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (122, 30, NULL, 37.80, 4, '2025-04-03 10:41:23.146');
INSERT INTO "public"."dynamic_modulus_temperature" VALUES (123, 30, NULL, 54.00, 5, '2025-04-03 10:41:23.271');

-- ----------------------------
-- Table structure for dynamic_modulus_test
-- ----------------------------
DROP TABLE IF EXISTS "public"."dynamic_modulus_test";
CREATE TABLE "public"."dynamic_modulus_test" (
  "id" int8 NOT NULL DEFAULT nextval('dynamic_modulus_test_id_seq'::regclass),
  "task_id" varchar(255) COLLATE "pg_catalog"."default",
  "experiment_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT '动态模量试验'::character varying,
  "mix_ratio_id" varchar(255) COLLATE "pg_catalog"."default",
  "mix_ratio_name" varchar(255) COLLATE "pg_catalog"."default",
  "mix_ratio_display_name" varchar(255) COLLATE "pg_catalog"."default",
  "equipment_id" varchar(255) COLLATE "pg_catalog"."default",
  "equipment_manufacturer" varchar(255) COLLATE "pg_catalog"."default",
  "equipment_model" varchar(255) COLLATE "pg_catalog"."default",
  "equipment_purchase_year" varchar(255) COLLATE "pg_catalog"."default",
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."dynamic_modulus_test"."id" IS '相当于其他三张表中的test_id';

-- ----------------------------
-- Records of dynamic_modulus_test
-- ----------------------------
INSERT INTO "public"."dynamic_modulus_test" VALUES (26, '52e60002-4437-45c7-99f7-29f5d3584943-0', '动态模量试验', '9', '配比1', '配比1', NULL, NULL, NULL, NULL, '2025-04-03 00:54:36.032', '2025-04-03 00:54:36.032');
INSERT INTO "public"."dynamic_modulus_test" VALUES (27, '52e60002-4437-45c7-99f7-29f5d3584943-0', '动态模量试验', '9', '配比1', '配比1', NULL, NULL, NULL, NULL, '2025-04-03 00:54:45.55', '2025-04-03 00:54:45.55');
INSERT INTO "public"."dynamic_modulus_test" VALUES (28, '830cf16b-e98b-4593-8eaf-a583e9e8226a-0', '动态模量试验', '9', '配比1', '配比1', NULL, NULL, NULL, NULL, '2025-04-03 10:34:54.561', '2025-04-03 10:34:54.561');
INSERT INTO "public"."dynamic_modulus_test" VALUES (29, '830cf16b-e98b-4593-8eaf-a583e9e8226a-0', '动态模量试验', '9', '配比1', '配比1', NULL, NULL, NULL, NULL, '2025-04-03 10:34:59.933', '2025-04-03 10:34:59.933');
INSERT INTO "public"."dynamic_modulus_test" VALUES (30, '003184cc-c8b3-4b3a-b4a0-12a804755286-0', '动态模量试验', '9', '配比1', '配比1', NULL, NULL, NULL, NULL, '2025-04-03 10:41:22.31', '2025-04-03 10:41:22.31');

-- ----------------------------
-- Table structure for dynamic_shear_rheometer_test
-- ----------------------------
DROP TABLE IF EXISTS "public"."dynamic_shear_rheometer_test";
CREATE TABLE "public"."dynamic_shear_rheometer_test" (
  "id" int8 NOT NULL DEFAULT nextval('dynamic_shear_rheometer_test_id_seq'::regclass),
  "task_id" varchar(255) COLLATE "pg_catalog"."default",
  "operator_id" varchar(255) COLLATE "pg_catalog"."default",
  "control_mode" varchar(255) COLLATE "pg_catalog"."default",
  "plate_gap" float8,
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "experiment_values" text COLLATE "pg_catalog"."default",
  "material_type" varchar(255) COLLATE "pg_catalog"."default",
  "remarks" varchar(1000) COLLATE "pg_catalog"."default",
  "specimen_id" varchar(255) COLLATE "pg_catalog"."default",
  "specimen_type" varchar(255) COLLATE "pg_catalog"."default",
  "test_radius" float8
)
;

-- ----------------------------
-- Records of dynamic_shear_rheometer_test
-- ----------------------------
INSERT INTO "public"."dynamic_shear_rheometer_test" VALUES (8, '645c3a5f-2dec-4680-bcf9-e7a461ad0c60', '啊啊啊', '14', 13, '2025-04-03 12:11:02.458414', '2025-04-03 12:11:02.458414', '{"asphalt_supplier":"供应商1","complex_modulus_1":"71.4","phase_angle_1":"9","asphalt_grade":"ymmm1","plate_radius":"12","control_mode":"14","max_shear_strain_1":"7","frequency_1":"3","asphalt_catalog":"NORMAL","asphalt_id":"3","plate_gap":"13","temperature_1":"1","max_shear_stress_1":"5"}', '', '', '', '', NULL);
INSERT INTO "public"."dynamic_shear_rheometer_test" VALUES (9, '645c3a5f-2dec-4680-bcf9-e7a461ad0c60', '啊啊啊', '14', 13, '2025-04-03 12:41:12.439247', '2025-04-03 12:41:12.439247', '{"asphalt_supplier":"供应商1","complex_modulus_1":"91.7","phase_angle_1":"13","asphalt_grade":"ymmm1","plate_radius":"12","control_mode":"14","max_shear_strain_1":"12","frequency_1":"654321","asphalt_catalog":"NORMAL","asphalt_id":"3","plate_gap":"13","temperature_1":"123456","max_shear_stress_1":"11"}', '', '', '', '', NULL);
INSERT INTO "public"."dynamic_shear_rheometer_test" VALUES (10, '31931f37-4231-4a5e-bb8a-bf312e3414a1', '啊啊啊', '25', 20, '2025-04-03 13:39:49.904483', '2025-04-03 13:39:49.904483', '{"asphalt_supplier":"供应商1","test_radius":"15","complex_modulus_1":"80.0","phase_angle_1":"6","asphalt_grade":"ymmm1","control_mode":"25","max_shear_strain_1":"5","frequency_1":"2","asphalt_catalog":"NORMAL","asphalt_id":"3","plate_gap":"20","temperature_1":"1","max_shear_stress_1":"4"}', '', '', '', '', 15);

-- ----------------------------
-- Table structure for hamburg_rutting_test
-- ----------------------------
DROP TABLE IF EXISTS "public"."hamburg_rutting_test";
CREATE TABLE "public"."hamburg_rutting_test" (
  "id" int8 NOT NULL DEFAULT nextval('hamburg_rutting_test_id_seq'::regclass),
  "steady_slope1" float4,
  "steady_curvilinear1" float4,
  "steady_slope2" float4,
  "steady_curvilinear2" float4,
  "task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "mix_ratio_id" int8 NOT NULL,
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "created_at" timestamp(6),
  "updated_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."hamburg_rutting_test"."steady_slope1" IS '第一稳态曲线斜率';
COMMENT ON COLUMN "public"."hamburg_rutting_test"."steady_curvilinear1" IS '第一稳态曲线截距';
COMMENT ON COLUMN "public"."hamburg_rutting_test"."steady_slope2" IS '第二稳态曲线斜率';
COMMENT ON COLUMN "public"."hamburg_rutting_test"."steady_curvilinear2" IS '第二稳态曲线截距';
COMMENT ON COLUMN "public"."hamburg_rutting_test"."task_id" IS '任务ID';
COMMENT ON COLUMN "public"."hamburg_rutting_test"."mix_ratio_id" IS '配比ID';
COMMENT ON TABLE "public"."hamburg_rutting_test" IS '汉堡车辙实验数据表';

-- ----------------------------
-- Records of hamburg_rutting_test
-- ----------------------------

-- ----------------------------
-- Table structure for marshall_test
-- ----------------------------
DROP TABLE IF EXISTS "public"."marshall_test";
CREATE TABLE "public"."marshall_test" (
  "id" int8 NOT NULL DEFAULT nextval('marshall_test_id_seq'::regclass),
  "task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "stability_1" float4,
  "stream_value1" float4,
  "stability_2" float4,
  "stream_value2" float4,
  "stability_3" float4,
  "stream_value3" float4,
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "public"."marshall_test"."id" IS '主键ID';
COMMENT ON COLUMN "public"."marshall_test"."task_id" IS '关联的混合料任务ID';
COMMENT ON COLUMN "public"."marshall_test"."stability_1" IS '第一次马歇尔稳定度(KN)';
COMMENT ON COLUMN "public"."marshall_test"."stream_value1" IS '第一次流值(mm)';
COMMENT ON COLUMN "public"."marshall_test"."stability_2" IS '第二次马歇尔稳定度(KN)';
COMMENT ON COLUMN "public"."marshall_test"."stream_value2" IS '第二次流值(mm)';
COMMENT ON COLUMN "public"."marshall_test"."stability_3" IS '第三次马歇尔稳定度(KN)';
COMMENT ON COLUMN "public"."marshall_test"."stream_value3" IS '第三次流值(mm)';
COMMENT ON COLUMN "public"."marshall_test"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."marshall_test"."updated_at" IS '更新时间';
COMMENT ON TABLE "public"."marshall_test" IS '马歇尔试验数据表';

-- ----------------------------
-- Records of marshall_test
-- ----------------------------
INSERT INTO "public"."marshall_test" VALUES (30, '16bddc71-9e8b-4df3-8cfc-378b116ced57-1', 12, 15, 13, 16, 14, 17, '2025-04-03 14:10:11.446495', '2025-04-03 14:10:11.446495');

-- ----------------------------
-- Table structure for mixratio
-- ----------------------------
DROP TABLE IF EXISTS "public"."mixratio";
CREATE TABLE "public"."mixratio" (
  "id" int8 NOT NULL DEFAULT nextval('mixratio_id_seq'::regclass),
  "mix_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "mix_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "mix_company" varchar(255) COLLATE "pg_catalog"."default",
  "created_by" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of mixratio
-- ----------------------------
INSERT INTO "public"."mixratio" VALUES (7, '查干湖', 'phb20250314002', '2025-03-14 18:07:52.53687', NULL, NULL);
INSERT INTO "public"."mixratio" VALUES (8, '古人', 'phb20250314003', '2025-03-14 18:13:29.992178', '2259397646222444157', '6');
INSERT INTO "public"."mixratio" VALUES (9, '配比1', 'phb20250331001', '2025-03-31 11:27:52.806442', '1329150149859754791', '23');

-- ----------------------------
-- Table structure for mixratio_asphalt
-- ----------------------------
DROP TABLE IF EXISTS "public"."mixratio_asphalt";
CREATE TABLE "public"."mixratio_asphalt" (
  "id" int8 NOT NULL DEFAULT nextval('mixratio_asphalt_id_seq'::regclass),
  "mixratio_id" int8 NOT NULL,
  "asphalt_id" int8 NOT NULL,
  "percentage" numeric(5,2) NOT NULL
)
;

-- ----------------------------
-- Records of mixratio_asphalt
-- ----------------------------
INSERT INTO "public"."mixratio_asphalt" VALUES (3, 7, 6, 29.90);
INSERT INTO "public"."mixratio_asphalt" VALUES (4, 8, 6, 22.00);
INSERT INTO "public"."mixratio_asphalt" VALUES (5, 9, 8, 33.12);

-- ----------------------------
-- Table structure for mixratio_sand
-- ----------------------------
DROP TABLE IF EXISTS "public"."mixratio_sand";
CREATE TABLE "public"."mixratio_sand" (
  "id" int8 NOT NULL DEFAULT nextval('mixratio_sand_id_seq'::regclass),
  "mixratio_id" int8 NOT NULL,
  "sand_id" int8 NOT NULL,
  "gradation" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "percentage" numeric(5,2) NOT NULL
)
;

-- ----------------------------
-- Records of mixratio_sand
-- ----------------------------
INSERT INTO "public"."mixratio_sand" VALUES (3, 7, 2, '4.75mm', 30.00);
INSERT INTO "public"."mixratio_sand" VALUES (4, 8, 2, '4.75mm', 33.00);
INSERT INTO "public"."mixratio_sand" VALUES (5, 9, 4, '4.75mm', 31.15);

-- ----------------------------
-- Table structure for mixratio_stone
-- ----------------------------
DROP TABLE IF EXISTS "public"."mixratio_stone";
CREATE TABLE "public"."mixratio_stone" (
  "id" int8 NOT NULL DEFAULT nextval('mixratio_stone_id_seq'::regclass),
  "mixratio_id" int8 NOT NULL,
  "stone_id" int8 NOT NULL,
  "gradation" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "percentage" numeric(5,2) NOT NULL
)
;

-- ----------------------------
-- Records of mixratio_stone
-- ----------------------------
INSERT INTO "public"."mixratio_stone" VALUES (3, 7, 2, '4.75mm', 40.10);
INSERT INTO "public"."mixratio_stone" VALUES (4, 8, 2, '4.75mm', 45.00);
INSERT INTO "public"."mixratio_stone" VALUES (5, 9, 4, '4.75mm', 35.73);

-- ----------------------------
-- Table structure for mixture_four_point_bending_result
-- ----------------------------
DROP TABLE IF EXISTS "public"."mixture_four_point_bending_result";
CREATE TABLE "public"."mixture_four_point_bending_result" (
  "id" int8 NOT NULL DEFAULT nextval('mixture_four_point_bending_result_id_seq'::regclass),
  "specimen_id" int8,
  "result_type" varchar(50) COLLATE "pg_catalog"."default",
  "result_type_display_name" varchar(100) COLLATE "pg_catalog"."default",
  "result_type_english_name" varchar(100) COLLATE "pg_catalog"."default",
  "result_type_unit" varchar(50) COLLATE "pg_catalog"."default",
  "result_index" int4,
  "initial_value" numeric(15,6),
  "current_value" numeric(15,6),
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;

-- ----------------------------
-- Records of mixture_four_point_bending_result
-- ----------------------------
INSERT INTO "public"."mixture_four_point_bending_result" VALUES (109, 22, '1', '最大拉应力', 'Tensile stress', 'kPa', NULL, 8.000000, 9.000000, '2025-04-03 00:59:16.599', '2025-04-03 00:59:16.906741');
INSERT INTO "public"."mixture_four_point_bending_result" VALUES (110, 22, '2', '最大拉应变', 'Tensile strain', 'με', NULL, 8.000000, 9.000000, '2025-04-03 00:59:17.008', '2025-04-03 00:59:17.315705');
INSERT INTO "public"."mixture_four_point_bending_result" VALUES (111, 22, '3', '弯曲劲度模量', 'Flexural stiffness', 'MPa', NULL, 8.000000, 9.000000, '2025-04-03 00:59:17.417', '2025-04-03 00:59:17.725575');
INSERT INTO "public"."mixture_four_point_bending_result" VALUES (112, 22, '4', '相位角', 'Phase Angle', 'deg', NULL, 8.000000, 9.000000, '2025-04-03 00:59:17.827', '2025-04-03 00:59:18.134918');
INSERT INTO "public"."mixture_four_point_bending_result" VALUES (113, 22, '5', '单个循环耗散能', 'Dissipated energy', 'J/m³', NULL, 8.000000, 9.000000, '2025-04-03 00:59:18.237', '2025-04-03 00:59:18.544715');
INSERT INTO "public"."mixture_four_point_bending_result" VALUES (114, 22, '6', '累积耗散能', 'Cumulative dissipated energy', 'kJ/m³', NULL, 8.000000, 9.000000, '2025-04-03 00:59:18.646', '2025-04-03 00:59:18.954422');

-- ----------------------------
-- Table structure for mixture_four_point_bending_specimen
-- ----------------------------
DROP TABLE IF EXISTS "public"."mixture_four_point_bending_specimen";
CREATE TABLE "public"."mixture_four_point_bending_specimen" (
  "id" int8 NOT NULL DEFAULT nextval('mixture_four_point_bending_specimen_id_seq'::regclass),
  "test_id" int8,
  "specimen_number" int4,
  "length_mm" numeric(10,2),
  "width_mm" numeric(10,2),
  "height_mm" numeric(10,2),
  "span_mm" numeric(10,2),
  "strain_range" numeric(10,2),
  "frequency_hz" numeric(10,2),
  "test_temperature" numeric(10,2),
  "fatigue_life" int4,
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;

-- ----------------------------
-- Records of mixture_four_point_bending_specimen
-- ----------------------------
INSERT INTO "public"."mixture_four_point_bending_specimen" VALUES (22, 23, 1, 1.00, 2.00, 3.00, 4.00, 5.00, 6.00, 7.00, 88, '2025-04-03 00:59:16.394', '2025-04-03 00:59:16.496701');

-- ----------------------------
-- Table structure for mixture_four_point_bending_test
-- ----------------------------
DROP TABLE IF EXISTS "public"."mixture_four_point_bending_test";
CREATE TABLE "public"."mixture_four_point_bending_test" (
  "id" int8 NOT NULL DEFAULT nextval('mixture_four_point_bending_test_id_seq'::regclass),
  "task_id" varchar(255) COLLATE "pg_catalog"."default",
  "mix_ratio_id" varchar(255) COLLATE "pg_catalog"."default",
  "mix_ratio_name" varchar(255) COLLATE "pg_catalog"."default",
  "experiment_name" varchar(255) COLLATE "pg_catalog"."default",
  "mix_temperature" numeric(10,2),
  "mix_speed" numeric(10,2),
  "mix_time" numeric(10,2),
  "compaction_method" varchar(100) COLLATE "pg_catalog"."default",
  "test_date" timestamp(6),
  "operator" varchar(100) COLLATE "pg_catalog"."default",
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;

-- ----------------------------
-- Records of mixture_four_point_bending_test
-- ----------------------------
INSERT INTO "public"."mixture_four_point_bending_test" VALUES (23, '52e60002-4437-45c7-99f7-29f5d3584943-0', '9', '全任务', NULL, 0.00, 0.00, 0.00, '', '2025-04-03 00:59:15.986', '未知', '2025-04-03 00:59:15.988', '2025-04-03 00:59:16.292488');

-- ----------------------------
-- Table structure for mixture_splitting_test
-- ----------------------------
DROP TABLE IF EXISTS "public"."mixture_splitting_test";
CREATE TABLE "public"."mixture_splitting_test" (
  "test_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "task_id" varchar(255) COLLATE "pg_catalog"."default",
  "mix_ratio_id" varchar(255) COLLATE "pg_catalog"."default",
  "test_temperature" numeric(6,2),
  "test_time" timestamp(6),
  "operator" varchar(50) COLLATE "pg_catalog"."default",
  "test_equipment" varchar(100) COLLATE "pg_catalog"."default",
  "test_method" varchar(100) COLLATE "pg_catalog"."default",
  "test_standard" varchar(100) COLLATE "pg_catalog"."default",
  "remarks" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;

-- ----------------------------
-- Records of mixture_splitting_test
-- ----------------------------
INSERT INTO "public"."mixture_splitting_test" VALUES ('bee18545-4287-4284-8e3d-ed0a78e3b98d', '52e60002-4437-45c7-99f7-29f5d3584943-0', '9', NULL, '2025-04-03 01:02:15.693285', NULL, '', '', '', NULL, '2025-04-03 01:02:15.693285', '2025-04-03 01:02:15.693285');

-- ----------------------------
-- Table structure for mixture_splitting_test_specimen
-- ----------------------------
DROP TABLE IF EXISTS "public"."mixture_splitting_test_specimen";
CREATE TABLE "public"."mixture_splitting_test_specimen" (
  "specimen_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "test_id" varchar(255) COLLATE "pg_catalog"."default",
  "specimen_number" int4,
  "diameter" numeric(6,2),
  "height" numeric(6,2),
  "p1_value" numeric(10,4),
  "p2_value" numeric(10,4),
  "p3_value" numeric(10,4),
  "p_average" numeric(10,4),
  "x1_value" numeric(10,4),
  "x2_value" numeric(10,4),
  "x3_value" numeric(10,4),
  "x_average" numeric(10,4),
  "poisson_ratio" numeric(6,4),
  "tensile_strength" numeric(10,4),
  "failure_strain" numeric(10,6),
  "stiffness_modulus" numeric(10,4)
)
;

-- ----------------------------
-- Records of mixture_splitting_test_specimen
-- ----------------------------
INSERT INTO "public"."mixture_splitting_test_specimen" VALUES ('5b1fed04-2706-467a-b0b4-65c66e09512f', 'bee18545-4287-4284-8e3d-ed0a78e3b98d', 1, 1.00, 2.00, 3.0000, 4.0000, 5.0000, 4.0000, 6.0000, 7.0000, 8.0000, 0.0000, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for mixture_task
-- ----------------------------
DROP TABLE IF EXISTS "public"."mixture_task";
CREATE TABLE "public"."mixture_task" (
  "id" int8 NOT NULL DEFAULT nextval('mixture_task_id_seq'::regclass),
  "task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "task_company" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "est_by" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "project_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "mixratio_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "specimen_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "task_assignment" varchar(255) COLLATE "pg_catalog"."default",
  "remarks" varchar(255) COLLATE "pg_catalog"."default",
  "creation_time" int8 NOT NULL,
  "status" varchar(20) COLLATE "pg_catalog"."default" DEFAULT 'CREATED'::character varying,
  "task_name" varchar(255) COLLATE "pg_catalog"."default",
  "due_date" varchar(255) COLLATE "pg_catalog"."default",
  "acceptor" varchar(255) COLLATE "pg_catalog"."default",
  "accept_time" int8,
  "prepare_status" varchar(255) COLLATE "pg_catalog"."default",
  "making_status" varchar(255) COLLATE "pg_catalog"."default",
  "testing_status" varchar(255) COLLATE "pg_catalog"."default",
  "assigned_mixing_equipment" varchar(255) COLLATE "pg_catalog"."default",
  "assigned_forming_equipment" varchar(255) COLLATE "pg_catalog"."default",
  "assigned_testing_equipment" varchar(255) COLLATE "pg_catalog"."default",
  "mixing_equipment_manufacturer" varchar(255) COLLATE "pg_catalog"."default",
  "forming_equipment_manufacturer" varchar(255) COLLATE "pg_catalog"."default",
  "testing_equipment_manufacturer" varchar(255) COLLATE "pg_catalog"."default",
  "task_type" varchar(255) COLLATE "pg_catalog"."default",
  "task_names" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."mixture_task"."task_id" IS '任务ID，可以用UUID生成或其他方式';
COMMENT ON COLUMN "public"."mixture_task"."task_company" IS '制定任务用户的单位ID';
COMMENT ON COLUMN "public"."mixture_task"."est_by" IS '制定任务用户的ID';
COMMENT ON COLUMN "public"."mixture_task"."project_id" IS '选择的项目ID';
COMMENT ON COLUMN "public"."mixture_task"."mixratio_id" IS '选择的配合比ID';
COMMENT ON COLUMN "public"."mixture_task"."specimen_id" IS '制件方式ID';
COMMENT ON COLUMN "public"."mixture_task"."task_assignment" IS '所指派的任务对应的ID';
COMMENT ON COLUMN "public"."mixture_task"."prepare_status" IS '该任务的准备状态';
COMMENT ON COLUMN "public"."mixture_task"."making_status" IS '该任务的制件状态';
COMMENT ON COLUMN "public"."mixture_task"."testing_status" IS '该任务的实验完成状态';
COMMENT ON TABLE "public"."mixture_task" IS '混合任务表，记录用户流程中选择的项目、配比、制件方式等信息';

-- ----------------------------
-- Records of mixture_task
-- ----------------------------
INSERT INTO "public"."mixture_task" VALUES (374, '52e60002-4437-45c7-99f7-29f5d3584943-3', '1329150149859754791', '23', '20250330132915014985975479141917301', '9', '19', '动态模量试验', '', 1743612672254, 'COMPLETE', '全任务', '2025-03-30', '啊啊啊', 1743612709291, 'finished', 'finished', 'finished', '20-0160-60', '20-1500', NULL, 'infratest', 'infratest', NULL, NULL, NULL);
INSERT INTO "public"."mixture_task" VALUES (375, '52e60002-4437-45c7-99f7-29f5d3584943-4', '1329150149859754791', '23', '20250330132915014985975479141917301', '9', '19', '沥青混合料直接拉伸循环疲劳测黏弹损伤试验', '', 1743612672664, 'COMPLETE', '全任务', '2025-03-30', '啊啊啊', 1743612709291, 'finished', 'finished', 'finished', '20-0160-60', '20-1500', NULL, 'infratest', 'infratest', NULL, NULL, NULL);
INSERT INTO "public"."mixture_task" VALUES (376, '52e60002-4437-45c7-99f7-29f5d3584943-5', '1329150149859754791', '23', '20250330132915014985975479141917301', '9', '19', '沥青混合料四点弯曲疲劳寿命试验', '', 1743612673075, 'COMPLETE', '全任务', '2025-03-30', '啊啊啊', 1743612709291, 'finished', 'finished', 'finished', '20-0160-60', '20-1500', NULL, 'infratest', 'infratest', NULL, NULL, NULL);
INSERT INTO "public"."mixture_task" VALUES (377, '52e60002-4437-45c7-99f7-29f5d3584943-6', '1329150149859754791', '23', '20250330132915014985975479141917301', '9', '19', '沥青混合料单轴压缩试验(圆柱体法)', '', 1743612673483, 'COMPLETE', '全任务', '2025-03-30', '啊啊啊', 1743612709291, 'finished', 'finished', 'finished', '20-0160-60', '20-1500', NULL, 'infratest', 'infratest', NULL, NULL, NULL);
INSERT INTO "public"."mixture_task" VALUES (378, '52e60002-4437-45c7-99f7-29f5d3584943-7', '1329150149859754791', '23', '20250330132915014985975479141917301', '9', '19', '沥青混合料劈裂试验', '', 1743612673892, 'COMPLETE', '全任务', '2025-03-30', '啊啊啊', 1743612709291, 'finished', 'finished', 'finished', '20-0160-60', '20-1500', NULL, 'infratest', 'infratest', NULL, NULL, NULL);
INSERT INTO "public"."mixture_task" VALUES (371, '52e60002-4437-45c7-99f7-29f5d3584943-0', '1329150149859754791', '23', '20250330132915014985975479141917301', '9', '19', '马歇尔稳定度试验', '', 1743612671027, 'ONGOING', '全任务', '2025-03-30', '啊啊啊', 1743612709291, 'finished', 'finished', 'unfinished', '20-0160-60', '20-1500', NULL, 'infratest', 'infratest', NULL, NULL, NULL);
INSERT INTO "public"."mixture_task" VALUES (372, '52e60002-4437-45c7-99f7-29f5d3584943-1', '1329150149859754791', '23', '20250330132915014985975479141917301', '9', '19', '沥青混合料车辙实验（汉堡车辙）', '', 1743612671435, 'ONGOING', '全任务', '2025-03-30', '啊啊啊', 1743612709291, 'finished', 'finished', 'unfinished', '20-0160-60', '20-1500', NULL, 'infratest', 'infratest', NULL, NULL, NULL);
INSERT INTO "public"."mixture_task" VALUES (373, '52e60002-4437-45c7-99f7-29f5d3584943-2', '1329150149859754791', '23', '20250330132915014985975479141917301', '9', '19', '沥青混合料弯曲试验', '', 1743612671846, 'COMPLETE', '全任务', '2025-03-30', '啊啊啊', 1743612709291, 'finished', 'finished', 'finished', '20-0160-60', '20-1500', NULL, 'infratest', 'infratest', NULL, NULL, NULL);
INSERT INTO "public"."mixture_task" VALUES (381, '830cf16b-e98b-4593-8eaf-a583e9e8226a-0', '1329150149859754791', '23', '20250330132915014985975479141917301', '9', '19', '动态模量试验', '', 1743647486381, 'COMPLETE', '动态模量', '2025-03-30', '啊啊啊', 1743647496955, 'unfinished', 'unfinished', 'finished', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO "public"."mixture_task" VALUES (382, '003184cc-c8b3-4b3a-b4a0-12a804755286-0', '1329150149859754791', '23', '20250330132915014985975479141917301', '9', '19', '动态模量试验', '', 1743647978021, 'COMPLETE', 'dm修', '2025-03-30', '啊啊啊', 1743647988455, 'unfinished', 'unfinished', 'finished', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO "public"."mixture_task" VALUES (379, '62eb46b1-2fc9-40c5-a2ef-ad00901d4450-0', '1329150149859754791', '23', '20250330132915014985975479141917301', '9', '19', '沥青混合料弯曲试验', '', 1743613540694, 'COMPLETE', '弯曲', '2025-03-30', '啊啊啊', 1743613596091, 'unfinished', 'unfinished', 'finished', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO "public"."mixture_task" VALUES (380, '16bddc71-9e8b-4df3-8cfc-378b116ced57-0', '1329150149859754791', '23', '20250330132915014985975479141917301', '9', '19', '马歇尔稳定度试验', '', 1743615499571, 'COMPLETE', '任务', '2025-03-30', '啊啊啊', 1743659513479, 'unfinished', 'unfinished', 'finished', NULL, NULL, '76-B3002', NULL, NULL, 'Controls', NULL, NULL);

-- ----------------------------
-- Table structure for mixture_uniaxial_compression_p_values
-- ----------------------------
DROP TABLE IF EXISTS "public"."mixture_uniaxial_compression_p_values";
CREATE TABLE "public"."mixture_uniaxial_compression_p_values" (
  "id" int4 NOT NULL DEFAULT nextval('mixture_uniaxial_compression_p_values_id_seq'::regclass),
  "specimen_id" varchar(255) COLLATE "pg_catalog"."default",
  "p_index" int4,
  "p_value" numeric(10,3),
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;

-- ----------------------------
-- Records of mixture_uniaxial_compression_p_values
-- ----------------------------
INSERT INTO "public"."mixture_uniaxial_compression_p_values" VALUES (68, '6357c614-f57d-42ad-8cb7-f7092ebfe2d0', 1, 4.000, '2025-04-03 01:01:33.300103', '2025-04-03 01:01:33.300103');
INSERT INTO "public"."mixture_uniaxial_compression_p_values" VALUES (69, '6357c614-f57d-42ad-8cb7-f7092ebfe2d0', 2, 5.000, '2025-04-03 01:01:33.505222', '2025-04-03 01:01:33.505222');
INSERT INTO "public"."mixture_uniaxial_compression_p_values" VALUES (70, '6357c614-f57d-42ad-8cb7-f7092ebfe2d0', 3, 6.000, '2025-04-03 01:01:33.71046', '2025-04-03 01:01:33.71046');

-- ----------------------------
-- Table structure for mixture_uniaxial_compression_specimen
-- ----------------------------
DROP TABLE IF EXISTS "public"."mixture_uniaxial_compression_specimen";
CREATE TABLE "public"."mixture_uniaxial_compression_specimen" (
  "id" int8 NOT NULL DEFAULT nextval('mixture_uniaxial_compression_specimen_id_seq'::regclass),
  "test_id" varchar(255) COLLATE "pg_catalog"."default",
  "specimen_number" varchar(50) COLLATE "pg_catalog"."default",
  "diameter" numeric(10,2),
  "height" numeric(10,2),
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "specimen_id" varchar(255) COLLATE "pg_catalog"."default",
  "updated_at" timestamp(6)
)
;
COMMENT ON TABLE "public"."mixture_uniaxial_compression_specimen" IS '沥青混合料单轴压缩试验试件数据表';

-- ----------------------------
-- Records of mixture_uniaxial_compression_specimen
-- ----------------------------
INSERT INTO "public"."mixture_uniaxial_compression_specimen" VALUES (35, 'e08af260-b1f3-41eb-9fe3-ad6a8567fe7b', '1', 2.00, 3.00, '2025-04-03 01:01:33.096515', '6357c614-f57d-42ad-8cb7-f7092ebfe2d0', '2025-04-03 01:01:33.096515');

-- ----------------------------
-- Table structure for mixture_uniaxial_compression_test
-- ----------------------------
DROP TABLE IF EXISTS "public"."mixture_uniaxial_compression_test";
CREATE TABLE "public"."mixture_uniaxial_compression_test" (
  "test_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT nextval('mixture_uniaxial_compression_test_id_seq'::regclass),
  "task_id" varchar(255) COLLATE "pg_catalog"."default",
  "mix_ratio_id" varchar(255) COLLATE "pg_catalog"."default",
  "mix_ratio_name" varchar(100) COLLATE "pg_catalog"."default",
  "compaction_method" varchar(100) COLLATE "pg_catalog"."default",
  "mixing_temperature" numeric(5,2),
  "mixing_speed" int4,
  "mixing_time" int4,
  "test_temperature" numeric(5,2),
  "test_date" timestamp(6),
  "average_force" numeric(10,3),
  "device_type" varchar(100) COLLATE "pg_catalog"."default",
  "device_model" varchar(100) COLLATE "pg_catalog"."default",
  "device_manufacturer" varchar(100) COLLATE "pg_catalog"."default",
  "remarks" text COLLATE "pg_catalog"."default",
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "public"."mixture_uniaxial_compression_test" IS '沥青混合料单轴压缩试验基本信息表';

-- ----------------------------
-- Records of mixture_uniaxial_compression_test
-- ----------------------------
INSERT INTO "public"."mixture_uniaxial_compression_test" VALUES ('e08af260-b1f3-41eb-9fe3-ad6a8567fe7b', '52e60002-4437-45c7-99f7-29f5d3584943-0', '9', NULL, NULL, NULL, NULL, NULL, 1.00, NULL, NULL, NULL, NULL, NULL, NULL, '2025-04-03 01:01:32.891456', '2025-04-03 01:01:32.891456');

-- ----------------------------
-- Table structure for mixture_uniaxial_compression_uts028_data
-- ----------------------------
DROP TABLE IF EXISTS "public"."mixture_uniaxial_compression_uts028_data";
CREATE TABLE "public"."mixture_uniaxial_compression_uts028_data" (
  "id" int8 NOT NULL DEFAULT nextval('mixture_uniaxial_compression_uts028_data_id_seq'::regclass),
  "test_id" int8,
  "specimen_id" varchar(255) COLLATE "pg_catalog"."default",
  "max_force" numeric(10,3),
  "min_force" numeric(10,3),
  "work_ratio" numeric(10,3),
  "displacement" numeric(10,3),
  "strain" numeric(10,3),
  "rebound_modulus" numeric(10,3),
  "temperature" numeric(5,2),
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6),
  "pressure_level" varchar(10) COLLATE "pg_catalog"."default"
)
;
COMMENT ON TABLE "public"."mixture_uniaxial_compression_uts028_data" IS '沥青混合料单轴压缩试验UTS028程序数据表';

-- ----------------------------
-- Records of mixture_uniaxial_compression_uts028_data
-- ----------------------------
INSERT INTO "public"."mixture_uniaxial_compression_uts028_data" VALUES (115, NULL, '6357c614-f57d-42ad-8cb7-f7092ebfe2d0', 7.000, 7.000, 7.000, 7.000, 7.000, 7.000, 7.00, '2025-04-03 01:01:33.915693', '2025-04-03 01:01:33.915693', '0.1P');
INSERT INTO "public"."mixture_uniaxial_compression_uts028_data" VALUES (116, NULL, '6357c614-f57d-42ad-8cb7-f7092ebfe2d0', 8.000, 8.000, 8.000, 8.000, 8.000, 8.000, 8.00, '2025-04-03 01:01:34.119917', '2025-04-03 01:01:34.119917', '0.2P');
INSERT INTO "public"."mixture_uniaxial_compression_uts028_data" VALUES (117, NULL, '6357c614-f57d-42ad-8cb7-f7092ebfe2d0', 9.000, 9.000, 9.000, 9.000, 9.000, 9.000, 9.00, '2025-04-03 01:01:34.324228', '2025-04-03 01:01:34.324228', '0.3P');
INSERT INTO "public"."mixture_uniaxial_compression_uts028_data" VALUES (118, NULL, '6357c614-f57d-42ad-8cb7-f7092ebfe2d0', 10.000, 10.000, 10.000, 10.000, 10.000, 10.000, 10.00, '2025-04-03 01:01:34.529966', '2025-04-03 01:01:34.529966', '0.4P');
INSERT INTO "public"."mixture_uniaxial_compression_uts028_data" VALUES (119, NULL, '6357c614-f57d-42ad-8cb7-f7092ebfe2d0', 11.000, 11.000, 11.000, 11.000, 11.000, 11.000, 11.00, '2025-04-03 01:01:34.735628', '2025-04-03 01:01:34.735628', '0.5P');
INSERT INTO "public"."mixture_uniaxial_compression_uts028_data" VALUES (120, NULL, '6357c614-f57d-42ad-8cb7-f7092ebfe2d0', 12.000, 12.000, 12.000, 12.000, 12.000, 12.000, 12.00, '2025-04-03 01:01:34.938773', '2025-04-03 01:01:34.938773', '0.6P');
INSERT INTO "public"."mixture_uniaxial_compression_uts028_data" VALUES (121, NULL, '6357c614-f57d-42ad-8cb7-f7092ebfe2d0', 13.000, 13.000, 13.000, 13.000, 13.000, 13.000, 13.00, '2025-04-03 01:01:35.144257', '2025-04-03 01:01:35.144257', '0.7P');

-- ----------------------------
-- Table structure for molding_methods
-- ----------------------------
DROP TABLE IF EXISTS "public"."molding_methods";
CREATE TABLE "public"."molding_methods" (
  "id" int8 NOT NULL DEFAULT nextval('molding_methods_id_seq'::regclass),
  "created_at" timestamptz(6) NOT NULL,
  "updated_at" timestamptz(6) NOT NULL,
  "version" int8,
  "created_by" varchar(255) COLLATE "pg_catalog"."default",
  "updated_by" varchar(255) COLLATE "pg_catalog"."default",
  "description" varchar(500) COLLATE "pg_catalog"."default",
  "is_public" bool NOT NULL,
  "mix_ratio_id" int8,
  "name" varchar(100) COLLATE "pg_catalog"."default",
  "organization_id" int8
)
;

-- ----------------------------
-- Records of molding_methods
-- ----------------------------
INSERT INTO "public"."molding_methods" VALUES (1, '2025-03-15 12:10:34.050709+08', '2025-03-15 12:10:34.050709+08', 0, 'system', 'system', '11的特殊制件方法', 'f', NULL, '11专用击实法', 5371211465312940040);
INSERT INTO "public"."molding_methods" VALUES (2, '2025-03-15 12:10:34.343481+08', '2025-03-15 12:10:34.343481+08', 0, 'system', 'system', '单位3的特殊制件方法', 'f', NULL, '单位3专用击实法', 2259397646222444157);
INSERT INTO "public"."molding_methods" VALUES (3, '2025-03-15 12:10:34.600502+08', '2025-03-15 12:10:34.600502+08', 0, 'system', 'system', '单位3的特殊制件方法', 'f', NULL, '单位3专用击实法', 2259397646222444157);
INSERT INTO "public"."molding_methods" VALUES (4, '2025-03-15 12:10:34.852115+08', '2025-03-15 12:10:34.852115+08', 0, 'system', 'system', '11的特殊制件方法', 'f', NULL, '11专用击实法', 5371211465312940040);
INSERT INTO "public"."molding_methods" VALUES (5, '2025-03-15 12:10:35.100806+08', '2025-03-15 12:10:35.100806+08', 0, 'system', 'system', '使用马歇尔击实仪进行击实成型', 't', NULL, '马歇尔击实法', NULL);
INSERT INTO "public"."molding_methods" VALUES (6, '2025-03-15 12:10:35.352885+08', '2025-03-15 12:10:35.352885+08', 0, 'system', 'system', '使用旋转压实仪进行旋转压实成型', 't', NULL, '旋转压实法', NULL);

-- ----------------------------
-- Table structure for penetration_tests
-- ----------------------------
DROP TABLE IF EXISTS "public"."penetration_tests";
CREATE TABLE "public"."penetration_tests" (
  "id" int8 NOT NULL DEFAULT nextval('penetration_tests_id_seq'::regclass),
  "created_at" timestamp(6),
  "device_id" varchar(255) COLLATE "pg_catalog"."default",
  "device_manufacturer" varchar(255) COLLATE "pg_catalog"."default",
  "device_model" varchar(255) COLLATE "pg_catalog"."default",
  "device_name" varchar(255) COLLATE "pg_catalog"."default",
  "experimenter" varchar(255) COLLATE "pg_catalog"."default",
  "reading" varchar(255) COLLATE "pg_catalog"."default",
  "task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "temperature" varchar(255) COLLATE "pg_catalog"."default",
  "test_date" timestamp(6),
  "updated_at" timestamp(6),
  "asphalt_task_assignment_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Records of penetration_tests
-- ----------------------------

-- ----------------------------
-- Table structure for projects
-- ----------------------------
DROP TABLE IF EXISTS "public"."projects";
CREATE TABLE "public"."projects" (
  "project_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT nextval('projects_project_id_seq'::regclass),
  "project_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "project_company" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "project_est_time" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "project_due" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "id" int8 NOT NULL DEFAULT nextval('projects_id_seq'::regclass),
  "created_at" timestamptz(6) NOT NULL,
  "updated_at" timestamptz(6) NOT NULL,
  "version" int8,
  "company_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "created_by" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Records of projects
-- ----------------------------
INSERT INTO "public"."projects" VALUES ('20250314225939764622244415715094701', '项目', '2259397646222444157', '2025-03-14 00:42:30', '2025-03-14', 9, '2025-03-14 00:42:30.968255+08', '2025-03-14 00:42:30.968255+08', 0, '2259397646222444157', '56012', '通过移动端创建的项目', '项目');
INSERT INTO "public"."projects" VALUES ('20250317225939764622244415767777801', '项目2', '2259397646222444157', '2025-03-17 16:37:57', '2025-03-29', 10, '2025-03-17 16:37:57.795272+08', '2025-03-17 16:37:57.795272+08', 0, '2259397646222444157', '56012', '通过移动端创建的项目', '项目2');
INSERT INTO "public"."projects" VALUES ('20250330default8587401', '项目1', 'default', '2025-03-30 19:28:05', '2025-03-30', 11, '2025-03-30 19:28:05.915107+08', '2025-03-30 19:28:05.915107+08', 0, 'default', '19368', '通过移动端创建的项目', '项目1');
INSERT INTO "public"."projects" VALUES ('20250330default9889702', '去', 'default', '2025-03-30 19:28:18', '2025-03-03', 12, '2025-03-30 19:28:18.897287+08', '2025-03-30 19:28:18.897287+08', 0, 'default', '19368', '通过移动端创建的项目', '去');
INSERT INTO "public"."projects" VALUES ('20250330132915014985975479141917301', '项目1', '1329150149859754791', '2025-03-30 21:30:19', '2025-03-30', 13, '2025-03-30 21:30:19.197811+08', '2025-03-30 21:30:19.197811+08', 0, '1329150149859754791', '00045', '通过移动端创建的项目', '项目1');

-- ----------------------------
-- Table structure for sand_material
-- ----------------------------
DROP TABLE IF EXISTS "public"."sand_material";
CREATE TABLE "public"."sand_material" (
  "sand_id" int8 NOT NULL GENERATED ALWAYS AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1
),
  "sand_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "sand_company" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Records of sand_material
-- ----------------------------
INSERT INTO "public"."sand_material" OVERRIDING SYSTEM VALUE VALUES (2, '都能贷', '2259397646222444157');
INSERT INTO "public"."sand_material" OVERRIDING SYSTEM VALUE VALUES (3, '示例', 'default');
INSERT INTO "public"."sand_material" OVERRIDING SYSTEM VALUE VALUES (4, '沙子2', '1329150149859754791');
INSERT INTO "public"."sand_material" OVERRIDING SYSTEM VALUE VALUES (5, '啊', '1329150149859754791');

-- ----------------------------
-- Table structure for specimen_devices
-- ----------------------------
DROP TABLE IF EXISTS "public"."specimen_devices";
CREATE TABLE "public"."specimen_devices" (
  "id" int4 NOT NULL DEFAULT nextval('specimen_devices_id_seq'::regclass),
  "specimen_id" int8 NOT NULL,
  "device_type" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "device_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Records of specimen_devices
-- ----------------------------

-- ----------------------------
-- Table structure for specimens
-- ----------------------------
DROP TABLE IF EXISTS "public"."specimens";
CREATE TABLE "public"."specimens" (
  "id" int8 NOT NULL DEFAULT nextval('specimens_id_seq'::regclass),
  "mix_ratio_id" int8,
  "mixing_temperature" float4 NOT NULL,
  "mixing_speed" float4 NOT NULL,
  "compaction_method" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "created_by" int8 NOT NULL,
  "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "creation_time" int8 NOT NULL,
  "cut_count" int4,
  "cut_shape" varchar(255) COLLATE "pg_catalog"."default",
  "height" float4,
  "length" float4,
  "radius" float4,
  "width" float4,
  "specimen_company" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "mixing_time" int4 NOT NULL
)
;

-- ----------------------------
-- Records of specimens
-- ----------------------------
INSERT INTO "public"."specimens" VALUES (17, NULL, 150, 155, '震动压实', 6, '2025-03-15 14:32:30.224499', 1742020349511, 1, 'rectangle', 0, 0, 0, 0, '2259397646222444157', 160);
INSERT INTO "public"."specimens" VALUES (18, NULL, 150, 580, '轮碾压实', 6, '2025-03-15 17:50:52.751243', 1742032252168, 1, 'rectangle', 0, 0, 0, 0, '2259397646222444157', 239);
INSERT INTO "public"."specimens" VALUES (19, NULL, 150, 155, '震动压实', 23, '2025-03-30 21:31:18.535412', 1743341477629, 1, 'rectangle', 0, 0, 0, 0, '1329150149859754791', 160);

-- ----------------------------
-- Table structure for stone_material
-- ----------------------------
DROP TABLE IF EXISTS "public"."stone_material";
CREATE TABLE "public"."stone_material" (
  "stone_id" int8 NOT NULL GENERATED ALWAYS AS IDENTITY (
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1
),
  "stone_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "stone_company" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Records of stone_material
-- ----------------------------
INSERT INTO "public"."stone_material" OVERRIDING SYSTEM VALUE VALUES (2, '都讲课', '2259397646222444157');
INSERT INTO "public"."stone_material" OVERRIDING SYSTEM VALUE VALUES (3, '去', 'default');
INSERT INTO "public"."stone_material" OVERRIDING SYSTEM VALUE VALUES (4, '石子1', '1329150149859754791');

-- ----------------------------
-- Table structure for support_mixture_task
-- ----------------------------
DROP TABLE IF EXISTS "public"."support_mixture_task";
CREATE TABLE "public"."support_mixture_task" (
  "task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "task_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "task_type" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Records of support_mixture_task
-- ----------------------------
INSERT INTO "public"."support_mixture_task" VALUES ('1', '马歇尔稳定度试验', 'MINTURE');
INSERT INTO "public"."support_mixture_task" VALUES ('6', '动稳定度试验', 'MINTURE');
INSERT INTO "public"."support_mixture_task" VALUES ('7', '沥青混合料车辙实验（汉堡车辙）', 'MINTURE');
INSERT INTO "public"."support_mixture_task" VALUES ('8', '沥青混合料弯曲试验', 'MINTURE');
INSERT INTO "public"."support_mixture_task" VALUES ('9', '动态模量试验', 'MINTURE');
INSERT INTO "public"."support_mixture_task" VALUES ('10', '沥青混合料直接拉伸循环疲劳测黏弹损伤试验', 'MINTURE');
INSERT INTO "public"."support_mixture_task" VALUES ('11', '沥青混合料四点弯曲疲劳寿命试验', 'MINTURE');
INSERT INTO "public"."support_mixture_task" VALUES ('12', '沥青混合料单轴压缩试验(圆柱体法)', 'MINTURE');
INSERT INTO "public"."support_mixture_task" VALUES ('13', '沥青混合料劈裂试验', 'MINTURE');

-- ----------------------------
-- Table structure for support_mixture_task_copy1
-- ----------------------------
DROP TABLE IF EXISTS "public"."support_mixture_task_copy1";
CREATE TABLE "public"."support_mixture_task_copy1" (
  "task_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "task_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "task_type" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Records of support_mixture_task_copy1
-- ----------------------------
INSERT INTO "public"."support_mixture_task_copy1" VALUES ('1', '马歇尔稳定度试验', 'MINTURE');
INSERT INTO "public"."support_mixture_task_copy1" VALUES ('6', '动稳定度试验', 'MINTURE');
INSERT INTO "public"."support_mixture_task_copy1" VALUES ('7', '沥青混合料车辙实验（汉堡车辙）', 'MINTURE');
INSERT INTO "public"."support_mixture_task_copy1" VALUES ('8', '沥青混合料弯曲试验', 'MINTURE');
INSERT INTO "public"."support_mixture_task_copy1" VALUES ('9', '动态模量试验', 'MINTURE');
INSERT INTO "public"."support_mixture_task_copy1" VALUES ('10', '沥青混合料直接拉伸循环疲劳测黏弹损伤试验', 'MINTURE');
INSERT INTO "public"."support_mixture_task_copy1" VALUES ('11', '沥青混合料四点弯曲疲劳寿命试验', 'MINTURE');
INSERT INTO "public"."support_mixture_task_copy1" VALUES ('12', '沥青混合料单轴压缩试验(圆柱体法)', 'MINTURE');
INSERT INTO "public"."support_mixture_task_copy1" VALUES ('13', '沥青混合料劈裂试验', 'MINTURE');

-- ----------------------------
-- Table structure for supported_devices
-- ----------------------------
DROP TABLE IF EXISTS "public"."supported_devices";
CREATE TABLE "public"."supported_devices" (
  "id" int8 NOT NULL DEFAULT nextval('supported_devices_id_seq'::regclass),
  "created_at" timestamptz(6) NOT NULL,
  "updated_at" timestamptz(6) NOT NULL,
  "version" int8,
  "description" varchar(255) COLLATE "pg_catalog"."default",
  "manufacturer" varchar(100) COLLATE "pg_catalog"."default",
  "model" varchar(100) COLLATE "pg_catalog"."default",
  "type" varchar(50) COLLATE "pg_catalog"."default",
  "assigned_mixing_equipment" varchar(255) COLLATE "pg_catalog"."default",
  "assigned_forming_equipment" varchar(255) COLLATE "pg_catalog"."default",
  "assigned_testing_equipment" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of supported_devices
-- ----------------------------
INSERT INTO "public"."supported_devices" VALUES (1, '2025-03-08 17:53:43.489776+08', '2025-03-08 17:53:43.489776+08', 0, 'u6c99u7816u5f0fu6c99u6d53u62ccu5408u673a', 'infratest', '20-0160-60', 'MIXING', NULL, NULL, NULL);
INSERT INTO "public"."supported_devices" VALUES (2, '2025-03-08 17:53:43.558734+08', '2025-03-08 17:53:43.558734+08', 0, 'u81eau52a8u6c99u6d53u62ccu5408u673a', 'Controls', '77-PV0077/C', 'MIXING', NULL, NULL, NULL);
INSERT INTO "public"."supported_devices" VALUES (3, '2025-03-08 17:53:43.562436+08', '2025-03-08 17:53:43.562436+08', 0, 'u9a6cu6b47u5c14u51fbu5b9eu4eea', 'infratest', '20-1500', 'FORMING', NULL, NULL, NULL);
INSERT INTO "public"."supported_devices" VALUES (4, '2025-03-08 17:53:43.564657+08', '2025-03-08 17:53:43.564657+08', 0, 'u5236u6837u5207u5272u673a', 'infratest', '60-0220', 'FORMING', NULL, NULL, NULL);
INSERT INTO "public"."supported_devices" VALUES (5, '2025-03-08 17:53:43.567175+08', '2025-03-08 17:53:43.567175+08', 0, 'u81eau52a8u9a6cu6b47u5c14u51fbu5b9eu4eea', 'Controls', '77-PV41A02', 'FORMING', NULL, NULL, NULL);
INSERT INTO "public"."supported_devices" VALUES (6, '2025-03-08 17:53:43.571373+08', '2025-03-08 17:53:43.571373+08', 0, 'u6c99u6d53u5236u6837u5207u5272u673a', 'Controls', '77-PV75202', 'FORMING', NULL, NULL, NULL);
INSERT INTO "public"."supported_devices" VALUES (7, '2025-03-08 17:53:43.574032+08', '2025-03-08 17:53:43.574032+08', 0, 'u9a6cu6b47u5c14u7a33u5b9au5ea6u4eea', 'infratest', '20-1672', 'TESTING', NULL, NULL, NULL);
INSERT INTO "public"."supported_devices" VALUES (8, '2025-03-08 17:53:43.57759+08', '2025-03-08 17:53:43.57759+08', 0, 'u6570u5b57u5316u9a6cu6b47u5c14u7a33u5b9au5ea6u4eea', 'Controls', '76-B3002', 'TESTING', NULL, NULL, NULL);
INSERT INTO "public"."supported_devices" VALUES (9, '2025-03-08 17:53:43.579715+08', '2025-03-08 17:53:43.579715+08', 0, 'u96c6u6599u7b5bu5206u4eea', 'u6d59u6c5fu5409u529bu68ee', 'ZBSX-92A', 'SIEVING', NULL, NULL, NULL);
INSERT INTO "public"."supported_devices" VALUES (10, '2025-03-08 17:53:43.583421+08', '2025-03-08 17:53:43.583421+08', 0, 'u9ad8u7cbeu5ea6u96c6u6599u7b5bu5206u4eea', 'u6d59u6c5fu63a2u77ff', '8411', 'SIEVING', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for test_asphalt_material
-- ----------------------------
DROP TABLE IF EXISTS "public"."test_asphalt_material";
CREATE TABLE "public"."test_asphalt_material" (
  "id" int8 NOT NULL DEFAULT nextval('test_asphalt_material_id_seq'::regclass),
  "asphalt_supplier" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "asphalt_test_due" date NOT NULL,
  "asphalt_grade" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "asphalt_catalog" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "created_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp(6) DEFAULT CURRENT_TIMESTAMP,
  "organization_id" int8 NOT NULL,
  "created_by" int8
)
;

-- ----------------------------
-- Records of test_asphalt_material
-- ----------------------------
INSERT INTO "public"."test_asphalt_material" VALUES (1, '公交卡', '2025-03-15', 'bjk', 'MODIFIED', '2025-03-15 20:16:02.701016', '2025-03-15 20:16:02.701016', 2259397646222444157, 6);
INSERT INTO "public"."test_asphalt_material" VALUES (2, '发挥好', '2025-03-31', 'cbj', 'MODIFIED', '2025-03-16 00:21:03.213419', '2025-03-16 00:21:03.213419', 2259397646222444157, 6);
INSERT INTO "public"."test_asphalt_material" VALUES (3, '供应商1', '2025-05-25', 'ymmm1', 'NORMAL', '2025-03-30 21:41:17.846586', '2025-03-30 21:41:17.846586', 1329150149859754791, 23);
INSERT INTO "public"."test_asphalt_material" VALUES (4, '供应商2', '2025-06-29', 'yiooo2', 'MODIFIED', '2025-03-30 21:41:54.98953', '2025-03-30 21:41:54.98953', 1329150149859754791, 23);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS "public"."users";
CREATE TABLE "public"."users" (
  "id" int8 NOT NULL DEFAULT nextval('users_id_seq'::regclass),
  "created_at" timestamptz(6) NOT NULL,
  "updated_at" timestamptz(6) NOT NULL,
  "version" int8,
  "admin" bool NOT NULL,
  "email" varchar(40) COLLATE "pg_catalog"."default",
  "name" varchar(40) COLLATE "pg_catalog"."default",
  "organization" varchar(255) COLLATE "pg_catalog"."default",
  "password" varchar(100) COLLATE "pg_catalog"."default",
  "phone" varchar(255) COLLATE "pg_catalog"."default",
  "username" varchar(15) COLLATE "pg_catalog"."default",
  "organization_id" int8,
  "allow_add_mixture" bool,
  "allow_add_asphalt" bool,
  "allow_add_mixratio" bool,
  "avatar_path" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO "public"."users" VALUES (1, '2025-03-06 15:38:15.978627+08', '2025-03-06 15:38:15.978627+08', 0, 'f', '1@gmail.com', '1111', '1111', '$2a$10$D.bI0HkmmUEKL1MOeMuCyOWweJntH/vXBIe.kWHz7SPsxfPYlN3rS', '15151515111', '19368', NULL, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (2, '2025-03-06 17:01:00.998417+08', '2025-03-06 17:01:00.998417+08', 0, 'f', '3@gmail.com', '1522', '1152', '$2a$10$j8CnMVm0zip/XETJK8rc3.9i0CUuxnqdMXkXW99ifWg8xuPyaDiGS', '15111111111', '39071', NULL, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (3, '2025-03-08 17:24:10.671291+08', '2025-03-08 17:24:10.671291+08', 0, 'f', '1@qq.com', '111', '111', '$2a$10$AtSchHVgwVGRFf72k5rKzeoc7RDRq2BJsLfEWkd2WrOgM.yxmb.Pi', '15111111111', '13738', NULL, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (4, '2025-03-09 15:55:54.450906+08', '2025-03-09 15:55:54.450906+08', 0, 'f', '2@qq.com', '123', '11', '$2a$10$bGELJhKrpqp9ZelR7/F1de4rCJnAgloygzZhUJjR8j7Gh.7N.lQU2', '15111111111', '21899', 5371211465312940040, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (5, '2025-03-09 15:59:31.21134+08', '2025-03-09 15:59:31.21134+08', 0, 'f', '4@163.com', 'asdd', '单位3', '$2a$10$SZ9vldfDkyJTE1PdnXsMxesj1ARhgr8PNjkmFg2CaPqvWH.ewp/ly', '15111111111', '44575', 2259397646222444157, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (6, '2025-03-09 16:01:05.015522+08', '2025-03-09 16:01:05.015522+08', 0, 'f', '5@qq.com', '12345', '单位3', '$2a$10$A6UYowbJ6P3Qqu3zs3V8t.Owe3mzezwc4BNStJVRbAW6q.k0qQr5e', '15111111111', '56012', 2259397646222444157, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (7, '2025-03-09 17:27:36.642118+08', '2025-03-09 17:27:36.642118+08', 0, 'f', '2@gmail.com', '1231', '11', '$2a$10$Rb5QHRW6.J13MjPpuWDkP.bOEI3q5.lc3PanwMfH.ckiNB05x6FW.', '15111111111', '24255', 5371211465312940040, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (8, '2025-03-28 20:42:24.962022+08', '2025-03-28 20:42:24.962022+08', 0, 'f', '200@qq.com', '硅谷广场', '翻掉的', '$2a$10$SGwO9Qlkx9au0sV.Z6gxO.P4/FHEYH7glIduh2/fhvfGdERgo2rh.', '15111111111', '20080', 3594276330215419945, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (9, '2025-03-30 15:34:53.433591+08', '2025-03-30 15:34:53.433591+08', 0, 'f', '999@qq.com', '的腹股沟', '防腐管', '$2a$10$.HR9iZQYyvSRKxDYtCoD/exNQxW9QuUsJ.a97k4JpceihetKfZyT.', '15111111111', '99997', 8284328811636307346, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (10, '2025-03-30 15:36:36.895881+08', '2025-03-30 15:36:36.895881+08', 0, 'f', '899899@qq.com', '地方滚滚滚', 'fghjhgvbnm', '$2a$10$rM1PkGpNbSbOoVotn6WW8e5twmHhT8AnWANYiCn/zjGe4uKZt7j.O', '15111111111', '899899', 1224645970324966018, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (11, '2025-03-30 15:40:36.82225+08', '2025-03-30 15:40:36.82225+08', 0, 't', '123654@gmail.com', '对不对还打不打', '电话电话手表', '$2a$10$ja1erY/iMoy3iV/p0cDEpeGmfbbF5r8w09nyGI3u3tf2rcUK8q8qa', '15111111111', '123654', 8154373918757111414, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (12, '2025-03-30 15:41:17.39617+08', '2025-03-30 15:41:17.39617+08', 0, 'f', '45698765@gmail.com', '对不对不对不对', '多喝点', '$2a$10$9ZuUVJuHhoHdDyaitc/8wu2ewHCCsWjFbjfb9KNO9rWmcoh85fJ.K', '15111111111', '45698765', 6686834324109702399, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (13, '2025-03-30 15:56:48.328043+08', '2025-03-30 15:56:48.328043+08', 0, 't', '123456654321@gmail.com', '上海市区', '上海市', '$2a$10$bIx9C44U/S3TtyrvFhhW8eGYhNyewekVNphK1/qt9O90AnyrzIE8i', '15111111111', '123456654321', 1301433143261679451, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (14, '2025-03-30 15:58:31.535655+08', '2025-03-30 15:58:31.535655+08', 0, 'f', '456@163.com', '啥时候说的', '上海市', '$2a$10$ymCgN1ZcY75ESOVJzIDGLuNuQWJvYxpVxF4Tj3pT2ZtLWyqSBFTnS', '15111111111', '45677', 1301433143261679451, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (15, '2025-03-30 16:37:58.176371+08', '2025-03-30 16:37:58.176371+08', 0, 't', '789789@163.com', '虚假主叫', '电话线', '$2a$10$bBfmtboZk2SChbEfWi6bEurVCd/h42MeyO0JY07t.2JAEXY270/0q', '15111111111', '789789', 4757752133930273773, 't', 't', 't', NULL);
INSERT INTO "public"."users" VALUES (16, '2025-03-30 16:38:57.041153+08', '2025-03-30 16:38:57.041153+08', 0, 'f', '456456@163.com', '大喊大叫', '电话线', '$2a$10$0O/ydKz0a9zn1ss/7JVQyeK2lIPFhVmkFybdg4vtAQAhV1cbf4o1q', '15111111111', '456456', 4757752133930273773, 'f', 'f', 'f', NULL);
INSERT INTO "public"."users" VALUES (17, '2025-03-30 16:50:47.48437+08', '2025-03-30 16:50:47.48437+08', 0, 't', '147@qq.com', '实际上', '下划线', '$2a$10$XC/DB16tTtB2bdg/cXzdJeLYJKsyuZErHuKB34sT0XYmyqnRG7Syu', '15111111111', '14782', 2850349200073707383, 't', 't', 't', NULL);
INSERT INTO "public"."users" VALUES (18, '2025-03-30 17:02:21.505768+08', '2025-03-30 17:02:21.505768+08', 0, 't', '258@gmail.com', '1511', '小孩子', '$2a$10$lky7D/NasIypXDHYy0.1H.fC5DycB6qkCXHJaTdWEo1R6DrmTJY2G', '15111111111', '25836', 3809984879460171779, 't', 't', 't', NULL);
INSERT INTO "public"."users" VALUES (19, '2025-03-30 17:11:41.013943+08', '2025-03-30 17:11:41.013943+08', 0, 't', '147741@gmail.com', '啊啊啊', '啊啦啦', '$2a$10$kQn30egXbt3pdcfE8.tf3ehA9U/31NG4VgUse11Qm4xL29Pzfq7/K', '15111111111', '147741', 4751356350763481445, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (20, '2025-03-30 17:12:52.167909+08', '2025-03-30 17:12:52.167909+08', 0, 'f', '258852@gmail.com', '大姐姐', '啊啦啦', '$2a$10$Y4gywlxK9wiq.GP0ESVanul79t3YKlILBrWbnHw1MLFVSVv.KHusu', '15111111111', '258852', 4751356350763481445, NULL, NULL, NULL, NULL);
INSERT INTO "public"."users" VALUES (21, '2025-03-30 17:19:52.594548+08', '2025-03-30 17:19:52.594548+08', 0, 't', '159@qq.com', '发个干活', '续航哥', '$2a$10$Av5TuA.BOpIHmUxnyKkYfOLVmtlEHpkkGIIhszpacK7C4uNErIOr.', '15111111111', '15928', 4630555664774839271, 't', 't', 't', NULL);
INSERT INTO "public"."users" VALUES (22, '2025-03-30 17:20:58.892965+08', '2025-03-30 17:21:21.63835+08', 0, 'f', '357@qq.com', '习惯不就', '续航哥', '$2a$10$1IYuSWdKWjtSvGrudpM8nOljvlKVxOjlvx7GTI.i4I3gDDps2lOjC', '15111111111', '35776', 4630555664774839271, 'f', 't', 'f', NULL);
INSERT INTO "public"."users" VALUES (23, '2025-03-30 17:31:48.532755+08', '2025-03-31 12:20:15.509798+08', 0, 't', '0@gmail.com', '啊啊啊', '爱学习', '$2a$10$7dFa.yL9jI8gqnODwqhdjOTV0M4ACGD9CaSNEaORaMnDdIoq9jHdq', '15111111111', '00045', 1329150149859754791, 't', 't', 't', 'user_23_1743394815475.jpg');
INSERT INTO "public"."users" VALUES (25, '2025-03-31 11:34:16.992859+08', '2025-03-31 16:03:05.485149+08', 0, 'f', '000@gmail.com', '实验员3', '爱学习', '$2a$10$FNxEB.n2P0e/LM6CTKQngeQNRWmz5uPJfHhOF9qFfoh6vsYxV8PWy', '15111111111', '00066', 1329150149859754791, 'f', 't', 'f', 'user_25_1743408185449.jpg');
INSERT INTO "public"."users" VALUES (24, '2025-03-30 17:32:41.899811+08', '2025-03-31 16:03:32.167464+08', 0, 'f', '00@gmail.com', '发个呵呵', '爱学习', '$2a$10$trhGbpTo6nV4sx2pvM.r/eoJ7PTjPC1EUPm7IfTZks9bmLYS8C6UK', '15111111111', '00670', 1329150149859754791, 't', 't', 't', 'user_24_1743408212158.jpg');
INSERT INTO "public"."users" VALUES (26, '2025-04-03 00:34:34.208862+08', '2025-04-03 00:34:34.208862+08', 0, 't', '5@163.com', '请求请求', '5@126.com', '$2a$10$WXK/SgRpcZgUoQb5AD6WQOCUwDRTjiTGtmMVnh.xTPGtV/D/aAb.C', '15111111111', '57228', 7393056949553090473, 't', 't', 't', NULL);

-- ----------------------------
-- Function structure for generate_mix_id
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."generate_mix_id"();
CREATE FUNCTION "public"."generate_mix_id"()
  RETURNS "pg_catalog"."trigger" AS $BODY$
DECLARE
    date_part VARCHAR(8);
    seq_num INT;
    seq_formatted VARCHAR(3);
    new_id VARCHAR(20);
BEGIN
    -- 获取当前日期部分（年月日）
    date_part := TO_CHAR(CURRENT_DATE, 'YYYYMMDD');
    
    -- 查询今天已有的记录数，加1得到序号
    SELECT COUNT(*) + 1 INTO seq_num
    FROM mixratio
    WHERE mix_id LIKE 'phb' || date_part || '%';
    
    -- 格式化序号为3位数字（例如：001, 002, ...）
    seq_formatted := LPAD(seq_num::TEXT, 3, '0');
    
    -- 组合生成新ID
    new_id := 'phb' || date_part || seq_formatted;
    
    -- 设置新ID
    NEW.mix_id := new_id;
    
    RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."asphalt_material_asphalt_id_seq"
OWNED BY "public"."asphalt_material"."asphalt_id";
SELECT setval('"public"."asphalt_material_asphalt_id_seq"', 8, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."asphalt_mixture_bending_test_id_seq"
OWNED BY "public"."asphalt_mixture_bending_test"."id";
SELECT setval('"public"."asphalt_mixture_bending_test_id_seq"', 20, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."asphalt_softening_point_test_id_seq"
OWNED BY "public"."asphalt_softening_point_test"."id";
SELECT setval('"public"."asphalt_softening_point_test_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."asphalt_task_asphalt_experiment_id_seq"
OWNED BY "public"."asphalt_task"."asphalt_experiment_id";
SELECT setval('"public"."asphalt_task_asphalt_experiment_id_seq"', 151, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."asphalt_task_asphalt_task_id_seq"
OWNED BY "public"."asphalt_task"."asphalt_task_id";
SELECT setval('"public"."asphalt_task_asphalt_task_id_seq"', 151, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."bbr_test_id_seq"
OWNED BY "public"."bbr_test"."id";
SELECT setval('"public"."bbr_test_id_seq"', 8, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."brookfield_viscosity_measurement_id_seq"
OWNED BY "public"."brookfield_viscosity_measurement"."id";
SELECT setval('"public"."brookfield_viscosity_measurement_id_seq"', 18, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."brookfield_viscosity_temperature_point_id_seq"
OWNED BY "public"."brookfield_viscosity_temperature_point"."id";
SELECT setval('"public"."brookfield_viscosity_temperature_point_id_seq"', 14, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."brookfield_viscosity_test_id_seq"
OWNED BY "public"."brookfield_viscosity_test"."id";
SELECT setval('"public"."brookfield_viscosity_test_id_seq"', 11, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."compaction_methods_id_seq"
OWNED BY "public"."compaction_methods"."id";
SELECT setval('"public"."compaction_methods_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."direct_stretching_fatigue_data_id_seq"
OWNED BY "public"."direct_stretching_fatigue_data"."id";
SELECT setval('"public"."direct_stretching_fatigue_data_id_seq"', 24, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."direct_stretching_fatigue_specimens_id_seq"
OWNED BY "public"."direct_stretching_fatigue_specimens"."id";
SELECT setval('"public"."direct_stretching_fatigue_specimens_id_seq"', 21, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."direct_stretching_fatigue_test_id_seq"
OWNED BY "public"."direct_stretching_fatigue_test"."id";
SELECT setval('"public"."direct_stretching_fatigue_test_id_seq"', 46, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."direct_stretching_modulus_data_id_seq"
OWNED BY "public"."direct_stretching_modulus_data"."id";
SELECT setval('"public"."direct_stretching_modulus_data_id_seq"', 26, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."dsr_measurement_id_seq"
OWNED BY "public"."dsr_measurement"."id";
SELECT setval('"public"."dsr_measurement_id_seq"', 9, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."dsr_temperature_point_id_seq"
OWNED BY "public"."dsr_temperature_point"."id";
SELECT setval('"public"."dsr_temperature_point_id_seq"', 9, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."ductility_test_id_seq"
OWNED BY "public"."ductility_test"."id";
SELECT setval('"public"."ductility_test_id_seq"', 5, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."dynamic_modulus_measurement_id_seq"
OWNED BY "public"."dynamic_modulus_measurement"."id";
SELECT setval('"public"."dynamic_modulus_measurement_id_seq"', 720, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."dynamic_modulus_specimen_id_seq"
OWNED BY "public"."dynamic_modulus_specimen"."id";
SELECT setval('"public"."dynamic_modulus_specimen_id_seq"', 28, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."dynamic_modulus_temperature_id_seq"
OWNED BY "public"."dynamic_modulus_temperature"."id";
SELECT setval('"public"."dynamic_modulus_temperature_id_seq"', 123, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."dynamic_modulus_test_id_seq"
OWNED BY "public"."dynamic_modulus_test"."id";
SELECT setval('"public"."dynamic_modulus_test_id_seq"', 30, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."dynamic_shear_rheometer_test_id_seq"
OWNED BY "public"."dynamic_shear_rheometer_test"."id";
SELECT setval('"public"."dynamic_shear_rheometer_test_id_seq"', 10, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."hamburg_rutting_test_id_seq"
OWNED BY "public"."hamburg_rutting_test"."id";
SELECT setval('"public"."hamburg_rutting_test_id_seq"', 24, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."marshall_test_id_seq"
OWNED BY "public"."marshall_test"."id";
SELECT setval('"public"."marshall_test_id_seq"', 30, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mixratio_asphalt_id_seq"
OWNED BY "public"."mixratio_asphalt"."id";
SELECT setval('"public"."mixratio_asphalt_id_seq"', 5, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mixratio_id_seq"
OWNED BY "public"."mixratio"."id";
SELECT setval('"public"."mixratio_id_seq"', 9, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mixratio_sand_id_seq"
OWNED BY "public"."mixratio_sand"."id";
SELECT setval('"public"."mixratio_sand_id_seq"', 5, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mixratio_stone_id_seq"
OWNED BY "public"."mixratio_stone"."id";
SELECT setval('"public"."mixratio_stone_id_seq"', 5, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mixture_four_point_bending_result_id_seq"
OWNED BY "public"."mixture_four_point_bending_result"."id";
SELECT setval('"public"."mixture_four_point_bending_result_id_seq"', 114, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mixture_four_point_bending_specimen_id_seq"
OWNED BY "public"."mixture_four_point_bending_specimen"."id";
SELECT setval('"public"."mixture_four_point_bending_specimen_id_seq"', 22, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mixture_four_point_bending_test_id_seq"
OWNED BY "public"."mixture_four_point_bending_test"."id";
SELECT setval('"public"."mixture_four_point_bending_test_id_seq"', 23, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mixture_task_id_seq"
OWNED BY "public"."mixture_task"."id";
SELECT setval('"public"."mixture_task_id_seq"', 382, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mixture_uniaxial_compression_p_values_id_seq"
OWNED BY "public"."mixture_uniaxial_compression_p_values"."id";
SELECT setval('"public"."mixture_uniaxial_compression_p_values_id_seq"', 70, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mixture_uniaxial_compression_specimen_id_seq"
OWNED BY "public"."mixture_uniaxial_compression_specimen"."id";
SELECT setval('"public"."mixture_uniaxial_compression_specimen_id_seq"', 35, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mixture_uniaxial_compression_test_id_seq"
OWNED BY "public"."mixture_uniaxial_compression_test"."test_id";
SELECT setval('"public"."mixture_uniaxial_compression_test_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mixture_uniaxial_compression_uts028_data_id_seq"
OWNED BY "public"."mixture_uniaxial_compression_uts028_data"."id";
SELECT setval('"public"."mixture_uniaxial_compression_uts028_data_id_seq"', 121, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."molding_methods_id_seq"
OWNED BY "public"."molding_methods"."id";
SELECT setval('"public"."molding_methods_id_seq"', 6, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."penetration_test_id_seq"', 6, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."penetration_tests_id_seq"
OWNED BY "public"."penetration_tests"."id";
SELECT setval('"public"."penetration_tests_id_seq"', 2, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."projects_id_seq"
OWNED BY "public"."projects"."id";
SELECT setval('"public"."projects_id_seq"', 13, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."projects_project_id_seq"
OWNED BY "public"."projects"."project_id";
SELECT setval('"public"."projects_project_id_seq"', 1, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sand_material_sand_id_seq"
OWNED BY "public"."sand_material"."sand_id";
SELECT setval('"public"."sand_material_sand_id_seq"', 5, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."softening_point_test_id_seq"', 32, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."specimen_devices_id_seq"
OWNED BY "public"."specimen_devices"."id";
SELECT setval('"public"."specimen_devices_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."specimens_id_seq"
OWNED BY "public"."specimens"."id";
SELECT setval('"public"."specimens_id_seq"', 19, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."stone_material_stone_id_seq"
OWNED BY "public"."stone_material"."stone_id";
SELECT setval('"public"."stone_material_stone_id_seq"', 4, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."supported_devices_id_seq"
OWNED BY "public"."supported_devices"."id";
SELECT setval('"public"."supported_devices_id_seq"', 10, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."test_asphalt_material_id_seq"
OWNED BY "public"."test_asphalt_material"."id";
SELECT setval('"public"."test_asphalt_material_id_seq"', 4, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."users_id_seq"
OWNED BY "public"."users"."id";
SELECT setval('"public"."users_id_seq"', 26, true);

-- ----------------------------
-- Auto increment value for asphalt_material
-- ----------------------------
SELECT setval('"public"."asphalt_material_asphalt_id_seq"', 8, true);

-- ----------------------------
-- Checks structure for table asphalt_material
-- ----------------------------
ALTER TABLE "public"."asphalt_material" ADD CONSTRAINT "asphalt_material_asphalt_character_check" CHECK (asphalt_character::text = ANY (ARRAY['NORMAL'::character varying::text, 'MODIFIED'::character varying::text]));

-- ----------------------------
-- Primary Key structure for table asphalt_material
-- ----------------------------
ALTER TABLE "public"."asphalt_material" ADD CONSTRAINT "asphalt_material_pkey" PRIMARY KEY ("asphalt_id");

-- ----------------------------
-- Primary Key structure for table asphalt_mixture_bending_test
-- ----------------------------
ALTER TABLE "public"."asphalt_mixture_bending_test" ADD CONSTRAINT "asphalt_mixture_bending_test_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table asphalt_penetration_test
-- ----------------------------
ALTER TABLE "public"."asphalt_penetration_test" ADD CONSTRAINT "asphalt_penetration_test_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table asphalt_softening_point_test
-- ----------------------------
CREATE INDEX "idx_softening_point_task_id" ON "public"."asphalt_softening_point_test" USING btree (
  "task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table asphalt_softening_point_test
-- ----------------------------
ALTER TABLE "public"."asphalt_softening_point_test" ADD CONSTRAINT "asphalt_softening_point_test_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table asphalt_task
-- ----------------------------
ALTER TABLE "public"."asphalt_task" ADD CONSTRAINT "asphalt_task_pkey" PRIMARY KEY ("asphalt_task_id");

-- ----------------------------
-- Indexes structure for table bbr_test
-- ----------------------------
CREATE INDEX "idx_bbr_test_task_id" ON "public"."bbr_test" USING btree (
  "task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table bbr_test
-- ----------------------------
ALTER TABLE "public"."bbr_test" ADD CONSTRAINT "bbr_test_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table brookfield_viscosity_measurement
-- ----------------------------
ALTER TABLE "public"."brookfield_viscosity_measurement" ADD CONSTRAINT "brookfield_viscosity_measurement_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table brookfield_viscosity_temperature_point
-- ----------------------------
ALTER TABLE "public"."brookfield_viscosity_temperature_point" ADD CONSTRAINT "brookfield_viscosity_temperature_point_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table brookfield_viscosity_test
-- ----------------------------
ALTER TABLE "public"."brookfield_viscosity_test" ADD CONSTRAINT "brookfield_viscosity_test_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table compaction_methods
-- ----------------------------
ALTER TABLE "public"."compaction_methods" ADD CONSTRAINT "compaction_methods_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table devices
-- ----------------------------
ALTER TABLE "public"."devices" ADD CONSTRAINT "devices_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table direct_stretching_fatigue_data
-- ----------------------------
ALTER TABLE "public"."direct_stretching_fatigue_data" ADD CONSTRAINT "direct_stretching_fatigue_data_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table direct_stretching_fatigue_specimens
-- ----------------------------
ALTER TABLE "public"."direct_stretching_fatigue_specimens" ADD CONSTRAINT "direct_stretching_fatigue_specimens_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table direct_stretching_fatigue_test
-- ----------------------------
ALTER TABLE "public"."direct_stretching_fatigue_test" ADD CONSTRAINT "direct_stretching_fatigue_test_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table direct_stretching_modulus_data
-- ----------------------------
ALTER TABLE "public"."direct_stretching_modulus_data" ADD CONSTRAINT "direct_stretching_modulus_data_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table dsr_measurement
-- ----------------------------
ALTER TABLE "public"."dsr_measurement" ADD CONSTRAINT "dsr_measurement_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table dsr_temperature_point
-- ----------------------------
ALTER TABLE "public"."dsr_temperature_point" ADD CONSTRAINT "dsr_temperature_point_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table ductility_test
-- ----------------------------
CREATE INDEX "idx_ductility_task_id" ON "public"."ductility_test" USING btree (
  "task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table ductility_test
-- ----------------------------
ALTER TABLE "public"."ductility_test" ADD CONSTRAINT "ductility_test_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table dynamic_modulus_measurement
-- ----------------------------
CREATE INDEX "idx_dmmeasure_frequency" ON "public"."dynamic_modulus_measurement" USING btree (
  "frequency" "pg_catalog"."numeric_ops" ASC NULLS LAST
);
CREATE INDEX "idx_dmmeasure_temperature_id" ON "public"."dynamic_modulus_measurement" USING btree (
  "temperature_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table dynamic_modulus_measurement
-- ----------------------------
ALTER TABLE "public"."dynamic_modulus_measurement" ADD CONSTRAINT "dynamic_modulus_measurement_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table dynamic_modulus_specimen
-- ----------------------------
CREATE INDEX "idx_dmspecimen_test_id" ON "public"."dynamic_modulus_specimen" USING btree (
  "test_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table dynamic_modulus_specimen
-- ----------------------------
ALTER TABLE "public"."dynamic_modulus_specimen" ADD CONSTRAINT "dynamic_modulus_specimen_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table dynamic_modulus_temperature
-- ----------------------------
CREATE INDEX "idx_dmtemp_temperature" ON "public"."dynamic_modulus_temperature" USING btree (
  "temperature" "pg_catalog"."numeric_ops" ASC NULLS LAST
);
CREATE INDEX "idx_dmtemp_test_specimen" ON "public"."dynamic_modulus_temperature" USING btree (
  "test_id" "pg_catalog"."int8_ops" ASC NULLS LAST,
  "specimen_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table dynamic_modulus_temperature
-- ----------------------------
ALTER TABLE "public"."dynamic_modulus_temperature" ADD CONSTRAINT "dynamic_modulus_temperature_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table dynamic_modulus_test
-- ----------------------------
CREATE INDEX "idx_dmtest_mix_ratio_id" ON "public"."dynamic_modulus_test" USING btree (
  "mix_ratio_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_dmtest_task_id" ON "public"."dynamic_modulus_test" USING btree (
  "task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table dynamic_modulus_test
-- ----------------------------
ALTER TABLE "public"."dynamic_modulus_test" ADD CONSTRAINT "dynamic_modulus_test_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table dynamic_shear_rheometer_test
-- ----------------------------
ALTER TABLE "public"."dynamic_shear_rheometer_test" ADD CONSTRAINT "dynamic_shear_rheometer_test_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table hamburg_rutting_test
-- ----------------------------
ALTER TABLE "public"."hamburg_rutting_test" ADD CONSTRAINT "hamburg_rutting_test_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table marshall_test
-- ----------------------------
CREATE INDEX "idx_marshall_test_task_id" ON "public"."marshall_test" USING btree (
  "task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table marshall_test
-- ----------------------------
ALTER TABLE "public"."marshall_test" ADD CONSTRAINT "marshall_test_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Triggers structure for table mixratio
-- ----------------------------
CREATE TRIGGER "trg_generate_mix_id" BEFORE INSERT ON "public"."mixratio"
FOR EACH ROW
EXECUTE PROCEDURE "public"."generate_mix_id"();

-- ----------------------------
-- Uniques structure for table mixratio
-- ----------------------------
ALTER TABLE "public"."mixratio" ADD CONSTRAINT "mixratio_mix_id_key" UNIQUE ("mix_id");

-- ----------------------------
-- Primary Key structure for table mixratio
-- ----------------------------
ALTER TABLE "public"."mixratio" ADD CONSTRAINT "mixratio_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table mixratio_asphalt
-- ----------------------------
ALTER TABLE "public"."mixratio_asphalt" ADD CONSTRAINT "mixratio_asphalt_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table mixratio_sand
-- ----------------------------
ALTER TABLE "public"."mixratio_sand" ADD CONSTRAINT "mixratio_sand_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table mixratio_stone
-- ----------------------------
ALTER TABLE "public"."mixratio_stone" ADD CONSTRAINT "mixratio_stone_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table mixture_four_point_bending_result
-- ----------------------------
CREATE INDEX "idx_four_point_result_specimen_id" ON "public"."mixture_four_point_bending_result" USING btree (
  "specimen_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_four_point_result_type" ON "public"."mixture_four_point_bending_result" USING btree (
  "result_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mixture_four_point_bending_result
-- ----------------------------
ALTER TABLE "public"."mixture_four_point_bending_result" ADD CONSTRAINT "mixture_four_point_bending_result_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table mixture_four_point_bending_specimen
-- ----------------------------
CREATE INDEX "idx_four_point_specimen_test_id" ON "public"."mixture_four_point_bending_specimen" USING btree (
  "test_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mixture_four_point_bending_specimen
-- ----------------------------
ALTER TABLE "public"."mixture_four_point_bending_specimen" ADD CONSTRAINT "mixture_four_point_bending_specimen_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table mixture_four_point_bending_test
-- ----------------------------
CREATE INDEX "idx_four_point_test_mix_ratio_id" ON "public"."mixture_four_point_bending_test" USING btree (
  "mix_ratio_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_four_point_test_task_id" ON "public"."mixture_four_point_bending_test" USING btree (
  "task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mixture_four_point_bending_test
-- ----------------------------
ALTER TABLE "public"."mixture_four_point_bending_test" ADD CONSTRAINT "mixture_four_point_bending_test_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table mixture_splitting_test
-- ----------------------------
ALTER TABLE "public"."mixture_splitting_test" ADD CONSTRAINT "mixture_splitting_test_pkey" PRIMARY KEY ("test_id");

-- ----------------------------
-- Indexes structure for table mixture_splitting_test_specimen
-- ----------------------------
CREATE INDEX "idx_splitting_specimen_test_id" ON "public"."mixture_splitting_test_specimen" USING btree (
  "test_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mixture_splitting_test_specimen
-- ----------------------------
ALTER TABLE "public"."mixture_splitting_test_specimen" ADD CONSTRAINT "mixture_splitting_test_specimen_pkey" PRIMARY KEY ("specimen_id");

-- ----------------------------
-- Indexes structure for table mixture_task
-- ----------------------------
CREATE INDEX "idx_company" ON "public"."mixture_task" USING btree (
  "task_company" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_mixratio" ON "public"."mixture_task" USING btree (
  "mixratio_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_project" ON "public"."mixture_task" USING btree (
  "project_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_task_id" ON "public"."mixture_task" USING btree (
  "task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Uniques structure for table mixture_task
-- ----------------------------
ALTER TABLE "public"."mixture_task" ADD CONSTRAINT "unique_task_mix_specimen" UNIQUE ("task_id", "mixratio_id", "specimen_id");

-- ----------------------------
-- Primary Key structure for table mixture_task
-- ----------------------------
ALTER TABLE "public"."mixture_task" ADD CONSTRAINT "mixture_task_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table mixture_uniaxial_compression_p_values
-- ----------------------------
ALTER TABLE "public"."mixture_uniaxial_compression_p_values" ADD CONSTRAINT "mixture_uniaxial_compression_p_values_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table mixture_uniaxial_compression_specimen
-- ----------------------------
CREATE INDEX "idx_ucs_test_id" ON "public"."mixture_uniaxial_compression_specimen" USING btree (
  "test_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mixture_uniaxial_compression_specimen
-- ----------------------------
ALTER TABLE "public"."mixture_uniaxial_compression_specimen" ADD CONSTRAINT "mixture_uniaxial_compression_specimen_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table mixture_uniaxial_compression_test
-- ----------------------------
CREATE INDEX "idx_uct_task_id" ON "public"."mixture_uniaxial_compression_test" USING btree (
  "task_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mixture_uniaxial_compression_test
-- ----------------------------
ALTER TABLE "public"."mixture_uniaxial_compression_test" ADD CONSTRAINT "mixture_uniaxial_compression_test_pkey" PRIMARY KEY ("test_id");

-- ----------------------------
-- Indexes structure for table mixture_uniaxial_compression_uts028_data
-- ----------------------------
CREATE INDEX "idx_uts028_specimen_id" ON "public"."mixture_uniaxial_compression_uts028_data" USING btree (
  "specimen_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_uts028_test_id" ON "public"."mixture_uniaxial_compression_uts028_data" USING btree (
  "test_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mixture_uniaxial_compression_uts028_data
-- ----------------------------
ALTER TABLE "public"."mixture_uniaxial_compression_uts028_data" ADD CONSTRAINT "mixture_uniaxial_compression_uts028_data_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table molding_methods
-- ----------------------------
ALTER TABLE "public"."molding_methods" ADD CONSTRAINT "molding_methods_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table penetration_tests
-- ----------------------------
ALTER TABLE "public"."penetration_tests" ADD CONSTRAINT "penetration_tests_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table projects
-- ----------------------------
ALTER TABLE "public"."projects" ADD CONSTRAINT "projects_pkey" PRIMARY KEY ("project_id");

-- ----------------------------
-- Auto increment value for sand_material
-- ----------------------------
SELECT setval('"public"."sand_material_sand_id_seq"', 5, true);

-- ----------------------------
-- Primary Key structure for table sand_material
-- ----------------------------
ALTER TABLE "public"."sand_material" ADD CONSTRAINT "sand_material_pkey" PRIMARY KEY ("sand_id");

-- ----------------------------
-- Indexes structure for table specimen_devices
-- ----------------------------
CREATE INDEX "idx_specimen_devices_device_id" ON "public"."specimen_devices" USING btree (
  "device_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_specimen_devices_specimen_id" ON "public"."specimen_devices" USING btree (
  "specimen_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Uniques structure for table specimen_devices
-- ----------------------------
ALTER TABLE "public"."specimen_devices" ADD CONSTRAINT "uk_specimen_device_type" UNIQUE ("specimen_id", "device_type");

-- ----------------------------
-- Primary Key structure for table specimen_devices
-- ----------------------------
ALTER TABLE "public"."specimen_devices" ADD CONSTRAINT "specimen_devices_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table specimens
-- ----------------------------
CREATE INDEX "idx_specimens_created_at" ON "public"."specimens" USING btree (
  "created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_specimens_mix_ratio_id" ON "public"."specimens" USING btree (
  "mix_ratio_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table specimens
-- ----------------------------
ALTER TABLE "public"."specimens" ADD CONSTRAINT "specimens_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Auto increment value for stone_material
-- ----------------------------
SELECT setval('"public"."stone_material_stone_id_seq"', 4, true);

-- ----------------------------
-- Primary Key structure for table stone_material
-- ----------------------------
ALTER TABLE "public"."stone_material" ADD CONSTRAINT "stone_material_pkey" PRIMARY KEY ("stone_id");

-- ----------------------------
-- Primary Key structure for table support_mixture_task
-- ----------------------------
ALTER TABLE "public"."support_mixture_task" ADD CONSTRAINT "support_mixture_task_pkey" PRIMARY KEY ("task_id");

-- ----------------------------
-- Primary Key structure for table support_mixture_task_copy1
-- ----------------------------
ALTER TABLE "public"."support_mixture_task_copy1" ADD CONSTRAINT "support_mixture_task_copy1_pkey" PRIMARY KEY ("task_id");

-- ----------------------------
-- Primary Key structure for table supported_devices
-- ----------------------------
ALTER TABLE "public"."supported_devices" ADD CONSTRAINT "supported_devices_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table test_asphalt_material
-- ----------------------------
CREATE INDEX "idx_test_asphalt_material_catalog" ON "public"."test_asphalt_material" USING btree (
  "asphalt_catalog" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_test_asphalt_material_grade" ON "public"."test_asphalt_material" USING btree (
  "asphalt_grade" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_test_asphalt_material_org" ON "public"."test_asphalt_material" USING btree (
  "organization_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_test_asphalt_material_supplier" ON "public"."test_asphalt_material" USING btree (
  "asphalt_supplier" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Checks structure for table test_asphalt_material
-- ----------------------------
ALTER TABLE "public"."test_asphalt_material" ADD CONSTRAINT "test_asphalt_material_asphalt_catalog_check" CHECK (asphalt_catalog::text = ANY (ARRAY['NORMAL'::character varying::text, 'MODIFIED'::character varying::text]));

-- ----------------------------
-- Primary Key structure for table test_asphalt_material
-- ----------------------------
ALTER TABLE "public"."test_asphalt_material" ADD CONSTRAINT "test_asphalt_material_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table users
-- ----------------------------
ALTER TABLE "public"."users" ADD CONSTRAINT "ukr43af9ap4edm43mmtq01oddj6" UNIQUE ("username");
ALTER TABLE "public"."users" ADD CONSTRAINT "uk6dotkott2kjsp8vw4d0m25fb7" UNIQUE ("email");

-- ----------------------------
-- Primary Key structure for table users
-- ----------------------------
ALTER TABLE "public"."users" ADD CONSTRAINT "users_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table brookfield_viscosity_measurement
-- ----------------------------
ALTER TABLE "public"."brookfield_viscosity_measurement" ADD CONSTRAINT "fkbyq23t072ncaa44tf98dxaymx" FOREIGN KEY ("temperature_point_id") REFERENCES "public"."brookfield_viscosity_temperature_point" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table brookfield_viscosity_temperature_point
-- ----------------------------
ALTER TABLE "public"."brookfield_viscosity_temperature_point" ADD CONSTRAINT "fk4cw34bhuwylx60hvww6pb37sa" FOREIGN KEY ("test_id") REFERENCES "public"."brookfield_viscosity_test" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table devices
-- ----------------------------
ALTER TABLE "public"."devices" ADD CONSTRAINT "fkrfbri1ymrwywdydc4dgywe1bt" FOREIGN KEY ("user_id") REFERENCES "public"."users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table dynamic_modulus_measurement
-- ----------------------------
ALTER TABLE "public"."dynamic_modulus_measurement" ADD CONSTRAINT "fkjg0vr5tbrju95lrd2vuux21bu" FOREIGN KEY ("temperature_id") REFERENCES "public"."dynamic_modulus_temperature" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table dynamic_modulus_specimen
-- ----------------------------
ALTER TABLE "public"."dynamic_modulus_specimen" ADD CONSTRAINT "fkr0bdp23fqkmg6vsjfvpd8tpcj" FOREIGN KEY ("test_id") REFERENCES "public"."dynamic_modulus_test" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table dynamic_modulus_temperature
-- ----------------------------
ALTER TABLE "public"."dynamic_modulus_temperature" ADD CONSTRAINT "fk77bd6j5b3to351fdx572xtgbf" FOREIGN KEY ("test_id") REFERENCES "public"."dynamic_modulus_test" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."dynamic_modulus_temperature" ADD CONSTRAINT "fks121ss96nu5ysx0t0i7bfpmqg" FOREIGN KEY ("specimen_id") REFERENCES "public"."dynamic_modulus_specimen" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table mixratio_asphalt
-- ----------------------------
ALTER TABLE "public"."mixratio_asphalt" ADD CONSTRAINT "fk_asphalt" FOREIGN KEY ("asphalt_id") REFERENCES "public"."asphalt_material" ("asphalt_id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."mixratio_asphalt" ADD CONSTRAINT "fk_mixratio_asphalt" FOREIGN KEY ("mixratio_id") REFERENCES "public"."mixratio" ("id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table mixratio_sand
-- ----------------------------
ALTER TABLE "public"."mixratio_sand" ADD CONSTRAINT "fk_mixratio_sand" FOREIGN KEY ("mixratio_id") REFERENCES "public"."mixratio" ("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."mixratio_sand" ADD CONSTRAINT "fk_sand" FOREIGN KEY ("sand_id") REFERENCES "public"."sand_material" ("sand_id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table mixratio_stone
-- ----------------------------
ALTER TABLE "public"."mixratio_stone" ADD CONSTRAINT "fk_mixratio_stone" FOREIGN KEY ("mixratio_id") REFERENCES "public"."mixratio" ("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."mixratio_stone" ADD CONSTRAINT "fk_stone" FOREIGN KEY ("stone_id") REFERENCES "public"."stone_material" ("stone_id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table mixture_task
-- ----------------------------
ALTER TABLE "public"."mixture_task" ADD CONSTRAINT "fkdlsbwxg5ce1700eg369vavbxa" FOREIGN KEY ("project_id") REFERENCES "public"."projects" ("project_id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table specimen_devices
-- ----------------------------
ALTER TABLE "public"."specimen_devices" ADD CONSTRAINT "fk_device" FOREIGN KEY ("device_id") REFERENCES "public"."devices" ("id") ON DELETE RESTRICT ON UPDATE NO ACTION;
ALTER TABLE "public"."specimen_devices" ADD CONSTRAINT "fk_specimen" FOREIGN KEY ("specimen_id") REFERENCES "public"."specimens" ("id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table specimens
-- ----------------------------
ALTER TABLE "public"."specimens" ADD CONSTRAINT "fk_mix_ratio" FOREIGN KEY ("mix_ratio_id") REFERENCES "public"."mixratio" ("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."specimens" ADD CONSTRAINT "fkkklhbep58li2n8k3luchl98ny" FOREIGN KEY ("created_by") REFERENCES "public"."users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
