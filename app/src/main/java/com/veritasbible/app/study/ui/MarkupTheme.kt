package com.veritasbible.app.study.ui

import androidx.compose.ui.graphics.Color
import com.veritasbible.app.study.data.MarkType

/**
 * 마킹 타입별 시각 색상. 핵심 5종은 강한 색을 주고, 확장 타입은
 * 보조 색으로 차분하게 처리한다.
 *
 * 색상은 본문 다크 모드와 라이트 모드 모두에서 가독성이 떨어지지 않도록
 * 0.22~0.32 알파를 사용한다.
 */
object MarkupTheme {
    private val palette: Map<String, Color> = mapOf(
        MarkType.SUBJECT to Color(0xFF1976D2),
        MarkType.VERB to Color(0xFFE53935),
        MarkType.OBJECT to Color(0xFF8E24AA),
        MarkType.CONNECTIVE to Color(0xFFFB8C00),
        MarkType.KEYWORD to Color(0xFFFBC02D),
        MarkType.REPEATED_WORD to Color(0xFF00ACC1),
        MarkType.COMMAND to Color(0xFFD81B60),
        MarkType.REQUEST to Color(0xFFC2185B),
        MarkType.STATEMENT to Color(0xFF6D4C41),
        MarkType.NARRATIVE to Color(0xFF7CB342),
        MarkType.CAUSE to Color(0xFF5E35B1),
        MarkType.RESULT to Color(0xFF00897B),
        MarkType.CONTRAST to Color(0xFF424242),
        MarkType.PURPOSE to Color(0xFF3949AB),
        MarkType.CONDITION to Color(0xFF9E9D24),
        MarkType.BASIS to Color(0xFF455A64),
        MarkType.DEFINITION to Color(0xFF6A1B9A),
        MarkType.APPLICATION_CLUE to Color(0xFFEF6C00)
    )

    fun colorFor(markType: String): Color = palette[markType] ?: Color(0xFF607D8B)

    fun highlightAlpha(): Float = 0.28f
}
