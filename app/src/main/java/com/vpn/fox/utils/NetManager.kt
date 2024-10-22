package com.vpn.fox.utils

import android.content.pm.PackageManager
import android.webkit.WebSettings
import androidx.lifecycle.MutableLiveData
import com.vpn.fox.VpnApp
import com.vpn.fox.VpnApp.Companion.toFoxPrint
import com.vpn.fox.utils.DataManager.togging
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject

object NetManager {
    private val client = HttpClient(CIO) {
        install(HttpRequestRetry) {
            maxRetries = 5
            retryIf { _, response ->
                response.status.value != 200
            }
            retryOnException(5, true)
            delayMillis { retry -> retry * 3000L }
        }
        install(UserAgent) {
            agent = runCatching { WebSettings.getDefaultUserAgent(VpnApp.app) }.getOrNull()
                ?: "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 20000
        }
    }
    val netRegion = MutableLiveData<String>()

    fun region() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                client.get("https://api.myip.com").run {
                    if (status.isSuccess()) {
                        runCatching { netRegion.postValue(JSONObject(bodyAsText()).getString("cc")) }
                    } else {
                        region2()
                    }
                }
            }.onFailure {
                region2()
            }
        }
    }

    private fun region2() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                client.get("https://ipapi.co/json").run {
                    if (status.isSuccess()) {
                        runCatching { netRegion.postValue(JSONObject(bodyAsText()).getString("country")) }
                    } else {
                        region3()
                    }
                }
            }.onFailure {
                region3()
            }
        }
    }

    private fun region3() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                client.get("https://ipinfo.io/json").run {
                    if (status.isSuccess()) {
                        runCatching { netRegion.postValue(JSONObject(bodyAsText()).getString("country")) }
                    }
                }
            }
        }
    }

    fun getTogging(cloy: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            if (togging.isNotEmpty()) cancel()
            runCatching {
                val client = HttpClient()
                fun getAppVersionName(): String {
                    return try {
                        val packageInfo = VpnApp.app.packageManager.getPackageInfo(VpnApp.app.packageName, 0)
                        packageInfo.versionName // 获取版本名称
                    } catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                        "1.1"
                    }
                }
                try {
                    val response = client.get("https://wag.connectionlink.link/togging/morrill" +
                            "?erudite=com.foxmod.connectionlink&dustbin=breton&brought=" +
                            getAppVersionName() +
                            "&cloy=$cloy")
                    if(response.status.isSuccess()) {
                        togging = response.bodyAsText()
                    }
                } catch (e: ClientRequestException) {
                    toFoxPrint("Client error: ${e.response.status}")
                } catch (e: Exception) {
                    toFoxPrint("Error: ${e.message}")
                } finally {
                    client.close()
                }
            }
        }
    }
}