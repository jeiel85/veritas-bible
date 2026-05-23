"""
Generate launcher icons and Play Store feature graphic for Veritas Bible.

- Square store icon (512x512 PNG)
- Adaptive icon foreground/background drawables
- mipmap WEBPs at every density (legacy + round)
- Feature graphic (1024x500 PNG)

Design:
  - Primary brand color: Deep Navy #1A237E
  - Glyph: stylised open Bible silhouette with central spine and 3 horizontal text lines
  - Foreground occupies ~66% of the safe zone for adaptive icons
"""
from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
PLAY = ROOT / "play_store" / "assets"

PRIMARY = (26, 35, 126, 255)        # #1A237E Deep Navy
PRIMARY_DARK = (16, 22, 84, 255)    # darker shade
PAGE = (255, 250, 240, 255)         # off-white parchment
PAGE_SHADOW = (218, 210, 195, 255)
GOLD = (255, 213, 102, 255)
GOLD_SOFT = (251, 192, 45, 255)
INK = (44, 51, 99, 255)


def draw_open_bible(img: Image.Image, cx: float, cy: float, size: float) -> None:
    """Stylised open Bible centred at (cx, cy), bounding size."""
    draw = ImageDraw.Draw(img, "RGBA")

    half_w = size * 0.46
    half_h = size * 0.32

    # Two trapezoid-shaped pages joined at the spine.
    # Left page
    left_page = [
        (cx - half_w, cy - half_h * 0.78),       # top-outer
        (cx - 4, cy - half_h * 0.95),           # top-inner (spine)
        (cx - 4, cy + half_h * 0.95),           # bottom-inner
        (cx - half_w * 1.05, cy + half_h * 0.78),  # bottom-outer
    ]
    right_page = [
        (cx + 4, cy - half_h * 0.95),
        (cx + half_w, cy - half_h * 0.78),
        (cx + half_w * 1.05, cy + half_h * 0.78),
        (cx + 4, cy + half_h * 0.95),
    ]

    # Soft shadow under book
    shadow_layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow_layer)
    shadow_box = [
        (cx - half_w * 1.1, cy + half_h * 0.85),
        (cx + half_w * 1.1, cy + half_h * 1.05),
    ]
    shadow_draw.ellipse(shadow_box, fill=(0, 0, 0, 120))
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(radius=size * 0.04))
    img.alpha_composite(shadow_layer)

    # Page shadows behind (stacked pages illusion)
    for offset in (6, 4, 2):
        sh_l = [(x - offset, y + offset) for x, y in left_page]
        sh_r = [(x + offset, y + offset) for x, y in right_page]
        draw.polygon(sh_l, fill=PAGE_SHADOW)
        draw.polygon(sh_r, fill=PAGE_SHADOW)

    # Front pages
    draw.polygon(left_page, fill=PAGE)
    draw.polygon(right_page, fill=PAGE)

    # Spine (gold accent)
    spine = [
        (cx - 6, cy - half_h * 0.95),
        (cx + 6, cy - half_h * 0.95),
        (cx + 6, cy + half_h * 0.95),
        (cx - 6, cy + half_h * 0.95),
    ]
    draw.polygon(spine, fill=GOLD)

    # Text lines (3 per page)
    line_color = INK
    line_thickness = max(2, int(size * 0.012))
    for i in range(3):
        y = cy - half_h * 0.45 + i * half_h * 0.42
        # Left page lines
        x_start = cx - half_w * 0.9
        x_end = cx - 16
        # Slight tilt to match page perspective
        tilt = (i - 1) * 2
        draw.line(
            [(x_start, y + tilt), (x_end, y - tilt)],
            fill=line_color,
            width=line_thickness,
        )
        # Right page lines
        x_start = cx + 16
        x_end = cx + half_w * 0.9
        draw.line(
            [(x_start, y - tilt), (x_end, y + tilt)],
            fill=line_color,
            width=line_thickness,
        )

    # Gold bookmark ribbon hanging from spine
    rib_x = cx - 2
    ribbon = [
        (rib_x - 6, cy - half_h * 0.95),
        (rib_x + 6, cy - half_h * 0.95),
        (rib_x + 6, cy + half_h * 1.18),
        (rib_x, cy + half_h * 1.30),
        (rib_x - 6, cy + half_h * 1.18),
    ]
    draw.polygon(ribbon, fill=GOLD_SOFT)


