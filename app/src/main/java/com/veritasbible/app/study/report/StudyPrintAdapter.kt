package com.veritasbible.app.study.report

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Android 시스템 인쇄 메뉴(`PrintManager`)와 연구 리포트 PDF 를 연결한다.
 *
 * 시스템은 어댑터에게 두 단계로 PDF 를 요청한다:
 *   1) [onLayout]: 미리보기를 위해 페이지 수와 정보를 알려 줘야 한다. 여기서
 *      한 번 PDF 를 캐시에 만들어 두고 페이지 수만 보고한다.
 *   2) [onWrite]: 실제 파일 디스크립터에 PDF 를 복사한다.
 *
 * 이 어댑터는 외부 라이브러리 없이 [StudyPdfExporter] 를 재사용해 동일한
 * Markdown→PDF 변환을 그대로 활용한다.
 */
class StudyPrintAdapter(
    private val context: Context,
    private val markdown: String,
    private val jobName: String
) : PrintDocumentAdapter() {

    private var pdfFile: File? = null
    private val exporter = StudyPdfExporter()

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        try {
            pdfFile = exporter.writeToCache(context, markdown, jobName)
        } catch (e: Exception) {
            callback.onLayoutFailed(e.localizedMessage ?: "PDF generation failed")
            return
        }

        val info = PrintDocumentInfo.Builder("${sanitize(jobName)}.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()
        callback.onLayoutFinished(info, !newAttributes.equals(oldAttributes))
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {
        val src = pdfFile
        if (src == null || !src.exists()) {
            callback.onWriteFailed("PDF file not ready")
            return
        }
        try {
            FileInputStream(src).use { input ->
                FileOutputStream(destination.fileDescriptor).use { output ->
                    input.copyTo(output)
                }
            }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback.onWriteFailed(e.localizedMessage ?: "PDF write failed")
        }
    }

    private fun sanitize(name: String): String =
        name.replace(Regex("[^A-Za-z0-9가-힣_-]"), "_").take(40).ifBlank { "study_report" }

    companion object {
        /**
         * 시스템 인쇄 시트를 띄운다. PDF 저장·다른 프린터·HP·Brother 등
         * 시스템에 설치된 모든 print service 가 자동 노출된다.
         */
        fun startPrintJob(context: Context, markdown: String, jobName: String) {
            val pm = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
            val attrs = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(PrintAttributes.Resolution("res", "veritas", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()
            pm.print(jobName, StudyPrintAdapter(context, markdown, jobName), attrs)
        }
    }
}

/** ReportPanel 등 외부에서 호출하기 좋은 헬퍼. */
fun startStudyPrintJob(context: Context, markdown: String, jobName: String) {
    StudyPrintAdapter.startPrintJob(context, markdown, jobName)
}

/** 출력용 Intent — 일부 ROM 에서 print 메뉴가 막혀 있을 경우 fallback. */
fun pdfShareIntent(context: Context, file: File, subject: String): Intent {
    val uri = StudyPdfExporter().shareUri(context, file)
    return Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}
