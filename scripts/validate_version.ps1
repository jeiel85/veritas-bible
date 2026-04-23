# =============================================================================
# Version Sync Validator (PowerShell)
# =============================================================================
# This script validates that the GitHub release tag matches the app version
# defined in pubspec.yaml to prevent version mismatch errors.
#
# Usage: .\scripts\validate_version.ps1
# =============================================================================

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Version Sync Validator"
Write-Host "========================================"
Write-Host ""

# Get GitHub tag version (from environment or git)
$GitHubTag = $env:GITHUB_REF_NAME
if (-not $GitHubTag) {
    try {
        $GitHubTag = git describe --tags --abbrev=0 2>$null
    } catch {
        $GitHubTag = $null
    }
}

if ($GitHubTag) {
    $GitHubVersion = $GitHubTag -replace '^v', ''
} else {
    $GitHubVersion = "unknown"
}

# Get version from pubspec.yaml
$PubspecLines = Get-Content "pubspec.yaml" -Encoding UTF8
$VersionLine = $PubspecLines | Where-Object { $_ -match "^version:" }
if ($VersionLine) {
    $PubspecVersion = ($VersionLine -split "version:")[1].Trim()
    $AppVersion = $PubspecVersion -replace '\+\d+$', ''
} else {
    Write-Host "[ERROR] Could not find version in pubspec.yaml" -ForegroundColor Red
    exit 1
}

Write-Host "GitHub Release Tag: v$GitHubVersion"
Write-Host "pubspec.yaml Version: $AppVersion"
Write-Host ""

# Compare versions (skip if GitHub tag is unknown - local validation)
if ($GitHubTag -and $GitHubVersion -ne "unknown") {
    if ($GitHubVersion -ne $AppVersion) {
        Write-Host "[ERROR] Version mismatch detected!" -ForegroundColor Red
        Write-Host ""
        Write-Host "  GitHub Tag:  v$GitHubVersion"
        Write-Host "  App Version: $AppVersion"
        Write-Host ""
        Write-Host "Please ensure both versions match before creating a release."
        Write-Host ""
        Write-Host "To fix:"
        Write-Host "  1. Update pubspec.yaml version to $GitHubVersion"
        Write-Host "  2. Or create a new tag matching the current version"
        Write-Host ""
        exit 1
    } else {
        Write-Host "[OK] Version check passed!" -ForegroundColor Green
        Write-Host "  GitHub Tag and App Version are in sync."
        Write-Host ""
        exit 0
    }
} else {
    # Local validation - just show version
    Write-Host "[INFO] GitHub tag not detected (local run)"
    Write-Host "  pubspec.yaml Version: $AppVersion"
    Write ""
    exit 0
}