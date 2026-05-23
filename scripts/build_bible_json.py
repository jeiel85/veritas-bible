"""
USFM (kor + engwebp from ebible.org) -> bible.json
Output:
{
  "books": [
    {"id": 1, "code": "GEN", "ko": "창세기", "en": "Genesis", "chapters": int},
    ...
  ],
  "verses": [
    {"b": 1, "c": 1, "v": 1, "ko": "...", "en": "..."}, ...
  ]
}

Strips USFM markup (\\w, \\f, \\x, character styles, footnotes).
"""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC_KO = ROOT / ".bible_source" / "kor"
SRC_EN = ROOT / ".bible_source" / "engwebp"
OUT_DIR = ROOT / "app" / "src" / "main" / "assets"
OUT_FILE = OUT_DIR / "bible.json"

BOOKS = [
    ("GEN", 1, "창세기", "Genesis"),
    ("EXO", 2, "출애굽기", "Exodus"),
    ("LEV", 3, "레위기", "Leviticus"),
    ("NUM", 4, "민수기", "Numbers"),
    ("DEU", 5, "신명기", "Deuteronomy"),
    ("JOS", 6, "여호수아", "Joshua"),
    ("JDG", 7, "사사기", "Judges"),
    ("RUT", 8, "룻기", "Ruth"),
    ("1SA", 9, "사무엘상", "1 Samuel"),
    ("2SA", 10, "사무엘하", "2 Samuel"),
    ("1KI", 11, "열왕기상", "1 Kings"),
    ("2KI", 12, "열왕기하", "2 Kings"),
    ("1CH", 13, "역대상", "1 Chronicles"),
    ("2CH", 14, "역대하", "2 Chronicles"),
    ("EZR", 15, "에스라", "Ezra"),
    ("NEH", 16, "느헤미야", "Nehemiah"),
    ("EST", 17, "에스더", "Esther"),
    ("JOB", 18, "욥기", "Job"),
    ("PSA", 19, "시편", "Psalms"),
    ("PRO", 20, "잠언", "Proverbs"),
    ("ECC", 21, "전도서", "Ecclesiastes"),
    ("SNG", 22, "아가", "Song of Solomon"),
    ("ISA", 23, "이사야", "Isaiah"),
    ("JER", 24, "예레미야", "Jeremiah"),
    ("LAM", 25, "예레미야애가", "Lamentations"),
    ("EZK", 26, "에스겔", "Ezekiel"),
    ("DAN", 27, "다니엘", "Daniel"),
    ("HOS", 28, "호세아", "Hosea"),
    ("JOL", 29, "요엘", "Joel"),
    ("AMO", 30, "아모스", "Amos"),
    ("OBA", 31, "오바댜", "Obadiah"),
    ("JON", 32, "요나", "Jonah"),
    ("MIC", 33, "미가", "Micah"),
    ("NAM", 34, "나훔", "Nahum"),
    ("HAB", 35, "하박국", "Habakkuk"),
    ("ZEP", 36, "스바냐", "Zephaniah"),
    ("HAG", 37, "학개", "Haggai"),
    ("ZEC", 38, "스가랴", "Zechariah"),
    ("MAL", 39, "말라기", "Malachi"),
    ("MAT", 40, "마태복음", "Matthew"),
    ("MRK", 41, "마가복음", "Mark"),
    ("LUK", 42, "누가복음", "Luke"),
    ("JHN", 43, "요한복음", "John"),
    ("ACT", 44, "사도행전", "Acts"),
    ("ROM", 45, "로마서", "Romans"),
    ("1CO", 46, "고린도전서", "1 Corinthians"),
    ("2CO", 47, "고린도후서", "2 Corinthians"),
    ("GAL", 48, "갈라디아서", "Galatians"),
    ("EPH", 49, "에베소서", "Ephesians"),
    ("PHP", 50, "빌립보서", "Philippians"),
    ("COL", 51, "골로새서", "Colossians"),
    ("1TH", 52, "데살로니가전서", "1 Thessalonians"),
    ("2TH", 53, "데살로니가후서", "2 Thessalonians"),
    ("1TI", 54, "디모데전서", "1 Timothy"),
    ("2TI", 55, "디모데후서", "2 Timothy"),
    ("TIT", 56, "디도서", "Titus"),
    ("PHM", 57, "빌레몬서", "Philemon"),
    ("HEB", 58, "히브리서", "Hebrews"),
    ("JAS", 59, "야고보서", "James"),
    ("1PE", 60, "베드로전서", "1 Peter"),
    ("2PE", 61, "베드로후서", "2 Peter"),
    ("1JN", 62, "요한1서", "1 John"),
    ("2JN", 63, "요한2서", "2 John"),
    ("3JN", 64, "요한3서", "3 John"),
    ("JUD", 65, "유다서", "Jude"),
    ("REV", 66, "요한계시록", "Revelation"),
]


