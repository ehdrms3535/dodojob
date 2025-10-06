package com.example.dodojob.data.naver

import android.content.Context
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.bodyAsText // ⬅️ bodyAsText 확장 함수
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

import java.io.IOException

object NaverGeocoding {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 10_000
        }
        install(HttpRequestRetry) {
            maxRetries = 2
            retryOnExceptionOrServerErrors(maxRetries)
            exponentialDelay()
        }
        // ⬇️ 추가: 응답 내용까지 찍히는 로깅
        //install(Logging) {
        //    level = LogLevel.BODY
        //    logger = Logger.DEFAULT
        //}
    }

    // ===== 데이터 모델 (Geocode) =====
    @Serializable
    private data class GeocodeResp(
        val addresses: List<NcpAddress> = emptyList(),
        val status: String? = null,
        val errorMessage: String? = null
    )

    @Serializable
    private data class NcpAddress(
        @SerialName("roadAddress") val roadAddress: String? = null,
        @SerialName("jibunAddress") val jibunAddress: String? = null,
        val x: String? = null, // 경도
        val y: String? = null  // 위도
    )

    // ===== 데이터 모델 (Reverse) =====
    @Serializable
    private data class ReverseResp(val results: List<RevResult> = emptyList())

    @Serializable
    private data class RevResult(
        val region: RevRegion? = null,
        val land: RevLand? = null
    )

    @Serializable
    private data class RevRegion(
        val area1: RevName? = null, val area2: RevName? = null,
        val area3: RevName? = null, val area4: RevName? = null
    )

    @Serializable
    private data class RevName(val name: String? = null)

    @Serializable
    private data class RevLand(
        @SerialName("number1") val number1: String? = null,
        @SerialName("number2") val number2: String? = null,
        val name: String? = null
    )

    data class GeocodeResult(
        val lat: Double,
        val lng: Double,
        val display: String,           // 도로명 > 지번 > 입력값
        val roadAddress: String? = null,
        val jibunAddress: String? = null
    )

    /**
     * B안: 주소 → 좌표
     */
    suspend fun geocode(context: Context, query: String): GeocodeResult? {
        val url = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode"
        val keyId = context.getString(com.example.dodojob.R.string.ncp_api_key_id)
        val keySecret = context.getString(com.example.dodojob.R.string.ncp_api_key_secret)

        return try {
            val httpRes = client.get(url) {
                headers {
                    append("X-NCP-APIGW-API-KEY-ID", keyId)
                    append("X-NCP-APIGW-API-KEY", keySecret)
                }
                url { parameters.append("query", query) }
                accept(ContentType.Application.Json)
            }

            val raw = httpRes.bodyAsText()
            println("Geocode HTTP ${httpRes.status.value} for '$query' raw=$raw")

            // HTTP 에러면 바로 null
            if (!httpRes.status.isSuccess()) return null

            // 원본에서 직접 파싱
            val res = Json { ignoreUnknownKeys = true }.decodeFromString(GeocodeResp.serializer(), raw)

            if (res.status != null && res.status != "OK") {
                println("Geocode status=${res.status} error=${res.errorMessage}")
            }

            val first = res.addresses.firstOrNull() ?: run {
                println("Geocode EMPTY result for '$query'")
                return null
            }

            val lat = first.y?.toDoubleOrNull() ?: return null
            val lng = first.x?.toDoubleOrNull() ?: return null
            val display = first.roadAddress ?: first.jibunAddress ?: query
            GeocodeResult(lat, lng, display, first.roadAddress, first.jibunAddress)

        } catch (e: ClientRequestException) { // 4xx
            val body = e.response.bodyAsText()
            println("Geocode 4xx ${e.response.status} body=$body")
            null
        } catch (e: ServerResponseException) { // 5xx
            val body = e.response.bodyAsText()
            println("Geocode 5xx ${e.response.status} body=$body")
            null
        } catch (e: IOException) {
            println("Geocode network error: ${e.message}")
            null
        } catch (e: Exception) {
            println("Geocode unknown error: ${e.message}")
            null
        }
    }


    /**
     * A안(내부): 좌표 → 주소 (키 직접 주입)
     * - 외부에 굳이 노출할 필요 없으면 private로 내려도 됩니다.
     */
    suspend fun reverse(lat: Double, lng: Double, apiKeyId: String, apiKey: String): String? {
        val url = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc"
        val res: ReverseResp = client.get(url) {
            headers {
                append("X-NCP-APIGW-API-KEY-ID", apiKeyId)
                append("X-NCP-APIGW-API-KEY", apiKey)
            }
            url {
                parameters.append("coords", "$lng,$lat") // 경도,위도
                parameters.append("sourcecrs", "epsg:4326")
                parameters.append("orders", "roadaddr,addr")
                parameters.append("output", "json")
            }
            accept(ContentType.Application.Json)
        }.body()

        val r = res.results.firstOrNull() ?: return null
        val a1 = r.region?.area1?.name.orEmpty()
        val a2 = r.region?.area2?.name.orEmpty()
        val a3 = r.region?.area3?.name.orEmpty()
        val a4 = r.region?.area4?.name.orEmpty()
        val num = buildString {
            r.land?.number1?.let { append(it) }
            r.land?.number2?.takeIf { !it.isNullOrBlank() }?.let { append("-").append(it) }
        }
        val base = listOf(a1, a2, a3, a4).filter { it.isNotBlank() }.joinToString(" ")
        return if (num.isNotBlank()) "$base $num" else base.ifBlank { null }
    }

    /**
     * B안(권장 공개 API): 좌표 → 주소
     * - 화면에서는 이 함수를 호출하세요.
     */
    suspend fun reverse(context: Context, lat: Double, lng: Double): String? {
        val keyId = context.getString(com.example.dodojob.R.string.ncp_api_key_id)
        val keySecret = context.getString(com.example.dodojob.R.string.ncp_api_key_secret)
        return reverse(lat, lng, keyId, keySecret)
    }
}
