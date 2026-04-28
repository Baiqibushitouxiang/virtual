CREATE DATABASE IF NOT EXISTS `dam` DEFAULT CHARACTER SET utf8mb4;
USE `dam`;

SET FOREIGN_KEY_CHECKS = 0;

-- Drop legacy tables and unused platform tables
DROP TABLE IF EXISTS `scene_point_bindings`;
DROP TABLE IF EXISTS `point_history_values`;
DROP TABLE IF EXISTS `point_latest_values`;
DROP TABLE IF EXISTS `device_point_mappings`;
DROP TABLE IF EXISTS `device_channels`;
DROP TABLE IF EXISTS `device_instances`;
DROP TABLE IF EXISTS `device_model_points`;
DROP TABLE IF EXISTS `device_models`;
DROP TABLE IF EXISTS `user_scenes`;
DROP TABLE IF EXISTS `subcategories`;
DROP TABLE IF EXISTS `categories`;

-- Drop active runtime tables before re-initialization
DROP TABLE IF EXISTS `alert_notification_log`;
DROP TABLE IF EXISTS `alert_rule`;
DROP TABLE IF EXISTS `model_data_binding`;
DROP TABLE IF EXISTS `data_panel`;
DROP TABLE IF EXISTS `device_data`;
DROP TABLE IF EXISTS `devices`;
DROP TABLE IF EXISTS `composite_model_components`;
DROP TABLE IF EXISTS `composite_models`;
DROP TABLE IF EXISTS `scene_assets`;
DROP TABLE IF EXISTS `userdata`;
DROP TABLE IF EXISTS `Users`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `models`;

