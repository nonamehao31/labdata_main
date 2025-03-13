# 简化清理脚本
# 作者: Administrator
# 日期: 2025-03-11

Write-Host "===== 开始执行最终清理 =====" -ForegroundColor Green

# 1. 清理模型类
Write-Host "清理模型类..." -ForegroundColor Cyan
$modelPath = "src\main\java\com\example\labdata\model"
if (Test-Path "$modelPath\ExperimentData.java") { Remove-Item "$modelPath\ExperimentData.java" -Force }
if (Test-Path "$modelPath\ExperimentTask.java") { Remove-Item "$modelPath\ExperimentTask.java" -Force }
if (Test-Path "$modelPath\ExperimentType.java") { Remove-Item "$modelPath\ExperimentType.java" -Force }
if (Test-Path "$modelPath\Material.java") { Remove-Item "$modelPath\Material.java" -Force }
if (Test-Path "$modelPath\MaterialProperty.java") { Remove-Item "$modelPath\MaterialProperty.java" -Force }
if (Test-Path "$modelPath\MixRatio.java") { Remove-Item "$modelPath\MixRatio.java" -Force }
if (Test-Path "$modelPath\MixingMethod.java") { Remove-Item "$modelPath\MixingMethod.java" -Force }
if (Test-Path "$modelPath\Project.java") { Remove-Item "$modelPath\Project.java" -Force }
if (Test-Path "$modelPath\Specimen.java") { Remove-Item "$modelPath\Specimen.java" -Force }

# 2. 清理仓库接口
Write-Host "清理仓库接口..." -ForegroundColor Cyan
$repoPath = "src\main\java\com\example\labdata\repository"
if (Test-Path "$repoPath\ExperimentDataRepository.java") { Remove-Item "$repoPath\ExperimentDataRepository.java" -Force }
if (Test-Path "$repoPath\ExperimentTaskRepository.java") { Remove-Item "$repoPath\ExperimentTaskRepository.java" -Force }
if (Test-Path "$repoPath\ExperimentTypeRepository.java") { Remove-Item "$repoPath\ExperimentTypeRepository.java" -Force }
if (Test-Path "$repoPath\MaterialRepository.java") { Remove-Item "$repoPath\MaterialRepository.java" -Force }
if (Test-Path "$repoPath\MixRatioRepository.java") { Remove-Item "$repoPath\MixRatioRepository.java" -Force }
if (Test-Path "$repoPath\MixingMethodRepository.java") { Remove-Item "$repoPath\MixingMethodRepository.java" -Force }
if (Test-Path "$repoPath\ProjectRepository.java") { Remove-Item "$repoPath\ProjectRepository.java" -Force }
if (Test-Path "$repoPath\SpecimenRepository.java") { Remove-Item "$repoPath\SpecimenRepository.java" -Force }

# 3. 清理控制器
Write-Host "清理控制器..." -ForegroundColor Cyan
$controllerPath = "src\main\java\com\example\labdata\controller"
if (Test-Path "$controllerPath\ExperimentTaskController.java") { Remove-Item "$controllerPath\ExperimentTaskController.java" -Force }
if (Test-Path "$controllerPath\ExperimentTypeController.java") { Remove-Item "$controllerPath\ExperimentTypeController.java" -Force }
if (Test-Path "$controllerPath\MaterialController.java") { Remove-Item "$controllerPath\MaterialController.java" -Force }
if (Test-Path "$controllerPath\MixRatioController.java") { Remove-Item "$controllerPath\MixRatioController.java" -Force }
if (Test-Path "$controllerPath\MixingMethodController.java") { Remove-Item "$controllerPath\MixingMethodController.java" -Force }
if (Test-Path "$controllerPath\ProjectController.java") { Remove-Item "$controllerPath\ProjectController.java" -Force }
if (Test-Path "$controllerPath\SpecimenController.java") { Remove-Item "$controllerPath\SpecimenController.java" -Force }
if (Test-Path "$controllerPath\SyncController.java") { Remove-Item "$controllerPath\SyncController.java" -Force }
if (Test-Path "$controllerPath\WebSocketController.java") { Remove-Item "$controllerPath\WebSocketController.java" -Force }

# 4. 清理服务类
Write-Host "清理服务类..." -ForegroundColor Cyan
$servicePath = "src\main\java\com\example\labdata\service"
if (Test-Path "$servicePath\ExperimentDataService.java") { Remove-Item "$servicePath\ExperimentDataService.java" -Force }
if (Test-Path "$servicePath\ExperimentTaskService.java") { Remove-Item "$servicePath\ExperimentTaskService.java" -Force }
if (Test-Path "$servicePath\ExperimentTypeService.java") { Remove-Item "$servicePath\ExperimentTypeService.java" -Force }
if (Test-Path "$servicePath\MaterialService.java") { Remove-Item "$servicePath\MaterialService.java" -Force }
if (Test-Path "$servicePath\MixRatioService.java") { Remove-Item "$servicePath\MixRatioService.java" -Force }
if (Test-Path "$servicePath\MixingMethodService.java") { Remove-Item "$servicePath\MixingMethodService.java" -Force }
if (Test-Path "$servicePath\NotificationService.java") { Remove-Item "$servicePath\NotificationService.java" -Force }
if (Test-Path "$servicePath\ProjectService.java") { Remove-Item "$servicePath\ProjectService.java" -Force }
if (Test-Path "$servicePath\SpecimenService.java") { Remove-Item "$servicePath\SpecimenService.java" -Force }
if (Test-Path "$servicePath\SyncService.java") { Remove-Item "$servicePath\SyncService.java" -Force }

# 5. 清理配置类
Write-Host "清理配置类..." -ForegroundColor Cyan
$configPath = "src\main\java\com\example\labdata\config"
if (Test-Path "$configPath\WebSocketConfig.java") { Remove-Item "$configPath\WebSocketConfig.java" -Force }

# 6. 清理payload类
Write-Host "清理payload类..." -ForegroundColor Cyan
$payloadPath = "src\main\java\com\example\labdata\payload"
if (Test-Path "$payloadPath\EntityChangeMessage.java") { Remove-Item "$payloadPath\EntityChangeMessage.java" -Force }
if (Test-Path "$payloadPath\SyncRequest.java") { Remove-Item "$payloadPath\SyncRequest.java" -Force }
if (Test-Path "$payloadPath\WebSocketMessage.java") { Remove-Item "$payloadPath\WebSocketMessage.java" -Force }

Write-Host "===== 文件清理完成 =====" -ForegroundColor Green
Write-Host "接下来需要执行以下步骤:" -ForegroundColor Green
Write-Host "1. 使用数据库管理工具执行 src\main\resources\db\cleanup.sql 脚本来清理数据库" -ForegroundColor Yellow
Write-Host "2. 重新编译项目并测试保留的功能" -ForegroundColor Yellow
Write-Host "3. 更新API文档" -ForegroundColor Yellow

Write-Host "注意: 在执行数据库清理脚本前，请确保已备份数据库!" -ForegroundColor Red
