package com.vpn.fox.utils

import android.util.Base64
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.vpn.fox.VpnApp
import com.vpn.fox.utils.Constant.Companion.LOCAL_AIEC
import com.vpn.fox.utils.Constant.Companion.LOCAL_DAON
import com.vpn.fox.utils.Constant.Companion.LOCAL_FM_ADS
import com.vpn.fox.utils.Constant.Companion.LOCAL_PEIHR
import com.vpn.fox.utils.DataManager.aiec
import com.vpn.fox.utils.DataManager.daon
import com.vpn.fox.utils.DataManager.fm_ads
import com.vpn.fox.utils.DataManager.peihr
import com.vpn.fox.utils.FoxAdManager.IVBACKRST_KEY
import com.vpn.fox.utils.FoxAdManager.IVBACKSV_KEY
import com.vpn.fox.utils.FoxAdManager.IVCNT_KEY
import com.vpn.fox.utils.FoxAdManager.ONP_KEY
import org.json.JSONArray
import org.json.JSONObject

object FoxFbManager {
    //参数为1，使用cloak规则
    //命中状态屏蔽所有插屏+首页原生
    //未命中状态不屏蔽
    //参数为2，不起用屏蔽规则
    //默认1
    var rria: String = "1"
    var usvt: String = "" //fb id

    var foxSmartServers = arrayListOf<ServerItem>() //smart服务器配置
    var foxServers = arrayListOf<ServerItem>() //服务器配置

    var foxFbDataLoadedFinished: Boolean = false //是否获取到firebase的数据


    fun parseFmAds(result: String) {
        if (result.isEmpty()) return
        runCatching {
            JSONObject(String(Base64.decode(result, Base64.DEFAULT)))
        }.onSuccess {
            val insver = it.optJSONObject("insver")
            val unsver = it.optJSONObject("unsver")
            FoxAdManager.onp.updateFoxId(unsver?.optString(ONP_KEY)?:"")
            FoxAdManager.ivcnt.updateFoxId(unsver?.optString(IVCNT_KEY)?:"")
            FoxAdManager.ivbacksv.updateFoxId(unsver?.optString(IVBACKSV_KEY)?:"")
            FoxAdManager.ivbackrst.updateFoxId(unsver?.optString(IVBACKRST_KEY)?:"")

            FoxAdManager.onp1.updateFoxId(insver?.optString(ONP_KEY)?:"")
            FoxAdManager.ivcnt1.updateFoxId(insver?.optString(IVCNT_KEY)?:"")
            FoxAdManager.ivbacksv1.updateFoxId(insver?.optString(IVBACKSV_KEY)?:"")
            FoxAdManager.ivbackrst1.updateFoxId(insver?.optString(IVBACKRST_KEY)?:"")
        }
    }

    fun parseDaon(result: String) {
        if (result.isEmpty()) return
        runCatching {
            JSONObject(String(Base64.decode(result, Base64.DEFAULT)))
        }.onSuccess {
            rria = it.optString("rria").ifEmpty { "1" }
            usvt = it.optString("usvt")
        }
    }

    fun parsePeiHr(result: String) {
        if (result.isEmpty()) return
        runCatching {
            JSONArray(String(Base64.decode(result, Base64.DEFAULT)))
        }.onSuccess { array ->
            if (array.length() == 0) return
            try {
                val items = (0 until array.length()).map { array[it] as JSONObject }
                foxSmartServers.clear()
                items.forEach {
                    foxSmartServers.add(
                        ServerItem(
                            ip = it.optString("hide"),
                            method = it.optString("gho"),
                            pwd = it.optString("otaa"),
                            port = it.optInt("wdte"),
                            city = it.optString("yeg"),
                            country = it.optString("ilrr")
                        )
                    )
                }
            } catch (e: Exception) {}
        }
    }

    fun parseAiec(result: String) {
        if (result.isEmpty()) return
        runCatching {
            JSONArray(String(Base64.decode(result, Base64.DEFAULT)))
        }.onSuccess { array ->
            if (array.length() == 0) return
            try {
                val items = (0 until array.length()).map { array[it] as JSONObject }
                foxServers.clear()
                items.forEach {
                    foxServers.add(
                        ServerItem(
                            ip = it.optString("hide"),
                            method = it.optString("gho"),
                            pwd = it.optString("otaa"),
                            port = it.optInt("wdte"),
                            city = it.optString("yeg"),
                            country = it.optString("ilrr")
                        )
                    )
                }
            } catch (e: Exception) {}
        }
    }

    fun Boolean.foxLoadFb() {
        parseFmAds(fm_ads.ifEmpty { LOCAL_FM_ADS })
        parseDaon(daon.ifEmpty { LOCAL_DAON })
        parsePeiHr(peihr.ifEmpty { LOCAL_PEIHR })
        parseAiec(aiec.ifEmpty { LOCAL_AIEC })
        try {
            Firebase.remoteConfig.apply {
                fetchAndActivate()
                    .addOnCompleteListener {
                        this.getString("fm_ads").also {
                            if (it.isNotEmpty()) {
                                fm_ads = it
                            }
                            val result = fm_ads.ifEmpty { LOCAL_FM_ADS }
                            parseFmAds(result)
                        }
                        this.getString("daon").also {
                            if (it.isNotEmpty()) {
                                daon = it
                            }
                            val result = daon.ifEmpty { LOCAL_DAON }
                            parseDaon(result)
                            if (usvt.isNotEmpty()) {
                                FacebookSdk.setApplicationId(usvt)
                                FacebookSdk.sdkInitialize(VpnApp.app.applicationContext)
                                AppEventsLogger.activateApp(VpnApp.app)
                            }
                        }
                        this.getString("peihr").also {
                            if (it.isNotEmpty()) {
                                peihr = it
                            }
                            val result = peihr.ifEmpty { LOCAL_PEIHR }
                            parsePeiHr(result)
                        }
                        this.getString("aiec").also {
                            if (it.isNotEmpty()) {
                                aiec = it
                            }
                            val result = aiec.ifEmpty { LOCAL_AIEC }
                            parseAiec(result)
                        }
                        foxFbDataLoadedFinished = true
                    }
                    .addOnFailureListener {
                        if (this@foxLoadFb) {
                            //加载失败，则再加载一次
                            false.foxLoadFb()
                        }
                    }
            }
        } catch (e: Exception) {
            if (this@foxLoadFb) {
                //加载失败，则再加载一次
                false.foxLoadFb()
            }
        }
    }
}