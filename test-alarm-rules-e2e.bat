@echo off
setlocal enabledelayedexpansion

set "ROOT_DIR=%~dp0"
set "FRONT_DIR=%ROOT_DIR%jeecgboot-vue3"
set "TEST_SPEC=tests/e2e/alarm-rules-settings.spec.ts"

echo [INFO] Switching to frontend directory...
cd /d "%FRONT_DIR%"
if errorlevel 1 (
  echo [ERROR] Cannot enter directory: %FRONT_DIR%
  exit /b 1
)

echo [INFO] Installing frontend dependencies...
call pnpm install
if errorlevel 1 (
  echo [ERROR] pnpm install failed.
  exit /b 1
)

echo [INFO] Installing Playwright browsers...
call pnpm exec playwright install
if errorlevel 1 (
  echo [ERROR] playwright install failed.
  exit /b 1
)

if /i "%~1"=="headed" (
  echo [INFO] Running E2E in headed mode...
  call pnpm test:e2e:headed -- %TEST_SPEC%
) else (
  echo [INFO] Running E2E in headless mode...
  call pnpm test:e2e -- %TEST_SPEC%
)

if errorlevel 1 (
  echo [ERROR] E2E test failed.
  exit /b 1
)

echo [SUCCESS] Alarm rules E2E test passed.
exit /b 0

