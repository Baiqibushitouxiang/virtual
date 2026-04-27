# 当前系统数据库说明

## 主数据库

- 数据库名：`dam`
- 初始化脚本：`dam_init.sql`

## 当前保留策略

- 项目 SQL 已收敛为一份初始化脚本：`dam_init.sql`
- 原先分散的 `dam.sql`、增量表脚本、汇总脚本已清理
- `target/classes/sql` 下的生成 SQL 也已一并清理

## 当前系统实际使用的表

- `users`
- `userdata`
- `models`
- `scene_assets`
- `composite_models`
- `composite_model_components`
- `devices`
- `device_data`
- `data_panel`
- `model_data_binding`
- `alert_rule`
- `alert_notification_log`

## 已纳入初始化脚本的清理项

以下旧表或未启用表会在执行 `dam_init.sql` 时先被清理：

- `categories`
- `subcategories`
- `user_scenes`
- `device_models`
- `device_model_points`
- `device_instances`
- `device_channels`
- `device_point_mappings`
- `point_latest_values`
- `point_history_values`
- `scene_point_bindings`

## 结构对齐说明

- `data_panel.model_id` 已统一为 `VARCHAR(128)`
- 这样与当前 Java 实体 `DataPanel.modelId` 的 `String` 类型保持一致
