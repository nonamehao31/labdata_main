# 数据库清理和后端精简指南

## 概述

本文档指导如何完成后端数据库的清理工作，只保留`users`、`supported_devices`和`devices`表及其相关逻辑，删除其他所有表及其逻辑。

## 清理内容

以下组件已被删除：

1. **模型类**:
   - ExperimentData.java
   - ExperimentTask.java
   - ExperimentType.java
   - Material.java
   - MaterialProperty.java
   - MixRatio.java
   - MixingMethod.java
   - Project.java
   - Specimen.java

2. **仓库接口**:
   - ExperimentDataRepository.java
   - ExperimentTaskRepository.java
   - ExperimentTypeRepository.java
   - MaterialRepository.java
   - MixRatioRepository.java
   - MixingMethodRepository.java
   - ProjectRepository.java
   - SpecimenRepository.java

3. **控制器**:
   - ExperimentTaskController.java
   - ExperimentTypeController.java
   - MaterialController.java
   - MixRatioController.java
   - MixingMethodController.java
   - ProjectController.java
   - SpecimenController.java
   - SyncController.java
   - WebSocketController.java

4. **服务类**:
   - ExperimentDataService.java
   - ExperimentTaskService.java
   - ExperimentTypeService.java
   - MaterialService.java
   - MixRatioService.java
   - MixingMethodService.java
   - NotificationService.java
   - ProjectService.java
   - SpecimenService.java
   - SyncService.java

5. **配置和其他**:
   - WebSocketConfig.java
   - EntityChangeMessage.java
   - SyncRequest.java
   - WebSocketMessage.java

## 保留内容

以下组件被保留：

1. **模型类**:
   - User.java
   - SupportedDevice.java
   - Device.java
   - 审计类 (DateAudit.java, UserDateAudit.java)

2. **仓库接口**:
   - UserRepository.java
   - SupportedDeviceRepository.java
   - DeviceRepository.java

3. **控制器**:
   - AuthController.java
   - DeviceController.java
   - SupportedDeviceController.java
   - DebugController.java

4. **安全相关**:
   - 所有安全相关类和配置

## 执行清理步骤

### 1. 备份数据库

在执行任何清理操作前，**务必**进行完整的数据库备份：

```sql
-- 使用PostgreSQL的备份功能
pg_dump -U postgres -d labdata > labdata_backup_before_cleanup.sql
```

### 2. 文件清理

运行提供的PowerShell脚本以删除不需要的Java文件：

```powershell
# 在后端项目根目录执行
.\final_cleanup.ps1
```

### 3. 数据库清理

使用数据库管理工具（如pgAdmin或DBeaver）执行以下脚本，清理数据库：

```sql
-- 执行清理脚本
-- 该脚本会先备份要删除的表，然后删除它们
-- 脚本位置：src\main\resources\db\cleanup.sql
```

### 4. 重新构建和测试

清理完成后，重新构建项目并测试保留的功能：

```bash
# 重新构建项目
./mvnw clean package

# 启动应用程序
./mvnw spring-boot:run
```

### 5. 验证和测试

清理完成后，验证以下功能是否正常工作：

- 用户登录和注册
- 设备管理（添加、修改、查询）
- 支持设备管理

## 注意事项

1. 该清理操作是**不可逆**的。如果需要恢复已删除的功能，必须从备份中恢复。

2. 清理后，前端应用可能会遇到API不兼容问题，需要相应地更新前端代码。

3. 如果在清理过程中遇到任何问题，请使用备份恢复系统到之前的状态。

4. 建议先在测试环境中执行清理流程，验证无误后再在生产环境执行。

## 联系与支持

如有任何问题或需要支持，请联系系统管理员。
