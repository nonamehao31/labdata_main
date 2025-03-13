# Delete unneeded payload (DTO) classes
$payloadFilesToDelete = @(
    "EntityChangeMessage.java",
    "SyncRequest.java",
    "WebSocketMessage.java"
)

foreach ($file in $payloadFilesToDelete) {
    $path = "src\main\java\com\example\labdata\payload\$file"
    if (Test-Path $path) {
        Remove-Item $path
        Write-Host "Deleted payload class: $file"
    } else {
        Write-Host "File does not exist: $path"
    }
}

Write-Host "Payload cleanup completed!"
