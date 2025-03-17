-- 向沥青任务表添加接受人和接受时间字段
ALTER TABLE asphalt_task
ADD COLUMN acceptor VARCHAR(255),
ADD COLUMN accept_time BIGINT;