# Markers to drop entire line for (header/meta lines)
META_MARKERS = {
    "id", "ide", "h", "toc1", "toc2", "toc3", "mt", "mt1", "mt2", "mt3", "mt4",
    "sts", "rem", "cl", "cp", "ca", "va", "vp",
    "ms", "ms1", "ms2", "mr", "s", "s1", "s2", "s3", "sr", "r", "d", "sp",
    "lit", "iex", "qa",
}

# Inline character markers that wrap content with backslash-asterisk close
# We'll strip the markers but keep the content for these
CHAR_KEEP_CONTENT = re.compile(r"\\\+?(?:w|nd|wj|qs|qac|sig|sls|tl|em|bd|bdit|it|sc|no)\s+([^\\]*?)\\\+?(?:w|nd|wj|qs|qac|sig|sls|tl|em|bd|bdit|it|sc|no)\*", re.DOTALL)
# Drop content for footnote/cross-ref markers
DROP_FX = re.compile(r"\\f\b.*?\\f\*", re.DOTALL)
DROP_X = re.compile(r"\\x\b.*?\\x\*", re.DOTALL)
DROP_FE = re.compile(r"\\fe\b.*?\\fe\*", re.DOTALL)
# Remove word-level attributes like |strong="H1234" lemma="..." x-morph="..."
# Match key="value" pairs prefixed by |, regardless of what follows.
ATTR_RE = re.compile(r"\|(?:[a-zA-Z][\w\-]*=\"[^\"]*\"\s*)+")
# Any remaining \tag* (close) markers
CLOSE_TAG_RE = re.compile(r"\\[+\w]+\*")
# Standalone \tag (no content)
STANDALONE_TAG_RE = re.compile(r"\\[+\w]+\b")

C_RE = re.compile(r"^\\c\s+(\d+)")
V_RE = re.compile(r"^\\v\s+(\d+(?:[a-z])?)\s*(.*)$")
ANY_TAG_AT_START = re.compile(r"^\\(\+?\w+)\b\s*(.*)$")


def strip_markup(text: str) -> str:
    # Drop footnotes/cross-refs entirely
    text = DROP_FX.sub("", text)
    text = DROP_X.sub("", text)
    text = DROP_FE.sub("", text)
    # Remove word attributes BEFORE unwrapping (so |strong="..." doesn't leak)
    text = ATTR_RE.sub("", text)
    # Keep content for \w...\w* style markers, strip the markers
    prev = None
    while prev != text:
        prev = text
        text = CHAR_KEEP_CONTENT.sub(r"\1", text)
    # Repeat attribute cleanup in case markers exposed more attributes
    text = ATTR_RE.sub("", text)
    # Remove all remaining close tags
    text = CLOSE_TAG_RE.sub("", text)
    # Remove standalone tags
    text = STANDALONE_TAG_RE.sub("", text)
    # Collapse whitespace
    text = re.sub(r"\s+", " ", text).strip()
    return text


