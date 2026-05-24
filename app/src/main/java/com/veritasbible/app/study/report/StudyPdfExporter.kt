package com.veritasbible.app.study.report

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * 외부 라이브러리 없이 Android 표준 [PdfDocument] 만으로
 * Markdown 리포트 본문을 PDF 로 렌더링한다.
 *
 * - 라이선스: AOSP Apache 2.0 (외부 의존성 없음).
 * - 한글 폰트: 시스템 기본 폰트(`Typeface.DEFAULT`)가 한글을 지원하므로 추가 폰트 번들 불필요.
 * - 출력: A4 세로(595×842pt). 한 줄씩 텍스트 줄바꿈, 페이지 자동 분할.
 * - Markdown 의 `#`/`##` 헤더와 `> `/`- ` 같은 접두사는 가독성 위주의 단순 스타일로 처리한다.
 */
class StudyPdfExporter(
    private val pageWidthPt: Int = 595,
    private val pageHeightPt: Int = 842,
    private val marginPt: Float = 36f
) {

    private val bodyPaint = Paint().apply {
        textSize = 11f
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }
    private val boldPaint = Paint().apply {
        textSize = 11f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    private val h1Paint = Paint().apply {
        textSize = 18f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    private val h2Paint = Paint().apply {
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    private val quotePaint = Paint().apply {
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        color = 0xFF555555.toInt()
        isAntiAlias = true
    }

    private val usableWidth get() = pageWidthPt - 2 * marginPt
    private val lineSpacing = 4f

    /**
     * Markdown 문자열을 받아 PDF [File] 로 저장한다. 호출자는 [shareUri] 로
     * 곧바로 공유 인텐트를 만들 수 있다.
     */
    fun writeToCache(context: Context, markdown: String, fileNameBase: String): File {
        val cacheDir = File(context.cacheDir, "study_reports").apply { mkdirs() }
        val sanitized = fileNameBase
            .replace(Regex("[^A-Za-z0-9가-힣_-]"), "_")
            .take(40)
            .ifBlank { "study_report" }
        val outFile = File(cacheDir, "${sanitized}.pdf")

        val doc = PdfDocument()
        var pageNum = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, pageNum).create())
        var canvas = page.canvas
        var y = marginPt + bodyPaint.textSize

        fun finishPageIfNeeded(neededHeight: Float) {
            if (y + neededHeight > pageHeightPt - marginPt) {
                doc.finishPage(page)
                pageNum += 1
                page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, pageNum).create())
                canvas = page.canvas
                y = marginPt + bodyPaint.textSize
            }
        }

        fun drawLine(text: String, paint: Paint, leftPad: Float = 0f) {
            val lines = wrapLine(text, paint, usableWidth - leftPad)
            lines.forEach { ln ->
                finishPageIfNeeded(paint.textSize + lineSpacing)
                canvas.drawText(ln, marginPt + leftPad, y, paint)
                y += paint.textSize + lineSpacing
            }
        }

        markdown.split('\n').forEach { raw ->
            val line = raw.trimEnd()
            when {
                line.isBlank() -> {
                    finishPageIfNeeded(bodyPaint.textSize)
                    y += bodyPaint.textSize / 2
                }
                line.startsWith("# ") -> {
                    y += 4f
                    drawLine(line.removePrefix("# "), h1Paint)
                    y += 4f
                }
                line.startsWith("## ") -> {
                    y += 6f
                    drawLine(line.removePrefix("## "), h2Paint)
                    y += 2f
                }
                line.startsWith("### ") -> {
                    drawLine(line.removePrefix("### "), boldPaint)
                }
                line.startsWith("> ") -> {
                    drawLine(line.removePrefix("> "), quotePaint, leftPad = 8f)
                }
                line.startsWith("- ") -> {
                    drawLine("• " + line.removePrefix("- "), bodyPaint, leftPad = 8f)
                }
                line.startsWith("  - ") -> {
                    drawLine("· " + line.removePrefix("  - "), bodyPaint, leftPad = 20f)
                }
                line.startsWith("    - ") -> {
                    drawLine("· " + line.removePrefix("    - "), bodyPaint, leftPad = 32f)
                }
                else -> drawLine(line, bodyPaint)
            }
        }

        doc.finishPage(page)
        FileOutputStream(outFile).use { doc.writeTo(it) }
        doc.close()
        return outFile
    }

    /** [FileProvider] 권한이 필요한 공유용 URI 를 반환한다. */
    fun shareUri(context: Context, file: File) =
        FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )

    private fun wrapLine(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isEmpty()) return listOf("")
        val result = mutableListOf<String>()
        var current = StringBuilder()
        text.forEach { ch ->
            current.append(ch)
            if (paint.measureText(current.toString()) > maxWidth) {
                // 한 글자 줄이면서 줄바꿈
                if (current.length > 1) {
                    val carry = current.last()
                    current.setLength(current.length - 1)
                    result.add(current.toString())
                    current = StringBuilder().append(carry)
                } else {
                    result.add(current.toString())
                    current = StringBuilder()
                }
            }
        }
        if (current.isNotEmpty()) result.add(current.toString())
        return result
    }
}
