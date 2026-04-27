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
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `models`;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `users` (
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
    CONSTRAINT `fk_userdata_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
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