def make_icon(size: int, with_background: bool = True, with_inset: float = 0.0) -> Image.Image:
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    if with_background:
        bg_draw = ImageDraw.Draw(img)
        # Solid navy background with a subtle vertical gradient
        for y in range(size):
            t = y / (size - 1)
            r = int(PRIMARY[0] * (1 - t) + PRIMARY_DARK[0] * t)
            g = int(PRIMARY[1] * (1 - t) + PRIMARY_DARK[1] * t)
            b = int(PRIMARY[2] * (1 - t) + PRIMARY_DARK[2] * t)
            bg_draw.line([(0, y), (size, y)], fill=(r, g, b, 255))

    # Draw the open Bible glyph
    glyph_size = size * (0.82 if not with_background else 0.62)
    if with_inset:
        glyph_size = size * with_inset
    draw_open_bible(img, size / 2, size / 2, glyph_size)
    return img


def make_round_icon(size: int) -> Image.Image:
    full = make_icon(size)
    mask = Image.new("L", (size, size), 0)
    mdraw = ImageDraw.Draw(mask)
    mdraw.ellipse((0, 0, size, size), fill=255)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(full, (0, 0), mask)
    return out


def save_webp(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path, format="WEBP", quality=90, method=6)


def save_png(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path, format="PNG", optimize=True)


def main() -> None:
    # Densities (per Android docs)
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    for folder, px in densities.items():
        save_webp(make_icon(px), RES / folder / "ic_launcher.webp")
        save_webp(make_round_icon(px), RES / folder / "ic_launcher_round.webp")

    # Adaptive icon
    # Foreground is 108dp = 432px at xxxhdpi; safe zone ~66dp = 264px
    # We render the glyph centered on a 432px transparent canvas with proper inset.
    adaptive_fg = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
    draw_open_bible(adaptive_fg, 216, 216, 264)
    fg_dir = RES / "drawable"
    fg_dir.mkdir(parents=True, exist_ok=True)
    save_png(adaptive_fg, fg_dir / "ic_launcher_foreground.png")

    # Solid navy adaptive background
    adaptive_bg = Image.new("RGBA", (108, 108), PRIMARY)
    save_png(adaptive_bg, fg_dir / "ic_launcher_background.png")

    # Replace XML drawable shims that referenced vector resources
    (fg_dir / "ic_launcher_foreground.xml").unlink(missing_ok=True)
    (fg_dir / "ic_launcher_background.xml").unlink(missing_ok=True)

    # Store icon (512x512 high-res for Play Console)
    PLAY.mkdir(parents=True, exist_ok=True)
    save_png(make_icon(512), PLAY / "play_store_icon_512.png")

    # Feature graphic 1024x500
    fg = Image.new("RGBA", (1024, 500), (0, 0, 0, 0))
    # Gradient background
    fg_draw = ImageDraw.Draw(fg)
    for x in range(1024):
        t = x / 1023
        r = int(PRIMARY_DARK[0] * (1 - t) + PRIMARY[0] * t)
        g = int(PRIMARY_DARK[1] * (1 - t) + PRIMARY[1] * t)
        b = int(PRIMARY_DARK[2] * (1 - t) + PRIMARY[2] * t)
        fg_draw.line([(x, 0), (x, 500)], fill=(r, g, b, 255))

    # Bible glyph on the left
    draw_open_bible(fg, 260, 250, 340)

    # Title text on the right
    title_size = 84
    sub_size = 30
    try:
        # Try to use a bundled font; if not available, fall back to default
        font_title = ImageFont.truetype("malgun.ttf", title_size)
        font_sub = ImageFont.truetype("malgun.ttf", sub_size)
    except OSError:
        try:
            font_title = ImageFont.truetype("arial.ttf", title_size)
            font_sub = ImageFont.truetype("arial.ttf", sub_size)
        except OSError:
            font_title = ImageFont.load_default()
            font_sub = ImageFont.load_default()

    fg_draw.text((520, 170), "Veritas Bible", fill=(255, 250, 240, 255), font=font_title)
    fg_draw.text((522, 270), "오프라인 한·영 성경 연구", fill=(255, 213, 102, 255), font=font_sub)
    fg_draw.text((522, 310), "Offline Bible Study", fill=(220, 222, 245, 230), font=font_sub)

    save_png(fg, PLAY / "feature_graphic_1024x500.png")

    print("Icons:")
    for folder, px in densities.items():
        print(f"  {folder}: {px}px")
    print(f"Store icon: {PLAY / 'play_store_icon_512.png'}")
    print(f"Feature graphic: {PLAY / 'feature_graphic_1024x500.png'}")


if __name__ == "__main__":
    main()
