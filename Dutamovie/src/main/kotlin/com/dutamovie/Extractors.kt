package com.dutamovie

import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.apmap
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.base64Decode
import com.lagradost.cloudstream3.extractors.*
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.M3u8Helper
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.extractors.JWPlayer


class Embedfirex : JWPlayer() {
    override var name = "Embedfirex"
    override var mainUrl = "https://embedfirex.xyz"
}

class Ryderjet : JWPlayer() {
    override val name = "Ryderjet"
    override val mainUrl = "https://ryderjet.com"
}

class Meownime : JWPlayer() {
    override val name = "Meownime"
    override val mainUrl = "https://meownime.ltd"
}

class DesuOdchan : JWPlayer() {
    override val name = "DesuOdchan"
    override val mainUrl = "https://desustream.me/odchan/"
}

class DesuArcg : JWPlayer() {
    override val name = "DesuArcg"
    override val mainUrl = "https://desustream.me/arcg/"
}

class DesuDrive : JWPlayer() {
    override val name = "DesuDrive"
    override val mainUrl = "https://desustream.me/desudrive/"
}

class DesuOdvip : JWPlayer() {
    override val name = "DesuOdvip"
    override val mainUrl = "https://desustream.me/odvip/"
}

open class JWPlayer : ExtractorApi() {
    override val name = "JWPlayer"
    override val mainUrl = "https://www.jwplayer.com"
    override val requiresReferer = false

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val sources = mutableListOf<ExtractorLink>()
        with(app.get(url).document) {
            val data = this.select("script").mapNotNull { script ->
                if (script.data().contains("sources: [")) {
                    script.data().substringAfter("sources: [")
                        .substringBefore("],").replace("'", "\"")
                } else if (script.data().contains("otakudesu('")) {
                    script.data().substringAfter("otakudesu('")
                        .substringBefore("');")
                } else {
                    null
                }
            }

            tryParseJson<List<ResponseSource>>("$data")?.map {
                sources.add(
                    ExtractorLink(
                        name,
                        name,
                        it.file,
                        referer = url,
                        quality = getQualityFromName(
                            Regex("(\\d{3,4}p)").find(it.file)?.groupValues?.get(
                                1
                            )
                        )
                    )
                )
            }
        }
        return sources
    }

    private data class ResponseSource(
        @JsonProperty("file") val file: String,
        @JsonProperty("type") val type: String?,
        @JsonProperty("label") val label: String?
    )

}

class Gofile : ExtractorApi() {
    override val name = "Gofile"
    override val mainUrl = "https://gofile.io"
    override val requiresReferer = false
    private val mainApi = "https://api.gofile.io"

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val id = Regex("/(?:\\?c=|d/)([\\da-zA-Z]+)").find(url)?.groupValues?.get(1)
        val token = app.get("$mainApi/createAccount").parsedSafe<Account>()?.data?.get("token")
        app.get("$mainApi/getContent?contentId=$id&token=$token&websiteToken=12345")
            .parsedSafe<Source>()?.data?.contents?.forEach {
                callback.invoke(
                    ExtractorLink(
                        this.name,
                        this.name,
                        it.value["link"] ?: return,
                        "",
                        getQuality(it.value["name"]),
                        headers = mapOf(
                            "Cookie" to "accountToken=$token"
                        )
                    )
                )
            }

    }

    private fun getQuality(str: String?): Int {
        return Regex("(\\d{3,4})[pP]").find(str ?: "")?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: Qualities.Unknown.value
    }

    data class Account(
        @JsonProperty("data") val data: HashMap<String, String>? = null,
    )

    data class Data(
        @JsonProperty("contents") val contents: HashMap<String, HashMap<String, String>>? = null,
    )

    data class Source(
        @JsonProperty("data") val data: Data? = null,
    )

}