"""
성경 데이터 변환 스크립트
출처: https://github.com/thiagobodruk/bible
라이선스: Creative Commons BY-NC (비상업적 용도)

사용법:
  pip install requests
  python scripts/convert_bible_data.py

결과: assets/bible_krv.json, assets/bible_kjv.json 생성
"""

import json
import urllib.request
import os

KOREAN_BOOK_NAMES = [
    # 구약 39권
    "창세기", "출애굽기", "레위기", "민수기", "신명기",
    "여호수아", "사사기", "룻기", "사무엘상", "사무엘하",
    "열왕기상", "열왕기하", "역대상", "역대하", "에스라",
    "느헤미야", "에스더", "욥기", "시편", "잠언",
    "전도서", "아가", "이사야", "예레미야", "예레미야애가",
    "에스겔", "다니엘", "호세아", "요엘", "아모스",
    "오바댜", "요나", "미가", "나훔", "하박국",
    "스바냐", "학개", "스가랴", "말라기",
    # 신약 27권
    "마태복음", "마가복음", "누가복음", "요한복음", "사도행전",
    "로마서", "고린도전서", "고린도후서", "갈라디아서", "에베소서",
    "빌립보서", "골로새서", "데살로니가전서", "데살로니가후서",
    "디모데전서", "디모데후서", "디도서", "빌레몬서", "히브리서",
    "야고보서", "베드로전서", "베드로후서",
    "요한일서", "요한이서", "요한삼서", "유다서", "요한계시록",
]


def fetch_json(url):
    print(f"  다운로드 중: {url}")
    with urllib.request.urlopen(url, timeout=60) as resp:
        return json.loads(resp.read().decode("utf-8-sig"))


def convert(source_books, translation, use_index_for_name=False):
    """thiagobodruk 형식 → 앱 형식 변환"""
    result_books = []

    for i, book in enumerate(source_books):
        if use_index_for_name:
            book_name = KOREAN_BOOK_NAMES[i]
        else:
            raw_name = book.get("book") or book.get("name", "")
            # 이미 한글이면 그대로, 영어면 인덱스 기반으로 매핑
            book_name = raw_name if any("가" <= c <= "힣" for c in raw_name) else KOREAN_BOOK_NAMES[i]

        chapters = book.get("chapters", [])
        verses = []
        for chapter_idx, chapter_verses in enumerate(chapters):
            for verse_idx, text in enumerate(chapter_verses):
                verses.append({
                    "chapter": chapter_idx + 1,
                    "verse": verse_idx + 1,
                    "text": text.strip(),
                })

        result_books.append({"name": book_name, "verses": verses})

    return {"translation": translation, "books": result_books}


def save(data, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, separators=(",", ":"))
    size_mb = os.path.getsize(path) / 1024 / 1024
    verse_count = sum(len(b["verses"]) for b in data["books"])
    print(f"  저장 완료: {path} ({size_mb:.1f} MB, {verse_count:,} 구절)")


def main():
    base_url = "https://raw.githubusercontent.com/thiagobodruk/bible/master/json"
    assets_dir = os.path.join(os.path.dirname(__file__), "..", "assets")

    # KRV (개역한글)
    print("\n[1/2] 개역한글(KRV) 다운로드 및 변환...")
    krv_source = fetch_json(f"{base_url}/ko_ko.json")
    krv_data = convert(krv_source, "KRV", use_index_for_name=False)
    save(krv_data, os.path.join(assets_dir, "bible_krv.json"))

    # KJV (King James Version)
    print("\n[2/2] KJV 다운로드 및 변환...")
    kjv_source = fetch_json(f"{base_url}/en_kjv.json")
    # KJV는 영어 책 이름이므로 인덱스 기반으로 한국어 이름 사용 (앱 내 책 이름 통일)
    kjv_data = convert(kjv_source, "KJV", use_index_for_name=True)
    save(kjv_data, os.path.join(assets_dir, "bible_kjv.json"))

    print("\n완료! assets/ 폴더를 확인하세요.")
    print("이후 'git add assets/ && git commit && git push' 로 GitHub에 업로드하세요.")


if __name__ == "__main__":
    main()
