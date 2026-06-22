@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

title 江南出行 - Git 一键提交

set GIT="C:\Program Files\Git\bin\git.exe"

echo.
echo  ╔════════════════════════════════════════════╗
echo  ║    江 南 出 行 — Git 一 键 提 交           ║
echo  ╚════════════════════════════════════════════╝
echo.

:: 检查仓库状态
echo  [1/3] 检查仓库状态...
%GIT% status --short

:: 如果没有变更则退出
%GIT% diff-index --quiet HEAD --
if %errorlevel% equ 0 (
    echo.
    echo  没有需要提交的变更。
    pause
    exit /b 0
)

:: 输入提交信息
echo.
set /p MSG="  [2/3] 输入提交信息: "
if "%MSG%"=="" (
    echo  提交信息不能为空！
    pause
    exit /b 1
)

:: 提交
echo.
echo  [3/3] 执行提交...
%GIT% add -A
%GIT% commit -m "%MSG%"

if %errorlevel% equ 0 (
    echo.
    echo  ✓ 提交成功！更新日志已自动生成到 CHANGELOG.md
) else (
    echo.
    echo  ✗ 提交失败，请检查错误信息。
)

endlocal
pause
