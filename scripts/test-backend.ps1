# 后端测试一键脚本 — 从 deploy/.env 加载环境变量后运行 mvn test
# 前置条件：本机 MySQL(3306) 与 Redis(6379) 已启动，deploy/.env 已配置
$ErrorActionPreference = 'Stop'

$envFile = Join-Path $PSScriptRoot '..\deploy\.env'
if (-not (Test-Path $envFile)) {
    Write-Error "未找到 $envFile — 请先复制 deploy/.env.example 为 deploy/.env 并填写本机配置"
}

Get-Content $envFile | Where-Object { $_ -match '^\s*[^#].*=' } | ForEach-Object {
    $k, $v = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($k.Trim(), $v.Trim(), 'Process')
}

Set-Location (Join-Path $PSScriptRoot '..\jiangnan-travel')
mvn test
exit $LASTEXITCODE
