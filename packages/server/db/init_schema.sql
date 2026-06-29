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
                                          `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '档案创建时间',
                                          `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记'
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
                          `install_date` DATE DEFAULT NULL COMMENT '安装日期',
                          `status` ENUM('在线', '离线') DEFAULT '在线' COMMENT '在线状态',
                          `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
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
CREATE INDEX `idx_grade_weight` ON `yield_stat` (`quality_grade`, `yield_weight`);
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

INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `phone`) VALUES
                                                                               ('tech03', '123456', '周农技', '农技员', '13800000005'),
                                                                               ('tech04', '123456', '赵农技', '农技员', '13800000006'),
                                                                               ('tech05', '123456', '孙农技', '农技员', '13800000007'),
                                                                               ('tech06', '123456', '吴农技', '农技员', '13800000008'),
                                                                               ('tech07', '123456', '郑农技', '农技员', '13800000009'),
                                                                               ('tech08', '123456', '冯农技', '农技员', '13800000010'),
                                                                               ('admin03', '123456', '许晓峰', '管理员', '13800000012'),
                                                                               ('admin04', '123456', '高雅楠', '管理员', '13800000013'),
                                                                               ('tech10', '123456', '何农技', '农技员', '13800000014'),
                                                                               ('tech11', '123456', '吕农技', '农技员', '13800000015'),
                                                                               ('tech12', '123456', '施农技', '农技员', '13800000016'),
                                                                               ('tech13', '123456', '曹农技', '农技员', '13800000017'),
                                                                               ('tech14', '123456', '袁农技', '农技员', '13800000018'),
                                                                               ('tech15', '123456', '潘农技', '农技员', '13800000019'),
                                                                               ('tech16', '123456', '杜农技', '农技员', '13800000020'),
                                                                               ('tech17', '123456', '蒋农技', '农技员', '13800000021'),
                                                                               ('tech18', '123456', '戴农技', '农技员', '13800000022'),
                                                                               ('tech19', '123456', '崔农技', '农技员', '13800000023'),
                                                                               ('admin05', '123456', '韩文博', '管理员', '13800000024'),
                                                                               ('admin06', '123456', '宋雨泽', '管理员', '13800000025'),
                                                                               ('admin07', '123456', '顾明轩', '管理员', '13800000026'),
                                                                               ('admin08', '123456', '邵清妍', '管理员', '13800000027'),
                                                                               ('admin09', '123456', '陆嘉宁', '管理员', '13800000028'),
                                                                               ('tech20', '123456', '鲁农技', '农技员', '13800000029'),
                                                                               ('tech21', '123456', '韦农技', '农技员', '13800000030'),
                                                                               ('tech22', '123456', '昌农技', '农技员', '13800000031'),
                                                                               ('tech23', '123456', '马农技', '农技员', '13800000032'),
                                                                               ('tech24', '123456', '苗农技', '农技员', '13800000033'),
                                                                               ('tech25', '123456', '凤农技', '农技员', '13800000034'),
                                                                               ('tech26', '123456', '花农技', '农技员', '13800000035'),
                                                                               ('tech27', '123456', '方农技', '农技员', '13800000036'),
                                                                               ('tech28', '123456', '俞农技', '农技员', '13800000037'),
                                                                               ('tech29', '123456', '任农技', '农技员', '13800000038'),
                                                                               ('tech30', '123456', '柳农技', '农技员', '13800000039'),
                                                                               ('tech31', '123456', '唐农技', '农技员', '13800000040'),
                                                                               ('tech32', '123456', '罗农技', '农技员', '13800000041'),
                                                                               ('tech33', '123456', '毕农技', '农技员', '13800000042'),
                                                                               ('tech34', '123456', '郝农技', '农技员', '13800000043'),
                                                                               ('tech35', '123456', '邬农技', '农技员', '13800000044'),
                                                                               ('tech36', '123456', '安农技', '农技员', '13800000045'),
                                                                               ('tech37', '123456', '常农技', '农技员', '13800000046'),
                                                                               ('tech38', '123456', '乐农技', '农技员', '13800000047'),
                                                                               ('tech39', '123456', '于农技', '农技员', '13800000048'),
                                                                               ('tech40', '123456', '时农技', '农技员', '13800000049'),
                                                                               ('tech41', '123456', '傅农技', '农技员', '13800000050'),
                                                                               ('tech42', '123456', '皮农技', '农技员', '13800000051'),
                                                                               ('tech43', '123456', '卞农技', '农技员', '13800000052'),
                                                                               ('tech44', '123456', '齐农技', '农技员', '13800000053'),
                                                                               ('tech45', '123456', '康农技', '农技员', '13800000054'),
                                                                               ('tech46', '123456', '伍农技', '农技员', '13800000055'),
                                                                               ('tech47', '123456', '余农技', '农技员', '13800000056'),
                                                                               ('tech48', '123456', '元农技', '农技员', '13800000057'),
                                                                               ('tech49', '123456', '卜农技', '农技员', '13800000058'),
                                                                               ('tech50', '123456', '孟农技', '农技员', '13800000059'),
                                                                               ('tech51', '123456', '平农技', '农技员', '13800000060'),
                                                                               ('tech52', '123456', '黄农技', '农技员', '13800000061'),
                                                                               ('tech53', '123456', '和农技', '农技员', '13800000062'),
                                                                               ('tech54', '123456', '穆农技', '农技员', '13800000063'),
                                                                               ('tech55', '123456', '萧农技', '农技员', '13800000064'),
                                                                               ('tech56', '123456', '尹农技', '农技员', '13800000065'),
                                                                               ('tech57', '123456', '姚农技', '农技员', '13800000066'),
                                                                               ('tech58', '123456', '邵农技', '农技员', '13800000067'),
                                                                               ('tech59', '123456', '湛农技', '农技员', '13800000068'),
                                                                               ('tech60', '123456', '汪农技', '农技员', '13800000069'),
                                                                               ('tech61', '123456', '祁农技', '农技员', '13800000070'),
                                                                               ('tech62', '123456', '毛农技', '农技员', '13800000071'),
                                                                               ('tech63', '123456', '禹农技', '农技员', '13800000072'),
                                                                               ('tech64', '123456', '狄农技', '农技员', '13800000073'),
                                                                               ('tech65', '123456', '米农技', '农技员', '13800000074'),
                                                                               ('tech66', '123456', '贝农技', '农技员', '13800000075'),
                                                                               ('tech67', '123456', '明农技', '农技员', '13800000076'),
                                                                               ('tech68', '123456', '臧农技', '农技员', '13800000077'),
                                                                               ('tech69', '123456', '计农技', '农技员', '13800000078');

-- 2. 注入地块数据 (提取自 M1、M2 的核心地块)
INSERT INTO `plot` (`plot_id`, `plot_name`, `area`, `soil_type`, `location`, `status`) VALUES
                                                                                           (1, 'A区1号田', 120.00, '壤土', '农场东区', '种植中'),
                                                                                           (2, 'A区2号田', 95.00, '壤土', '农场东区', '空闲'),
                                                                                           (3, 'B区试验田', 80.00, '砂壤土', '农场南区', '种植中'),
                                                                                           (4, 'D区育苗田', 60.00, '壤土', '农场北区', '休耕');

INSERT INTO `plot` (`plot_name`, `area`, `soil_type`, `location`, `status`) VALUES
                                                                                ('B区育苗田', 60.00, '砂壤土', '农场南区', '休耕'),
                                                                                ('C区1号田', 150.00, '黏土', '农场西区', '种植中'),
                                                                                ('C区2号田', 88.00, '黏土', '农场西区', '空闲'),
                                                                                ('D区温室1', 45.00, '腐殖土', '农场北区', '种植中'),
                                                                                ('D区温室2', 40.00, '腐殖土', '农场北区', '空闲'),
                                                                                ('E区果园1', 200.00, '沙土', '农场东北区', '种植中'),
                                                                                ('F区轮作田', 110.00, '壤土', '农场西南区', '休耕'),
                                                                                ('A区3号田', 130.00, '壤土', '农场东区', '种植中'),
                                                                                ('A区4号田', 102.00, '壤土', '农场东区', '空闲'),
                                                                                ('B区2号田', 76.00, '砂壤土', '农场南区', '休耕'),
                                                                                ('B区3号田', 92.00, '砂壤土', '农场南区', '种植中'),
                                                                                ('C区3号田', 160.00, '黏土', '农场西区', '种植中'),
                                                                                ('C区4号田', 98.00, '黏土', '农场西区', '空闲'),
                                                                                ('D区温室3', 55.00, '腐殖土', '农场北区', '种植中'),
                                                                                ('D区温室4', 48.00, '腐殖土', '农场北区', '空闲'),
                                                                                ('E区果园2', 175.00, '沙土', '农场东北区', '种植中'),
                                                                                ('E区果园3', 185.00, '沙土', '农场东北区', '休耕'),
                                                                                ('F区轮作田2', 118.00, '壤土', '农场西南区', '空闲'),
                                                                                ('G区示范田', 140.00, '黑土', '农场中区', '种植中'),
                                                                                ('H区1号田', 126.00, '壤土', '农场东扩区', '种植中'),
                                                                                ('H区2号田', 94.00, '壤土', '农场东扩区', '空闲'),
                                                                                ('H区3号田', 88.00, '砂壤土', '农场东扩区', '种植中'),
                                                                                ('H区4号田', 72.00, '砂壤土', '农场东扩区', '休耕'),
                                                                                ('H区5号田', 134.00, '黑土', '农场东扩区', '种植中'),
                                                                                ('H区6号田', 101.00, '黑土', '农场东扩区', '空闲'),
                                                                                ('H区7号田', 115.00, '壤土', '农场东扩区', '种植中'),
                                                                                ('H区8号田', 86.00, '壤土', '农场东扩区', '空闲'),
                                                                                ('H区9号田', 149.00, '黏土', '农场东扩区', '种植中'),
                                                                                ('H区10号田', 97.00, '黏土', '农场东扩区', '休耕'),
                                                                                ('I区1号田', 162.00, '黏土', '农场西扩区', '种植中'),
                                                                                ('I区2号田', 108.00, '黏土', '农场西扩区', '空闲'),
                                                                                ('I区3号田', 74.00, '砂壤土', '农场西扩区', '休耕'),
                                                                                ('I区4号田', 91.00, '砂壤土', '农场西扩区', '种植中'),
                                                                                ('I区5号田', 155.00, '沙土', '农场西扩区', '种植中'),
                                                                                ('I区6号田', 120.00, '沙土', '农场西扩区', '空闲'),
                                                                                ('I区7号田', 68.00, '腐殖土', '农场西扩区', '休耕'),
                                                                                ('I区8号田', 83.00, '腐殖土', '农场西扩区', '种植中'),
                                                                                ('I区9号田', 142.00, '黑土', '农场西扩区', '种植中'),
                                                                                ('I区10号田', 99.00, '黑土', '农场西扩区', '空闲'),
                                                                                ('J区1号田', 117.00, '壤土', '农场南扩区', '种植中'),
                                                                                ('J区2号田', 89.00, '壤土', '农场南扩区', '空闲'),
                                                                                ('J区3号田', 77.00, '砂壤土', '农场南扩区', '休耕'),
                                                                                ('J区4号田', 96.00, '砂壤土', '农场南扩区', '种植中'),
                                                                                ('J区5号田', 168.00, '黏土', '农场南扩区', '种植中'),
                                                                                ('J区6号田', 111.00, '黏土', '农场南扩区', '空闲'),
                                                                                ('J区7号田', 58.00, '腐殖土', '农场南扩区', '休耕'),
                                                                                ('J区8号田', 64.00, '腐殖土', '农场南扩区', '种植中'),
                                                                                ('J区9号田', 137.00, '黑土', '农场南扩区', '种植中'),
                                                                                ('J区10号田', 103.00, '黑土', '农场南扩区', '空闲'),
                                                                                ('K区1号田', 124.00, '壤土', '农场北扩区', '种植中'),
                                                                                ('K区2号田', 90.00, '壤土', '农场北扩区', '空闲'),
                                                                                ('K区3号田', 82.00, '砂壤土', '农场北扩区', '休耕'),
                                                                                ('K区4号田', 98.00, '砂壤土', '农场北扩区', '种植中'),
                                                                                ('K区5号田', 172.00, '黏土', '农场北扩区', '种植中'),
                                                                                ('K区6号田', 113.00, '黏土', '农场北扩区', '空闲'),
                                                                                ('K区7号田', 62.00, '腐殖土', '农场北扩区', '休耕'),
                                                                                ('K区8号田', 70.00, '腐殖土', '农场北扩区', '种植中'),
                                                                                ('K区9号田', 144.00, '黑土', '农场北扩区', '种植中'),
                                                                                ('K区10号田', 106.00, '黑土', '农场北扩区', '空闲'),
                                                                                ('L区1号田', 132.00, '壤土', '农场中心区', '种植中'),
                                                                                ('L区2号田', 93.00, '壤土', '农场中心区', '空闲'),
                                                                                ('L区3号田', 79.00, '砂壤土', '农场中心区', '休耕'),
                                                                                ('L区4号田', 100.00, '砂壤土', '农场中心区', '种植中'),
                                                                                ('L区5号田', 178.00, '黏土', '农场中心区', '种植中'),
                                                                                ('L区6号田', 116.00, '黏土', '农场中心区', '空闲'),
                                                                                ('L区7号田', 66.00, '腐殖土', '农场中心区', '休耕'),
                                                                                ('L区8号田', 75.00, '腐殖土', '农场中心区', '种植中'),
                                                                                ('L区9号田', 151.00, '黑土', '农场中心区', '种植中'),
                                                                                ('L区10号田', 109.00, '黑土', '农场中心区', '空闲'),
                                                                                ('M区1号田', 128.00, '壤土', '农场水肥区', '种植中'),
                                                                                ('M区2号田', 92.00, '壤土', '农场水肥区', '空闲'),
                                                                                ('M区3号田', 81.00, '砂壤土', '农场水肥区', '休耕'),
                                                                                ('M区4号田', 104.00, '砂壤土', '农场水肥区', '种植中'),
                                                                                ('M区5号田', 181.00, '黏土', '农场水肥区', '种植中'),
                                                                                ('M区6号田', 119.00, '黏土', '农场水肥区', '空闲'),
                                                                                ('M区7号田', 69.00, '腐殖土', '农场水肥区', '休耕'),
                                                                                ('M区8号田', 78.00, '腐殖土', '农场水肥区', '种植中'),
                                                                                ('M区9号田', 147.00, '黑土', '农场水肥区', '种植中'),
                                                                                ('M区10号田', 112.00, '黑土', '农场水肥区', '空闲');

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
INSERT INTO `agri_material` (`material_id`, `material_name`, `category`, `specification`, `unit`, `stock_qty`, `safe_stock`) VALUES
                                                                                                                               (1, '复合肥A', '肥料', '40kg/袋', '袋', 260.00, 80.00),
                                                                                                                               (2, '尿素', '肥料', '50kg/袋', '袋', 320.00, 100.00),
                                                                                                                               (3, '高效杀虫剂', '农药', '500ml/瓶', '瓶', 90.00, 30.00),
                                                                                                                               (4, '多菌灵', '农药', '1kg/袋', '袋', 120.00, 40.00),
                                                                                                                               (5, '番茄种子', '种子', '1000粒/袋', '袋', 75.00, 20.00),
                                                                                                                               (6, '黄瓜种子', '种子', '1000粒/袋', '袋', 68.00, 20.00),
                                                                                                                               (7, '滴灌带', '灌溉', '100m/卷', '卷', 55.00, 15.00),
                                                                                                                               (8, '育苗盘', '农具', '72孔/个', '个', 180.00, 50.00);

-- 5. 注入采购记录 (重构 M1，使用外键 supplier_id)
INSERT INTO `agri_purchase_record` (`material_id`, `supplier_id`, `purchase_qty`, `unit_price`, `purchase_date`, `buyer_id`, `remark`) VALUES
                                                                                                                                           (1, 1, 60.00, 145.00, '2026-03-05', 1, '春耕备肥采购'),
                                                                                                                                           (3, 3, 24.00, 58.00, '2026-03-12', 2, '蔬菜区虫害预防'),
                                                                                                                                           (5, 5, 18.00, 95.00, '2026-03-18', 1, '番茄育苗计划');

-- 6. 注入作物数据 (M2 与 M4 结合)
INSERT INTO `crop` (`crop_id`, `crop_name`, `category`, `growth_cycle_days`, `ideal_temp`, `ideal_humidity`) VALUES
                                                                                                                 (1, '水稻', '粮食作物', 120, 28.00, 80.00),
                                                                                                                 (2, '玉米', '粮食作物', 100, 26.00, 65.00),
                                                                                                                 (3, '辣椒', '经济作物', 150, 24.00, 70.00),
                                                                                                                 (4, '番茄', '蔬菜作物', 110, 23.00, 68.00),
                                                                                                                 (5, '草莓', '水果作物', 90, 20.00, 75.00),
                                                                                                                 (6, '黄瓜', '蔬菜作物', 85, 25.00, 78.00),
                                                                                                                 (7, '小麦', '粮食作物', 130, 18.00, 60.00),
                                                                                                                 (8, '茄子', '蔬菜作物', 105, 22.00, 72.00),
                                                                                                                 (9, '西瓜', '水果作物', 95, 27.00, 70.00),
                                                                                                                 (10, '大豆', '经济作物', 115, 21.00, 66.00);

-- 7. M2 / M5 的业务数据在后续回填阶段统一补齐，避免与完整批量数据重复

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
INSERT INTO `planting_plan` (`plot_id`, `crop_id`, `start_date`, `expected_harvest`, `plant_area`, `status`, `created_by`)
WITH plan_seed AS (
    SELECT 1 AS plot_id, 1 AS crop_id, '2026-03-01' AS start_date, '2026-07-05' AS expected_harvest, 50.00 AS plant_area, '进行中' AS status UNION ALL
    SELECT 2, 3, '2026-03-15', '2026-08-20', 40.00, '进行中' UNION ALL
    SELECT 3, 4, '2026-02-10', '2026-06-10', 35.00, '已完成' UNION ALL
    SELECT 1, 2, '2026-07-20', '2026-10-28', 18.00, '未开始' UNION ALL
    SELECT 4, 5, '2026-01-05', '2026-04-10', 40.00, '已完成' UNION ALL
    SELECT 2, 6, '2026-08-01', '2026-10-30', 28.00, '未开始' UNION ALL
    SELECT 3, 2, '2026-09-01', '2026-12-15', 30.00, '未开始' UNION ALL
    SELECT 4, 8, '2026-02-20', '2026-06-15', 25.00, '进行中' UNION ALL
    SELECT 1, 7, '2026-10-01', '2027-03-01', 15.00, '未开始' UNION ALL
    SELECT 2, 10, '2026-04-01', '2026-08-01', 22.00, '进行中' UNION ALL
    SELECT 1, 4, '2025-05-01', '2025-08-20', 45.00, '已完成' UNION ALL
    SELECT 2, 2, '2025-06-01', '2025-09-10', 30.00, '已完成' UNION ALL
    SELECT 3, 6, '2025-04-15', '2025-07-10', 45.00, '已完成' UNION ALL
    SELECT 4, 7, '2025-09-15', '2026-01-20', 35.00, '已完成' UNION ALL
    SELECT 1, 9, '2026-05-01', '2026-08-05', 35.00, '进行中' UNION ALL
    SELECT 2, 8, '2026-05-10', '2026-08-25', 30.00, '进行中' UNION ALL
    SELECT 3, 1, '2026-04-20', '2026-08-20', 40.00, '进行中' UNION ALL
    SELECT 4, 3, '2026-06-01', '2026-11-05', 15.00, '进行中' UNION ALL
    SELECT 1, 5, '2026-11-15', '2027-02-15', 20.00, '未开始' UNION ALL
    SELECT 2, 7, '2026-11-01', '2027-03-10', 40.00, '未开始' UNION ALL
    SELECT 3, 9, '2026-12-01', '2027-03-05', 50.00, '未开始' UNION ALL
    SELECT 4, 10, '2026-10-15', '2027-02-10', 12.00, '未开始' UNION ALL
    SELECT 1, 3, '2025-01-10', '2025-06-15', 50.00, '已完成' UNION ALL
    SELECT 2, 1, '2025-02-20', '2025-06-25', 55.00, '已完成' UNION ALL
    SELECT 3, 5, '2025-03-01', '2025-06-01', 30.00, '已完成' UNION ALL
    SELECT 1, 6, '2026-04-01', '2026-07-15', 20.00, '进行中' UNION ALL
    SELECT 2, 4, '2026-05-01', '2026-09-10', 25.00, '进行中' UNION ALL
    SELECT 3, 8, '2026-03-01', '2026-07-01', 15.00, '进行中' UNION ALL
    SELECT 4, 9, '2026-02-15', '2026-06-15', 10.00, '已完成' UNION ALL
    SELECT 1, 10, '2026-08-15', '2026-12-01', 15.00, '未开始' UNION ALL
    SELECT 2, 5, '2026-09-01', '2026-12-10', 12.00, '未开始' UNION ALL
    SELECT 3, 7, '2026-07-01', '2026-11-15', 25.00, '未开始' UNION ALL
    SELECT 4, 1, '2026-11-01', '2027-03-15', 15.00, '未开始' UNION ALL
    SELECT 1, 8, '2025-03-15', '2025-07-01', 30.00, '已完成' UNION ALL
    SELECT 2, 9, '2025-04-01', '2025-08-01', 20.00, '已完成' UNION ALL
    SELECT 3, 3, '2025-05-10', '2025-10-15', 40.00, '已完成' UNION ALL
    SELECT 4, 2, '2025-02-01', '2025-06-01', 25.00, '已完成' UNION ALL
    SELECT 1, 5, '2026-05-20', '2026-09-20', 15.00, '进行中' UNION ALL
    SELECT 2, 1, '2026-06-01', '2026-10-10', 35.00, '进行中' UNION ALL
    SELECT 3, 10, '2026-05-15', '2026-09-15', 30.00, '进行中' UNION ALL
    SELECT 4, 4, '2026-04-10', '2026-08-01', 18.00, '进行中' UNION ALL
    SELECT 1, 7, '2025-11-01', '2026-03-15', 22.00, '已完成' UNION ALL
    SELECT 2, 6, '2025-10-15', '2026-02-10', 14.00, '已完成' UNION ALL
    SELECT 3, 2, '2026-01-10', '2026-05-01', 35.00, '已完成' UNION ALL
    SELECT 4, 3, '2026-02-01', '2026-07-10', 20.00, '进行中' UNION ALL
    SELECT 1, 9, '2026-09-15', '2026-12-20', 14.00, '未开始' UNION ALL
    SELECT 2, 8, '2026-10-01', '2027-01-15', 16.00, '未开始' UNION ALL
    SELECT 3, 5, '2026-10-10', '2027-01-20', 22.00, '未开始' UNION ALL
    SELECT 4, 6, '2026-09-01', '2026-12-10', 11.00, '未开始' UNION ALL
    SELECT 1, 2, '2026-12-01', '2027-04-01', 25.00, '未开始'
)
SELECT plot_id,
       crop_id,
       start_date,
       expected_harvest,
       plant_area,
       status,
       ((ROW_NUMBER() OVER ()) - 1) % 4 + 1 AS `created_by`
FROM plan_seed;

-- 2. 补全 M2 的 50条 设备预警记录
INSERT INTO `device_alert` (`plot_id`, `alert_time`, `alert_type`, `alert_value`, `status`) VALUES
                                                                                                (1, '2026-06-01 08:00:00', '区域空气温度过高', 39.50, '未处理'), (1, '2026-06-01 09:10:00', '区域空气湿度过低', 28.00, '已处理'),
                                                                                                (2, '2026-06-02 10:30:00', '极端强光日照警告', 980.00, '未处理'), (3, '2026-06-03 14:20:00', '浅层土壤湿度过低', 12.50, '未处理'),
                                                                                                (4, '2026-06-04 16:45:00', '育苗环境温度过低', 5.00, '已处理'), (1, '2026-06-05 11:15:00', '连续热浪袭来异常', 41.20, '未处理'),
                                                                                                (2, '2026-06-05 13:40:00', '试验区土壤重度缺水', 9.80, '已处理'), (3, '2026-06-06 02:00:00', '无线节点基站断线', 0.00, '未处理'),
                                                                                                (4, '2026-06-06 05:22:00', '夜间地表辐射寒冻', -2.50, '未处理'), (1, '2026-06-07 12:05:00', '大棚高湿凝露预警', 98.50, '已处理'),
                                                                                                (2, '2026-06-08 09:50:00', '紫外线光照超量', 1250.00, '未处理'), (3, '2026-06-08 17:10:00', '根系层干旱极值', 8.20, '未处理'),
                                                                                                (4, '2026-06-09 10:00:00', '环境控温上限突破', 38.60, '已处理'), (1, '2026-06-10 11:30:00', '极端干热风袭来', 15.20, '未处理'),
                                                                                                (2, '2026-06-11 04:12:00', '环境感知线缆离线', 0.00, '已处理'), (3, '2026-06-12 13:00:00', '蓄水区局部高温', 40.10, '未处理'),
                                                                                                (4, '2026-06-12 15:45:00', '土壤基质严重水分流失', 7.50, '已处理'), (1, '2026-06-13 22:15:00', '供电电压大幅波动', 0.00, '未处理'),
                                                                                                (2, '2026-06-14 08:30:00', '逆温层空气湿度急降', 22.10, '已处理'), (3, '2026-06-14 11:00:00', '多云导致连续光照不足', 1100.00, '未处理'),
                                                                                                (4, '2026-06-15 14:00:00', '幼苗床极度干燥', 11.20, '未处理'), (1, '2026-06-15 19:20:00', '强冷空气降温预警', 3.40, '已处理'),
                                                                                                (2, '2026-06-16 12:10:00', '冠层叶片表面过热', 37.90, '未处理'), (3, '2026-06-16 15:35:00', '采摘区土壤龟裂预警', 31.00, '已处理'),
                                                                                                (4, '2026-06-17 01:20:00', '夜间突发冰冻危害', 6.20, '未处理'), (1, '2026-06-17 10:00:00', '设备高温报警', 42.10, '未处理'),
                                                                                                (2, '2026-06-17 11:20:00', '棚内极度缺水', 10.50, '未处理'), (3, '2026-06-18 09:15:00', '日照强度越界', 1350.00, '已处理'),
                                                                                                (4, '2026-06-18 14:40:00', '极区低温寒害', 1.20, '未处理'), (1, '2026-06-19 03:10:00', '设备突发断电', 0.00, '已处理'),
                                                                                                (2, '2026-06-19 16:22:00', '空气湿度过度饱和', 99.10, '未处理'), (3, '2026-06-20 11:05:00', '极端干旱预警', 5.80, '已处理'),
                                                                                                (4, '2026-06-20 22:50:00', '夜间温控失灵', 35.60, '未处理'), (1, '2026-06-21 13:00:00', '正午太阳暴晒超标', 1180.00, '未处理'),
                                                                                                (2, '2026-06-21 17:40:00', '土壤含水量骤降', 11.50, '已处理'), (3, '2026-06-22 08:30:00', '空气温度骤升', 38.20, '未处理'),
                                                                                                (4, '2026-06-22 12:15:00', '环境极度干燥', 19.50, '已处理'), (1, '2026-06-23 04:00:00', '清晨霜冻预警', 2.10, '未处理'),
                                                                                                (2, '2026-06-23 15:30:00', '大棚热流聚集', 40.80, '未处理'), (3, '2026-06-24 10:20:00', '光照感应器故障', 0.00, '已处理'),
                                                                                                (4, '2026-06-24 19:10:00', '泥土表层极干', 7.20, '未处理'), (1, '2026-06-25 11:00:00', '极端高温热浪', 43.00, '未处理'),
                                                                                                (2, '2026-06-25 14:55:00', '蒸腾过快低湿', 16.80, '已处理'), (3, '2026-06-26 09:30:00', '光强突变异常', 1020.00, '未处理'),
                                                                                                (4, '2026-06-26 23:10:00', '夜间大幅降温', 4.50, '已处理'), (1, '2026-06-27 13:20:00', '温室热害警报', 39.90, '未处理'),
                                                                                                (2, '2026-06-27 16:40:00', '作物根系重度缺水', 6.90, '未处理'), (3, '2026-06-28 10:10:00', '光照过饱和异常', 1210.00, '已处理'),
                                                                                                (4, '2026-06-28 21:00:00', '硬件信号传输中断', 0.00, '未处理'), (1, '2026-06-29 02:40:00', '夜间保温层破损超低温', 3.10, '已处理');

-- 3. 补全 M4 的 灌溉记录
INSERT INTO `irrigation_record` (`plot_id`, `irrigation_time`, `water_amount`, `operator_id`, `remark`) VALUES
                                                                                                            (1, '2026-04-01 08:30:00', 12.50, 2, 'routine spring irrigation'),
                                                                                                            (1, '2026-04-12 09:10:00', 10.20, 2, 'extra water for hotter weather'),
                                                                                                            (2, '2026-04-20 07:50:00', 8.60, 3, 'pre-sowing moisture support'),
                                                                                                            (3, '2026-04-05 10:00:00', 16.80, 3, 'growth-stage irrigation'),
                                                                                                            (6, '2026-05-06 06:40:00', 14.30, 2, 'supplemental irrigation');

-- 4. 重构并还原 M1 的 500条 递归采购数据 (完美解决外键匹配问题！)
INSERT INTO `agri_purchase_record` (`material_id`, `supplier_id`, `purchase_qty`, `unit_price`, `purchase_date`, `buyer_id`, `remark`)
WITH RECURSIVE seq AS (
    SELECT 1 AS n UNION ALL SELECT n + 1 FROM seq WHERE n < 240
)
SELECT
    ((n - 1) % 8) + 1 AS material_id,
    CASE ((n - 1) % 8) + 1
    WHEN 1 THEN 1 WHEN 2 THEN 2 WHEN 3 THEN 3 WHEN 4 THEN 4
    WHEN 5 THEN 5 WHEN 6 THEN 5 WHEN 7 THEN 6 ELSE 7
END AS supplier_id,
    CAST(12 + (n % 18) AS DECIMAL(10,2)) AS purchase_qty,
    CASE ((n - 1) % 8) + 1
        WHEN 1 THEN 145.00 + (n % 7) * 0.50
        WHEN 2 THEN 131.00 + (n % 6) * 0.60
        WHEN 3 THEN 57.50 + (n % 5) * 0.40
        WHEN 4 THEN 26.50 + (n % 5) * 0.30
        WHEN 5 THEN 94.00 + (n % 6) * 0.45
        WHEN 6 THEN 86.50 + (n % 6) * 0.35
        WHEN 7 THEN 205.00 + (n % 8) * 0.75
        ELSE 11.50 + (n % 5) * 0.20
    END AS unit_price,
    DATE_ADD('2026-06-18', INTERVAL n DAY) AS purchase_date,
    buyer.`user_id` AS buyer_id,
    CONCAT('批量扩充采购记录-', n) AS remark
FROM seq
JOIN (
    SELECT
        `user_id`,
        ROW_NUMBER() OVER (ORDER BY `user_id`) AS rn,
        COUNT(*) OVER () AS total_count
    FROM `user`
) buyer ON buyer.rn = ((seq.n - 1) % buyer.total_count) + 1;

-- 5. 补全 M5 的农事日志与产量统计，支撑分页、筛选、汇总和大屏图表
INSERT INTO `farm_log` (`plan_id`, `operator_id`, `operation_type`, `operation_date`, `description`)
WITH RECURSIVE seq AS (
    SELECT 1 AS n UNION ALL SELECT n + 1 FROM seq WHERE n < 180
)
SELECT
    pp.`plan_id`,
    operator_user.`user_id` AS `operator_id`,
    CASE seq.n % 10
        WHEN 0 THEN '整地'
        WHEN 1 THEN '播种'
        WHEN 2 THEN '施肥'
        WHEN 3 THEN '灌溉'
        WHEN 4 THEN '除草'
        WHEN 5 THEN '病虫害巡检'
        WHEN 6 THEN '病虫害防治'
        WHEN 7 THEN '修剪'
        WHEN 8 THEN '采收'
        ELSE '巡田'
    END AS `operation_type`,
    LEAST(
        DATE_ADD(pp.`start_date`, INTERVAL ((seq.n * 7) % GREATEST(DATEDIFF(pp.`expected_harvest`, pp.`start_date`), 1)) DAY),
        pp.`expected_harvest`
    ) AS `operation_date`,
    CONCAT(
        c.`crop_name`, ' - ',
        CASE seq.n % 10
            WHEN 0 THEN '完成地块整理和基础检查'
            WHEN 1 THEN '完成播种或移栽作业'
            WHEN 2 THEN '按计划完成追肥'
            WHEN 3 THEN '根据墒情完成灌溉'
            WHEN 4 THEN '完成杂草清理'
            WHEN 5 THEN '完成病虫害巡检并记录风险'
            WHEN 6 THEN '完成病虫害防治处置'
            WHEN 7 THEN '完成枝蔓修剪和支架维护'
            WHEN 8 THEN '完成分批采收和现场称重'
            ELSE '完成日常巡田和长势记录'
        END,
        '，记录批次M5-', seq.n
    ) AS `description`
FROM seq
JOIN `planting_plan` pp ON pp.`plan_id` = ((seq.n - 1) % 50) + 1
JOIN `crop` c ON c.`crop_id` = pp.`crop_id`
JOIN (
    SELECT
        `user_id`,
        ROW_NUMBER() OVER (ORDER BY `user_id`) AS rn,
        COUNT(*) OVER () AS total_count
    FROM `user`
    WHERE `role` = '农技员'
) operator_user ON operator_user.rn = ((seq.n - 1) % operator_user.total_count) + 1;

INSERT INTO `yield_stat` (`plan_id`, `harvest_date`, `yield_weight`, `quality_grade`)
WITH RECURSIVE seq AS (
    SELECT 1 AS n UNION ALL SELECT n + 1 FROM seq WHERE n < 90
),
completed_plan AS (
    SELECT
        pp.`plan_id`,
        pp.`expected_harvest`,
        pp.`plant_area`,
        ROW_NUMBER() OVER (ORDER BY pp.`plan_id`) AS rn,
        COUNT(*) OVER () AS total_count
    FROM `planting_plan` pp
    WHERE pp.`status` = '已完成'
)
SELECT
    cp.`plan_id`,
    DATE_ADD(cp.`expected_harvest`, INTERVAL ((seq.n - 1) DIV cp.total_count) DAY) AS `harvest_date`,
    ROUND(cp.`plant_area` * (28 + (seq.n % 9) * 1.85) + (seq.n % 11) * 13.50, 2) AS `yield_weight`,
    CASE seq.n % 5
        WHEN 0 THEN '优'
        WHEN 1 THEN '良'
        WHEN 2 THEN '合格'
        WHEN 3 THEN '优'
        ELSE '良'
    END AS `quality_grade`
FROM seq
JOIN completed_plan cp ON cp.rn = ((seq.n - 1) % cp.total_count) + 1;

-- 6. 依据 IoT 时序数据回填日报，保证首页日报与统计视图可查询
INSERT INTO `iot_daily_report` (
    `report_date`,
    `data_start_time`,
    `data_end_time`,
    `avg_temperature`,
    `min_temperature`,
    `max_temperature`,
    `avg_humidity`,
    `min_humidity`,
    `max_humidity`,
    `avg_soil_moisture`,
    `avg_light_intensity`,
    `total_alerts`,
    `pending_alerts`,
    `report_content`
)
SELECT
    d.`report_date`,
    d.`data_start_time`,
    d.`data_end_time`,
    d.`avg_temperature`,
    d.`min_temperature`,
    d.`max_temperature`,
    d.`avg_humidity`,
    d.`min_humidity`,
    d.`max_humidity`,
    d.`avg_soil_moisture`,
    d.`avg_light_intensity`,
    COALESCE(a.`total_alerts`, 0) AS `total_alerts`,
    COALESCE(a.`pending_alerts`, 0) AS `pending_alerts`,
    CONCAT('AI日报：', d.`report_date`, ' 环境监测数据已汇总，可结合预警与灌溉记录安排巡检。') AS `report_content`
FROM (
    SELECT
        DATE(sd.`collect_time`) AS `report_date`,
        MIN(sd.`collect_time`) AS `data_start_time`,
        MAX(sd.`collect_time`) AS `data_end_time`,
        ROUND(AVG(sd.`temperature`), 2) AS `avg_temperature`,
        MIN(sd.`temperature`) AS `min_temperature`,
        MAX(sd.`temperature`) AS `max_temperature`,
        ROUND(AVG(sd.`humidity`), 2) AS `avg_humidity`,
        MIN(sd.`humidity`) AS `min_humidity`,
        MAX(sd.`humidity`) AS `max_humidity`,
        ROUND(AVG(sd.`soil_moisture`), 2) AS `avg_soil_moisture`,
        ROUND(AVG(sd.`light_intensity`), 2) AS `avg_light_intensity`
    FROM `sensor_data` sd
    GROUP BY DATE(sd.`collect_time`)
) d
LEFT JOIN (
    SELECT
        DATE(`alert_time`) AS `report_date`,
        COUNT(*) AS `total_alerts`,
        SUM(CASE WHEN `status` = '未处理' THEN 1 ELSE 0 END) AS `pending_alerts`
    FROM `device_alert`
    GROUP BY DATE(`alert_time`)
) a
    ON a.`report_date` = d.`report_date`;


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

