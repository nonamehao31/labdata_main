# 最终清理脚本 - 仅保留users, supported_devices, devices表及其逻辑
# 作者: Administrator
# 日期: 2025-03-11

Write-Host "===== 开始执行最终清理 =====" -ForegroundColor Green

# 定义项目根目录
$PROJECT_ROOT = Get-Location

# 1. 清理模型类
Write-Host "`n[1/5] 清理模型类..." -ForegroundColor Cyan
$modelFilesToDelete = @(
    "ExperimentData.java",
    "ExperimentTask.java",
    "ExperimentType.java",
    "Material.java",
    "MaterialProperty.java",
    "MixRatio.java",
    "MixingMethod.java",
    "Project.java",
    "Specimen.java"
)

foreach ($file in $modelFilesToDelete) {
    $path = "src\main\java\com\example\labdata\model\$file"
    if (Test-Path $path) {
        Remove-Item $path -Force
        Write-Host "  已删除: $file" -ForegroundColor Yellow
    }
}

# 2. 清理仓库接口
Write-Host "`n[2/5] 清理仓库接口..." -ForegroundColor Cyan
$repoFilesToDelete = @(
    "ExperimentDataRepository.java",
    "ExperimentTaskRepository.java",
    "ExperimentTypeRepository.java",
    "MaterialRepository.java",
    "MixRatioRepository.java",
    "MixingMethodRepository.java",
    "ProjectRepository.java",
    "SpecimenRepository.java"
)

foreach ($file in $repoFilesToDelete) {
    $path = "src\main\java\com\example\labdata\repository\$file"
    if (Test-Path $path) {
        Remove-Item $path -Force
        Write-Host "  已删除: $file" -ForegroundColor Yellow
    }
}

# 3. 清理控制器
Write-Host "`n[3/5] 清理控制器..." -ForegroundColor Cyan
$controllerFilesToDelete = @(
    "ExperimentTaskController.java",
    "ExperimentTypeController.java",
    "MaterialController.java",
    "MixRatioController.java",
    "MixingMethodController.java",
    "ProjectController.java",
    "SpecimenController.java",
    "SyncController.java",
    "WebSocketController.java"
)

foreach ($file in $controllerFilesToDelete) {
    $path = "src\main\java\com\example\labdata\controller\$file"
    if (Test-Path $path) {
        Remove-Item $path -Force
        Write-Host "  已删除: $file" -ForegroundColor Yellow
    }
}

# 4. 清理服务类
Write-Host "`n[4/5] 清理服务类..." -ForegroundColor Cyan
$serviceFilesToDelete = @(
    "ExperimentDataService.java",
    "ExperimentTaskService.java",
    "ExperimentTypeService.java",
    "MaterialService.java",
    "MixRatioService.java",
    "MixingMethodService.java",
    "NotificationService.java",
    "ProjectService.java",
    "SpecimenService.java",
    "SyncService.java"
)

foreach ($file in $serviceFilesToDelete) {
    $path = "src\main\java\com\example\labdata\service\$file"
    if (Test-Path $path) {
        Remove-Item $path -Force
        Write-Host "  已删除: $file" -ForegroundColor Yellow
    }
}

# 5. 清理配置类和其他组件
Write-Host "`n[5/5] 清理配置类和其他组件..." -ForegroundColor Cyan
$configFilesToDelete = @(
    "WebSocketConfig.java"
)

foreach ($file in $configFilesToDelete) {
    $path = "src\main\java\com\example\labdata\config\$file"
    if (Test-Path $path) {
        Remove-Item $path -Force
        Write-Host "  已删除: $file" -ForegroundColor Yellow
    }
}

# 6. 清理payload类
$payloadFilesToDelete = @(
    "EntityChangeMessage.java",
    "SyncRequest.java",
    "WebSocketMessage.java"
)

foreach ($file in $payloadFilesToDelete) {
    $path = "src\main\java\com\example\labdata\payload\$file"
    if (Test-Path $path) {
        Remove-Item $path -Force
        Write-Host "  已删除: $file" -ForegroundColor Yellow
    }
}

Write-Host "`n===== 文件清理完成 =====" -ForegroundColor Green
Write-Host "接下来需要执行以下步骤:" -ForegroundColor Green
Write-Host "1. 使用数据库管理工具执行 src\main\resources\db\cleanup.sql 脚本来清理数据库" -ForegroundColor Yellow
Write-Host "2. 重新编译项目并测试保留的功能" -ForegroundColor Yellow
Write-Host "3. 更新API文档" -ForegroundColor Yellow

Write-Host "`n注意: 在执行数据库清理脚本前，请确保已备份数据库!" -ForegroundColor Red
