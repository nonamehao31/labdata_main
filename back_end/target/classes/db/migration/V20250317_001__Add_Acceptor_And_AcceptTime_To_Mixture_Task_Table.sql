-- 添加接受人和接受时间字段到mixture_task表
ALTER TABLE mixture_task 
ADD COLUMN acceptor VARCHAR(100),
ADD COLUMN accept_time BIGINT;

-- 添加注释
COMMENT ON COLUMN mixture_task.acceptor IS '任务接受人';
COMMENT ON COLUMN mixture_task.accept_time IS '任务接受时间（时间戳）';
