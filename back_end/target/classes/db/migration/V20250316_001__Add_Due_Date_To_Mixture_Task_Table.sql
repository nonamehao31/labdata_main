-- 为混合料任务表添加截止日期字段
ALTER TABLE mixture_task
ADD COLUMN due_date VARCHAR(255) NULL COMMENT '任务截止日期，从项目截止日期获取';