CREATE TABLE `Users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `email` VARCHAR(100) DEFAULT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_username` (`username`),
    KEY `idx_users_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `userdata` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `user_info` JSON NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_userdata_user_id` (`user_id`),
    CONSTRAINT `fk_userdata_user` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `models` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `category` VARCHAR(255) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `file_path` VARCHAR(1000) NOT NULL,
    `description` TEXT,
    PRIMARY KEY (`id`),
    KEY `idx_models_category` (`category`),
    KEY `idx_models_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `scene_assets` (
    `id` BIGINT NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `file_type` VARCHAR(10) NOT NULL,
    `path` VARCHAR(512) NOT NULL,
    `description` VARCHAR(255) DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `texture_info` TEXT,
    PRIMARY KEY (`id`),
    KEY `idx_scene_assets_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `composite_models` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT,
    `created_by` VARCHAR(255) DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_composite_models_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `composite_model_components` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `composite_model_id` BIGINT NOT NULL,
    `model_id` VARCHAR(255) DEFAULT NULL,
    `position_x` DOUBLE DEFAULT 0,
    `position_y` DOUBLE DEFAULT 0,
    `position_z` DOUBLE DEFAULT 0,
    `rotation_x` DOUBLE DEFAULT 0,
    `rotation_y` DOUBLE DEFAULT 0,
    `rotation_z` DOUBLE DEFAULT 0,
    `scale_x` DOUBLE DEFAULT 1,
    `scale_y` DOUBLE DEFAULT 1,
    `scale_z` DOUBLE DEFAULT 1,
    PRIMARY KEY (`id`),
    KEY `idx_components_composite_model_id` (`composite_model_id`),
    CONSTRAINT `fk_components_composite_model` FOREIGN KEY (`composite_model_id`) REFERENCES `composite_models` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `devices` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `device_id` VARCHAR(128) NOT NULL,
    `name` VARCHAR(255) DEFAULT NULL,
    `description` TEXT,
    `certificate` LONGTEXT,
    `certificate_thumbprint` VARCHAR(128) DEFAULT NULL,
    `status` INT DEFAULT 1,
    `user_id` BIGINT DEFAULT NULL,
    `last_seen_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_devices_device_id` (`device_id`),
    UNIQUE KEY `uk_devices_certificate_thumbprint` (`certificate_thumbprint`),
    KEY `idx_devices_user_id` (`user_id`),
    KEY `idx_devices_status` (`status`),
    KEY `idx_devices_last_seen_at` (`last_seen_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `device_data` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `device_id` VARCHAR(128) NOT NULL,
    `data_type` VARCHAR(64) DEFAULT NULL,
    `value` DOUBLE DEFAULT NULL,
    `unit` VARCHAR(64) DEFAULT NULL,
    `metadata` TEXT,
    `recorded_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_device_data_device_time` (`device_id`, `recorded_at`),
    KEY `idx_device_data_device_type_time` (`device_id`, `data_type`, `recorded_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `data_panel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `device_id` BIGINT DEFAULT NULL,
    `device_name` VARCHAR(100) DEFAULT NULL,
    `model_id` VARCHAR(128) DEFAULT NULL,
    `model_name` VARCHAR(100) DEFAULT NULL,
    `model_type` VARCHAR(50) DEFAULT NULL,
    `position` VARCHAR(500) DEFAULT NULL,
    `size` VARCHAR(500) DEFAULT NULL,
    `style` TEXT,
    `status` INT DEFAULT 1,
    `user_id` BIGINT NOT NULL,
    `scene_id` BIGINT DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_data_panel_user_id` (`user_id`),
    KEY `idx_data_panel_scene_id` (`scene_id`),
    KEY `idx_data_panel_device_id` (`device_id`),
    KEY `idx_data_panel_model_id` (`model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `model_data_binding` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `scene_id` BIGINT NOT NULL,
    `model_id` VARCHAR(128) NOT NULL,
    `model_name` VARCHAR(255) DEFAULT NULL,
    `device_id` BIGINT DEFAULT NULL,
    `device_code` VARCHAR(128) DEFAULT NULL,
    `device_name` VARCHAR(255) DEFAULT NULL,
    `data_type` VARCHAR(64) DEFAULT NULL,
    `rule_status` INT DEFAULT 1,
    `user_id` BIGINT DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_model_data_binding_scene_model` (`scene_id`, `model_id`),
    KEY `idx_model_data_binding_user_id` (`user_id`),
    KEY `idx_model_data_binding_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `alert_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `device_id` BIGINT DEFAULT NULL,
    `device_code` VARCHAR(128) DEFAULT NULL,
    `device_name` VARCHAR(255) DEFAULT NULL,
    `data_type` VARCHAR(64) NOT NULL,
    `operator` VARCHAR(8) NOT NULL,
    `threshold` DOUBLE NOT NULL,
    `enabled` INT DEFAULT 1,
    `message_template` VARCHAR(500) DEFAULT NULL,
    `cooldown_seconds` INT DEFAULT 60,
    `last_triggered_at` DATETIME DEFAULT NULL,
    `user_id` BIGINT DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_alert_rule_device_code` (`device_code`),
    KEY `idx_alert_rule_user_id` (`user_id`),
    KEY `idx_alert_rule_lookup` (`device_code`, `data_type`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `alert_notification_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `rule_id` BIGINT DEFAULT NULL,
    `device_id` BIGINT DEFAULT NULL,
    `device_code` VARCHAR(128) DEFAULT NULL,
    `device_name` VARCHAR(255) DEFAULT NULL,
    `data_type` VARCHAR(64) DEFAULT NULL,
    `channel` VARCHAR(64) NOT NULL,
    `send_status` VARCHAR(32) NOT NULL,
    `notify_target` VARCHAR(255) DEFAULT NULL,
    `message_title` VARCHAR(255) DEFAULT NULL,
    `message_content` TEXT,
    `request_body` TEXT,
    `response_body` TEXT,
    `error_message` VARCHAR(500) DEFAULT NULL,
    `http_status` INT DEFAULT NULL,
    `platform_code` VARCHAR(64) DEFAULT NULL,
    `platform_message` VARCHAR(255) DEFAULT NULL,
    `current_value` DOUBLE DEFAULT NULL,
    `threshold_value` DOUBLE DEFAULT NULL,
    `operator` VARCHAR(8) DEFAULT NULL,
    `triggered_at` DATETIME DEFAULT NULL,
    `sent_at` DATETIME DEFAULT NULL,
    `user_id` BIGINT DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_alert_log_rule_id` (`rule_id`),
    KEY `idx_alert_log_device_code` (`device_code`),
    KEY `idx_alert_log_user_id` (`user_id`),
    KEY `idx_alert_log_channel_status` (`channel`, `send_status`),
    KEY `idx_alert_log_sent_at` (`sent_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed data kept for deployment
-- Default administrator account: admin / 123456
INSERT INTO `Users` (`id`, `username`, `password`, `phone`, `email`, `role`, `is_deleted`) VALUES
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '12345678901', '123456@qq.com', 'ADMIN', 0);

INSERT INTO `userdata` (`id`, `user_id`, `user_info`, `created_at`, `updated_at`) VALUES
(1, 1, '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed model catalog kept from current dam.sql
INSERT INTO `models` VALUES (-200000040, '水务水利/电气与动力设施', '配电柜.glb', '/models/水务水利/电气与动力设施/配电柜.glb', '这是一个自动导入的模型：配电柜.glb');
INSERT INTO `models` VALUES (-200000039, '水务水利/电气与动力设施', '水利发电机.glb', '/models/水务水利/电气与动力设施/水利发电机.glb', '这是一个自动导入的模型：水利发电机.glb');
INSERT INTO `models` VALUES (-200000038, '水务水利/电气与动力设施', '变频器.glb', '/models/水务水利/电气与动力设施/变频器.glb', '这是一个自动导入的模型：变频器.glb');
INSERT INTO `models` VALUES (-200000037, '水务水利/监测设备', '雨量监测设备.glb', '/models/水务水利/监测设备/雨量监测设备.glb', '这是一个自动导入的模型：雨量监测设备.glb');
INSERT INTO `models` VALUES (-200000036, '水务水利/监测设备', '水质检测站.glb', '/models/水务水利/监测设备/水质检测站.glb', '这是一个自动导入的模型：水质检测站.glb');
INSERT INTO `models` VALUES (-200000035, '水务水利/监测设备', '水位监测设备.glb', '/models/水务水利/监测设备/水位监测设备.glb', '这是一个自动导入的模型：水位监测设备.glb');
INSERT INTO `models` VALUES (-200000034, '水务水利/水坝工程', '重力坝.glb', '/models/水务水利/水坝工程/重力坝.glb', '这是一个自动导入的模型：重力坝.glb');
INSERT INTO `models` VALUES (-200000033, '水务水利/管道体系', '管道2.glb', '/models/水务水利/管道体系/管道2.glb', '这是一个自动导入的模型：管道2.glb');
INSERT INTO `models` VALUES (-200000032, '水务水利/管道体系', '管道1.glb', '/models/水务水利/管道体系/管道1.glb', '这是一个自动导入的模型：管道1.glb');
INSERT INTO `models` VALUES (-200000031, '水务水利/测量设备', '测量设备3.glb', '/models/水务水利/测量设备/测量设备3.glb', '这是一个自动导入的模型：测量设备3.glb');
INSERT INTO `models` VALUES (-200000030, '水务水利/测量设备', '测量设备2.glb', '/models/水务水利/测量设备/测量设备2.glb', '这是一个自动导入的模型：测量设备2.glb');
INSERT INTO `models` VALUES (-200000029, '水务水利/测量设备', '测量设备1.glb', '/models/水务水利/测量设备/测量设备1.glb', '这是一个自动导入的模型：测量设备1.glb');
INSERT INTO `models` VALUES (-200000028, '水务水利/水泵设备', '水泵4.glb', '/models/水务水利/水泵设备/水泵4.glb', '这是一个自动导入的模型：水泵4.glb');
INSERT INTO `models` VALUES (-200000027, '水务水利/水泵设备', '水泵3.glb', '/models/水务水利/水泵设备/水泵3.glb', '这是一个自动导入的模型：水泵3.glb');
INSERT INTO `models` VALUES (-200000026, '水务水利/水泵设备', '水泵2.glb', '/models/水务水利/水泵设备/水泵2.glb', '这是一个自动导入的模型：水泵2.glb');
INSERT INTO `models` VALUES (-200000025, '水务水利/水泵设备', '水泵1.glb', '/models/水务水利/水泵设备/水泵1.glb', '这是一个自动导入的模型：水泵1.glb');
INSERT INTO `models` VALUES (-200000024, '水务水利/水坝工程', '监测点.glb', '/models/水务水利/水坝工程/监测点.glb', '这是一个自动导入的模型：监测点.glb');
INSERT INTO `models` VALUES (-200000023, '水务水利/水坝工程', '溢洪道.glb', '/models/水务水利/水坝工程/溢洪道.glb', '这是一个自动导入的模型：溢洪道glb');
INSERT INTO `models` VALUES (-200000022, '水务水利/水坝工程', '水坝结构.glb', '/models/水务水利/水坝工程/水坝结构.glb', '这是一个自动导入的模型：水坝结构.glb');
INSERT INTO `models` VALUES (-200000021, '水务水利/水坝工程', '桥.glb', '/models/水务水利/水坝工程/桥.glb', '这是一个自动导入的模型：桥.glb');
INSERT INTO `models` VALUES (-200000020, '水务水利/水坝工程', '导流洞.glb', '/models/水务水利/水坝工程/导流洞.glb', '这是一个自动导入的模型：导流洞.glb');
INSERT INTO `models` VALUES (-200000019, '水务水利/水闸设备', '液压启闭机.glb', '/models/水务水利/水闸设备/液压启闭机.glb', '这是一个自动导入的模型：液压启闭机glb');
INSERT INTO `models` VALUES (-200000018, '水务水利/水闸设备', '水位标尺.glb', '/models/水务水利/水闸设备/水位标尺.glb', '这是一个自动导入的模型：水位标尺.glb');
INSERT INTO `models` VALUES (-200000017, '水务水利/水闸设备', '弧形闸门.glb', '/models/水务水利/水闸设备/弧形闸门.glb', '这是一个自动导入的模型：弧形闸门.glb');
INSERT INTO `models` VALUES (-200000016, '水务水利/水闸设备', '平面钢闸门.glb', '/models/水务水利/水闸设备/平面钢闸门.glb', '这是一个自动导入的模型：平面钢闸门.glb');
INSERT INTO `models` VALUES (-200000015, '水务水利/施工机械', '运输卡车.glb', '/models/水务水利/施工机械/运输卡车.glb', '这是一个自动导入的模型：运输卡车.glb');
INSERT INTO `models` VALUES (-200000014, '水务水利/施工机械', '混凝土泵车.glb', '/models/水务水利/施工机械/混凝土泵车.glb', '这是一个自动导入的模型：混凝土泵车.glb');
INSERT INTO `models` VALUES (-200000013, '水务水利/施工机械', '推土机.glb', '/models/水务水利/施工机械/推土机.glb', '这是一个自动导入的模型：推土机.glb');
INSERT INTO `models` VALUES (-200000012, '水务水利/施工机械', '挖掘机.glb', '/models/水务水利/施工机械/挖掘机.glb', '这是一个自动导入的模型：挖掘机.glb');
INSERT INTO `models` VALUES (-200000011, '水务水利/施工机械', '吊车.glb', '/models/水务水利/施工机械/吊车.glb', '这是一个自动导入的模型：吊车.glb');
INSERT INTO `models` VALUES (-200000010, '水务水利/环境要素', '河道地形.glb', '/models/水务水利/环境要素/河道地形.glb', '这是一个自动导入的模型：河道地形.glb');
INSERT INTO `models` VALUES (-200000009, '水务水利/环境要素', '植被.glb', '/models/水务水利/环境要素/植被.glb', '这是一个自动导入的模型：植被.glb');
INSERT INTO `models` VALUES (-200000008, '水务水利/环境要素', '办公楼.glb', '/models/水务水利/环境要素/办公楼.glb', '这是一个自动导入的模型：办公楼.glb');
INSERT INTO `models` VALUES (-200000007, '水务水利/环境要素', '施工围栏.glb', '/models/水务水利/环境要素/施工围栏.glb', '这是一个自动导入的模型：施工围栏.glb');
INSERT INTO `models` VALUES (-200000006, '水务水利/环境要素', '六角块护坡.glb', '/models/水务水利/环境要素/六角块护坡.glb', '这是一个自动导入的模型：六角块护坡.glb');
INSERT INTO `models` VALUES (-200000005, '水务水利/水处理设备', '絮凝池.glb', '/models/水务水利/水处理设备/絮凝池.glb', '这是一个自动导入的模型：絮凝池.glb');
INSERT INTO `models` VALUES (-200000004, '水务水利/水处理设备', '紫外线消毒通道.glb', '/models/水务水利/水处理设备/紫外线消毒通道.glb', '这是一个自动导入的模型：紫外线消毒通道.glb');
INSERT INTO `models` VALUES (-200000003, '水务水利/水处理设备', '污泥脱水机.glb', '/models/水务水利/水处理设备/污泥脱水机.glb', '这是一个自动导入的模型：污泥脱水机.glb');
INSERT INTO `models` VALUES (-200000002, '水务水利/水处理设备', '加药罐.glb', '/models/水务水利/水处理设备/加药罐.glb', '这是一个自动导入的模型：加药罐.glb');
INSERT INTO `models` VALUES (-200000001, '水务水利/水处理设备', 'V型滤池.glb', '/models/水务水利/水处理设备/V型滤池.glb', '这是一个自动导入的模型：V型滤池.glb');
INSERT INTO `models` VALUES (-164839423, '办公室', '办公室.glb', '/models/办公室/办公室.glb', '这是一个自动导入的模型：办公室.glb');
INSERT INTO `models` VALUES (-164839422, '办公室', '字画1.glb', '/models/办公室/字画1.glb', '这是一个自动导入的模型：字画1.glb');
INSERT INTO `models` VALUES (-164839421, '办公室', '字画2.glb', '/models/办公室/字画2.glb', '这是一个自动导入的模型：字画2.glb');
INSERT INTO `models` VALUES (-164839420, '办公室', '字画3.glb', '/models/办公室/字画3.glb', '这是一个自动导入的模型：字画3.glb');
INSERT INTO `models` VALUES (-164839419, '办公室', '柜子.glb', '/models/办公室/柜子.glb', '这是一个自动导入的模型：柜子.glb');
INSERT INTO `models` VALUES (-164839418, '办公室', '桌子.glb', '/models/办公室/桌子.glb', '这是一个自动导入的模型：桌子.glb');
INSERT INTO `models` VALUES (-164839417, '办公室', '椅子1.glb', '/models/办公室/椅子1.glb', '这是一个自动导入的模型：椅子1.glb');
INSERT INTO `models` VALUES (-164839416, '办公室', '椅子2.glb', '/models/办公室/椅子2.glb', '这是一个自动导入的模型：椅子2.glb');
INSERT INTO `models` VALUES (-164839415, '办公室', '沙发1.glb', '/models/办公室/沙发1.glb', '这是一个自动导入的模型：沙发1.glb');
INSERT INTO `models` VALUES (-164839414, '办公室', '沙发2.glb', '/models/办公室/沙发2.glb', '这是一个自动导入的模型：沙发2.glb');
INSERT INTO `models` VALUES (-164839413, '办公室', '沙发3.glb', '/models/办公室/沙发3.glb', '这是一个自动导入的模型：沙发3.glb');
INSERT INTO `models` VALUES (-164839412, '办公室', '沙发4.glb', '/models/办公室/沙发4.glb', '这是一个自动导入的模型：沙发4.glb');
INSERT INTO `models` VALUES (-164839411, '办公室', '沙发5.glb', '/models/办公室/沙发5.glb', '这是一个自动导入的模型：沙发5.glb');
INSERT INTO `models` VALUES (-164839410, '办公室', '盆栽1.glb', '/models/办公室/盆栽1.glb', '这是一个自动导入的模型：盆栽1.glb');
INSERT INTO `models` VALUES (-164839409, '办公室', '盆栽2.glb', '/models/办公室/盆栽2.glb', '这是一个自动导入的模型：盆栽2.glb');
INSERT INTO `models` VALUES (-164839408, '办公室', '盆栽3.glb', '/models/办公室/盆栽3.glb', '这是一个自动导入的模型：盆栽3.glb');
INSERT INTO `models` VALUES (-135479303, '教室', '三角尺.glb', '/models/教室/三角尺.glb', '这是一个自动导入的模型：三角尺.glb');
INSERT INTO `models` VALUES (-135479302, '教室', '书架.glb', '/models/教室/书架.glb', '这是一个自动导入的模型：书架.glb');
INSERT INTO `models` VALUES (-135479301, '教室', '黑板.glb', '/models/教室/黑板.glb', '这是一个自动导入的模型：黑板.glb');
INSERT INTO `models` VALUES (-135479295, '办公室', '花盆1.glb', '/models/办公室/花盆1.glb', '这是一个自动导入的模型：花盆1.glb');
INSERT INTO `models` VALUES (-135479294, '办公室', '花盆2.glb', '/models/办公室/花盆2.glb', '这是一个自动导入的模型：花盆2.glb');
INSERT INTO `models` VALUES (-135479293, '办公室', '花盆3.glb', '/models/办公室/花盆3.glb', '这是一个自动导入的模型：花盆3.glb');
INSERT INTO `models` VALUES (-135479292, '实验室', '剪子.glb', '/models/实验室/剪子.glb', '这是一个自动导入的模型：剪子.glb');
INSERT INTO `models` VALUES (-135479291, '实验室', '吸瓶.glb', '/models/实验室/吸瓶.glb', '这是一个自动导入的模型：吸瓶.glb');
INSERT INTO `models` VALUES (-135479290, '实验室', '吸附棉.glb', '/models/实验室/吸附棉.glb', '这是一个自动导入的模型：吸附棉.glb');
INSERT INTO `models` VALUES (-135479289, '实验室', '天平.glb', '/models/实验室/天平.glb', '这是一个自动导入的模型：天平.glb');
INSERT INTO `models` VALUES (-135479288, '实验室', '实验台.glb', '/models/实验室/实验台.glb', '这是一个自动导入的模型：实验台.glb');
INSERT INTO `models` VALUES (-135479287, '实验室', '废液桶.glb', '/models/实验室/废液桶.glb', '这是一个自动导入的模型：废液桶.glb');
INSERT INTO `models` VALUES (-135479286, '实验室', '废液桶托盘.glb', '/models/实验室/废液桶托盘.glb', '这是一个自动导入的模型：废液桶托盘.glb');
INSERT INTO `models` VALUES (-135479285, '实验室', '手套.glb', '/models/实验室/手套.glb', '这是一个自动导入的模型：手套.glb');
INSERT INTO `models` VALUES (-135479284, '实验室', '托盘.glb', '/models/实验室/托盘.glb', '这是一个自动导入的模型：托盘.glb');
INSERT INTO `models` VALUES (-135479283, '实验室', '振荡器.glb', '/models/实验室/振荡器.glb', '这是一个自动导入的模型：振荡器.glb');
INSERT INTO `models` VALUES (-135479282, '实验室', '易制毒试剂柜.glb', '/models/实验室/易制毒试剂柜.glb', '这是一个自动导入的模型：易制毒试剂柜.glb');
INSERT INTO `models` VALUES (-135479281, '实验室', '显微镜.glb', '/models/实验室/显微镜.glb', '这是一个自动导入的模型：显微镜.glb');
INSERT INTO `models` VALUES (-135479280, '实验室', '普通试剂柜.glb', '/models/实验室/普通试剂柜.glb', '这是一个自动导入的模型：普通试剂柜.glb');
INSERT INTO `models` VALUES (-135479279, '实验室', '水浴锅.glb', '/models/实验室/水浴锅.glb', '这是一个自动导入的模型：水浴锅.glb');
INSERT INTO `models` VALUES (-135479278, '实验室', '洗眼器.glb', '/models/实验室/洗眼器.glb', '这是一个自动导入的模型：洗眼器.glb');
INSERT INTO `models` VALUES (-135479277, '实验室', '消防柜.glb', '/models/实验室/消防柜.glb', '这是一个自动导入的模型：消防柜.glb');
INSERT INTO `models` VALUES (-135479276, '实验室', '滴管.glb', '/models/实验室/滴管.glb', '这是一个自动导入的模型：滴管.glb');
INSERT INTO `models` VALUES (-135479275, '实验室', '漏斗.glb', '/models/实验室/漏斗.glb', '这是一个自动导入的模型：漏斗.glb');
INSERT INTO `models` VALUES (-135479274, '实验室', '灭火器.glb', '/models/实验室/灭火器.glb', '这是一个自动导入的模型：灭火器.glb');
INSERT INTO `models` VALUES (-135479273, '实验室', '灭菌锅.glb', '/models/实验室/灭菌锅.glb', '这是一个自动导入的模型：灭菌锅.glb');
INSERT INTO `models` VALUES (-135479272, '实验室', '烘箱.glb', '/models/实验室/烘箱.glb', '这是一个自动导入的模型：烘箱.glb');
INSERT INTO `models` VALUES (-135479271, '实验室', '烧杯.glb', '/models/实验室/烧杯.glb', '这是一个自动导入的模型：烧杯.glb');
INSERT INTO `models` VALUES (-135479270, '实验室', '玻璃棒.glb', '/models/实验室/玻璃棒.glb', '这是一个自动导入的模型：玻璃棒.glb');
INSERT INTO `models` VALUES (-135479269, '实验室', '研钵、研磨棒.glb', '/models/实验室/研钵、研磨棒.glb', '这是一个自动导入的模型：研钵、研磨棒.glb');
INSERT INTO `models` VALUES (-135479268, '实验室', '离心机.glb', '/models/实验室/离心机.glb', '这是一个自动导入的模型：离心机.glb');
INSERT INTO `models` VALUES (-135479267, '实验室', '离心管.glb', '/models/实验室/离心管.glb', '这是一个自动导入的模型：离心管.glb');
INSERT INTO `models` VALUES (-135479266, '实验室', '称量纸.glb', '/models/实验室/称量纸.glb', '这是一个自动导入的模型：称量纸.glb');
INSERT INTO `models` VALUES (-135479265, '实验室', '药勺.glb', '/models/实验室/药勺.glb', '这是一个自动导入的模型：药勺.glb');
INSERT INTO `models` VALUES (-135479264, '实验室', '试剂瓶.glb', '/models/实验室/试剂瓶.glb', '这是一个自动导入的模型：试剂瓶.glb');
INSERT INTO `models` VALUES (-135479263, '实验室', '试管.glb', '/models/实验室/试管.glb', '这是一个自动导入的模型：试管.glb');
INSERT INTO `models` VALUES (-135479262, '实验室', '试管架.glb', '/models/实验室/试管架.glb', '这是一个自动导入的模型：试管架.glb');
INSERT INTO `models` VALUES (-135479261, '实验室', '超净台.glb', '/models/实验室/超净台.glb', '这是一个自动导入的模型：超净台.glb');
INSERT INTO `models` VALUES (-135479260, '实验室', '通风橱.glb', '/models/实验室/通风橱.glb', '这是一个自动导入的模型：通风橱.glb');
INSERT INTO `models` VALUES (-135479259, '实验室', '酒精桶.glb', '/models/实验室/酒精桶.glb', '这是一个自动导入的模型：酒精桶.glb');
INSERT INTO `models` VALUES (-135479258, '实验室', '酒精灯.glb', '/models/实验室/酒精灯.glb', '这是一个自动导入的模型：酒精灯.glb');
INSERT INTO `models` VALUES (-135479257, '实验室', '防爆冰箱.glb', '/models/实验室/防爆冰箱.glb', '这是一个自动导入的模型：防爆冰箱.glb');
INSERT INTO `models` VALUES (-135479256, '实验室', '马弗炉.glb', '/models/实验室/马弗炉.glb', '这是一个自动导入的模型：马弗炉.glb');
INSERT INTO `models` VALUES (-135479255, '律师所', '储物柜.glb', '/models/律师所/储物柜.glb', '这是一个自动导入的模型：储物柜.glb');
INSERT INTO `models` VALUES (-135479254, '律师所', '宣传牌.glb', '/models/律师所/宣传牌.glb', '这是一个自动导入的模型：宣传牌.glb');
INSERT INTO `models` VALUES (-135479253, '律师所', '导览牌.glb', '/models/律师所/导览牌.glb', '这是一个自动导入的模型：导览牌.glb');
INSERT INTO `models` VALUES (-135479252, '律师所', '标识牌.glb', '/models/律师所/标识牌.glb', '这是一个自动导入的模型：标识牌.glb');
INSERT INTO `models` VALUES (-135479251, '律师所', '椅子.glb', '/models/律师所/椅子.glb', '这是一个自动导入的模型：椅子.glb');
INSERT INTO `models` VALUES (-135479250, '律师所', '电脑.glb', '/models/律师所/电脑.glb', '这是一个自动导入的模型：电脑.glb');
INSERT INTO `models` VALUES (-135479249, '律师所', '询问台.glb', '/models/律师所/询问台.glb', '这是一个自动导入的模型：询问台.glb');
INSERT INTO `models` VALUES (-135479248, '律师所', '长椅.glb', '/models/律师所/长椅.glb', '这是一个自动导入的模型：长椅.glb');
INSERT INTO `models` VALUES (-135479246, '教室', 'book53.glb', '/models/教室/book53.glb', '这是一个自动导入的模型：book53.glb');
INSERT INTO `models` VALUES (-135479245, '教室', 'broom01_1.glb', '/models/教室/broom01_1.glb', '这是一个自动导入的模型：broom01_1.glb');
INSERT INTO `models` VALUES (-135479244, '教室', 'broom01_2.glb', '/models/教室/broom01_2.glb', '这是一个自动导入的模型：broom01_2.glb');
INSERT INTO `models` VALUES (-135479243, '教室', 'chair01.glb', '/models/教室/chair01.glb', '这是一个自动导入的模型：chair01.glb');
INSERT INTO `models` VALUES (-135479242, '教室', 'desk02.glb', '/models/教室/desk02.glb', '这是一个自动导入的模型：desk02.glb');
INSERT INTO `models` VALUES (-135479241, '教室', 'lamp01_1.glb', '/models/教室/lamp01_1.glb', '这是一个自动导入的模型：lamp01_1.glb');
INSERT INTO `models` VALUES (-135479240, '教室', 'lamp02.glb', '/models/教室/lamp02.glb', '这是一个自动导入的模型：lamp02.glb');
INSERT INTO `models` VALUES (-135479239, '教室', 'projector01.glb', '/models/教室/projector01.glb', '这是一个自动导入的模型：projector01.glb');
INSERT INTO `models` VALUES (-135479238, '教室', 'screen01_1.glb', '/models/教室/screen01_1.glb', '这是一个自动导入的模型：screen01_1.glb');
INSERT INTO `models` VALUES (-135479237, '教室', 'screen01_2.glb', '/models/教室/screen01_2.glb', '这是一个自动导入的模型：screen01_2.glb');
INSERT INTO `models` VALUES (-135479236, '教室', 'trashbox01.glb', '/models/教室/trashbox01.glb', '这是一个自动导入的模型：trashbox01.glb');
INSERT INTO `models` VALUES (-135479235, '教室', 'tuoba.glb', '/models/教室/tuoba.glb', '这是一个自动导入的模型：tuoba.glb');
INSERT INTO `models` VALUES (-135479234, '服装仓库', '主机.glb', '/models/服装仓库/主机.glb', '这是一个自动导入的模型：主机.glb');
INSERT INTO `models` VALUES (-135479233, '服装仓库', '传送带.glb', '/models/服装仓库/传送带.glb', '这是一个自动导入的模型：传送带.glb');
INSERT INTO `models` VALUES (-135479232, '服装仓库', '分拣车.glb', '/models/服装仓库/分拣车.glb', '这是一个自动导入的模型：分拣车.glb');
INSERT INTO `models` VALUES (-135479231, '服装仓库', '包装盒.glb', '/models/服装仓库/包装盒.glb', '这是一个自动导入的模型：包装盒.glb');
INSERT INTO `models` VALUES (-135479230, '服装仓库', '塑料周转箱.glb', '/models/服装仓库/塑料周转箱.glb', '这是一个自动导入的模型：塑料周转箱.glb');
INSERT INTO `models` VALUES (-135479229, '服装仓库', '夹克.glb', '/models/服装仓库/夹克.glb', '这是一个自动导入的模型：夹克.glb');
INSERT INTO `models` VALUES (-135479228, '服装仓库', '平板车.glb', '/models/服装仓库/平板车.glb', '这是一个自动导入的模型：平板车.glb');
INSERT INTO `models` VALUES (-135479227, '服装仓库', '手推车.glb', '/models/服装仓库/手推车.glb', '这是一个自动导入的模型：手推车.glb');
INSERT INTO `models` VALUES (-135479226, '服装仓库', '打包机.glb', '/models/服装仓库/打包机.glb', '这是一个自动导入的模型：打包机.glb');
INSERT INTO `models` VALUES (-135479225, '服装仓库', '扫码枪.glb', '/models/服装仓库/扫码枪.glb', '这是一个自动导入的模型：扫码枪.glb');
INSERT INTO `models` VALUES (-135479224, '服装仓库', '播种墙.glb', '/models/服装仓库/播种墙.glb', '这是一个自动导入的模型：播种墙.glb');
INSERT INTO `models` VALUES (-135479223, '服装仓库', '显示器.glb', '/models/服装仓库/显示器.glb', '这是一个自动导入的模型：显示器.glb');
INSERT INTO `models` VALUES (-135479222, '服装仓库', '标识牌a.glb', '/models/服装仓库/标识牌a.glb', '这是一个自动导入的模型：标识牌a.glb');
INSERT INTO `models` VALUES (-135479221, '服装仓库', '标识牌b.glb', '/models/服装仓库/标识牌b.glb', '这是一个自动导入的模型：标识牌b.glb');
INSERT INTO `models` VALUES (-135479220, '服装仓库', '标识牌c.glb', '/models/服装仓库/标识牌c.glb', '这是一个自动导入的模型：标识牌c.glb');
INSERT INTO `models` VALUES (-135479219, '服装仓库', '标识牌cc.glb', '/models/服装仓库/标识牌cc.glb', '这是一个自动导入的模型：标识牌cc.glb');
INSERT INTO `models` VALUES (-135479218, '服装仓库', '标识牌d.glb', '/models/服装仓库/标识牌d.glb', '这是一个自动导入的模型：标识牌d.glb');
INSERT INTO `models` VALUES (-135479217, '服装仓库', '标识牌dbc.glb', '/models/服装仓库/标识牌dbc.glb', '这是一个自动导入的模型：标识牌dbc.glb');
INSERT INTO `models` VALUES (-135479216, '服装仓库', '标识牌ddc.glb', '/models/服装仓库/标识牌ddc.glb', '这是一个自动导入的模型：标识牌ddc.glb');
INSERT INTO `models` VALUES (-135479215, '服装仓库', '标识牌fh.glb', '/models/服装仓库/标识牌fh.glb', '这是一个自动导入的模型：标识牌fh.glb');
INSERT INTO `models` VALUES (-135479214, '服装仓库', '标识牌hj.glb', '/models/服装仓库/标识牌hj.glb', '这是一个自动导入的模型：标识牌hj.glb');
INSERT INTO `models` VALUES (-135479213, '服装仓库', '标识牌rcbz.glb', '/models/服装仓库/标识牌rcbz.glb', '这是一个自动导入的模型：标识牌rcbz.glb');
INSERT INTO `models` VALUES (-135479212, '服装仓库', '衬衣.glb', '/models/服装仓库/衬衣.glb', '这是一个自动导入的模型：衬衣.glb');
INSERT INTO `models` VALUES (-135479211, '服装仓库', '裙子.glb', '/models/服装仓库/裙子.glb', '这是一个自动导入的模型：裙子.glb');
INSERT INTO `models` VALUES (-135479210, '服装仓库', '裤子.glb', '/models/服装仓库/裤子.glb', '这是一个自动导入的模型：裤子.glb');
INSERT INTO `models` VALUES (-135479209, '服装仓库', '西装.glb', '/models/服装仓库/西装.glb', '这是一个自动导入的模型：西装.glb');
INSERT INTO `models` VALUES (-135479208, '服装仓库', '西裤.glb', '/models/服装仓库/西裤.glb', '这是一个自动导入的模型：西裤.glb');
INSERT INTO `models` VALUES (-135479207, '服装仓库', '货架.glb', '/models/服装仓库/货架.glb', '这是一个自动导入的模型：货架.glb');
INSERT INTO `models` VALUES (-135479206, '服装仓库', '运输.glb', '/models/服装仓库/运输.glb', '这是一个自动导入的模型：运输.glb');
INSERT INTO `models` VALUES (-135479205, '服装仓库', '键盘.glb', '/models/服装仓库/键盘.glb', '这是一个自动导入的模型：键盘.glb');
INSERT INTO `models` VALUES (-135479204, '服装仓库', '鼠标.glb', '/models/服装仓库/鼠标.glb', '这是一个自动导入的模型：鼠标.glb');
INSERT INTO `models` VALUES (-135479203, '港口码头', 'a1.glb', '/models/港口码头/a1.glb', '这是一个自动导入的模型：a1.glb');
INSERT INTO `models` VALUES (-135479202, '港口码头', 'a10.glb', '/models/港口码头/a10.glb', '这是一个自动导入的模型：a10.glb');
INSERT INTO `models` VALUES (-135479201, '港口码头', 'a11.glb', '/models/港口码头/a11.glb', '这是一个自动导入的模型：a11.glb');
INSERT INTO `models` VALUES (-135479200, '港口码头', 'a2.glb', '/models/港口码头/a2.glb', '这是一个自动导入的模型：a2.glb');
INSERT INTO `models` VALUES (-135479199, '港口码头', 'a3.glb', '/models/港口码头/a3.glb', '这是一个自动导入的模型：a3.glb');
INSERT INTO `models` VALUES (-135479198, '港口码头', 'a4.glb', '/models/港口码头/a4.glb', '这是一个自动导入的模型：a4.glb');
INSERT INTO `models` VALUES (-135479197, '港口码头', 'a5.glb', '/models/港口码头/a5.glb', '这是一个自动导入的模型：a5.glb');
INSERT INTO `models` VALUES (-135479196, '港口码头', 'a6.glb', '/models/港口码头/a6.glb', '这是一个自动导入的模型：a6.glb');
INSERT INTO `models` VALUES (-135479195, '港口码头', 'a7.glb', '/models/港口码头/a7.glb', '这是一个自动导入的模型：a7.glb');
INSERT INTO `models` VALUES (-135479194, '港口码头', 'a8.glb', '/models/港口码头/a8.glb', '这是一个自动导入的模型：a8.glb');
INSERT INTO `models` VALUES (-135479193, '港口码头', 'a9.glb', '/models/港口码头/a9.glb', '这是一个自动导入的模型：a9.glb');
INSERT INTO `models` VALUES (-135479192, '港口码头', 'b.glb', '/models/港口码头/b.glb', '这是一个自动导入的模型：b.glb');
INSERT INTO `models` VALUES (-135479191, '港口码头', 'b1.glb', '/models/港口码头/b1.glb', '这是一个自动导入的模型：b1.glb');
INSERT INTO `models` VALUES (-135479190, '港口码头', 'baowen_c.glb', '/models/港口码头/baowen_c.glb', '这是一个自动导入的模型：baowen_c.glb');
INSERT INTO `models` VALUES (-135479189, '港口码头', 'c.glb', '/models/港口码头/c.glb', '这是一个自动导入的模型：c.glb');
INSERT INTO `models` VALUES (-135479188, '港口码头', 'ContainerSpreader_6m_01.glb', '/models/港口码头/ContainerSpreader_6m_01.glb', '这是一个自动导入的模型：ContainerSpreader_6m_01.glb');
INSERT INTO `models` VALUES (-135479187, '港口码头', 'CraneCable_5m_01.glb', '/models/港口码头/CraneCable_5m_01.glb', '这是一个自动导入的模型：CraneCable_5m_01.glb');
INSERT INTO `models` VALUES (-135479186, '港口码头', 'GantryCrane_01_Traction_01.glb', '/models/港口码头/GantryCrane_01_Traction_01.glb', '这是一个自动导入的模型：GantryCrane_01_Traction_01.glb');
INSERT INTO `models` VALUES (-135479185, '港口码头', 'GantryCrane_02_Structure_01.glb', '/models/港口码头/GantryCrane_02_Structure_01.glb', '这是一个自动导入的模型：GantryCrane_02_Structure_01.glb');
INSERT INTO `models` VALUES (-135479184, '港口码头', 'GantryCrane_02_Traction_01.glb', '/models/港口码头/GantryCrane_02_Traction_01.glb', '这是一个自动导入的模型：GantryCrane_02_Traction_01.glb');
INSERT INTO `models` VALUES (-135479183, '港口码头', 'GantryCrane_02_Trolley_01.glb', '/models/港口码头/GantryCrane_02_Trolley_01.glb', '这是一个自动导入的模型：GantryCrane_02_Trolley_01.glb');
INSERT INTO `models` VALUES (-135479182, '港口码头', 'guashi_c.glb', '/models/港口码头/guashi_c.glb', '这是一个自动导入的模型：guashi_c.glb');
INSERT INTO `models` VALUES (-135479181, '港口码头', 'kaiding_c.glb', '/models/港口码头/kaiding_c.glb', '这是一个自动导入的模型：kaiding_c.glb');
INSERT INTO `models` VALUES (-135479180, '港口码头', 'lengcang_c.glb', '/models/港口码头/lengcang_c.glb', '这是一个自动导入的模型：lengcang_c.glb');
INSERT INTO `models` VALUES (-135479179, '港口码头', 'qiaoxingqizhongji.glb', '/models/港口码头/qiaoxingqizhongji.glb', '这是一个自动导入的模型：qiaoxingqizhongji.glb');
INSERT INTO `models` VALUES (-135479178, '港口码头', 'qizhongji.glb', '/models/港口码头/qizhongji.glb', '这是一个自动导入的模型：qizhongji.glb');
INSERT INTO `models` VALUES (-135479177, '港口码头', 'ShipyardCrane_Bottom_01.glb', '/models/港口码头/ShipyardCrane_Bottom_01.glb', '这是一个自动导入的模型：ShipyardCrane_Bottom_01.glb');
INSERT INTO `models` VALUES (-135479176, '港口码头', 'ShipyardCrane_Cabin_01.glb', '/models/港口码头/ShipyardCrane_Cabin_01.glb', '这是一个自动导入的模型：ShipyardCrane_Cabin_01.glb');
INSERT INTO `models` VALUES (-135479175, '港口码头', 'ShipyardCrane_Front_01.glb', '/models/港口码头/ShipyardCrane_Front_01.glb', '这是一个自动导入的模型：ShipyardCrane_Front_01.glb');
INSERT INTO `models` VALUES (-135479174, '港口码头', 'ShipyardCrane_Shaft_01.glb', '/models/港口码头/ShipyardCrane_Shaft_01.glb', '这是一个自动导入的模型：ShipyardCrane_Shaft_01.glb');
INSERT INTO `models` VALUES (-135479173, '港口码头', 'ShipyardCrane_Top_01.glb', '/models/港口码头/ShipyardCrane_Top_01.glb', '这是一个自动导入的模型：ShipyardCrane_Top_01.glb');
INSERT INTO `models` VALUES (-135479172, '港口码头', '中型货架.glb', '/models/港口码头/中型货架.glb', '这是一个自动导入的模型：中型货架.glb');
INSERT INTO `models` VALUES (-135479171, '港口码头', '传送带.glb', '/models/港口码头/传送带.glb', '这是一个自动导入的模型：传送带.glb');
INSERT INTO `models` VALUES (-135479170, '港口码头', '卡车.glb', '/models/港口码头/卡车.glb', '这是一个自动导入的模型：卡车.glb');
INSERT INTO `models` VALUES (-135479169, '港口码头', '叉车.glb', '/models/港口码头/叉车.glb', '这是一个自动导入的模型：叉车.glb');
INSERT INTO `models` VALUES (-135479168, '港口码头', '托盘.glb', '/models/港口码头/托盘.glb', '这是一个自动导入的模型：托盘.glb');
INSERT INTO `models` VALUES (-135479167, '港口码头', '机械臂.glb', '/models/港口码头/机械臂.glb', '这是一个自动导入的模型：机械臂.glb');
INSERT INTO `models` VALUES (-135479166, '港口码头', '海警船glb.glb', '/models/港口码头/海警船glb.glb', '这是一个自动导入的模型：海警船glb.glb');
INSERT INTO `models` VALUES (-135479165, '港口码头', '液压抓斗.glb', '/models/港口码头/液压抓斗.glb', '这是一个自动导入的模型：液压抓斗.glb');
INSERT INTO `models` VALUES (-135479164, '港口码头', '砂船.glb', '/models/港口码头/砂船.glb', '这是一个自动导入的模型：砂船.glb');
INSERT INTO `models` VALUES (-135479163, '港口码头', '货船.glb', '/models/港口码头/货船.glb', '这是一个自动导入的模型：货船.glb');
INSERT INTO `models` VALUES (-135479162, '港口码头', '货船2.glb', '/models/港口码头/货船2.glb', '这是一个自动导入的模型：货船2.glb');
INSERT INTO `models` VALUES (-135479161, '港口码头', '轻型货架.glb', '/models/港口码头/轻型货架.glb', '这是一个自动导入的模型：轻型货架.glb');
INSERT INTO `models` VALUES (-135479160, '港口码头', '重型货架.glb', '/models/港口码头/重型货架.glb', '这是一个自动导入的模型：重型货架.glb');
INSERT INTO `models` VALUES (-135479159, '港口码头', '集装箱1.glb', '/models/港口码头/集装箱1.glb', '这是一个自动导入的模型：集装箱1.glb');
INSERT INTO `models` VALUES (-135479158, '港口码头', '集装箱2.glb', '/models/港口码头/集装箱2.glb', '这是一个自动导入的模型：集装箱2.glb');
INSERT INTO `models` VALUES (-135479157, '港口码头', '集装箱3.glb', '/models/港口码头/集装箱3.glb', '这是一个自动导入的模型：集装箱3.glb');
INSERT INTO `models` VALUES (-135479156, '港口码头', '集装箱4.glb', '/models/港口码头/集装箱4.glb', '这是一个自动导入的模型：集装箱4.glb');
INSERT INTO `models` VALUES (-135479155, '港口码头', '集装箱5.glb', '/models/港口码头/集装箱5.glb', '这是一个自动导入的模型：集装箱5.glb');
INSERT INTO `models` VALUES (200000041, '水务水利/监测与自动化设备', 'PH计.glb', '/models/水务水利/监测与自动化设备/PH计.glb', '这是一个自动导入的模型：PH计.glb');
INSERT INTO `models` VALUES (200000042, '水务水利/监测与自动化设备', '压力传感器.glb', '/models/水务水利/监测与自动化设备/压力传感器.glb', '这是一个自动导入的模型：压力传感器.glb');
INSERT INTO `models` VALUES (200000043, '水务水利/监测与自动化设备', '操作台.glb', '/models/水务水利/监测与自动化设备/操作台.glb', '这是一个自动导入的模型：操作台.glb');
INSERT INTO `models` VALUES (200000044, '水务水利/监测与自动化设备', '浊度计.glb', '/models/水务水利/监测与自动化设备/浊度计.glb', '这是一个自动导入的模型：浊度计.glb');
INSERT INTO `models` VALUES (200000045, '水务水利/监测与自动化设备', '浮子水位计.glb', '/models/水务水利/监测与自动化设备/浮子水位计.glb', '这是一个自动导入的模型：浮子水位计.glb');
INSERT INTO `models` VALUES (200000046, '水务水利/监测与自动化设备', '溶氧仪.glb', '/models/水务水利/监测与自动化设备/溶氧仪.glb', '这是一个自动导入的模型：溶氧仪.glb');
INSERT INTO `models` VALUES (200000047, '水务水利/监测与自动化设备', '电磁流量计.glb', '/models/水务水利/监测与自动化设备/电磁流量计.glb', '这是一个自动导入的模型：电磁流量计.glb');
INSERT INTO `models` VALUES (200000048, '水务水利/监测与自动化设备', '自动化控制柜.glb', '/models/水务水利/监测与自动化设备/自动化控制柜.glb', '这是一个自动导入的模型：自动化控制柜.glb');
INSERT INTO `models` VALUES (200000049, '水务水利/监测与自动化设备', '视频监控摄像头.glb', '/models/水务水利/监测与自动化设备/视频监控摄像头.glb', '这是一个自动导入的模型：视频监控摄像头.glb');
INSERT INTO `models` VALUES (200000050, '水务水利/监测与自动化设备', '超声波水位计.glb', '/models/水务水利/监测与自动化设备/超声波水位计.glb', '这是一个自动导入的模型：超声波水位计.glb');
INSERT INTO `models` VALUES (200000051, '水务水利/监测与自动化设备', '超声波流量计.glb', '/models/水务水利/监测与自动化设备/超声波流量计.glb', '这是一个自动导入的模型：超声波流量计.glb');
INSERT INTO `models` VALUES (200000052, '水务水利/监测与自动化设备', '雷达水位计.glb', '/models/水务水利/监测与自动化设备/雷达水位计.glb', '这是一个自动导入的模型：雷达水位计.glb');
INSERT INTO `models` VALUES (200000053, '水务水利/水闸设备', '叠梁闸门.glb', '/models/水务水利/水闸设备/叠梁闸门.glb', '这是一个自动导入的模型：叠梁闸门.glb');
INSERT INTO `models` VALUES (200000054, '水务水利/水闸设备', '蝶阀.glb', '/models/水务水利/水闸设备/蝶阀.glb', '这是一个自动导入的模型：蝶阀.glb');
INSERT INTO `models` VALUES (200000055, '水务水利/水闸设备', '拍门.glb', '/models/水务水利/水闸设备/拍门.glb', '这是一个自动导入的模型：拍门.glb');
INSERT INTO `models` VALUES (200000056, '水务水利/水泵设备', '泵房.glb', '/models/水务水利/水泵设备/泵房.glb', '这是一个自动导入的模型：泵房.glb');
INSERT INTO `models` VALUES (200000057, '水务水利/水泵设备', '离心泵.glb', '/models/水务水利/水泵设备/离心泵.glb', '这是一个自动导入的模型：离心泵.glb');
INSERT INTO `models` VALUES (200000058, '水务水利/水泵设备', '潜水泵.glb', '/models/水务水利/水泵设备/潜水泵.glb', '这是一个自动导入的模型：潜水泵.glb');
INSERT INTO `models` VALUES (200000059, '水务水利/水泵设备', '轴流泵.glb', '/models/水务水利/水泵设备/轴流泵.glb', '这是一个自动导入的模型：轴流泵.glb');
INSERT INTO `models` VALUES (200000060, '水务水利/水闸设备', '电动启闭机.glb', '/models/水务水利/水闸设备/电动启闭机.glb', '这是一个自动导入的模型：电动启闭机.glb');
INSERT INTO `models` VALUES (200000061, '水务水利/水闸设备', '卷扬式启闭机.glb', '/models/水务水利/水闸设备/卷扬式启闭机.glb', '这是一个自动导入的模型：卷扬式启闭机.glb');
INSERT INTO `models` VALUES (200000062, '水务水利/水闸设备', '闸阀.glb', '/models/水务水利/水闸设备/闸阀.glb', '这是一个自动导入的模型：闸阀.glb');
INSERT INTO `models` VALUES (200000063, '水务水利/水闸设备', '止回阀.glb', '/models/水务水利/水闸设备/止回阀.glb', '这是一个自动导入的模型：止回阀.glb');
INSERT INTO `models` VALUES (200000064, '水务水利/水闸设备', 'P型止水橡皮.glb', '/models/水务水利/水闸设备/P型止水橡皮.glb', '这是一个自动导入的模型：P型止水橡皮.glb');
INSERT INTO `models` VALUES (200000065, '水务水利/环境要素', '道路.glb', '/models/水务水利/环境要素/道路.glb', '这是一个自动导入的模型：道路.glb');
INSERT INTO `models` VALUES (200000066, '水务水利/水处理设备', '沉淀池.glb', '/models/水务水利/水处理设备/沉淀池.glb', '这是一个自动导入的模型：沉淀池.glb');
INSERT INTO `models` VALUES (200000067, '水务水利/水处理设备', '活性炭过滤器.glb', '/models/水务水利/水处理设备/活性炭过滤器.glb', '这是一个自动导入的模型：活性炭过滤器.glb');
INSERT INTO `models` VALUES (200000068, '水务水利/水处理设备', '砂滤器.glb', '/models/水务水利/水处理设备/砂滤器.glb', '这是一个自动导入的模型：砂滤器.glb');
INSERT INTO `models` VALUES (200000069, '水务水利/水处理设备', '格栅除污机.glb', '/models/水务水利/水处理设备/格栅除污机.glb', '这是一个自动导入的模型：格栅除污机.glb');
INSERT INTO `models` VALUES (200000070, '水务水利/水处理设备', '集水池.glb', '/models/水务水利/水处理设备/集水池.glb', '这是一个自动导入的模型：集水池.glb');
INSERT INTO `models` VALUES (200000071, '水务水利/电气与动力设施', '控制台.glb', '/models/水务水利/电气与动力设施/控制台.glb', '这是一个自动导入的模型：控制台.glb');
INSERT INTO `models` VALUES (200000072, '水务水利/电气与动力设施', '螺旋输送机.glb', '/models/水务水利/电气与动力设施/螺旋输送机.glb', '这是一个自动导入的模型：螺旋输送机.glb');
INSERT INTO `models` VALUES (200000073, '水务水利/电气与动力设施', '台式电脑.glb', '/models/水务水利/电气与动力设施/台式电脑.glb', '这是一个自动导入的模型：台式电脑.glb');
INSERT INTO `models` VALUES (200000074, '水务水利/水闸设备', '闸底板.glb', '/models/水务水利/水闸设备/闸底板.glb', '这是一个自动导入的模型：闸底板.glb');
INSERT INTO `models` VALUES (200000075, '水务水利/水闸设备', '闸墩.glb', '/models/水务水利/水闸设备/闸墩.glb', '这是一个自动导入的模型：闸墩.glb');
INSERT INTO `models` VALUES (200000076, '水务水利/水闸设备', '水闸建筑.glb', '/models/水务水利/水闸设备/水闸建筑.glb', '这是一个自动导入的模型：水闸建筑.glb');
INSERT INTO `models` VALUES (200000077, '水务水利/环境要素', '安全防护栏.glb', '/models/水务水利/环境要素/安全防护栏.glb', '这是一个自动导入的模型：安全防护栏.glb');
INSERT INTO `models` VALUES (200000078, '水务水利/环境要素', '安全警示标志.glb', '/models/水务水利/环境要素/安全警示标志.glb', '这是一个自动导入的模型：安全警示标志.glb');
INSERT INTO `models` VALUES (200000079, '水务水利/环境要素', '消防栓.glb', '/models/水务水利/环境要素/消防栓.glb', '这是一个自动导入的模型：消防栓.glb');
INSERT INTO `models` VALUES (200000080, '水务水利/环境要素', '应急照明设备.glb', '/models/水务水利/环境要素/应急照明设备.glb', '这是一个自动导入的模型：应急照明设备.glb');
INSERT INTO `models` VALUES (200000081, '水务水利/监测与自动化设备', 'AI摄像头.glb', '/models/水务水利/监测与自动化设备/AI摄像头.glb', '这是一个自动导入的模型：AI摄像头.glb');
INSERT INTO `models` VALUES (200000082, '水务水利/监测与自动化设备', '水下机器人.glb', '/models/水务水利/监测与自动化设备/水下机器人.glb', '这是一个自动导入的模型：水下机器人.glb');
INSERT INTO `models` VALUES (200000083, '水务水利/水处理设备', '拦污栅清理机.glb', '/models/水务水利/水处理设备/拦污栅清理机.glb', '这是一个自动导入的模型：拦污栅清理机.glb');
INSERT INTO `models` VALUES (200000084, '水务水利/监测设备', '地下水监测井.glb', '/models/水务水利/监测设备/地下水监测井.glb', '这是一个自动导入的模型：地下水监测井.glb');
INSERT INTO `models` VALUES (200000085, '水务水利/环境要素', '检修车间.glb', '/models/水务水利/环境要素/检修车间.glb', '这是一个自动导入的模型：检修车间.glb');
INSERT INTO `models` VALUES (200000086, '水务水利/水处理设备', '空气阀.glb', '/models/水务水利/水处理设备/空气阀.glb', '这是一个自动导入的模型：空气阀.glb');
INSERT INTO `models` VALUES (200000087, '水务水利/水处理设备', '离子交换器.glb', '/models/水务水利/水处理设备/离子交换器.glb', '这是一个自动导入的模型：离子交换器.glb');
INSERT INTO `models` VALUES (200000088, '水务水利/测量设备', '流速仪(便携式).glb', '/models/水务水利/测量设备/流速仪(便携式).glb', '这是一个自动导入的模型：流速仪(便携式).glb');
INSERT INTO `models` VALUES (200000089, '水务水利/测量设备', '流速仪(固定式).glb', '/models/水务水利/测量设备/流速仪(固定式).glb', '这是一个自动导入的模型：流速仪(固定式).glb');
INSERT INTO `models` VALUES (200000090, '水务水利/水处理设备', '闸膜过滤系统.glb', '/models/水务水利/水处理设备/膜过滤系统.glb', '这是一个自动导入的模型：膜过滤系统.glb');
INSERT INTO `models` VALUES (200000091, '水务水利/监测设备', '泥沙监测仪.glb', '/models/水务水利/监测设备/泥沙监测仪.glb', '这是一个自动导入的模型：泥沙监测仪.glb');
INSERT INTO `models` VALUES (200000092, '水务水利/监测设备', '气象站(一体化).glb', '/models/水务水利/监测设备/气象站(一体化).glb', '这是一个自动导入的模型：气象站(一体化).glb');
INSERT INTO `models` VALUES (200000093, '水务水利/水处理设备', '污泥浓缩机.glb', '/models/水务水利/水处理设备/污泥浓缩机.glb', '这是一个自动导入的模型：污泥浓缩机.glb');
INSERT INTO `models` VALUES (200000094, '水务水利/环境要素', '安全爬梯.glb', '/models/水务水利/环境要素/安全爬梯.glb', '这是一个自动导入的模型：安全爬梯.glb');
INSERT INTO `models` VALUES (200000095, '水务水利/环境要素', '防雷接地装置.glb', '/models/水务水利/环境要素/防雷接地装置.glb', '这是一个自动导入的模型：防雷接地装置.glb');
INSERT INTO `models` VALUES (200000096, '水务水利/环境要素', '景观照明灯.glb', '/models/水务水利/环境要素/景观照明灯.glb', '这是一个自动导入的模型：景观照明灯.glb');
INSERT INTO `models` VALUES (200000097, '水务水利/环境要素', '救生圈.glb', '/models/水务水利/环境要素/救生圈.glb', '这是一个自动导入的模型：救生圈.glb');
INSERT INTO `models` VALUES (200000098, '水务水利/环境要素', '生态浮岛.glb', '/models/水务水利/环境要素/生态浮岛.glb', '这是一个自动导入的模型：生态浮岛.glb');
INSERT INTO `models` VALUES (200000099, '水务水利/环境要素', '太阳能供电系统.glb', '/models/水务水利/环境要素/太阳能供电系统.glb', '这是一个自动导入的模型：太阳能供电系统.glb');
INSERT INTO `models` VALUES (200000100, '水务水利/环境要素', 'PLC控制箱.glb', '/models/水务水利/环境要素/PLC控制箱.glb', '这是一个自动导入的模型：PLC控制箱.glb');
INSERT INTO `models` VALUES (200000101, '水务水利/水坝工程', '水坝.glb', '/models/水务水利/水坝工程/水坝.glb', '这是一个自动导入的模型：水坝.glb');
INSERT INTO `models` VALUES (200000102, '水务水利/施工机械', '工程抢修车.glb', '/models/水务水利/施工机械/工程抢修车.glb', '这是一个自动导入的模型：工程抢修车.glb');
INSERT INTO `models` VALUES (200000103, '水务水利/管道体系', '钢管.glb', '/models/水务水利/管道体系/钢管.glb', '这是一个自动导入的模型：钢管.glb');
INSERT INTO `models` VALUES (200000104, '水务水利/管道体系', '混凝土管.glb', '/models/水务水利/管道体系/混凝土管.glb', '这是一个自动导入的模型：混凝土管.glb');
INSERT INTO `models` VALUES (200000105, '水务水利/环境要素', '仓库.glb', '/models/水务水利/环境要素/仓库.glb', '这是一个自动导入的模型：仓库.glb');
INSERT INTO `models` VALUES (200000106, '水务水利/环境要素', '配电室.glb', '/models/水务水利/环境要素/配电室.glb', '这是一个自动导入的模型：配电室.glb');
INSERT INTO `models` VALUES (200000107, '水务水利/环境要素', '控制室.glb', '/models/水务水利/环境要素/控制室.glb', '这是一个自动导入的模型：控制室.glb');
INSERT INTO `models` VALUES (200000108, '水务水利/环境要素', '厂区道路.glb', '/models/水务水利/环境要素/厂区道路.glb', '这是一个自动导入的模型：厂区道路.glb');
INSERT INTO `models` VALUES (200000109, '水务水利/环境要素', '施工大门.glb', '/models/水务水利/环境要素/施工大门.glb', '这是一个自动导入的模型：施工大门.glb');
INSERT INTO `models` VALUES (200000110, '水务水利/环境要素', '排水沟.glb', '/models/水务水利/环境要素/排水沟.glb', '这是一个自动导入的模型：排水沟.glb');
INSERT INTO `models` VALUES (200000111, '水务水利/环境要素', '引水渠.glb', '/models/水务水利/环境要素/引水渠.glb', '这是一个自动导入的模型：引水渠.glb');
INSERT INTO `models` VALUES (200000112, '水务水利/环境要素', '水塔.glb', '/models/水务水利/环境要素/水塔.glb', '这是一个自动导入的模型：水塔.glb');
INSERT INTO `models` VALUES (200000113, '水务水利/环境要素', '净水厂.glb', '/models/水务水利/环境要素/净水厂.glb', '这是一个自动导入的模型：净水厂.glb');
INSERT INTO `models` VALUES (200000114, '水务水利/环境要素', '污水处理厂.glb', '/models/水务水利/环境要素/污水处理厂.glb', '这是一个自动导入的模型：污水处理厂.glb');
INSERT INTO `models` VALUES (200000115, '水务水利/环境要素', '码头.glb', '/models/水务水利/环境要素/码头.glb', '这是一个自动导入的模型：码头.glb');
INSERT INTO `models` VALUES (200000116, '水务水利/管道体系', '管件(法兰).glb', '/models/水务水利/管道体系/管件(法兰).glb', '这是一个自动导入的模型：管件(法兰).glb');
INSERT INTO `models` VALUES (200000117, '水务水利/管道体系', '管件(法兰)2.glb', '/models/水务水利/管道体系/管件(法兰)2.glb', '这是一个自动导入的模型：管件(法兰)2.glb');
INSERT INTO `models` VALUES (200000118, '水务水利/管道体系', '管件(三通).glb', '/models/水务水利/管道体系/管件(三通).glb', '这是一个自动导入的模型：管件(三通).glb');
INSERT INTO `models` VALUES (200000119, '水务水利/管道体系', '管件(弯头).glb', '/models/水务水利/管道体系/管件(弯头).glb', '这是一个自动导入的模型：管件(弯头).glb');
INSERT INTO `models` VALUES (200000120, '水务水利/管道体系', '管件(弯头)2.glb', '/models/水务水利/管道体系/管件(弯头)2.glb', '这是一个自动导入的模型：管件(弯头)2.glb');
INSERT INTO `models` VALUES (200000121, '水务水利/环境要素', '门卫机器人1.glb', '/models/水务水利/环境要素/门卫机器人1.glb', '这是一个自动导入的模型：门卫机器人1.glb');
INSERT INTO `models` VALUES (200000122, '水务水利/环境要素', '门卫机器人2.glb', '/models/水务水利/环境要素/门卫机器人2.glb', '这是一个自动导入的模型：门卫机器人2.glb');
INSERT INTO `models` VALUES (200000123, '水务水利/水坝工程', '减震装置.glb', '/models/水务水利/水坝工程/减震装置.glb', '这是一个自动导入的模型：减震装置.glb');
INSERT INTO `models` VALUES (200000124, '水务水利/施工机械', '取水泵船.glb', '/models/水务水利/施工机械/取水泵船.glb', '这是一个自动导入的模型：取水泵船.glb');
INSERT INTO `models` VALUES (200000125, '水务水利/环境要素', '取水隧洞.glb', '/models/水务水利/环境要素/取水隧洞.glb', '这是一个自动导入的模型：取水隧洞.glb');
INSERT INTO `models` VALUES (200000126, '水务水利/环境要素', '护脚构造.glb', '/models/水务水利/环境要素/护脚构造.glb', '这是一个自动导入的模型：护脚构造.glb');
INSERT INTO `models` VALUES (200000127, '水务水利/环境要素', '轨道.glb', '/models/水务水利/环境要素/轨道.glb', '这是一个自动导入的模型：轨道.glb');
INSERT INTO `models` VALUES (200000128, '水务水利/环境要素', '路缘石.glb', '/models/水务水利/环境要素/路缘石.glb', '这是一个自动导入的模型：路缘石.glb');
INSERT INTO `models` VALUES (200000129, '水务水利/雨洪利用', '处理机组.glb', '/models/水务水利/雨洪利用/处理机组.glb', '这是一个自动导入的模型：处理机组.glb');
INSERT INTO `models` VALUES (200000130, '水务水利/雨洪利用', '回用泵站.glb', '/models/水务水利/雨洪利用/回用泵站.glb', '这是一个自动导入的模型：回用泵站.glb');
INSERT INTO `models` VALUES (200000131, '水务水利/雨洪利用', '回用水箱.glb', '/models/水务水利/雨洪利用/回用水箱.glb', '这是一个自动导入的模型：回用水箱.glb');
INSERT INTO `models` VALUES (200000132, '水务水利/雨洪利用', '下凹绿地.glb', '/models/水务水利/雨洪利用/下凹绿地.glb', '这是一个自动导入的模型：下凹绿地.glb');
INSERT INTO `models` VALUES (200000133, '水务水利/雨洪利用', '溢流口.glb', '/models/水务水利/雨洪利用/溢流口.glb', '这是一个自动导入的模型：溢流口.glb');
INSERT INTO `models` VALUES (200000134, '水务水利/雨洪利用', '雨水花园.glb', '/models/水务水利/雨洪利用/雨水花园.glb', '这是一个自动导入的模型：雨水花园.glb');
INSERT INTO `models` VALUES (200000135, '水务水利/取水与原水系统', '沉砂槽.glb', '/models/水务水利/取水与原水系统/沉砂槽.glb', '这是一个自动导入的模型：沉砂槽.glb');
INSERT INTO `models` VALUES (200000136, '水务水利/取水与原水系统', '出水管.glb', '/models/水务水利/取水与原水系统/出水管.glb', '这是一个自动导入的模型：出水管.glb');
INSERT INTO `models` VALUES (200000137, '水务水利/取水与原水系统', '防涡板.glb', '/models/水务水利/取水与原水系统/防涡板.glb', '这是一个自动导入的模型：防涡板.glb');
INSERT INTO `models` VALUES (200000138, '水务水利/取水与原水系统', '井门.glb', '/models/水务水利/取水与原水系统/井门.glb', '这是一个自动导入的模型：井门.glb');
INSERT INTO `models` VALUES (200000139, '水务水利/取水与原水系统', '前池.glb', '/models/水务水利/取水与原水系统/前池.glb', '这是一个自动导入的模型：前池.glb');
INSERT INTO `models` VALUES (200000140, '水务水利/取水与原水系统', '浅井.glb', '/models/水务水利/取水与原水系统/浅井.glb', '这是一个自动导入的模型：浅井.glb');
INSERT INTO `models` VALUES (200000141, '水务水利/取水与原水系统', '深井.glb', '/models/水务水利/取水与原水系统/深井.glb', '这是一个自动导入的模型：深井.glb');
INSERT INTO `models` VALUES (200000142, '水务水利/取水与原水系统', '吸水井.glb', '/models/水务水利/取水与原水系统/吸水井.glb', '这是一个自动导入的模型：吸水井.glb');
INSERT INTO `models` VALUES (200000143, '水务水利/取水与原水系统', '原水泵.glb', '/models/水务水利/取水与原水系统/原水泵.glb', '这是一个自动导入的模型：原水泵.glb');
INSERT INTO `models` VALUES (200000144, '水务水利/取水与原水系统', '阀门井.glb', '/models/水务水利/取水与原水系统/阀门井.glb', '这是一个自动导入的模型：阀门井.glb');
INSERT INTO `models` VALUES (200000145, '水务水利/污水再生水处理', '超滤装置.glb', '/models/水务水利/污水再生水处理/超滤装置.glb', '这是一个自动导入的模型：超滤装置.glb');
INSERT INTO `models` VALUES (200000146, '水务水利/污水再生水处理', '二沉池.glb', '/models/水务水利/污水再生水处理/二沉池.glb', '这是一个自动导入的模型：二沉池.glb');
INSERT INTO `models` VALUES (200000147, '水务水利/污水再生水处理', '反硝化滤池.glb', '/models/水务水利/污水再生水处理/反硝化滤池.glb', '这是一个自动导入的模型：反硝化滤池.glb');
INSERT INTO `models` VALUES (200000148, '水务水利/污水再生水处理', '鼓风机房.glb', '/models/水务水利/污水再生水处理/鼓风机房.glb', '这是一个自动导入的模型：鼓风机房.glb');
INSERT INTO `models` VALUES (200000149, '水务水利/污水再生水处理', '化学洗涤塔.glb', '/models/水务水利/污水再生水处理/化学洗涤塔.glb', '这是一个自动导入的模型：化学洗涤塔.glb');
INSERT INTO `models` VALUES (200000150, '水务水利/污水再生水处理', '活性炭吸附塔.glb', '/models/水务水利/污水再生水处理/活性炭吸附塔.glb', '这是一个自动导入的模型：活性炭吸附塔.glb');
INSERT INTO `models` VALUES (200000151, '水务水利/污水再生水处理', '矿化装置.glb', '/models/水务水利/污水再生水处理/矿化装置.glb', '这是一个自动导入的模型：矿化装置.glb');
INSERT INTO `models` VALUES (200000152, '水务水利/污水再生水处理', '浓缩池.glb', '/models/水务水利/污水再生水处理/浓缩池.glb', '这是一个自动导入的模型：浓缩池.glb');
INSERT INTO `models` VALUES (200000153, '水务水利/污水再生水处理', '生物滤池除臭塔.glb', '/models/水务水利/污水再生水处理/生物滤池除臭塔.glb', '这是一个自动导入的模型：生物滤池除臭塔.glb');
INSERT INTO `models` VALUES (200000154, '水务水利/污水再生水处理', '尾水排放口.glb', '/models/水务水利/污水再生水处理/尾水排放口.glb', '这是一个自动导入的模型：尾水排放口.glb');
INSERT INTO `models` VALUES (200000155, '水务水利/污水再生水处理', '消毒接触池.glb', '/models/水务水利/污水再生水处理/消毒接触池.glb', '这是一个自动导入的模型：消毒接触池.glb');
INSERT INTO `models` VALUES (200000156, '水务水利/污水再生水处理', '氧化池.glb', '/models/水务水利/污水再生水处理/氧化池.glb', '这是一个自动导入的模型：氧化池.glb');
INSERT INTO `models` VALUES (200000157, '水务水利/污水再生水处理', '在线监测小屋.glb', '/models/水务水利/污水再生水处理/在线监测小屋.glb', '这是一个自动导入的模型：在线监测小屋.glb');
INSERT INTO `models` VALUES (200000158, '水务水利/污水再生水处理', '贮泥仓.glb', '/models/水务水利/污水再生水处理/贮泥仓.glb', '这是一个自动导入的模型：贮泥仓.glb');
INSERT INTO `models` VALUES (200000159, '水务水利/污水再生水处理', '装车平台.glb', '/models/水务水利/污水再生水处理/装车平台.glb', '这是一个自动导入的模型：装车平台.glb');
INSERT INTO `models` VALUES (200000160, '水务水利/污水再生水处理', 'A2O反应池.glb', '/models/水务水利/污水再生水处理/A2O反应池.glb', '这是一个自动导入的模型：A2O反应池.glb');
INSERT INTO `models` VALUES (200000161, '水务水利/污水再生水处理', 'MBR膜池.glb', '/models/水务水利/污水再生水处理/MBR膜池.glb', '这是一个自动导入的模型：MBR膜池.glb');
INSERT INTO `models` VALUES (200000162, '水务水利/水轮机', '两击式水轮机.glb', '/models/水务水利/水轮机/两击式水轮机.glb', '这是一个自动导入的模型：两击式水轮机.glb');
INSERT INTO `models` VALUES (200000163, '水务水利/水轮机', '斜击式水轮机.glb', '/models/水务水利/水轮机/斜击式水轮机.glb', '这是一个自动导入的模型：斜击式水轮机.glb');
INSERT INTO `models` VALUES (200000164, '水务水利/水轮机', '水斗式水轮机.glb', '/models/水务水利/水轮机/水斗式水轮机.glb', '这是一个自动导入的模型：水斗式水轮机.glb');
INSERT INTO `models` VALUES (200000165, '水务水利/水轮机', '混流式水轮机.glb', '/models/水务水利/水轮机/混流式水轮机.glb', '这是一个自动导入的模型：混流式水轮机.glb');
INSERT INTO `models` VALUES (200000166, '水务水利/水轮机', '贯流式水轮机.glb', '/models/水务水利/水轮机/贯流式水轮机.glb', '这是一个自动导入的模型：贯流式水轮机.glb');
INSERT INTO `models` VALUES (200000167, '水务水利/水轮机', '轴流式水轮机.glb', '/models/水务水利/水轮机/轴流式水轮机.glb', '这是一个自动导入的模型：轴流式水轮机.glb');
INSERT INTO `models` VALUES (200000168, '水务水利/管道体系', '高压水道布置.glb', '/models/水务水利/管道体系/高压水道布置.glb', '这是一个自动导入的模型：高压水道布置.glb');
INSERT INTO `models` VALUES (200000169, '水务水利/管道体系', '双室式调压室.glb', '/models/水务水利/管道体系/双室式调压室.glb', '这是一个自动导入的模型：双室式调压室.glb');
INSERT INTO `models` VALUES (200000170, '水务水利/管道体系', '差动式调压室.glb', '/models/水务水利/管道体系/差动式调压室.glb', '这是一个自动导入的模型：差动式调压室.glb');
INSERT INTO `models` VALUES (200000171, '水务水利/监测设备', '无人机巡检.glb', '/models/水务水利/监测设备/无人机巡检.glb', '这是一个自动导入的模型：无人机巡检.glb');
INSERT INTO `models` VALUES (200000172, '水务水利/雨洪利用', '移动式防洪墙.glb', '/models/水务水利/雨洪利用/移动式防洪墙.glb', '这是一个自动导入的模型：移动式防洪墙.glb');
INSERT INTO `models` VALUES (200000173, '水务水利/环境要素', '人工湿地单元.glb', '/models/水务水利/环境要素/人工湿地单元.glb', '这是一个自动导入的模型：人工湿地单元.glb');
INSERT INTO `models` VALUES (200000174, '水务水利/环境要素', '鱼类洄游通道.glb', '/models/水务水利/环境要素/鱼类洄游通道.glb', '这是一个自动导入的模型：鱼类洄游通道.glb');
INSERT INTO `models` VALUES (200000175, '水务水利/雨洪利用', '堤防河道整治.glb', '/models/水务水利/雨洪利用/堤防河道整治.glb', '这是一个自动导入的模型：堤防河道整治.glb');
INSERT INTO `models` VALUES (200000176, '水务水利/水坝工程', '大坝BIM模型.glb', '/models/水务水利/水坝工程/大坝BIM模型.glb', '这是一个自动导入的模型：大坝BIM模型.glb');
INSERT INTO `models` VALUES (200000177, '水务水利/环境要素', '数字地表模型.glb', '/models/水务水利/环境要素/数字地表模型.glb', '这是一个自动导入的模型：数字地表模型.glb');
INSERT INTO `models` VALUES (200000178, '水务水利/水闸设备', '水闸泵站glb', '/models/水务水利/水闸设备/水闸泵站.glb', '这是一个自动导入的模型：水闸泵站glb');
INSERT INTO `models` VALUES (200000179, '水务水利/管道体系', '输水管道.glb', '/models/水务水利/管道体系/输水管道.glb', '这是一个自动导入的模型：输水管道.glb');
INSERT INTO `models` VALUES (200000180, '水务水利/水坝工程', '拱坝.glb', '/models/水务水利/水坝工程/拱坝.glb', '这是一个自动导入的模型：拱坝.glb');
INSERT INTO `models` VALUES (200000181, '水务水利/水坝工程', '土石坝.glb', '/models/水务水利/水坝工程/土石坝.glb', '这是一个自动导入的模型：土石坝.glb');
INSERT INTO `models` VALUES (200000182, '水务水利/监测设备', '操控室.glb', '/models/水务水利/监测设备/操控室.glb', '这是一个自动导入的模型：操控室.glb');
INSERT INTO `models` VALUES (200000183, '水务水利/雨洪利用', '泄洪洞.glb', '/models/水务水利/雨洪利用/泄洪洞.glb', '这是一个自动导入的模型：泄洪洞.glb');
INSERT INTO `models` VALUES (200000184, '水务水利/环境要素', '交通设施.glb', '/models/水务水利/环境要素/交通设施.glb', '这是一个自动导入的模型：交通设施.glb');
INSERT INTO `models` VALUES (200000185, '水务水利/环境要素', '河道建筑物.glb', '/models/水务水利/环境要素/河道建筑物.glb', '这是一个自动导入的模型：河道建筑物.glb');
INSERT INTO `models` VALUES (200000186, '水务水利/环境要素', '通信设备.glb', '/models/水务水利/环境要素/通信设备.glb', '这是一个自动导入的模型：通信设备.glb');
INSERT INTO `models` VALUES (200000187, '水务水利/雨洪利用', '排涝泵站.glb', '/models/水务水利/雨洪利用/排涝泵站.glb', '这是一个自动导入的模型：排涝泵站.glb');
INSERT INTO `models` VALUES (200000188, '水务水利/雨洪利用', '倒虹吸.glb', '/models/水务水利/雨洪利用/倒虹吸.glb', '这是一个自动导入的模型：倒虹吸.glb');
INSERT INTO `models` VALUES (200000189, '水务水利/雨洪利用', '冲锋舟.glb', '/models/水务水利/雨洪利用/冲锋舟.glb', '这是一个自动导入的模型：冲锋舟.glb');
INSERT INTO `models` VALUES (200000190, '水务水利/灌溉系统', '滴灌带.glb', '/models/水务水利/灌溉系统/滴灌带.glb', '这是一个自动导入的模型：滴灌带.glb');
INSERT INTO `models` VALUES (200000191, '水务水利/灌溉系统', '滴灌管.glb', '/models/水务水利/灌溉系统/滴灌管.glb', '这是一个自动导入的模型：滴灌管.glb');
INSERT INTO `models` VALUES (200000192, '水务水利/灌溉系统', '喷灌喷头.glb', '/models/水务水利/灌溉系统/喷灌喷头.glb', '这是一个自动导入的模型：喷灌喷头.glb');
INSERT INTO `models` VALUES (200000193, '水务水利/灌溉系统', '施肥罐.glb', '/models/水务水利/灌溉系统/施肥罐.glb', '这是一个自动导入的模型：施肥罐.glb');
INSERT INTO `models` VALUES (200000194, '水务水利/灌溉系统', '田间渠道.glb', '/models/水务水利/灌溉系统/田间渠道.glb', '这是一个自动导入的模型：田间渠道.glb');
INSERT INTO `models` VALUES (200000195, '水务水利/环境要素', '雨棚.glb', '/models/水务水利/环境要素/雨棚.glb', '这是一个自动导入的模型：雨棚.glb');
INSERT INTO `models` VALUES (200000196, '水务水利/海洋工程', '波浪测量仪.glb', '/models/水务水利/海洋工程/波浪测量仪.glb', '这是一个自动导入的模型：波浪测量仪.glb');
INSERT INTO `models` VALUES (200000197, '水务水利/海洋工程', '潮位站.glb', '/models/水务水利/海洋工程/潮位站.glb', '这是一个自动导入的模型：潮位站.glb');
INSERT INTO `models` VALUES (200000198, '水务水利/海洋工程', '防波堤.glb', '/models/水务水利/海洋工程/防波堤.glb', '这是一个自动导入的模型：防波堤.glb');
INSERT INTO `models` VALUES (200000199, '水务水利/海洋工程', '海底管道.glb', '/models/水务水利/海洋工程/海底管道.glb', '这是一个自动导入的模型：海底管道.glb');
INSERT INTO `models` VALUES (200000200, '水务水利/海洋工程', '护岸.glb', '/models/水务水利/海洋工程/护岸.glb', '这是一个自动导入的模型：护岸.glb');
INSERT INTO `models` VALUES (200000201, '水务水利/海洋工程', '系船设施.glb', '/models/水务水利/海洋工程/系船设施.glb', '这是一个自动导入的模型：系船设施.glb');
INSERT INTO `models` VALUES (200000202, '水务水利/海洋工程', '海上风电基础.glb', '/models/水务水利/海洋工程/海上风电基础.glb', '这是一个自动导入的模型：海上风电基础.glb');
INSERT INTO `models` VALUES (200000203, '水务水利/海洋工程', '高桩码头.glb', '/models/水务水利/海洋工程/高桩码头.glb', '这是一个自动导入的模型：高桩码头.glb');
INSERT INTO `models` VALUES (200000204, '水务水利/海洋工程', '海上风电叶片.glb', '/models/水务水利/海洋工程/海上风电叶片.glb', '这是一个自动导入的模型：海上风电叶片.glb');
INSERT INTO `models` VALUES (200000205, '水务水利/海洋工程', '逃生艇.glb', '/models/水务水利/海洋工程/逃生艇.glb', '这是一个自动导入的模型：逃生艇.glb');
INSERT INTO `models` VALUES (200000206, '水务水利/海洋工程', '海底地震仪.glb', '/models/水务水利/海洋工程/海底地震仪.glb', '这是一个自动导入的模型：海底地震仪.glb');
INSERT INTO `models` VALUES (200000207, '水务水利/海洋工程', '重力式码头.glb', '/models/水务水利/海洋工程/重力式码头.glb', '这是一个自动导入的模型：重力式码头.glb');
INSERT INTO `models` VALUES (200000208, '水务水利/海洋工程', '栈桥.glb', '/models/水务水利/海洋工程/栈桥.glb', '这是一个自动导入的模型：栈桥.glb');
INSERT INTO `models` VALUES (200000209, '水务水利/海洋工程', '钻井平台.glb', '/models/水务水利/海洋工程/钻井平台.glb', '这是一个自动导入的模型：钻井平台.glb');
INSERT INTO `models` VALUES (200000210, '水务水利/海洋工程', '潮汐能发电机组.glb', '/models/水务水利/海洋工程/潮汐能发电机组.glb', '这是一个自动导入的模型：潮汐能发电机组.glb');
INSERT INTO `models` VALUES (200000211, '水务水利/海洋工程', '潜水装备.glb', '/models/水务水利/海洋工程/潜水装备.glb', '这是一个自动导入的模型：潜水装备.glb');
INSERT INTO `models` VALUES (200000212, '水务水利/海洋工程', '板桩码头.glb', '/models/水务水利/海洋工程/板桩码头.glb', '这是一个自动导入的模型：板桩码头.glb');
INSERT INTO `models` VALUES (200000213, '水务水利/海洋工程', '海床基.glb', '/models/水务水利/海洋工程/海床基.glb', '这是一个自动导入的模型：海床基.glb');
INSERT INTO `models` VALUES (200000214, '水务水利/海洋工程', '锚链.glb', '/models/水务水利/海洋工程/锚链.glb', '这是一个自动导入的模型：锚链.glb');
INSERT INTO `models` VALUES (200000215, '水务水利/海洋工程', '波浪能发电装置.glb', '/models/水务水利/海洋工程/波浪能发电装置.glb', '这是一个自动导入的模型：波浪能发电装置.glb');

SET FOREIGN_KEY_CHECKS = 1;
