# Play Console 릴리즈 노트

Google Play Console에 붙여 넣을 릴리즈 노트를 보관합니다. GitHub Release 본문과 달리 평문이며 언어별 BCP-47 태그를 사용합니다.

## 파일명

- `vX.Y.Z.txt`
- 예: `v1.0.0.txt`

## 형식

```text
<ko-KR>
vX.Y.Z 한 줄 부제

새로 추가
• 변경 1
• 변경 2
</ko-KR>
<en-US>
vX.Y.Z headline

What's new
• Change 1
• Change 2
</en-US>
```

## 제약

- 언어당 최대 500자 권장
- 마크다운/HTML 문법 사용 금지
- `<ko-KR>`, `<en-US>` 블록을 함께 유지
