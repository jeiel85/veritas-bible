param(
  [string]$Tag = "",
  [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$buildFile = Join-Path $root "app\build.gradle.kts"
$buildContent = Get-Content $buildFile -Raw

if ($Tag -eq "") {
  if ($buildContent -notmatch 'versionName\s*=\s*"(?<version>\d+\.\d+\.\d+)"') {
    throw "app/build.gradle.kts에서 versionName을 찾을 수 없습니다."
  }
  $Tag = "v$($Matches.version)"
}

& (Join-Path $PSScriptRoot "validate_release_version.ps1") $Tag

$desktop = [Environment]::GetFolderPath("Desktop")
if ([string]::IsNullOrWhiteSpace($desktop)) {
  $desktop = Join-Path $env:USERPROFILE "Desktop"
}

$outDir = Join-Path $desktop "veritas-bible-$Tag"
$aabSource = Join-Path $root "app\build\outputs\bundle\release\app-release.aab"
$notesSource = Join-Path $root "play_store\release_notes\$Tag.txt"
$aabTarget = Join-Path $outDir "veritas-bible-release.aab"
$notesTarget = Join-Path $outDir "veritas-bible-release-notes.txt"

New-Item -ItemType Directory -Force -Path $outDir | Out-Null
Copy-Item -LiteralPath $notesSource -Destination $notesTarget -Force

if (-not $SkipBuild) {
  $keystorePath = $env:ANDROID_RELEASE_KEYSTORE_PATH
  if ([string]::IsNullOrWhiteSpace($keystorePath)) {
    $keystorePath = $env:KEYSTORE_PATH
  }
  if ([string]::IsNullOrWhiteSpace($keystorePath)) {
    $keystorePath = Join-Path $root "my-upload-key.jks"
  }

  $hasStorePassword = -not [string]::IsNullOrWhiteSpace($env:ANDROID_RELEASE_KEYSTORE_PASSWORD) -or
    -not [string]::IsNullOrWhiteSpace($env:STORE_PASSWORD)
  $hasKeyPassword = -not [string]::IsNullOrWhiteSpace($env:ANDROID_RELEASE_KEY_PASSWORD) -or
    -not [string]::IsNullOrWhiteSpace($env:KEY_PASSWORD) -or
    -not [string]::IsNullOrWhiteSpace($env:ANDROID_RELEASE_KEYSTORE_PASSWORD)

  if (-not (Test-Path $keystorePath) -or -not $hasStorePassword -or -not $hasKeyPassword) {
    throw @"
릴리즈 AAB를 빌드하려면 업로드 키 설정이 필요합니다.
릴리즈 노트 TXT는 먼저 내보냈습니다: $notesTarget

필요 값:
- ANDROID_RELEASE_KEYSTORE_PATH 또는 KEYSTORE_PATH
- ANDROID_RELEASE_KEYSTORE_PASSWORD 또는 STORE_PASSWORD
- ANDROID_RELEASE_KEY_ALIAS (없으면 upload 사용)
- ANDROID_RELEASE_KEY_PASSWORD 또는 KEY_PASSWORD

현재 keystore 확인 경로: $keystorePath
"@
  }

  Push-Location $root
  try {
    & .\gradlew.bat bundleRelease
    if ($LASTEXITCODE -ne 0) {
      throw "bundleRelease 실패"
    }
  } finally {
    Pop-Location
  }
}

if (-not (Test-Path $aabSource)) {
  throw "AAB 파일이 없습니다. 먼저 릴리즈 빌드를 완료하세요: $aabSource"
}

Copy-Item -LiteralPath $aabSource -Destination $aabTarget -Force

Write-Host "바탕화면 내보내기 완료:"
Write-Host $aabTarget
Write-Host $notesTarget
