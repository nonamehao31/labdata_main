@echo off
echo ===== 开始执行最终清理 =====

set MODEL_PATH=src\main\java\com\example\labdata\model
set REPO_PATH=src\main\java\com\example\labdata\repository
set CONTROLLER_PATH=src\main\java\com\example\labdata\controller
set SERVICE_PATH=src\main\java\com\example\labdata\service
set CONFIG_PATH=src\main\java\com\example\labdata\config
set PAYLOAD_PATH=src\main\java\com\example\labdata\payload

echo 清理模型类...
if exist %MODEL_PATH%\ExperimentData.java del /F %MODEL_PATH%\ExperimentData.java
if exist %MODEL_PATH%\ExperimentTask.java del /F %MODEL_PATH%\ExperimentTask.java
if exist %MODEL_PATH%\ExperimentType.java del /F %MODEL_PATH%\ExperimentType.java
if exist %MODEL_PATH%\Material.java del /F %MODEL_PATH%\Material.java
if exist %MODEL_PATH%\MaterialProperty.java del /F %MODEL_PATH%\MaterialProperty.java
if exist %MODEL_PATH%\MixRatio.java del /F %MODEL_PATH%\MixRatio.java
if exist %MODEL_PATH%\MixingMethod.java del /F %MODEL_PATH%\MixingMethod.java
if exist %MODEL_PATH%\Project.java del /F %MODEL_PATH%\Project.java
if exist %MODEL_PATH%\Specimen.java del /F %MODEL_PATH%\Specimen.java

echo 清理仓库接口...
if exist %REPO_PATH%\ExperimentDataRepository.java del /F %REPO_PATH%\ExperimentDataRepository.java
if exist %REPO_PATH%\ExperimentTaskRepository.java del /F %REPO_PATH%\ExperimentTaskRepository.java
if exist %REPO_PATH%\ExperimentTypeRepository.java del /F %REPO_PATH%\ExperimentTypeRepository.java
if exist %REPO_PATH%\MaterialRepository.java del /F %REPO_PATH%\MaterialRepository.java
if exist %REPO_PATH%\MixRatioRepository.java del /F %REPO_PATH%\MixRatioRepository.java
if exist %REPO_PATH%\MixingMethodRepository.java del /F %REPO_PATH%\MixingMethodRepository.java
if exist %REPO_PATH%\ProjectRepository.java del /F %REPO_PATH%\ProjectRepository.java
if exist %REPO_PATH%\SpecimenRepository.java del /F %REPO_PATH%\SpecimenRepository.java

echo 清理控制器...
if exist %CONTROLLER_PATH%\ExperimentTaskController.java del /F %CONTROLLER_PATH%\ExperimentTaskController.java
if exist %CONTROLLER_PATH%\ExperimentTypeController.java del /F %CONTROLLER_PATH%\ExperimentTypeController.java
if exist %CONTROLLER_PATH%\MaterialController.java del /F %CONTROLLER_PATH%\MaterialController.java
if exist %CONTROLLER_PATH%\MixRatioController.java del /F %CONTROLLER_PATH%\MixRatioController.java
if exist %CONTROLLER_PATH%\MixingMethodController.java del /F %CONTROLLER_PATH%\MixingMethodController.java
if exist %CONTROLLER_PATH%\ProjectController.java del /F %CONTROLLER_PATH%\ProjectController.java
if exist %CONTROLLER_PATH%\SpecimenController.java del /F %CONTROLLER_PATH%\SpecimenController.java
if exist %CONTROLLER_PATH%\SyncController.java del /F %CONTROLLER_PATH%\SyncController.java
if exist %CONTROLLER_PATH%\WebSocketController.java del /F %CONTROLLER_PATH%\WebSocketController.java

echo 清理服务类...
if exist %SERVICE_PATH%\ExperimentDataService.java del /F %SERVICE_PATH%\ExperimentDataService.java
if exist %SERVICE_PATH%\ExperimentTaskService.java del /F %SERVICE_PATH%\ExperimentTaskService.java
if exist %SERVICE_PATH%\ExperimentTypeService.java del /F %SERVICE_PATH%\ExperimentTypeService.java
if exist %SERVICE_PATH%\MaterialService.java del /F %SERVICE_PATH%\MaterialService.java
if exist %SERVICE_PATH%\MixRatioService.java del /F %SERVICE_PATH%\MixRatioService.java
if exist %SERVICE_PATH%\MixingMethodService.java del /F %SERVICE_PATH%\MixingMethodService.java
if exist %SERVICE_PATH%\NotificationService.java del /F %SERVICE_PATH%\NotificationService.java
if exist %SERVICE_PATH%\ProjectService.java del /F %SERVICE_PATH%\ProjectService.java
if exist %SERVICE_PATH%\SpecimenService.java del /F %SERVICE_PATH%\SpecimenService.java
if exist %SERVICE_PATH%\SyncService.java del /F %SERVICE_PATH%\SyncService.java

echo 清理配置类...
if exist %CONFIG_PATH%\WebSocketConfig.java del /F %CONFIG_PATH%\WebSocketConfig.java

echo 清理payload类...
if exist %PAYLOAD_PATH%\EntityChangeMessage.java del /F %PAYLOAD_PATH%\EntityChangeMessage.java
if exist %PAYLOAD_PATH%\SyncRequest.java del /F %PAYLOAD_PATH%\SyncRequest.java
if exist %PAYLOAD_PATH%\WebSocketMessage.java del /F %PAYLOAD_PATH%\WebSocketMessage.java

echo ===== 文件清理完成 =====
echo 接下来需要执行以下步骤:
echo 1. 使用数据库管理工具执行 src\main\resources\db\cleanup.sql 脚本来清理数据库
echo 2. 重新编译项目并测试保留的功能
echo 3. 更新API文档

echo 注意: 在执行数据库清理脚本前，请确保已备份数据库!

pause
