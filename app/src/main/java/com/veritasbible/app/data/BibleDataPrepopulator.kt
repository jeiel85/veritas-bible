package com.veritasbible.app.data

import android.content.Context
import android.util.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

object BibleDataPrepopulator {
    private const val ASSET_NAME = "bible.json"
    private const val EXPECTED_VERSE_COUNT = 31105
    private const val BATCH_SIZE = 1000

    suspend fun prepopulateIfEmpty(
        context: Context,
        bibleDao: BibleDao,
        onProgress: suspend (Float, String) -> Unit = { _, _ -> },
    ) = withContext(Dispatchers.IO) {
        if (bibleDao.countVerses() > 0) {
            onProgress(1f, "")
            return@withContext
        }
        loadFromAssets(context, bibleDao, onProgress)
    }

    /**
     * v5→v6 마이그레이션을 거친 기존 사용자에게 영어 USFM 단락 마커를
     * 한 번에 적용한다. bible.json 의 `pb` 필드만 골라 UPDATE 만 실행.
     * 이미 paragraph mark 가 있는 경우(예: 새로 prepopulate 된 사용자)
     * 빠르게 skip.
     */
    suspend fun syncParagraphMarksIfNeeded(
        context: Context,
        bibleDao: BibleDao,
    ) = withContext(Dispatchers.IO) {
        if (bibleDao.countVerses() == 0) return@withContext
        if (bibleDao.countParagraphStarts() > 0) return@withContext
        context.assets.open(ASSET_NAME).use { input ->
            JsonReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "verses" -> {
                            reader.beginArray()
                            while (reader.hasNext()) {
                                var b = 0
                                var c = 0
                                var v = 0
                                var pb = false
                                reader.beginObject()
                                while (reader.hasNext()) {
                                    when (reader.nextName()) {
                                        "b" -> b = reader.nextInt()
                                        "c" -> c = reader.nextInt()
                                        "v" -> v = reader.nextInt()
                                        "pb" -> pb = reader.nextBoolean()
                                        else -> reader.skipValue()
                                    }
                                }
                                reader.endObject()
                                if (pb && b > 0 && c > 0 && v > 0) {
                                    bibleDao.markParagraphStart(b, c, v)
                                }
                            }
                            reader.endArray()
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
            }
        }
    }

    private suspend fun loadFromAssets(
        context: Context,
        bibleDao: BibleDao,
        onProgress: suspend (Float, String) -> Unit,
    ) {
        val bookLookup = HashMap<Int, Pair<String, String>>(66)

        context.assets.open(ASSET_NAME).use { input ->
            JsonReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "books" -> readBooks(reader, bookLookup)
                        "verses" -> readVerses(reader, bookLookup, bibleDao, onProgress)
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
            }
        }
        onProgress(1f, "")
    }

    private fun readBooks(
        reader: JsonReader,
        bookLookup: MutableMap<Int, Pair<String, String>>,
    ) {
        reader.beginArray()
        while (reader.hasNext()) {
            var id = 0
            var ko = ""
            var en = ""
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "id" -> id = reader.nextInt()
                    "ko" -> ko = reader.nextString()
                    "en" -> en = reader.nextString()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            if (id > 0) {
                bookLookup[id] = ko to en
            }
        }
        reader.endArray()
    }

    private suspend fun readVerses(
        reader: JsonReader,
        bookLookup: Map<Int, Pair<String, String>>,
        bibleDao: BibleDao,
        onProgress: suspend (Float, String) -> Unit,
    ) {
        val batch = ArrayList<BibleVerse>(BATCH_SIZE)
        var inserted = 0
        reader.beginArray()
        while (reader.hasNext()) {
            var b = 0
            var c = 0
            var v = 0
            var ko = ""
            var en = ""
            var pb = false
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "b" -> b = reader.nextInt()
                    "c" -> c = reader.nextInt()
                    "v" -> v = reader.nextInt()
                    "ko" -> ko = reader.nextString()
                    "en" -> en = reader.nextString()
                    "pb" -> pb = reader.nextBoolean()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()

            val names = bookLookup[b] ?: continue
            batch.add(
                BibleVerse(
                    book = names.first,
                    bookEn = names.second,
                    bookId = b,
                    chapter = c,
                    verse = v,
                    text = ko.ifEmpty { en },
                    textEn = en.ifEmpty { ko },
                    paragraphStart = pb,
                )
            )

            if (batch.size >= BATCH_SIZE) {
                bibleDao.insertVerses(batch)
                inserted += batch.size
                batch.clear()
                val progress = (inserted.toFloat() / EXPECTED_VERSE_COUNT).coerceIn(0f, 0.99f)
                onProgress(progress, "$inserted / $EXPECTED_VERSE_COUNT")
            }
        }
        reader.endArray()

        if (batch.isNotEmpty()) {
            bibleDao.insertVerses(batch)
            inserted += batch.size
            batch.clear()
        }
        onProgress(1f, "$inserted / $EXPECTED_VERSE_COUNT")
    }
}
