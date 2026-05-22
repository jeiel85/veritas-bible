package com.example.data

object BibleDataPrepopulator {
    fun createVerses(): List<BibleVerse> {
        val list = mutableListOf<BibleVerse>()

        // ------------------ BOOK 1: 창세기 (Genesis) - BookId 1 ------------------
        // Chapter 1: 창세기 1장 (창조)
        list.add(BibleVerse(
            book = "창세기", bookEn = "Genesis", bookId = 1, chapter = 1, verse = 1,
            text = "태초에 하나님이 천지를 창조하시니라.",
            textEn = "In the beginning, God created the heavens and the earth."
        ))
        list.add(BibleVerse(
            book = "창세기", bookEn = "Genesis", bookId = 1, chapter = 1, verse = 2,
            text = "땅이 혼돈하고 공허하며 흑암이 깊음 위에 있고 하나님의 영은 수면 위에 운행하시니라.",
            textEn = "The earth was formless and empty. Darkness was on the surface of the deep and God’s Spirit was hovering over the surface of the waters."
        ))
        list.add(BibleVerse(
            book = "창세기", bookEn = "Genesis", bookId = 1, chapter = 1, verse = 3,
            text = "하나님이 이르시되 빛이 있으라 하시니 빛이 있었고",
            textEn = "God said, \"Let there be light,\" and there was light."
        ))
        list.add(BibleVerse(
            book = "창세기", bookEn = "Genesis", bookId = 1, chapter = 1, verse = 4,
            text = "빛이 하나님이 보시기에 좋았더라 하나님이 빛과 어둠을 나누사",
            textEn = "God saw the light, and saw that it was good. God divided the light from the darkness."
        ))
        list.add(BibleVerse(
            book = "창세기", bookEn = "Genesis", bookId = 1, chapter = 1, verse = 5,
            text = "하나님이 빛을 낮이라 부르시고 어둠을 밤이라 부르시니라 저녁이 되고 아침이 되니 이는 첫째 날이니라.",
            textEn = "God called the light \"day,\" and the darkness he called \"night.\" There was evening and there was morning, one day."
        ))
        list.add(BibleVerse(
            book = "창세기", bookEn = "Genesis", bookId = 1, chapter = 1, verse = 26,
            text = "하나님이 이르시되 우리의 형상을 따라 우리의 모양대로 우리가 사람을 만들고 그들로 바다의 물고기와 하늘의 새와 가축과 온 땅과 땅에 기는 모든 것을 다스리게 하자 하시고",
            textEn = "God said, \"Let us make man in our image, after our likeness: and let them have dominion over the fish of the sea, and over the birds of the sky, and over the livestock, and over all the earth, and over every creeping thing that creeps on the earth.\""
        ))
        list.add(BibleVerse(
            book = "창세기", bookEn = "Genesis", bookId = 1, chapter = 1, verse = 27,
            text = "하나님이 자기 형상 곧 하나님의 형상대로 사람을 창조하시되 남자와 여자를 창조하시고",
            textEn = "God created man in his own image. In God’s image he created him; male and female he created them."
        ))
        list.add(BibleVerse(
            book = "창세기", bookEn = "Genesis", bookId = 1, chapter = 1, verse = 28,
            text = "하나님이 그들에게 복을 주시며 하나님이 그들에게 이르시되 생육하고 번성하여 땅에 충만하라, 땅을 정복하라, 바다의 물고기와 하늘의 새와 땅에 움직이는 모든 생물을 다스리라 하시니라.",
            textEn = "God blessed them. God said to them, \"Be fruitful, multiply, fill the earth, and subdue it. Have dominion over the fish of the sea, over the birds of the sky, and over every living thing that moves on the earth.\""
        ))
        list.add(BibleVerse(
            book = "창세기", bookEn = "Genesis", bookId = 1, chapter = 1, verse = 31,
            text = "하나님이 지으신 그 모든 것을 보시니 보시기에 심히 좋았더라 저녁이 되고 아침이 되니 이는 여섯째 날이니라.",
            textEn = "God saw everything that he had made, and, behold, it was very good. There was evening and there was morning, the sixth day."
        ))

        // Chapter 2: 창세기 2장
        list.add(BibleVerse(
            book = "창세기", bookEn = "Genesis", bookId = 1, chapter = 2, verse = 1,
            text = "천지와 만물이 다 이루어지니라.",
            textEn = "The heavens and the earth were finished, and all their vast array."
        ))
        list.add(BibleVerse(
            book = "창세기", bookEn = "Genesis", bookId = 1, chapter = 2, verse = 2,
            text = "하나님이 그가 하시던 일을 일곱째 날에 마치시니 그가 하시던 모든 일을 그치고 일곱째 날에 안식하시니라.",
            textEn = "On the seventh day God finished his work which he had made; and he rested on the seventh day from all his work which he had made."
        ))
        list.add(BibleVerse(
            book = "창세기", bookEn = "Genesis", bookId = 1, chapter = 2, verse = 3,
            text = "하나님이 그 일곱째 날을 복되게 하사 거룩하게 하셨으니 이는 하나님이 그 창조하시며 만드시던 모든 일을 마치시고 그 날에 안식하셨음이라.",
            textEn = "God blessed the seventh day, and made it holy, because in it he rested from all his work which he had created and made."
        ))

        // ------------------ BOOK 2: 시편 (Psalms) - BookId 2 ------------------
        // Psalm 1 (시편 1편 - 복 있는 사람)
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 1, verse = 1,
            text = "복 있는 사람은 악인들의 꾀를 따르지 아니하며 죄인들의 길에 서지 아니하며 오만한 자들의 자리에 앉지 아니하고",
            textEn = "Blessed is the man who doesn’t walk in the counsel of the wicked, nor stand in the way of sinners, nor sit in the seat of scoffers."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 1, verse = 2,
            text = "오직 여호와의 율법을 즐거워하여 그의 율법을 주야로 묵상하는도다.",
            textEn = "But his delight is in Yahweh’s law. On his law he meditates day and night."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 1, verse = 3,
            text = "그는 시냇가에 심은 나무가 철을 따라 열매를 맺으며 그 잎사귀가 마르지 아니함 같으니 그가 하는 모든 일이 다 형통하리로다.",
            textEn = "He will be like a tree planted by the streams of water, that produces its fruit in its season, whose leaf also doesn’t wither. Whatever he does shall prosper."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 1, verse = 4,
            text = "악인들은 그렇지 아니함이여 오직 바람에 나는 겨와 같도다.",
            textEn = "The wicked are not so, but are like the chaff which the wind drives away."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 1, verse = 5,
            text = "그러므로 악인들은 심판을 견디지 못하며 죄인들이 의인들의 모임에 들지 못하리로다.",
            textEn = "Therefore the wicked shall not stand in the judgment, nor sinners in the congregation of the righteous."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 1, verse = 6,
            text = "무릇 의인들의 길은 여호와께서 인정하시나 악인들의 길은 망하리로다.",
            textEn = "For Yahweh knows the way of the righteous, but the way of the wicked shall perish."
        ))

        // Psalm 23 (시편 23편 - 여호와는 나의 목자)
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 23, verse = 1,
            text = "여호와는 나의 목자시니 내게 부족함이 없으리로다.",
            textEn = "Yahweh is my shepherd. I shall lack nothing."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 23, verse = 2,
            text = "그가 나를 푸른 풀밭에 누이시며 쉴 만한 물 가로 인도하시는도다.",
            textEn = "He makes me lie down in green pastures. He leads me beside still waters."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 23, verse = 3,
            text = "내 영혼을 소생시키시고 자기 이름을 위하여 의의 길로 인도하시는도다.",
            textEn = "He restores my soul. He guides me in the paths of righteousness for his name’s sake."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 23, verse = 4,
            text = "내가 사망의 음침한 골짜기로 다닐지라도 해를 두려워하지 않을 것은 주께서 나와 함께 하심이라 주의 지팡이와 막대기가 나를 안위하시나이다.",
            textEn = "Even though I walk through the valley of the shadow of death, I will fear no evil, for you are with me. Your rod and your staff, they comfort me."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 23, verse = 5,
            text = "주께서 내 원수의 목전에서 내게 상을 차려 주시고 기름을 내 머리에 부으셨으니 내 잔이 넘치나이다.",
            textEn = "You prepare a table before me in the presence of my enemies. You anoint my head with oil. My cup runs over."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 23, verse = 6,
            text = "내 평생에 선하심과 인자하심이 반드시 나를 따르리니 내가 여호와의 집에 영원히 살리로다.",
            textEn = "Surely goodness and loving kindness shall follow me all the days of my life, and I will dwell in Yahweh’s house forever."
        ))

        // Psalm 121 (시편 121편 - 눈을 들어 산을 보니)
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 121, verse = 1,
            text = "내가 산을 향하여 눈을 들리라 나의 도움이 어디서 올까",
            textEn = "I will lift up my eyes to the hills. Where does my help come from?"
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 121, verse = 2,
            text = "나의 도움은 천지를 지으신 여호와에게서로다.",
            textEn = "My help comes from Yahweh, who made heaven and earth."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 121, verse = 3,
            text = "여호와께서 너를 실족하지 아니하게 하시며 너를 지키시는 이가 졸지 아니하시리로다.",
            textEn = "He will not allow your foot to be moved. He who keeps you will not slumber."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 121, verse = 4,
            text = "이스라엘을 지키시는 이는 졸지도 아니하시고 주무시지도 아니하시리로다.",
            textEn = "Behold, he who keeps Israel will neither slumber nor sleep."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 121, verse = 5,
            text = "여호와는 너를 지키시는 이시라 여호와께서 네 오른쪽에서 네 그늘이 되시나니",
            textEn = "Yahweh is your keeper. Yahweh is your shade on your right hand."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 121, verse = 6,
            text = "낮의 해가 너를 상하게 하지 아니하며 밤의 달도 너를 해치지 아니하리로다.",
            textEn = "The sun will not strike you by day, nor the moon by night."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 121, verse = 7,
            text = "여호와께서 너를 지켜 모든 환난을 면하게 하시며 또 네 영혼을 지키시리로다.",
            textEn = "Yahweh will keep you from all evil. He will keep your soul."
        ))
        list.add(BibleVerse(
            book = "시편", bookEn = "Psalms", bookId = 2, chapter = 121, verse = 8,
            text = "여호와께서 너의 출입을 지금부터 영원까지 지키시리로다.",
            textEn = "Yahweh will keep your going out and your coming in, from this time forth, and forevermore."
        ))

        // ------------------ BOOK 3: 요한복음 (John) - BookId 3 ------------------
        // Chapter 1: 요한복음 1장 (말씀이 육신이 되시다)
        list.add(BibleVerse(
            book = "요한복음", bookEn = "John", bookId = 3, chapter = 1, verse = 1,
            text = "태초에 말씀이 계시니라 이 말씀이 하나님과 함께 계셨으니 이 말씀은 곧 하나님이시니라.",
            textEn = "In the beginning was the Word, and the Word was with God, and the Word was God."
        ))
        list.add(BibleVerse(
            book = "요한복음", bookEn = "John", bookId = 3, chapter = 1, verse = 2,
            text = "그가 태초에 하나님과 함께 계셨고",
            textEn = "The same was in the beginning with God."
        ))
        list.add(BibleVerse(
            book = "요한복음", bookEn = "John", bookId = 3, chapter = 1, verse = 3,
            text = "만물이 그로 말미암아 지은 바 되었으니 지은 것이 하나도 그가 없이는 된 것이 없느니라.",
            textEn = "All things were made through him. Without him was not anything made that has been made."
        ))
        list.add(BibleVerse(
            book = "요한복음", bookEn = "John", bookId = 3, chapter = 1, verse = 4,
            text = "그 안에 생명이 있었으니 이 생명은 사람들의 빛이라.",
            textEn = "In him was life, and the life was the light of men."
        ))
        list.add(BibleVerse(
            book = "요한복음", bookEn = "John", bookId = 3, chapter = 1, verse = 5,
            text = "빛이 어둠에 비치되 어둠이 깨닫지 못하더라.",
            textEn = "The light shines in the darkness, and the darkness hasn’t overcome it."
        ))
        list.add(BibleVerse(
            book = "요한복음", bookEn = "John", bookId = 3, chapter = 1, verse = 9,
            text = "참빛 곧 세상에 와서 각 사람에게 비추는 빛이 있었나니",
            textEn = "The true light which shines on everyone was coming into the world."
        ))
        list.add(BibleVerse(
            book = "요한복음", bookEn = "John", bookId = 3, chapter = 1, verse = 10,
            text = "그가 세상에 계셨으며 세상은 그로 말미암아 지은 바 되었으되 세상이 그를 알지 못하였고",
            textEn = "He was in the world, and the world was made through him, and the world didn’t know him."
        ))
        list.add(BibleVerse(
            book = "요한복음", bookEn = "John", bookId = 3, chapter = 1, verse = 11,
            text = "자기 땅에 오매 자기 백성이 영접하지 아니하였으나",
            textEn = "He came to his own, and those who were his own didn’t receive him."
        ))
        list.add(BibleVerse(
            book = "요한복음", bookEn = "John", bookId = 3, chapter = 1, verse = 12,
            text = "영접하는 자 곧 그 이름을 믿는 자들에게는 하나님의 자녀가 되는 권세를 주셨으니",
            textEn = "But as many as received him, to them he gave the right to become God’s children, to those who believe in his name:"
        ))
        list.add(BibleVerse(
            book = "요한복음", bookEn = "John", bookId = 3, chapter = 1, verse = 14,
            text = "말씀이 육신이 되어 우리 가운데 거하시매 우리가 그의 영광을 보니 아버지의 독생자의 영광이요 은혜와 진리가 충만하더라.",
            textEn = "The Word became flesh, and lived among us. We saw his glory, such glory as of the one and only Son of the Father, full of grace and truth."
        ))

        // John Chapter 3: 요한복음 3장 (거듭남과 하나님의 사랑)
        list.add(BibleVerse(
            book = "요한복음", bookEn = "John", bookId = 3, chapter = 3, verse = 16,
            text = "하나님이 세상을 이처럼 사랑하사 독생자를 주셨으니 이는 그를 믿는 자마다 멸망하지 않고 영생을 얻게 하려 하심이라.",
            textEn = "For God so loved the world, that he gave his one and only Son, that whoever believes in him should not perish, but have eternal life."
        ))
        list.add(BibleVerse(
            book = "요한복음", bookEn = "John", bookId = 3, chapter = 3, verse = 17,
            text = "하나님이 그 아들을 세상에 보내신 것은 세상을 심판하려 하심이 아니요 그로 말미암아 세상이 구원을 받게 하려 하심이라.",
            textEn = "For God didn’t send his Son into the world to judge the world, but that the world should be saved through him."
        ))
        list.add(BibleVerse(
            book = "요한복음", bookEn = "John", bookId = 3, chapter = 3, verse = 18,
            text = "그를 믿는 자는 심판을 받지 아니하는 것이요 믿지 아니하는 자는 하나님의 독생자의 이름을 믿지 아니하므로 벌써 심판을 받은 것이니라.",
            textEn = "He who believes in him is not judged. He who doesn’t believe has been judged already, because he has not believed in the name of the one and only Son of God."
        ))

        // ------------------ BOOK 4: 로마서 (Romans) - BookId 4 ------------------
        // Romans Chapter 8: 로마서 8장 (성령과 정죄 없는 삶)
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 8, verse = 1,
            text = "그러므로 이제 그리스도 예수 안에 있는 자에게는 결코 정죄함이 없나니",
            textEn = "There is therefore now no condemnation to those who are in Christ Jesus, who don’t walk according to the flesh, but according to the Spirit."
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 8, verse = 2,
            text = "이는 그리스도 예수 안에 있는 생명의 성령의 법이 죄와 사망의 법에서 너를 해방하였음이라.",
            textEn = "For the law of the Spirit of life in Christ Jesus made me free from the law of sin and of death."
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 8, verse = 28,
            text = "우리가 알거니와 하나님을 사랑하는 자 곧 그의 뜻대로 부르심을 입은 자들에게는 모든 것이 합력하여 선을 이루느니라.",
            textEn = "We know that all things work together for good for those who love God, to those who are called according to his purpose."
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 8, verse = 31,
            text = "그런즉 이 일에 대하여 우리가 무슨 말 하리요 만일 하나님이 우리를 위하시면 누가 우리를 대적하리요",
            textEn = "What then shall we say about these things? If God is for us, who can be against us?"
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 8, verse = 32,
            text = "자기 아들을 아끼지 아니하시고 우리 모든 사람을 위하여 내주신 이가 어찌 그 아들과 함께 모든 것을 우리에게 주시지 아니하겠느냐",
            textEn = "He who didn’t spare his own Son, but delivered him up for us all, how would he not also with him freely give us all things?"
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 8, verse = 35,
            text = "누가 우리를 그리스도의 사랑에서 끊으리요 환난이나 곤고나 박해나 기근이나 적신이나 위험이나 칼이랴",
            textEn = "Who shall separate us from the love of Christ? Could oppression, or anguish, or persecution, or famine, or nakedness, or peril, or sword?"
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 8, verse = 37,
            text = "그러나 이 모든 일에 우리를 사랑하시는 이로 말미암아 우리가 넉넉히 이기느니라.",
            textEn = "No, in all these things, we are more than conquerors through him who loved us."
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 8, verse = 38,
            text = "내가 확신하노니 사망이나 생명이나 천사들이나 권세자들이나 현재 일이나 장래 일이나 능력이나",
            textEn = "For I am persuaded, that neither death, nor life, nor angels, nor principalities, nor things present, nor things to come, nor powers,"
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 8, verse = 39,
            text = "높음이나 깊음이나 다른 어떤 피조물이라도 우리를 우리 주 그리스도 예수 안에 있는 하나님의 사랑에서 끊을 수 없으리라.",
            textEn = "nor height, nor depth, nor any other created thing, will be able to separate us from the love of God, which is in Christ Jesus our Lord."
        ))

        // Romans Chapter 12: 로마서 12장 (그리스도인의 신앙 생활)
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 12, verse = 1,
            text = "그러므로 형제들아 내가 하나님의 모든 자비하심으로 너희를 권하노니 너희 몸을 하나님이 기뻐하시는 거룩한 산 제물로 드리라 이는 너희가 드릴 영적 예배니라.",
            textEn = "Therefore I urge you, brothers, by the mercies of God, to present your bodies a living sacrifice, holy, acceptable to God, which is your spiritual service."
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 12, verse = 2,
            text = "너희는 이 세대를 본받지 말고 오직 마음을 새롭게 함으로 변화를 받아 하나님의 선하시고 기뻐하시고 온전하신 뜻이 무엇인지 분별하도록 하라.",
            textEn = "Don’t be conformed to this world, but be transformed by the renewing of your mind, so that you may prove what is the good, well-pleasing, and perfect will of God."
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 12, verse = 9,
            text = "사랑에는 거짓이 없나니 악을 미워하고 선에 속하라",
            textEn = "Let love be without hypocrisy. Abhor that which is evil. Cling to that which is good."
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 12, verse = 10,
            text = "형제를 사랑하여 서로 우애하고 존경하기를 서로 먼저 하며",
            textEn = "In love of the brothers be tenderly affectionate one to another; in honor preferring one another;"
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 12, verse = 11,
            text = "부지런하여 게으르지 말고 열심을 품고 주를 섬기라",
            textEn = "not lagging in diligence; fervent in spirit; serving the Lord;"
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 12, verse = 12,
            text = "소망 중에 즐거워하며 환난 중에 참으며 기도에 항상 힘쓰며",
            textEn = "rejoicing in hope; enduring in troubles; continuing steadfastly in prayer;"
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 12, verse = 15,
            text = "즐거워하는 자들과 함께 즐거워하고 우는 자들과 함께 울라",
            textEn = "Rejoice with those who rejoice. Weep with those who weep."
        ))
        list.add(BibleVerse(
            book = "로마서", bookEn = "Romans", bookId = 4, chapter = 12, verse = 21,
            text = "악에게 지지 말고 선으로 악을 이기라.",
            textEn = "Don’t be overcome by evil, but overcome evil with good."
        ))

        return list
    }

    suspend fun prepopulateIfEmpty(bibleDao: BibleDao) {
        if (bibleDao.countVerses() == 0) {
            val list = createVerses()
            bibleDao.insertVerses(list)
        }
    }
}
