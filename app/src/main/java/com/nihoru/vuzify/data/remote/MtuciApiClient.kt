package com.nihoru.vuzify.data.remote

import com.nihoru.vuzify.data.model.ScheduleApiResponse
import com.nihoru.vuzify.data.model.SearchApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class MtuciApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun searchGroups(query: String, session: MtuciSession): SearchApiResponse {
        val url = "https://mtuci.ru/bitrix/services/main/ajax.php?c=mtuci:timetable&action=getItemsListBySearch&mode=class"
        return client.submitForm(
            url = url,
            formParameters = parameters {
                append("NAME", query)
                append("SITE_ID", "s3")
                session.sessid?.let { append("sessid", it) }
            }
        ) {
            header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
            header("Cookie", session.cookies)
            header("Referer", "https://mtuci.ru/time-table/")
            header("X-Requested-With", "XMLHttpRequest")
        }.body()
    }

    suspend fun getSchedule(group: String, month: Int, session: MtuciSession): ScheduleApiResponse {
        val url = "https://mtuci.ru/bitrix/services/main/ajax.php?c=mtuci:timetable&action=getTimetableByValue&mode=class"
        return client.submitForm(
            url = url,
            formParameters = parameters {
                append("VALUE", group)
                append("MONTH", month.toString())
                append("TYPE", "group")
                append("SITE_ID", "s3")
                session.sessid?.let { append("sessid", it) }
            }
        ) {
            header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
            header("Cookie", session.cookies)
            header("Referer", "https://mtuci.ru/time-table/")
            header("X-Requested-With", "XMLHttpRequest")
        }.body()
    }
}
