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

# Play Console hard limit: 500 Unicode chars per locale block (excluding tags).
# Over-limit text is silently truncated by Play Console — abort export instead
# of letting a bad file reach the desktop. (글로벌 지침 play-store-release-notes.md
# 의 박제된 enforcement 스니펫.)
$localePattern = '<(ko-KR|en-US|ja-JP|zh-CN|zh-TW)>([\s\S]*?)</\1>'
$violations = @()
foreach ($match in [regex]::Matches($playContent, $localePattern)) {
  $locale = $match.Groups[1].Value
  $body = $match.Groups[2].Value.Trim()
  $len = $body.Length
  $status = if ($len -gt 500) { 'OVER' } else { 'OK' }
  Write-Host ("  {0,-7}  {1,4} / 500  {2}" -f $locale, $len, $status)
  if ($len -gt 500) {
    $violations += "$locale ($len chars, $($len - 500) over)"
  }
}
if ($violations.Count -gt 0) {
  throw "Play Console release notes exceed the 500-character limit per locale: " +
    ($violations -join ', ') +
    ". Trim before exporting."
}

Write-Host "릴리즈 버전 검증 성공: $Tag"
