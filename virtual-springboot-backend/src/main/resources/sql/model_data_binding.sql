CREATE TABLE IF NOT EXISTS `model_data_binding` (
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
    UNIQUE KEY `uk_scene_model` (`scene_id`, `model_id`),
    KEY `idx_binding_user_id` (`user_id`),
    KEY `idx_binding_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型数据绑定表';
