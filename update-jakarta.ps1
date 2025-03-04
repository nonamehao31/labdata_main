# PowerShell script to replace javax packages with jakarta packages in all Java files
$baseDir = "f:\git_labdata\0209labdata\labdata_main\back_end\src\main\java"
$javaFiles = Get-ChildItem -Path $baseDir -Filter "*.java" -Recurse

# Counter for modified files
$modifiedFiles = 0

foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    $originalContent = $content
    
    # Replace javax.persistence with jakarta.persistence
    $content = $content -replace "javax\.persistence", "jakarta.persistence"
    
    # Replace javax.validation with jakarta.validation
    $content = $content -replace "javax\.validation", "jakarta.validation"
    
    # Replace javax.servlet with jakarta.servlet
    $content = $content -replace "javax\.servlet", "jakarta.servlet"
    
    # If content was modified, save the file
    if ($content -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $content
        Write-Host "Updated: $($file.FullName)"
        $modifiedFiles++
    }
}

Write-Host "Complete. Modified $modifiedFiles files."
