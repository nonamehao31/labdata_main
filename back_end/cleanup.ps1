# Delete unneeded model classes
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
        Remove-Item $path
        Write-Host "Deleted model class: $file"
    } else {
        Write-Host "File does not exist: $path"
    }
}

# Delete unneeded repository interfaces
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
        Remove-Item $path
        Write-Host "Deleted repository interface: $file"
    } else {
        Write-Host "File does not exist: $path"
    }
}

# Delete unneeded controller classes
$controllerFilesToDelete = @(
    "ExperimentTaskController.java",
    "ExperimentTypeController.java",
    "MaterialController.java",
    "MixRatioController.java",
    "MixingMethodController.java",
    "ProjectController.java",
    "SpecimenController.java",
    "SyncController.java"
)

foreach ($file in $controllerFilesToDelete) {
    $path = "src\main\java\com\example\labdata\controller\$file"
    if (Test-Path $path) {
        Remove-Item $path
        Write-Host "Deleted controller class: $file"
    } else {
        Write-Host "File does not exist: $path"
    }
}

# Delete all service classes
$serviceFilesToDelete = @(
    "ExperimentDataService.java",
    "ExperimentTaskService.java",
    "ExperimentTypeService.java",
    "MaterialService.java",
    "MixRatioService.java",
    "MixingMethodService.java",
    "ProjectService.java",
    "SpecimenService.java",
    "SyncService.java"
)

foreach ($file in $serviceFilesToDelete) {
    $path = "src\main\java\com\example\labdata\service\$file"
    if (Test-Path $path) {
        Remove-Item $path
        Write-Host "Deleted service class: $file"
    } else {
        Write-Host "File does not exist: $path"
    }
}

Write-Host "Cleanup operation completed!"
