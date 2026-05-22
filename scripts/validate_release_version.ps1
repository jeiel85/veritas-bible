param(
  [Parameter(Mandatory = $true)]
  [string]$Tag
)

$ErrorActionPreference = "Stop"

if ($Tag -notmatch '^v\d+\.\d+\.\d+$') {
  throw "태그는 vX.Y.Z 형식이어야 합니다. 입력값: $Tag"
}

$version = $Tag.Substring(1)
$buildFile = Join-Path $PSScriptRoot "..\app\build.gradle.kts"
$releaseNote = Join-Path $PSScriptRoot "..\docs\releases\$Tag.md"
$playNote = Join-Path $PSScriptRoot "..\play_store\release_notes\$Tag.txt"

$content = Get-Content $buildFile -Raw
if ($content -notmatch "versionName\s*=\s*`"$([regex]::Escape($version))`"") {
  throw "app/build.gradle.kts의 versionName이 $version 과 일치하지 않습니다."
}

if ($content -notmatch "versionCode\s*=\s*\d+") {
  throw "app/build.gradle.kts에서 versionCode를 찾을 수 없습니다."
}

if (-not (Test-Path $releaseNote)) {
  throw "GitHub Release 본문이 없습니다: docs/releases/$Tag.md"
}

if (-not (Test-Path $playNote)) {
  throw "Play Console 릴리즈 노트가 없습니다: play_store/release_notes/$Tag.txt"
}

$playContent = Get-Content $playNote -Raw
foreach ($locale in @("ko-KR", "en-US")) {
  if ($playContent -notmatch "<$locale>[\s\S]+</$locale>") {
    throw "Play Console 릴리즈 노트에 <$locale> 블록이 없습니다."
  }
}

Write-Host "릴리즈 버전 검증 성공: $Tag"
