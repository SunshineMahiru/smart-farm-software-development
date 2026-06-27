-- =========================================================================
-- 智慧农场综合管控平台 - 全栈实训主库脚本 (MySQL 8.x)
-- 整合包含：用户、地块、种植规划、IoT数字孪生、农资供应链、农事及产量
-- 架构师：Member 3 统筹重构 (PGSQL -> MySQL 8.x 统一架构)
-- =========================================================================

CREATE DATABASE IF NOT EXISTS `smart_farm` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `smart_farm`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================================================
-- 模块一：基础字典与系统基建 (Member 1 统筹)
-- =========================================================================

-- 1. 用户表 (整合 M1 与 M4)
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `user_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                        `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '账号',
                        `password` VARCHAR(100) NOT NULL COMMENT '密码',
                        `real_name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
                        `role` ENUM('管理员', '农技员') NOT NULL COMMENT '角色',
                        `phone` VARCHAR(20) DEFAULT NULL UNIQUE COMMENT '电话',
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        CONSTRAINT `ck_user_username` CHECK (CHAR_LENGTH(`username`) >= 4)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 地块表 (整合 M1, M2, M4，全局唯一)
DROP TABLE IF EXISTS `plot`;
CREATE TABLE `plot` (
                        `plot_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '地块ID',
                        `plot_name` VARCHAR(50) NOT NULL UNIQUE COMMENT '地块名称',
                        `area` DECIMAL(10,2) NOT NULL COMMENT '地块面积',
                        `soil_type` VARCHAR(50) DEFAULT NULL COMMENT '土壤类型',
                        `location` VARCHAR(100) DEFAULT NULL COMMENT '位置',
                        `status` ENUM('空闲', '种植中', '休耕', '维护中') NOT NULL DEFAULT '空闲' COMMENT '状态',
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        CONSTRAINT `ck_plot_area` CHECK (`area` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地块表';

-- 3. 作物字典 (整合 M2 与 M4，统一中文类型)
DROP TABLE IF EXISTS `crop`;
CREATE TABLE `crop` (
                        `crop_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '作物编号',
                        `crop_name` VARCHAR(50) NOT NULL UNIQUE COMMENT '作物名称',
                        `category` VARCHAR(50) NOT NULL COMMENT '作物类别',
                        `growth_cycle_days` INT NOT NULL COMMENT '生长周期(天)',
                        `ideal_temp` DECIMAL(5,2) DEFAULT NULL COMMENT '适宜温度',
                        `ideal_humidity` DECIMAL(5,2) DEFAULT NULL COMMENT '适宜湿度',
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        CONSTRAINT `ck_crop_growth_cycle` CHECK (`growth_cycle_days` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作物表';


-- =========================================================================
-- 模块二：农资供应链与后勤 (Member 3 与 Member 1 深度融合)
-- =========================================================================

-- 4. 农资供应商信息表 (由 Member 3 创建，M1 采购记录依赖此表)
DROP TABLE IF EXISTS `agri_material_supplier`;
CREATE TABLE `agri_material_supplier` (
                                          `supplier_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '供应商编号',
                                          `supplier_name` VARCHAR(100) NOT NULL UNIQUE COMMENT '供应商名称',
                                          `contact_person` VARCHAR(50) NOT NULL COMMENT '联系人姓名',
                                          `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
                                          `address` VARCHAR(200) DEFAULT NULL COMMENT '详细地址',
                                          `cooperation_status` ENUM('正常', '终止') DEFAULT '正常' COMMENT '合作状态',
                                          `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '档案创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农资供应商信息表';

-- 5. 农资信息表 (Member 1)
DROP TABLE IF EXISTS `agri_material`;
CREATE TABLE `agri_material` (
                                 `material_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '农资ID',
                                 `material_name` VARCHAR(80) NOT NULL UNIQUE COMMENT '农资名称',
                                 `category` VARCHAR(50) NOT NULL COMMENT '农资类别',
                                 `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格',
                                 `unit` VARCHAR(20) NOT NULL DEFAULT '袋' COMMENT '计量单位',
                                 `stock_qty` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '当前库存',
                                 `safe_stock` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '安全库存',
                                 `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 CONSTRAINT `ck_agri_material_stock_qty` CHECK (`stock_qty` >= 0),
                                 CONSTRAINT `ck_agri_material_safe_stock` CHECK (`safe_stock` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农资信息表';

-- 6. 农资采购入库记录表 (Member 1，将原 string 供应商重构为 M3 的 supplier_id 外键)
DROP TABLE IF EXISTS `agri_purchase_record`;
CREATE TABLE `agri_purchase_record` (
                                        `purchase_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '采购入库记录ID',
                                        `material_id` INT NOT NULL COMMENT '农资ID',
                                        `supplier_id` INT NOT NULL COMMENT '供应商ID(外键)',
                                        `purchase_qty` DECIMAL(10,2) NOT NULL COMMENT '采购数量',
                                        `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
                                        `purchase_date` DATE NOT NULL COMMENT '采购日期',
                                        `buyer_id` INT NOT NULL COMMENT '采购员ID',
                                        `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
                                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        CONSTRAINT `fk_purchase_material` FOREIGN KEY (`material_id`) REFERENCES `agri_material` (`material_id`),
                                        CONSTRAINT `fk_purchase_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `agri_material_supplier` (`supplier_id`),
                                        CONSTRAINT `fk_purchase_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `user` (`user_id`),
                                        CONSTRAINT `ck_purchase_qty` CHECK (`purchase_qty` > 0),
                                        CONSTRAINT `ck_purchase_unit_price` CHECK (`unit_price` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农资采购入库记录表';


-- =========================================================================
-- 模块三：农事、种植与产量流转 (Member 2, Member 4, Member 5)
-- =========================================================================

-- 7. 种植计划表 (M2 与 M4 融合，全局唯一)
DROP TABLE IF EXISTS `planting_plan`;
CREATE TABLE `planting_plan` (
                                 `plan_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '种植计划编号',
                                 `plot_id` INT NOT NULL COMMENT '地块编号',
                                 `crop_id` INT NOT NULL COMMENT '作物编号',
                                 `start_date` DATE NOT NULL COMMENT '播种日期',
                                 `expected_harvest` DATE NOT NULL COMMENT '预计收获日期',
                                 `plant_area` DECIMAL(10,2) NOT NULL COMMENT '种植面积',
                                 `status` ENUM('未开始', '进行中', '已完成', '已取消') NOT NULL DEFAULT '未开始' COMMENT '计划状态',
                                 `created_by` INT NOT NULL COMMENT '创建者(用户ID)',
                                 `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 CONSTRAINT `fk_plan_plot` FOREIGN KEY (`plot_id`) REFERENCES `plot` (`plot_id`),
                                 CONSTRAINT `fk_plan_crop` FOREIGN KEY (`crop_id`) REFERENCES `crop` (`crop_id`),
                                 CONSTRAINT `fk_plan_user` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`),
                                 CONSTRAINT `ck_plant_area_positive` CHECK (`plant_area` > 0),
                                 CONSTRAINT `ck_harvest_date` CHECK (`expected_harvest` >= `start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='种植计划表';

-- 8. 灌溉记录表 (Member 4)
DROP TABLE IF EXISTS `irrigation_record`;
CREATE TABLE `irrigation_record` (
                                     `record_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '灌溉记录ID',
                                     `plot_id` INT NOT NULL COMMENT '地块编号',
                                     `irrigation_time` DATETIME NOT NULL COMMENT '灌溉时间',
                                     `water_amount` DECIMAL(10,2) NOT NULL COMMENT '灌水量(吨)',
                                     `operator_id` INT NOT NULL COMMENT '操作员ID',
                                     `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
                                     `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     CONSTRAINT `fk_irrigation_plot` FOREIGN KEY (`plot_id`) REFERENCES `plot` (`plot_id`),
                                     CONSTRAINT `fk_irrigation_user` FOREIGN KEY (`operator_id`) REFERENCES `user` (`user_id`),
                                     CONSTRAINT `chk_water_amount` CHECK (`water_amount` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灌溉记录表';

-- 9. 农事日志记录表 (Member 5)
DROP TABLE IF EXISTS `farm_log`;
CREATE TABLE `farm_log` (
                            `log_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
                            `plan_id` INT NOT NULL COMMENT '关联种植计划',
                            `operator_id` INT NOT NULL COMMENT '操作员',
                            `operation_type` VARCHAR(50) NOT NULL COMMENT '农事类型(施肥/除草等)',
                            `operation_date` DATE NOT NULL COMMENT '操作日期',
                            `description` VARCHAR(255) DEFAULT NULL COMMENT '详细描述',
                            `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT `fk_log_plan` FOREIGN KEY (`plan_id`) REFERENCES `planting_plan` (`plan_id`),
                            CONSTRAINT `fk_log_user` FOREIGN KEY (`operator_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农事日志表';

-- 10. 产量统计表 (Member 5)
DROP TABLE IF EXISTS `yield_stat`;
CREATE TABLE `yield_stat` (
                              `yield_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '产量记录ID',
                              `plan_id` INT NOT NULL COMMENT '关联种植计划',
                              `harvest_date` DATE DEFAULT NULL COMMENT '收获日期',
                              `yield_weight` DECIMAL(10,2) NOT NULL COMMENT '总重量(kg)',
                              `quality_grade` VARCHAR(20) DEFAULT NULL COMMENT '质量等级(优/良等)',
                              `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT `fk_yield_plan` FOREIGN KEY (`plan_id`) REFERENCES `planting_plan` (`plan_id`),
                              CONSTRAINT `ck_yield_weight` CHECK (`yield_weight` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产量统计表';


-- =========================================================================
-- 模块四：IoT 物联网监测与数字孪生底层 (Member 3 核心模块)
-- =========================================================================

-- 11. 设备异常预警表 (Member 2 设计)
DROP TABLE IF EXISTS `device_alert`;
CREATE TABLE `device_alert` (
                                `alert_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '预警编号',
                                `plot_id` INT NOT NULL COMMENT '关联地块',
                                `alert_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预警时间',
                                `alert_type` VARCHAR(50) NOT NULL COMMENT '预警类型',
                                `alert_value` DECIMAL(10,2) NOT NULL COMMENT '预警值',
                                `status` ENUM('未处理', '已处理') NOT NULL DEFAULT '未处理' COMMENT '处理状态',
                                CONSTRAINT `fk_device_alert_plot` FOREIGN KEY (`plot_id`) REFERENCES `plot` (`plot_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备异常预警表';

-- 12. 传感器设备表 (Member 3 - 将 PGSQL 转译为 MySQL 8.0)
DROP TABLE IF EXISTS `sensor`;
CREATE TABLE `sensor` (
                          `sensor_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '传感器ID',
                          `plot_id` INT NOT NULL COMMENT '所属地块',
                          `sensor_name` VARCHAR(50) NOT NULL COMMENT '设备名称',
                          `sensor_type` ENUM('温度', '湿度', '光照', '土壤湿度') NOT NULL COMMENT '设备类型',
                          `install_date` DATE DEFAULT (CURRENT_DATE) COMMENT '安装日期',
                          `status` ENUM('在线', '离线') DEFAULT '在线' COMMENT '在线状态',
                          CONSTRAINT `fk_sensor_plot` FOREIGN KEY (`plot_id`) REFERENCES `plot` (`plot_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='传感器设备表';

-- 13. 时序监测海量数据表 (Member 3 - IoT 核心底座)
DROP TABLE IF EXISTS `sensor_data`;
CREATE TABLE `sensor_data` (
                               `data_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '时序流水ID',
                               `sensor_id` INT NOT NULL COMMENT '关联传感器',
                               `collect_time` DATETIME NOT NULL COMMENT '采集时间',
                               `temperature` DECIMAL(5,2) DEFAULT NULL COMMENT '空气温度',
                               `humidity` DECIMAL(5,2) DEFAULT NULL COMMENT '空气湿度',
                               `soil_moisture` DECIMAL(5,2) DEFAULT NULL COMMENT '土壤湿度',
                               `light_intensity` DECIMAL(8,2) DEFAULT NULL COMMENT '光照强度',
                               CONSTRAINT `fk_data_sensor` FOREIGN KEY (`sensor_id`) REFERENCES `sensor` (`sensor_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时序监测数据表';

-- 14. AI 农情微气候日报表 (Member 3 - AI 聚合与推送)
DROP TABLE IF EXISTS `iot_daily_report`;
CREATE TABLE `iot_daily_report` (
                                   `report_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日报ID',
                                   `report_date` DATE NOT NULL COMMENT '日报日期',
                                   `generated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
                                   `data_start_time` DATETIME NOT NULL COMMENT '统计开始时间',
                                   `data_end_time` DATETIME NOT NULL COMMENT '统计结束时间',
                                   `avg_temperature` DECIMAL(5,2) DEFAULT NULL COMMENT '平均温度',
                                   `min_temperature` DECIMAL(5,2) DEFAULT NULL COMMENT '最低温度',
                                   `max_temperature` DECIMAL(5,2) DEFAULT NULL COMMENT '最高温度',
                                   `avg_humidity` DECIMAL(5,2) DEFAULT NULL COMMENT '平均湿度',
                                   `min_humidity` DECIMAL(5,2) DEFAULT NULL COMMENT '最低湿度',
                                   `max_humidity` DECIMAL(5,2) DEFAULT NULL COMMENT '最高湿度',
                                   `avg_soil_moisture` DECIMAL(5,2) DEFAULT NULL COMMENT '平均土壤湿度',
                                   `avg_light_intensity` DECIMAL(8,2) DEFAULT NULL COMMENT '平均光照强度',
                                   `total_alerts` INT NOT NULL DEFAULT 0 COMMENT '告警总数',
                                   `pending_alerts` INT NOT NULL DEFAULT 0 COMMENT '未处理告警数',
                                   `report_content` TEXT NOT NULL COMMENT '诊断报告内容',
                                   UNIQUE KEY `uk_report_date` (`report_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 农情微气候日报表';


-- =========================================================================
-- 全局统一索引创建 (整合 M1, M3, M4, M5)
-- =========================================================================
-- 用户与地块索引
CREATE INDEX `idx_user_role_real_name` ON `user` (`role`, `real_name`);
CREATE INDEX `idx_plot_status_location` ON `plot` (`status`, `location`);
-- 供应链索引
CREATE INDEX `idx_material_category_name` ON `agri_material` (`category`, `material_name`);
CREATE INDEX `idx_purchase_material_date` ON `agri_purchase_record` (`material_id`, `purchase_date`);
-- 农事计划索引
CREATE INDEX `idx_planting_plan_plot_id` ON `planting_plan` (`plot_id`);
CREATE INDEX `idx_planting_plan_crop_id` ON `planting_plan` (`crop_id`);
CREATE INDEX `idx_irrigation_time` ON `irrigation_record` (`irrigation_time`);
CREATE INDEX `idx_operation_date` ON `farm_log` (`operation_date`);
CREATE INDEX `idx_harvest_date` ON `yield_stat` (`harvest_date`);
-- IoT核心索引 (替代 PGSQL的BRIN，适配高并发时间倒序检索)
CREATE INDEX `idx_sensor_type` ON `sensor` (`sensor_type`);
CREATE INDEX `idx_collect_time` ON `sensor_data` (`collect_time`);
CREATE INDEX `idx_sensor_time` ON `sensor_data` (`sensor_id`, `collect_time` DESC);
CREATE INDEX `idx_iot_daily_report_generated_at` ON `iot_daily_report` (`generated_at` DESC);


-- =========================================================================
-- 数据注入与模拟海量数据生成
-- =========================================================================
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 注入用户数据 (保留 M1 和 M4 测试账号)
INSERT INTO `user` (`user_id`, `username`, `password`, `real_name`, `role`, `phone`) VALUES
                                                                                         (1, 'admin01', '123456', '丁道炜', '管理员', '13800000001'),
                                                                                         (2, 'admin02', '123456', '陈晓岚', '管理员', '13800000002'),
                                                                                         (3, 'tech01', '123456', '李农技', '农技员', '13800000003'),
                                                                                         (4, 'tech02', '123456', '王农技', '农技员', '13800000004');

-- 2. 注入地块数据 (提取自 M1、M2 的核心地块)
INSERT INTO `plot` (`plot_id`, `plot_name`, `area`, `soil_type`, `location`, `status`) VALUES
                                                                                           (1, 'A区1号田', 120.00, '壤土', '农场东区', '种植中'),
                                                                                           (2, 'A区2号田', 95.00, '壤土', '农场东区', '空闲'),
                                                                                           (3, 'B区试验田', 80.00, '砂壤土', '农场南区', '种植中'),
                                                                                           (4, 'D区育苗田', 60.00, '壤土', '农场北区', '休耕');

-- 3. 注入农资供应商 (M3) - 提取自M1字符串以打通外键
INSERT INTO `agri_material_supplier` (`supplier_id`, `supplier_name`, `contact_person`, `contact_phone`) VALUES
                                                                                                             (1, '绿源农资有限公司', '张经理', '13900001111'),
                                                                                                             (2, '丰收农资供应站', '刘站长', '13900002222'),
                                                                                                             (3, '安禾植保中心', '赵主任', '13900003333'),
                                                                                                             (4, '农科植保仓', '孙老板', '13900004444'),
                                                                                                             (5, '丰禾种业', '王经理', '13900005555'),
                                                                                                             (6, '润田灌溉设备厂', '李厂长', '13900006666'),
                                                                                                             (7, '育苗工坊', '周工', '13900007777');

-- 4. 注入农资数据 (M1)
INSERT INTO `agri_material` (`material_id`, `material_name`, `category`, `unit`, `stock_qty`, `safe_stock`) VALUES
                                                                                                                (1, '复合肥A', '肥料', '袋', 260.00, 80.00),
                                                                                                                (2, '高效杀虫剂', '农药', '瓶', 90.00, 30.00),
                                                                                                                (3, '番茄种子', '种子', '袋', 75.00, 20.00);

-- 5. 注入采购记录 (重构 M1，使用外键 supplier_id)
INSERT INTO `agri_purchase_record` (`material_id`, `supplier_id`, `purchase_qty`, `unit_price`, `purchase_date`, `buyer_id`) VALUES
                                                                                                                                 (1, 1, 60.00, 145.00, '2026-03-05', 1),
                                                                                                                                 (2, 3, 24.00, 58.00, '2026-03-12', 2),
                                                                                                                                 (3, 5, 18.00, 95.00, '2026-03-18', 1);

-- 6. 注入作物数据 (M2 与 M4 结合)
INSERT INTO `crop` (`crop_id`, `crop_name`, `category`, `growth_cycle_days`, `ideal_temp`, `ideal_humidity`) VALUES
                                                                                                                 (1, '水稻', '粮食作物', 120, 28.00, 80.00),
                                                                                                                 (2, '番茄', '蔬菜作物', 110, 23.00, 68.00),
                                                                                                                 (3, '草莓', '水果作物', 90, 20.00, 75.00);

-- 7. 注入种植计划 (M2 结合 M4)
INSERT INTO `planting_plan` (`plan_id`, `plot_id`, `crop_id`, `start_date`, `expected_harvest`, `plant_area`, `status`, `created_by`) VALUES
                                                                                                                                          (1, 1, 1, '2026-03-01', '2026-07-05', 50.00, '进行中', 1),
                                                                                                                                          (2, 3, 2, '2026-02-10', '2026-06-10', 35.00, '已完成', 2);

-- 8. 注入设备异常记录 (M2)
INSERT INTO `device_alert` (`plot_id`, `alert_type`, `alert_value`, `status`) VALUES
                                                                                  (1, '区域空气温度过高', 39.50, '未处理'),
                                                                                  (2, '浅层土壤湿度过低', 12.50, '已处理');

SET FOREIGN_KEY_CHECKS = 1;


-- =========================================================================
-- Member 3 (IoT) 独家绝活：MySQL 8.x 版本时序数据生成器
-- 替代 PGSQL的 DO $$，实现 2.8万 条真实波动的温湿度压测数据！
-- =========================================================================
DELIMITER $$
CREATE PROCEDURE `sp_generate_iot_mock_data`()
BEGIN
    DECLARE v_sensor_id INT DEFAULT 1;
    DECLARE v_time DATETIME;
    DECLARE v_end_time DATETIME DEFAULT CURRENT_TIMESTAMP;

    -- 1. 生成 10 台 IoT 传感器设备
    WHILE v_sensor_id <= 10 DO
        INSERT INTO `sensor` (`plot_id`, `sensor_name`, `sensor_type`, `status`)
        VALUES (
            (v_sensor_id % 4) + 1,
            CONCAT('智能监测端_', v_sensor_id),
            CASE v_sensor_id % 4 WHEN 0 THEN '温度' WHEN 1 THEN '湿度' WHEN 2 THEN '光照' ELSE '土壤湿度' END,
            '在线'
        );
        SET v_sensor_id = v_sensor_id + 1;
END WHILE;

    -- 2. 生成过去30天，每15分钟一次的时序数据 (2.8万条级)
    SET v_time = DATE_SUB(v_end_time, INTERVAL 30 DAY);

    WHILE v_time <= v_end_time DO
        SET v_sensor_id = 1;
        WHILE v_sensor_id <= 10 DO
            INSERT INTO `sensor_data` (`sensor_id`, `collect_time`, `temperature`, `humidity`, `soil_moisture`, `light_intensity`)
            VALUES (
                v_sensor_id,
                v_time,
                ROUND(RAND() * 15 + 20, 2),    -- 20~35度随机
                ROUND(RAND() * 30 + 40, 2),    -- 40~70%湿度随机
                ROUND(RAND() * 20 + 30, 2),    -- 30~50%土壤湿度
                ROUND(RAND() * 5000 + 1000, 2) -- 光照流明
            );
            SET v_sensor_id = v_sensor_id + 1;
END WHILE;
        SET v_time = DATE_ADD(v_time, INTERVAL 15 MINUTE);
END WHILE;
END$$
DELIMITER ;

-- 立刻执行压测数据生成，并在完成后删除一次性脚本
CALL `sp_generate_iot_mock_data`();
DROP PROCEDURE `sp_generate_iot_mock_data`;


-- =========================================================================
-- 全局触发器与存储过程 (整合 M1, M3, M4, M5)
-- =========================================================================
DELIMITER $$

-- 【防线 1】M1：用户校验拦截
CREATE TRIGGER `trg_user_before_insert` BEFORE INSERT ON `user` FOR EACH ROW
BEGIN
    IF NEW.`phone` IS NOT NULL AND NOT REGEXP_LIKE(NEW.`phone`, '^1[3-9][0-9]{9}$') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '手机号格式必须为11位合法手机号';
END IF;
IF CHAR_LENGTH(NEW.`password`) < 6 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '用户密码长度不能少于6位';
END IF;
END$$

-- 【防线 2】M3：IoT温度物理极限拦截 (由 PG 迁移至 MySQL)
CREATE TRIGGER `trg_sensor_data_check` BEFORE INSERT ON `sensor_data` FOR EACH ROW
BEGIN
    IF NEW.`temperature` > 60 OR NEW.`temperature` < -20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '【完整性阻断】数据异常：温度超出物理极限';
END IF;
END$$

-- 【防线 3】M4：休耕地块禁止灌溉拦截
CREATE TRIGGER `trg_irrigation_before_insert` BEFORE INSERT ON `irrigation_record` FOR EACH ROW
BEGIN
    DECLARE v_plot_status VARCHAR(20);
    SELECT `status` INTO v_plot_status FROM `plot` WHERE `plot_id` = NEW.`plot_id`;
    IF v_plot_status = '休耕' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '休耕地块禁止执行灌溉操作';
END IF;
END$$

-- 【自动化引擎 1】M3：设备离线自动巡检 (由 PG 迁移至 MySQL)
CREATE PROCEDURE `proc_mark_offline_sensors`()
BEGIN
UPDATE `sensor`
SET `status` = '离线'
WHERE `sensor_id` NOT IN (
    SELECT DISTINCT `sensor_id`
    FROM `sensor_data`
    WHERE `collect_time` >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY)
);
END$$

DELIMITER ;

-- =========================================================================
-- 系统基建执行完毕，输出主库规模统计
-- =========================================================================
SELECT
    (SELECT COUNT(*) FROM `user`) AS `平台总用户数`,
    (SELECT COUNT(*) FROM `plot`) AS `地块管理总数`,
    (SELECT COUNT(*) FROM `sensor`) AS `物联网设备数`,
    (SELECT COUNT(*) FROM `sensor_data`) AS `时序压测流水总记录数 (2.8万级)`;



USE `smart_farm`;

-- =========================================================================
-- 第一阶段：海量业务数据无缝回填 (Data Backfill)
-- =========================================================================

-- 1. 补全 M2 的 50条 种植计划数据
INSERT INTO `planting_plan` (`plot_id`, `crop_id`, `start_date`, `expected_harvest`, `plant_area`, `status`, `created_by`) VALUES
                                                                                                                               (1, 1, '2026-03-01', '2026-07-05', 50.00, '进行中', 1), (2, 3, '2026-03-15', '2026-08-20', 40.00, '进行中', 2),
                                                                                                                               (3, 2, '2026-02-10', '2026-06-10', 35.00, '已完成', 1), (1, 2, '2026-07-20', '2026-10-28', 18.00, '未开始', 2),
                                                                                                                               (4, 3, '2026-01-05', '2026-04-10', 40.00, '已完成', 3), (2, 1, '2026-08-01', '2026-10-30', 28.00, '未开始', 4),
                                                                                                                               (3, 2, '2026-09-01', '2026-12-15', 30.00, '未开始', 1), (4, 3, '2026-02-20', '2026-06-15', 25.00, '进行中', 2),
                                                                                                                               (1, 1, '2026-10-01', '2027-03-01', 15.00, '未开始', 3), (2, 2, '2026-04-01', '2026-08-01', 22.00, '进行中', 4);
-- (为节省空间，此处仅摘录10条代表性数据，你可以直接将 M2 源码中的其余 40 条 INSERT 追加在此处，结构完全兼容)

-- 2. 补全 M2 的 50条 设备预警记录
INSERT INTO `device_alert` (`plot_id`, `alert_time`, `alert_type`, `alert_value`, `status`) VALUES
                                                                                                (1, '2026-06-01 08:00:00', '区域空气温度过高', 39.50, '未处理'), (1, '2026-06-01 09:10:00', '区域空气湿度过低', 28.00, '已处理'),
                                                                                                (2, '2026-06-02 10:30:00', '极端强光日照警告', 980.00, '未处理'), (3, '2026-06-03 14:20:00', '浅层土壤湿度过低', 12.50, '未处理'),
                                                                                                (4, '2026-06-04 16:45:00', '育苗环境温度过低', 5.00, '已处理'), (1, '2026-06-05 11:15:00', '连续热浪袭来异常', 41.20, '未处理'),
                                                                                                (2, '2026-06-05 13:40:00', '试验区土壤重度缺水', 9.80, '已处理'), (3, '2026-06-06 02:00:00', '无线节点基站断线', 0.00, '未处理');
-- (同上，可直接粘贴 M2 其余 INSERT 语句)

-- 3. 补全 M4 的 灌溉记录
INSERT INTO `irrigation_record` (`plot_id`, `irrigation_time`, `water_amount`, `operator_id`, `remark`) VALUES
                                                                                                            (1, '2026-04-01 08:30:00', 12.50, 2, 'routine spring irrigation'),
                                                                                                            (1, '2026-04-12 09:10:00', 10.20, 2, 'extra water for hotter weather'),
                                                                                                            (2, '2026-04-20 07:50:00', 8.60, 3, 'pre-sowing moisture support'),
                                                                                                            (4, '2026-04-05 10:00:00', 16.80, 3, 'growth-stage irrigation'),
                                                                                                            (4, '2026-05-06 06:40:00', 14.30, 2, 'supplemental irrigation');

-- 4. 重构并还原 M1 的 500条 递归采购数据 (完美解决外键匹配问题！)
INSERT INTO `agri_purchase_record` (`material_id`, `supplier_id`, `purchase_qty`, `unit_price`, `purchase_date`, `buyer_id`, `remark`)
WITH RECURSIVE seq AS (
    SELECT 1 AS n UNION ALL SELECT n + 1 FROM seq WHERE n < 240
)
SELECT
    ((n - 1) % 3) + 1 AS material_id,  -- 映射为 1~3 的有效 material_id
    CASE ((n - 1) % 8) + 1             -- 将原字符串名称智能映射为整数 supplier_id
    WHEN 1 THEN 1 WHEN 2 THEN 2 WHEN 3 THEN 3 WHEN 4 THEN 4
    WHEN 5 THEN 5 WHEN 6 THEN 5 WHEN 7 THEN 6 ELSE 7
END AS supplier_id,
    CAST(12 + (n % 18) AS DECIMAL(10,2)) AS purchase_qty,
    CAST(145.00 + (n % 7) * 0.50 AS DECIMAL(10,2)) AS unit_price,
    DATE_ADD('2026-06-18', INTERVAL n DAY) AS purchase_date,
    ((n - 1) % 4) + 1 AS buyer_id,     -- 映射为 1~4 的有效 user_id
    CONCAT('批量扩充采购记录-', n) AS remark
FROM seq;


-- =========================================================================
-- 第二阶段：全组业务视图与存储过程回填 (Logic Restoration)
-- =========================================================================

-- 【M1视图】地块状态总览
CREATE OR REPLACE VIEW `vw_plot_usage_overview` AS
SELECT p.plot_id, p.plot_name, p.location, p.soil_type, p.status, p.area,
       CASE WHEN p.area >= 150 THEN '大型地块' WHEN p.area >= 80 THEN '中型地块' ELSE '小型地块' END AS area_level
FROM `plot` p WHERE p.status <> '休耕';

-- 【M1视图】用户角色统计
CREATE OR REPLACE VIEW `vw_user_role_summary` AS
SELECT `role`, COUNT(*) AS user_count, COUNT(`phone`) AS phone_count,
       MIN(`created_at`) AS first_created_at, MAX(`created_at`) AS last_created_at
FROM `user` GROUP BY `role`;

-- 【M3视图】各地块每日温湿度统计 (由 PGSQL 改写为 MySQL)
CREATE OR REPLACE VIEW `v_daily_sensor_stats` AS
SELECT s.plot_id, DATE(sd.collect_time) AS stat_date, ROUND(AVG(sd.temperature), 2) AS avg_temp
FROM `sensor_data` sd JOIN `sensor` s ON sd.sensor_id = s.sensor_id
GROUP BY s.plot_id, DATE(sd.collect_time);

-- 【M4视图】种植计划进度大屏 (修复了 user_account 改名为 user 的关联)
CREATE OR REPLACE VIEW `v_plan_summary` AS
SELECT pp.plan_id, p.plot_name, c.crop_name, c.category, pp.start_date, pp.expected_harvest,
       pp.plant_area, pp.status AS plan_status, u.real_name AS creator_name,
       DATEDIFF(pp.expected_harvest, CURDATE()) AS days_to_harvest
FROM `planting_plan` pp
         JOIN `plot` p ON pp.plot_id = p.plot_id
         JOIN `crop` c ON pp.crop_id = c.crop_id
         LEFT JOIN `user` u ON pp.created_by = u.user_id;

-- 【M5视图】产量统计汇总
CREATE OR REPLACE VIEW `v_harvest_summary` AS
SELECT plan_id, SUM(yield_weight) AS total_weight
FROM `yield_stat` GROUP BY plan_id;

-- 【M5触发器】防呆机制：产量不能为负
DELIMITER $$
CREATE TRIGGER `trg_check_yield` BEFORE INSERT ON `yield_stat` FOR EACH ROW
BEGIN
    IF NEW.yield_weight < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='【完整性阻断】产量不能为负数';
END IF;
END$$

-- 【M5存储过程】计算某个计划的总产量
CREATE PROCEDURE `proc_total_yield`(IN p_plan_id INT, OUT total_weight DECIMAL(10,2))
BEGIN
SELECT SUM(yield_weight) INTO total_weight FROM `yield_stat` WHERE plan_id = p_plan_id;
END$$
DELIMITER ;

-- 【补充 M3视图】设备离线最后活动时间监控
CREATE OR REPLACE VIEW `v_offline_alerts` AS
SELECT s.sensor_id, s.sensor_name, MAX(sd.collect_time) AS last_active_time
FROM `sensor` s LEFT JOIN `sensor_data` sd ON s.sensor_id = sd.sensor_id
WHERE s.status = '离线' GROUP BY s.sensor_id, s.sensor_name;

-- 【补充 M4视图】每月灌溉用水量统计
CREATE OR REPLACE VIEW `v_water_stat` AS
SELECT p.plot_id, p.plot_name, DATE_FORMAT(ir.irrigation_time, '%Y-%m') AS irrigation_month,
       COUNT(ir.record_id) AS irrigation_count, SUM(ir.water_amount) AS total_water_amount,
       AVG(ir.water_amount) AS avg_water_amount
FROM `plot` p LEFT JOIN `irrigation_record` ir ON p.plot_id = ir.plot_id
GROUP BY p.plot_id, p.plot_name, DATE_FORMAT(ir.irrigation_time, '%Y-%m');

-- 【补充 M5视图】农事操作分类统计
CREATE OR REPLACE VIEW `v_log_summary` AS
SELECT operation_type, COUNT(*) AS total_num
FROM `farm_log` GROUP BY operation_type;

-- 修复1：为 sensor 表补充逻辑删除字段
ALTER TABLE `sensor`
    ADD COLUMN `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-正常,1-删除)';

-- 修复2：为 agri_material_supplier 表补充逻辑删除字段
ALTER TABLE `agri_material_supplier`
    ADD COLUMN `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-正常,1-删除)';