def parse_usfm(path: Path) -> dict[int, dict[int, str]]:
    text = path.read_text(encoding="utf-8")
    out: dict[int, dict[int, str]] = {}
    current_c = None
    current_v = None
    current_buf: list[str] = []

    def flush():
        nonlocal current_buf
        if current_c is not None and current_v is not None:
            joined = " ".join(current_buf)
            cleaned = strip_markup(joined)
            if cleaned:
                # Normalise verse number (drop trailing 'a', 'b', etc.)
                v_num = int(re.match(r"(\d+)", str(current_v)).group(1))
                out.setdefault(current_c, {})
                # Merge if duplicate (e.g. 1a + 1b)
                if v_num in out[current_c]:
                    out[current_c][v_num] = (out[current_c][v_num] + " " + cleaned).strip()
                else:
                    out[current_c][v_num] = cleaned
        current_buf = []

    for raw_line in text.splitlines():
        line = raw_line.strip()
        if not line:
            continue

        c_match = C_RE.match(line)
        if c_match:
            flush()
            current_c = int(c_match.group(1))
            current_v = None
            continue

        v_match = V_RE.match(line)
        if v_match:
            flush()
            current_v = v_match.group(1)
            rest = v_match.group(2)
            if rest:
                current_buf.append(rest)
            continue

        # Other backslash-tagged line
        tag_match = ANY_TAG_AT_START.match(line)
        if tag_match:
            tag = tag_match.group(1)
            rest = tag_match.group(2)
            if tag in META_MARKERS:
                continue
            # Paragraph-only markers (p, m, q, q1, q2, b, nb, pi, mi, li, etc.) often have empty rest
            if rest and current_v is not None:
                current_buf.append(rest)
            continue

        # Plain text continuation
        if current_v is not None:
            current_buf.append(line)

    flush()
    return out


def main() -> None:
    if not SRC_KO.exists() or not SRC_EN.exists():
        raise SystemExit(f"USFM source folders not found: {SRC_KO} / {SRC_EN}")

    OUT_DIR.mkdir(parents=True, exist_ok=True)

    book_index = []
    verses_out = []

    total_ko = 0
    total_en = 0
    matched = 0
    only_ko = 0
    only_en = 0

    for code, book_id, ko_name, en_name in BOOKS:
        ko_file = next(SRC_KO.glob(f"*-{code}kor.usfm"), None)
        en_file = next(SRC_EN.glob(f"*-{code}engwebp.usfm"), None)
        if not ko_file:
            print(f"WARN: KO USFM missing for {code}")
        if not en_file:
            print(f"WARN: EN USFM missing for {code}")
        ko_data = parse_usfm(ko_file) if ko_file else {}
        en_data = parse_usfm(en_file) if en_file else {}

        chapters = sorted(set(ko_data.keys()) | set(en_data.keys()))
        book_index.append({
            "id": book_id,
            "code": code,
            "ko": ko_name,
            "en": en_name,
            "chapters": len(chapters),
        })

        for ch in chapters:
            ko_v = ko_data.get(ch, {})
            en_v = en_data.get(ch, {})
            total_ko += len(ko_v)
            total_en += len(en_v)
            all_verses = sorted(set(ko_v.keys()) | set(en_v.keys()))
            for v in all_verses:
                ko_text = ko_v.get(v, "")
                en_text = en_v.get(v, "")
                if ko_text and en_text:
                    matched += 1
                elif ko_text:
                    only_ko += 1
                else:
                    only_en += 1
                verses_out.append({
                    "b": book_id,
                    "c": ch,
                    "v": v,
                    "ko": ko_text,
                    "en": en_text,
                })

    payload = {
        "version": "1910-kor + webp-eng",
        "license": "Public Domain",
        "source": "ebible.org",
        "books": book_index,
        "verses": verses_out,
    }

    OUT_FILE.write_text(
        json.dumps(payload, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8",
    )

    print(f"Books: {len(book_index)}")
    print(f"Verses: {len(verses_out)} (matched={matched}, only_ko={only_ko}, only_en={only_en})")
    print(f"Korean verses parsed: {total_ko}")
    print(f"English verses parsed: {total_en}")
    print(f"Output: {OUT_FILE} ({OUT_FILE.stat().st_size:,} bytes)")


if __name__ == "__main__":
    main()
