param(
    [string]$AdbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    [string]$ApkPath = ".\app\build\outputs\apk\debug\app-debug.apk"
)

$ErrorActionPreference = 'Stop'

if (!(Test-Path $AdbPath)) {
    throw "adb.exe not found at $AdbPath"
}

if (!(Test-Path $ApkPath)) {
    throw "APK not found at $ApkPath. Run .\\gradlew.bat :app:assembleDebug first."
}

Write-Host "Checking connected devices..."
& $AdbPath devices

Write-Host "Installing $ApkPath ..."
& $AdbPath install -r $ApkPath

Write-Host "Done. If install succeeded, launch on watch from app list: Platelet Monitor."
