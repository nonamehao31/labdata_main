# PowerShell脚本：将所有Java文件中的com.example.labdata包名更新为com.example.labdata_main

$appDir = "f:\git_labdata\0209labdata\labdata_main\app"
$javaFiles = Get-ChildItem -Path $appDir -Filter "*.java" -Recurse

$count = 0

foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    
    # 检查文件是否包含旧包名
    if ($content -match "import\s+com\.example\.labdata\.") {
        Write-Host "Processing: $($file.FullName)" -ForegroundColor Yellow
        
        # 替换包名
        $newContent = $content -replace "import\s+com\.example\.labdata\.", "import com.example.labdata_main."
        
        # 保存修改后的文件
        Set-Content -Path $file.FullName -Value $newContent
        $count++
    }
}

Write-Host "完成! 更新了 $count 个文件中的import语句." -ForegroundColor Green
