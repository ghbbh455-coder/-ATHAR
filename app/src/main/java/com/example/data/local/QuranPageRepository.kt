package com.example.data.local

import android.content.Context
import androidx.compose.runtime.Immutable
import org.json.JSONArray

@Immutable
data class QuranPageAyah(
    val surah: Int,
    val surahName: String,
    val ayah: Int,
    val text: String,
    val isNewSurah: Boolean = false
)

@Immutable
data class QuranPage(
    val pageNumber: Int,
    val juzNumber: Int,
    val surahNames: List<String>,
    val ayahs: List<QuranPageAyah>
)

object QuranPageRepository {
    private var cachedPages: List<QuranPage>? = null

    @Synchronized
    fun getPages(context: Context): List<QuranPage> {
        cachedPages?.let { return it }

        val list = ArrayList<QuranPage>(604)
        try {
            val jsonString = context.assets.open("quran_pages.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val len = jsonArray.length()
            for (i in 0 until len) {
                val pageObj = jsonArray.getJSONObject(i)
                val pageNumber = pageObj.optInt("page", i + 1)
                val juzNumber = pageObj.optInt("juz", 1)

                val surahNamesJson = pageObj.optJSONArray("surahNames")
                val surahNames = mutableListOf<String>()
                if (surahNamesJson != null) {
                    for (s in 0 until surahNamesJson.length()) {
                        surahNames.add(surahNamesJson.getString(s))
                    }
                }

                val ayahsJson = pageObj.optJSONArray("ayahs")
                val ayahs = mutableListOf<QuranPageAyah>()
                if (ayahsJson != null) {
                    for (a in 0 until ayahsJson.length()) {
                        val ayahObj = ayahsJson.getJSONObject(a)
                        ayahs.add(
                            QuranPageAyah(
                                surah = ayahObj.optInt("surah", 1),
                                surahName = ayahObj.optString("surahName", ""),
                                ayah = ayahObj.optInt("ayah", 1),
                                text = ayahObj.optString("text", ""),
                                isNewSurah = ayahObj.optBoolean("isNewSurah", false)
                            )
                        )
                    }
                }

                list.add(
                    QuranPage(
                        pageNumber = pageNumber,
                        juzNumber = juzNumber,
                        surahNames = surahNames,
                        ayahs = ayahs
                    )
                )
            }
            cachedPages = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cachedPages ?: emptyList()
    }

    fun getPage(context: Context, pageNumber: Int): QuranPage? {
        val pages = getPages(context)
        if (pageNumber in 1..pages.size) {
            return pages[pageNumber - 1]
        }
        return null
    }
}
