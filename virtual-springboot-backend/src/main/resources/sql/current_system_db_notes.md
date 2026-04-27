# 当前系统数据库初始化说明

## 当前结论

现在 `sql` 目录下的两个文件：

- `dam.sql`
- `dam_init.sql`

都已经整理为同一份“可直接部署到服务器的初始化脚本”。

也就是说：

- 两者表结构一致
- 两者保留的数据一致
- 都不再包含原来那份转储里的运行期脏数据

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

## 保留了什么

### 1. 保留全部当前运行所需表结构

初始化脚本保留了上面 12 张表的建表语句。

### 2. 保留默认管理员

- 用户名：`admin`
- 密码：`123456`
- 用户 ID：`1`

同时保留一条对应的 `userdata` 记录。

这样做是因为当前代码里有多处默认回落到 `userId = 1`，
服务器首次部署后也需要能直接登录后台。

### 3. 保留完整模型库数据

脚本保留了从当前库中整理出的全部 `models` 数据，共 374 条。

这是必须保留的，因为模型库就是系统基础资源，
而且你已经明确说明模型文件必须保留。

## 清理了什么

以下表只保留结构，不保留原来的数据记录：

- `scene_assets`
- `composite_models`
- `composite_model_components`
- `devices`
- `device_data`
- `data_panel`
- `model_data_binding`
- `alert_rule`
- `alert_notification_log`

清理原因：

- 这些基本都属于运行期数据、测试数据、日志数据或用户操作痕迹
- 直接带到服务器会引入历史脏数据
- 有些记录依赖本地场景文件、设备证书、历史绑定关系，不适合作为通用初始化数据

## 还会额外清理的旧表

执行初始化脚本时，还会先清理以下旧表或未启用表：

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

## 部署注意事项

- 数据库初始化执行 `dam.sql` 或 `dam_init.sql` 都可以
- 模型文件本体也必须同步部署到服务器
- 当前 `models.file_path` 使用的是 `/models/...` 路径
- 服务器必须保证这些模型文件能通过对应资源路径访问
