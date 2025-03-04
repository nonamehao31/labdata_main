# PowerShell脚本用于测试PostgreSQL连接

Write-Host "======= PostgreSQL 连接测试 =======" -ForegroundColor Cyan

# 检查端口是否处于监听状态
Write-Host "正在检查端口5432是否处于监听状态..." -ForegroundColor Yellow
$tcpConnections = netstat -an | Select-String "5432"
Write-Host $tcpConnections
if ($tcpConnections -match "LISTENING") {
    Write-Host "端口5432处于监听状态" -ForegroundColor Green
} else {
    Write-Host "警告: 端口5432不在监听状态，PostgreSQL可能未启动" -ForegroundColor Red
}

# 检查防火墙状态
Write-Host "正在检查Windows防火墙状态..." -ForegroundColor Yellow
$firewallStatus = Get-NetFirewallProfile | Select-Object Name,Enabled
Write-Host $firewallStatus

# 尝试ping本地主机
Write-Host "正在ping本地主机..." -ForegroundColor Yellow
ping localhost
ping 127.0.0.1

# 检查PostgreSQL服务状态
Write-Host "正在检查PostgreSQL服务状态..." -ForegroundColor Yellow
$pgService = Get-Service -Name "*postgresql*" -ErrorAction SilentlyContinue
if ($pgService) {
    Write-Host "PostgreSQL服务名称: $($pgService.Name)" -ForegroundColor Cyan
    Write-Host "服务状态: $($pgService.Status)" -ForegroundColor Cyan
    
    if ($pgService.Status -ne "Running") {
        Write-Host "尝试启动PostgreSQL服务..." -ForegroundColor Yellow
        Start-Service $pgService.Name
        Start-Sleep -Seconds 2
        $pgService = Get-Service -Name $pgService.Name
        Write-Host "服务状态现在是: $($pgService.Status)" -ForegroundColor Cyan
    }
} else {
    Write-Host "未找到PostgreSQL服务，请确认PostgreSQL已正确安装" -ForegroundColor Red
}

Write-Host "======= 测试结束 =======" -ForegroundColor Cyan
