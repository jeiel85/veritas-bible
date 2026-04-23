from PIL import Image, ImageDraw

def create_bible_icon(path):
    size = (1024, 1024)
    background_color = (26, 35, 126)  # #1A237E Deep Navy
    image = Image.new("RGBA", size, background_color)
    draw = ImageDraw.Draw(image)

    # 간단한 성경책 모양 그리기 (흰색)
    # 책 바디
    draw.rectangle([250, 300, 774, 800], fill="white")
    # 책 갈피/구분선
    draw.line([512, 300, 512, 800], fill=background_color, width=15)
    
    # 십자가 모양 (왼쪽 면에)
    draw.rectangle([350, 480, 410, 500], fill=background_color) # 가로
    draw.rectangle([370, 440, 390, 540], fill=background_color) # 세로

    image.save(path)

if __name__ == "__main__":
    create_bible_icon("assets/icon/icon.png")
