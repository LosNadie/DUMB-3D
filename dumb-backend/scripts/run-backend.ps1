# 从 dumb-backend/.env.local 加载环境变量并启动 Spring Boot
$ErrorActionPreference = "Stop"
$backendRoot = Split-Path -Parent $PSScriptRoot
Set-Location $backendRoot

$envFile = Join-Path $backendRoot ".env.local"
if (-not (Test-Path $envFile)) {
    Write-Host "未找到 $envFile" -ForegroundColor Yellow
    Write-Host "请复制 .env.local.example 为 .env.local 并填写 AI_API_KEY、LASTFM_API_KEY" -ForegroundColor Yellow
    exit 1
}

Get-Content -LiteralPath $envFile -Encoding UTF8 | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq "" -or $line.StartsWith("#")) {
        return
    }
    $eq = $line.IndexOf("=")
    if ($eq -lt 1) {
        return
    }
    $name = $line.Substring(0, $eq).Trim()
    $val = $line.Substring($eq + 1).Trim()
    if ($val.Length -ge 2) {
        $q = $val[0]
        if (($q -eq '"' -or $q -eq "'") -and $val[-1] -eq $q) {
            $val = $val.Substring(1, $val.Length - 2)
        }
    }
    if ($name -match '^[A-Za-z_][A-Za-z0-9_]*$') {
        Set-Item -Path "Env:$name" -Value $val
    }
}

if (-not $env:JAVA_HOME -and (Test-Path "D:\JAVA")) {
    $env:JAVA_HOME = "D:\JAVA"
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}

Write-Host "已加载 .env.local，正在启动 dumb-backend ..." -ForegroundColor Green
mvn spring-boot:run
