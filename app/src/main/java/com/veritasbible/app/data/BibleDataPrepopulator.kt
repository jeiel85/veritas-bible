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
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "b" -> b = reader.nextInt()
                    "c" -> c = reader.nextInt()
                    "v" -> v = reader.nextInt()
                    "ko" -> ko = reader.nextString()
                    "en" -> en = reader.nextString()
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
