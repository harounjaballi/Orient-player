package com.example.data.remote

import com.example.data.local.LiveChannelEntity
import com.example.data.local.VodMovieEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ParsedPlaylistData(
    val channels: List<LiveChannelEntity>,
    val movies: List<VodMovieEntity>,
    val seriesCount: Int,
    val serverMessage: String
)

object IptvProtocolService {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    /**
     * Parses raw M3U / M3U8 content (either downloaded from URL or imported from a local file).
     */
    fun parseM3uContent(
        rawM3u: String,
        playlistId: Long,
        startingChannelNumber: Int = 100
    ): ParsedPlaylistData {
        val lines = rawM3u.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        val channels = mutableListOf<LiveChannelEntity>()
        val movies = mutableListOf<VodMovieEntity>()
        var seriesCount = 0

        var currentName = "Chaîne IPTV"
        var currentGroup = "Général"
        var currentLogo = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=300&q=80"
        var currentEpgId = ""
        var currentCatchUp = false
        var currentCatchUpDays = 0
        var channelNum = startingChannelNumber

        for (line in lines) {
            if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                currentName = line.substringAfterLast(",", "Chaîne Direct").trim()
                currentGroup = extractAttribute(line, "group-title").ifBlank { "Bouquet M3U" }
                currentLogo = extractAttribute(line, "tvg-logo").ifBlank {
                    "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300&q=80"
                }
                currentEpgId = extractAttribute(line, "tvg-id")
                val catchupAttr = extractAttribute(line, "catchup")
                val catchupDaysAttr = extractAttribute(line, "catchup-days").toIntOrNull() ?: 0
                currentCatchUp = catchupAttr.isNotBlank() || catchupDaysAttr > 0
                currentCatchUpDays = if (catchupDaysAttr > 0) catchupDaysAttr else if (currentCatchUp) 3 else 0
            } else if (!line.startsWith("#") && (line.startsWith("http://") || line.startsWith("https://") || line.startsWith("rtmp://"))) {
                val lowerUrl = line.lowercase()
                val lowerGroup = currentGroup.lowercase()
                val isSeries = Regex("s\\d{1,2}\\s*e\\d{1,2}", RegexOption.IGNORE_CASE).containsMatchIn(currentName) ||
                    lowerGroup.contains("serie") || lowerGroup.contains("série")
                val isMovie = (!isSeries) && (
                    lowerUrl.endsWith(".mp4") || lowerUrl.endsWith(".mkv") ||
                        lowerGroup.contains("vod") || lowerGroup.contains("movie") || lowerGroup.contains("film")
                    )

                if (isSeries) {
                    seriesCount++
                } else if (isMovie && movies.size < 250) {
                    movies.add(
                        VodMovieEntity(
                            playlistId = playlistId,
                            title = currentName,
                            category = currentGroup,
                            posterUrl = currentLogo,
                            backdropUrl = currentLogo,
                            streamUrl = line,
                            year = 2025,
                            genre = currentGroup,
                            rating = 8.0,
                            durationMinutes = 110,
                            director = "Catalogue M3U",
                            cast = "VOD Stream",
                            description = "Film importé depuis la playlist M3U ($currentGroup)."
                        )
                    )
                } else if (channels.size < 500) {
                    val format = when {
                        lowerUrl.contains(".m3u8") -> "HLS"
                        lowerUrl.contains(".ts") -> "MPEG-TS"
                        else -> "HLS"
                    }
                    channels.add(
                        LiveChannelEntity(
                            playlistId = playlistId,
                            channelNumber = channelNum++,
                            name = currentName,
                            category = currentGroup,
                            logoUrl = currentLogo,
                            streamUrl = line,
                            streamFormat = format,
                            epgChannelId = currentEpgId,
                            currentProgramTitle = "Direct : $currentName",
                            currentProgramDesc = "Flux en direct ($format) • Catégorie $currentGroup",
                            currentProgramStart = "21:00",
                            currentProgramEnd = "23:00",
                            currentProgramProgress = 0.5f,
                            nextProgramTitle = "Programme Suivant",
                            nextProgramTime = "23:00 - 00:30",
                            hasCatchUp = currentCatchUp,
                            catchUpDays = currentCatchUpDays,
                            isAdult = lowerGroup.contains("adult") || lowerGroup.contains("+18") || lowerGroup.contains("xxx")
                        )
                    )
                }
            }
        }

