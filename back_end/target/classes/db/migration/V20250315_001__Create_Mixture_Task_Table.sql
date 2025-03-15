-- 创建混合料任务支持表
CREATE TABLE IF NOT EXISTS support_mixture_task (
    task_id INT PRIMARY KEY,
    task_name VARCHAR(255) NOT NULL,
    task_type VARCHAR(50) NOT NULL
);

-- 插入初始数据
INSERT INTO support_mixture_task (task_id, task_name, task_type) VALUES
(1, '马歇尔稳定度试验', 'MINTURE'),
(2, '理论最大相对密度试验', 'MINTURE'),
(3, '体积密度试验', 'MINTURE'),
(4, '空隙率试验', 'MINTURE'),
(5, '飞散试验', 'MINTURE'),
(6, '动稳定度试验', 'MINTURE'),
(7, '沥青混合料车辙实验（汉堡车辙）', 'MINTURE'),
(8, '沥青混合料弯曲试验', 'MINTURE'),
(9, '动态模量试验', 'MINTURE'),
(10, '沥青混合料直接拉伸循环疲劳测黏弹损伤试验', 'MINTURE'),
(11, '沥青混合料四点弯曲疲劳寿命试验', 'MINTURE'),
(12, '沥青混合料单轴压缩试验(圆柱体法)', 'MINTURE'),
(13, '沥青混合料劈裂试验', 'MINTURE');
