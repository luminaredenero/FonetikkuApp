package com.luminaredenero.fonetikku

object Fonetikku {

    private val ipaMap = linkedMapOf(
        "tʃər" to "cer", "prɑː" to "pra", "ɔːr" to "or", "ŋɡ" to "ngg", "ər" to "er",
        "aɪ" to "ai", "eɪ" to "ei", "oʊ" to "ou", "ɔɪ" to "oi", "aʊ" to "au", "juː" to "yu",
        "iː" to "i", "uː" to "u", "ɑː" to "a", "ɔː" to "o",
        "ɪ" to "i", "æ" to "a", "ɛ" to "e", "ʌ" to "a", "ɒ" to "a", "ə" to "e", "ʊ" to "u",
        "tʃ" to "c", "dʒ" to "j", "ʃ" to "sy", "ʒ" to "j", "θ" to "t", "ð" to "d",
        "p" to "p", "b" to "b", "t" to "t", "d" to "d", "k" to "k", "g" to "g",
        "m" to "m", "n" to "n", "ŋ" to "ng", "l" to "l", "r" to "r", "w" to "w", "j" to "y",
        "f" to "f", "v" to "v", "s" to "s", "z" to "z", "h" to "h"
    )

    private val sortedKeys = ipaMap.keys.sortedByDescending { it.length }

    private fun preProcess(ipaString: String): String {
        return ipaString.replace(Regex("[ˈˌ./()]"), "").lowercase()
    }

    private fun postProcess(fonetikString: String): String {
        if (fonetikString.isEmpty()) return ""
        return fonetikString.replaceFirstChar { it.uppercase() }
    }

    fun konversi(ipaString: String?): String {
        if (ipaString.isNullOrBlank()) {
            return ""
        }

        var fonetik = preProcess(ipaString)

        if (fonetik.endsWith('ə')) {
            fonetik = fonetik.dropLast(1) + 'a'
        }

        for (key in sortedKeys) {
            fonetik = fonetik.replace(key, ipaMap.getValue(key))
        }

        return postProcess(fonetik)
    }
}
