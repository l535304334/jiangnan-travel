# 江南出行 — 生产部署构建脚本
# 用法: .\deploy\build.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  江南出行 生产部署构建脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. 后端构建
Write-Host "[1/5] 后端 Maven 打包..." -ForegroundColor Yellow
Set-Location ..\jiangnan-travel
mvn package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 后端构建失败" -ForegroundColor Red
    exit 1
}
Write-Host "  ✅ 后端构建成功" -ForegroundColor Green

# 2. 前端构建
Write-Host "[2/5] 前端 npm 构建..." -ForegroundColor Yellow
Set-Location ..\jiangnan-travel-web
npm run build --silent 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 前端构建失败" -ForegroundColor Red
    exit 1
}
Write-Host "  ✅ 前端构建成功" -ForegroundColor Green

# 3. 复制构建产物
Write-Host "[3/5] 复制构建产物到 deploy 目录..." -ForegroundColor Yellow
Set-Location ..\deploy
Copy-Item ..\jiangnan-travel\target\jiangnan-travel-1.0.0-SNAPSHOT.jar backend\ -Force
Remove-Item frontend\dist -Recurse -Force -ErrorAction SilentlyContinue
Copy-Item ..\jiangnan-travel-web\dist frontend\ -Recurse -Force
Write-Host "  ✅ 构建产物已就位" -ForegroundColor Green

# 4. 验证文件
Write-Host "[4/5] 验证部署文件..." -ForegroundColor Yellow
$jarExists = Test-Path backend\jiangnan-travel-1.0.0-SNAPSHOT.jar
$distExists = Test-Path frontend\dist\index.html
$dockerExists = Test-Path docker-compose.yml

if ($jarExists -and $distExists -and $dockerExists) {
    Write-Host "  ✅ 所有文件就绪" -ForegroundColor Green
} else {
    Write-Host "  ❌ 文件缺失: jar=$jarExists dist=$distExists docker=$dockerExists" -ForegroundColor Red
    exit 1
}

# 5. 总结
Write-Host "[5/5] 部署信息" -ForegroundColor Yellow
$jarSize = (Get-Item backend\jiangnan-travel-1.0.0-SNAPSHOT.jar).Length / 1MB
Write-Host "  📦 JAR: {0:N1} MB" -f $jarSize -ForegroundColor White
$distSize = (Get-ChildItem frontend\dist -Recurse | Measure-Object Length -Sum).Sum / 1MB
Write-Host "  📦 前端: {0:N1} MB" -f $distSize -ForegroundColor White

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ✅ 构建完成！使用以下命令启动：" -ForegroundColor Green
Write-Host "  docker compose up -d" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

Set-Location ..