        return ParsedPlaylistData(
            channels = channels,
            movies = movies,
            seriesCount = seriesCount,
            serverMessage = "M3U analysé : ${channels.size} chaînes, ${movies.size} films, $seriesCount épisodes"
        )
    }

    suspend fun fetchAndParseM3uUrl(url: String, playlistId: Long): Result<ParsedPlaylistData> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url.trim())
                    .header("User-Agent", "OrientPlayer/2.0 (Android; IPTV)")
                    .get()
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Erreur HTTP ${response.code} lors du téléchargement M3U"))
                    }
                    val body = response.body?.string() ?: ""
                    if (!body.contains("#EXTM3U", ignoreCase = true) && !body.contains("#EXTINF", ignoreCase = true)) {
                        return@withContext Result.failure(Exception("Le fichier distant ne contient pas d'en-tête M3U valide"))
                    }
                    val parsed = parseM3uContent(body, playlistId)
                    Result.success(parsed)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Xtream Codes API authentication + Live & VOD catalog sync.
     */
    suspend fun syncXtreamCodes(
        serverUrl: String,
        username: String,
        password: String,
        playlistId: Long
    ): Result<ParsedPlaylistData> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = serverUrl.trim().trimEnd('/')
            val authUrl = "$baseUrl/player_api.php?username=$username&password=$password"
            val authReq = Request.Builder()
                .url(authUrl)
                .header("User-Agent", "OrientPlayer/2.0 (Android; XtreamAPI)")
                .get()
                .build()

            httpClient.newCall(authReq).execute().use { authResp ->
                if (!authResp.isSuccessful) {
                    return@withContext Result.failure(Exception("Serveur Xtream injoignable (HTTP ${authResp.code})"))
                }
                val bodyStr = authResp.body?.string() ?: "{}"
                val rootJson = JSONObject(bodyStr)
                val userInfo = rootJson.optJSONObject("user_info")
                val status = userInfo?.optString("status", "Active") ?: "Active"
                if (!status.equals("Active", ignoreCase = true)) {
                    return@withContext Result.failure(Exception("Compte Xtream Codes non actif : $status"))
                }
            }

            // Fetch Live Streams
            val liveUrl = "$baseUrl/player_api.php?username=$username&password=$password&action=get_live_streams"
            val liveReq = Request.Builder().url(liveUrl).get().build()
            val channels = mutableListOf<LiveChannelEntity>()

            httpClient.newCall(liveReq).execute().use { liveResp ->
                if (liveResp.isSuccessful) {
                    val arr = JSONArray(liveResp.body?.string() ?: "[]")
                    val limit = minOf(arr.length(), 300)
                    for (i in 0 until limit) {
                        val obj = arr.getJSONObject(i)
                        val streamId = obj.optInt("stream_id", i + 1)
                        val name = obj.optString("name", "Chaîne #$streamId")
                        val icon = obj.optString("stream_icon").ifBlank {
                            "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=300&q=80"
                        }
                        val tvArchive = obj.optInt("tv_archive", 0) == 1
                        val archiveDuration = obj.optInt("tv_archive_duration", 0)
                        val streamUrl = "$baseUrl/live/$username/$password/$streamId.m3u8"

                        channels.add(
                            LiveChannelEntity(
                                playlistId = playlistId,
                                channelNumber = obj.optInt("num", i + 1),
                                name = name,
                                category = "Xtream Direct",
                                logoUrl = icon,
                                streamUrl = streamUrl,
                                streamFormat = "HLS",
                                epgChannelId = obj.optString("epg_channel_id"),
                                currentProgramTitle = "Direct : $name",
                                currentProgramDesc = "Flux Xtream Codes API Ultra HD",
                                currentProgramStart = "21:00",
                                currentProgramEnd = "23:00",
                                currentProgramProgress = 0.5f,
                                nextProgramTitle = "Programme Suivant",
                                nextProgramTime = "23:00 - 00:30",
                                hasCatchUp = tvArchive,
                                catchUpDays = archiveDuration
                            )
                        )
                    }
                }
            }

            Result.success(
                ParsedPlaylistData(
                    channels = channels,
                    movies = emptyList(),
                    seriesCount = 0,
                    serverMessage = "Xtream Codes synchronisé (${channels.size} chaînes)"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stalker Portal / Ministra handshake & channel list sync using Virtual MAC.
     */
    suspend fun syncStalkerPortal(
        portalUrl: String,
        virtualMac: String,
        playlistId: Long
    ): Result<ParsedPlaylistData> = withContext(Dispatchers.IO) {
        try {
            val cleanBase = portalUrl.trim().trimEnd('/')
            val loadEndpoint = if (cleanBase.endsWith("load.php") || cleanBase.endsWith("portal.php")) {
                cleanBase
            } else {
                "$cleanBase/server/load.php"
            }

            val cookieHeader = "mac=$virtualMac; stb_lang=fr; timezone=Europe/Paris"
            val handshakeUrl = "$loadEndpoint?type=stb&action=handshake&token=&JsHttpRequest=1-xml"
            val handshakeReq = Request.Builder()
                .url(handshakeUrl)
                .header("Cookie", cookieHeader)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 2 rev: 250 Safari/533.3"
                )
                .header("X-User-Agent", "Model: MAG250; Link: WiFi")
                .get()
                .build()

            var bearerToken = ""
            httpClient.newCall(handshakeReq).execute().use { resp ->
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(Exception("Portail Stalker HTTP ${resp.code}"))
                }
                val json = JSONObject(resp.body?.string() ?: "{}")
                bearerToken = json.optJSONObject("js")?.optString("token") ?: ""
            }

            if (bearerToken.isBlank()) {
                return@withContext Result.failure(Exception("Token Ministra/Stalker refusé pour la MAC $virtualMac"))
            }

            val channelsUrl = "$loadEndpoint?type=itv&action=get_all_channels&JsHttpRequest=1-xml"
            val channelsReq = Request.Builder()
                .url(channelsUrl)
                .header("Cookie", cookieHeader)
                .header("Authorization", "Bearer $bearerToken")
                .header("X-User-Agent", "Model: MAG250; Link: WiFi")
                .get()
                .build()

            val channels = mutableListOf<LiveChannelEntity>()
            httpClient.newCall(channelsReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val root = JSONObject(resp.body?.string() ?: "{}")
                    val dataArr = root.optJSONObject("js")?.optJSONArray("data") ?: JSONArray()
                    val limit = minOf(dataArr.length(), 300)
                    for (i in 0 until limit) {
                        val ch = dataArr.getJSONObject(i)
                        val name = ch.optString("name", "Chaîne MAG #${i + 1}")
                        val cmd = ch.optString("cmd").replace("ffmpeg ", "").trim()
                        val logo = ch.optString("logo").ifBlank {
                            "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=300&q=80"
                        }
                        if (cmd.startsWith("http")) {
                            channels.add(
                                LiveChannelEntity(
                                    playlistId = playlistId,
                                    channelNumber = ch.optInt("number", i + 1),
                                    name = name,
                                    category = "Portail Stalker",
                                    logoUrl = logo,
                                    streamUrl = cmd,
                                    streamFormat = "HLS",
                                    currentProgramTitle = "Direct : $name",
                                    currentProgramDesc = "Flux Stalker Portal MAG ($virtualMac)",
                                    currentProgramStart = "21:00",
                                    currentProgramEnd = "23:00",
                                    currentProgramProgress = 0.5f,
                                    nextProgramTitle = "Programme Suivant",
                                    nextProgramTime = "23:00 - 00:30"
                                )
                            )
                        }
                    }
                }
            }

            Result.success(
                ParsedPlaylistData(
                    channels = channels,
                    movies = emptyList(),
                    seriesCount = 0,
                    serverMessage = "Portail Stalker connecté avec MAC $virtualMac (${channels.size} chaînes)"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractAttribute(line: String, attribute: String): String {
        val regex = Regex("""$attribute="([^"]*)"""", RegexOption.IGNORE_CASE)
        return regex.find(line)?.groupValues?.getOrNull(1)?.trim() ?: ""
    }
}
