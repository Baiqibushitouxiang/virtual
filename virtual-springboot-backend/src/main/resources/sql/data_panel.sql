-- 数据展板表
CREATE TABLE IF NOT EXISTS `data_panel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '展板名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '展板描述',
    `device_id` BIGINT DEFAULT NULL COMMENT '绑定的设备ID',
    `device_name` VARCHAR(100) DEFAULT NULL COMMENT '设备名称',
    `model_id` BIGINT DEFAULT NULL COMMENT '绑定的模型ID',
    `model_name` VARCHAR(100) DEFAULT NULL COMMENT '模型名称',
    `model_type` VARCHAR(50) DEFAULT NULL COMMENT '模型类型',
    `position` VARCHAR(200) DEFAULT NULL COMMENT '展板位置JSON {top, left}',
    `size` VARCHAR(200) DEFAULT NULL COMMENT '展板尺寸JSON {width, height}',
    `style` VARCHAR(1000) DEFAULT NULL COMMENT '展板样式JSON',
    `status` INT DEFAULT 1 COMMENT '状态：1启用，0禁用',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_model_id` (`model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据展板配置表';
