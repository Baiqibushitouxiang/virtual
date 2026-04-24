CREATE TABLE IF NOT EXISTS `alert_rule` (
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
    KEY `idx_alert_rule_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则表';
