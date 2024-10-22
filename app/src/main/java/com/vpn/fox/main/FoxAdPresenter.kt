package com.vpn.fox.main

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.vpn.fox.VpnApp
import com.vpn.fox.VpnApp.Companion.toFoxPrint
import com.vpn.fox.base.BaseActivity
import com.vpn.fox.utils.DataManager.togging
import com.vpn.fox.utils.FoxAdManager.IVBACKRST_KEY
import com.vpn.fox.utils.FoxAdManager.IVBACKSV_KEY
import com.vpn.fox.utils.FoxAdManager.IVCNT_KEY
import com.vpn.fox.utils.FoxAdManager.getFoxHost
import com.vpn.fox.utils.FoxFbManager.rria

class FoxAdPresenter {
    private var id: String = ""
    private var ading: Boolean = false
    private var placeKey: String = ""
    private var connected: Boolean = false
    private var ad: Any? = null
    private var loadSys: Long = 0
    private var loadHost: String = ""

    constructor(name: String, connect: Boolean) {
        this.placeKey = name
        this.connected = connect
    }

    fun updateFoxId(foxId: String) {
        this.id = foxId
    }

    fun foxAdEnable(): Boolean {
        return ad != null && System.currentTimeMillis() - loadSys < 60 * 60000 && loadHost == getFoxHost()
    }

    private fun foxLoadEnable(): Boolean {
        if (foxAdPb()) return false
        if (ading) return false
        if (id.isEmpty()) return false
        if (foxAdEnable()) return false
        return true
    }

    private fun foxShowEnable(activity: BaseActivity): Boolean {
        if (foxAdPb()) return false
        if (!foxAdEnable()) return false
        if (!activity.baseFoxVisible) return false
        return true
    }

    //是否屏蔽
    fun foxAdPb(): Boolean {
        return togging != "dairylea" && rria == "1" && (arrayListOf(IVCNT_KEY, IVBACKSV_KEY, IVBACKRST_KEY).any { it == placeKey })
    }

    fun foxLoadOpen(again: Boolean) {
        foxLoadEnable().apply {
            when(this) {
                false -> {}
                true -> {
                    ading = true
                    val host = getFoxHost()
                    toFoxPrint("load $placeKey---connect:$connected---id---$id-----")
                    AppOpenAd.load(
                        VpnApp.app,
                        id,
                        AdRequest.Builder().build(),
                        object : AppOpenAd.AppOpenAdLoadCallback() {
                            override fun onAdLoaded(openAd: AppOpenAd) {
                                toFoxPrint("load $placeKey---connect:$connected---id---$id-----success---")
                                ad = openAd
                                loadHost = host
                                loadSys = System.currentTimeMillis()
                                ading = false
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                toFoxPrint("load $placeKey---connect:$connected---id---$id-----failed---${loadAdError.message}")
                                ading = false
                                when(again) {
                                    true -> foxLoadOpen(false)
                                    false -> {}
                                }
                            }
                        })
                }
            }
        }
    }

    fun foxLoadInter() {
        foxLoadEnable().apply {
            when (this) {
                false -> {}
                true -> {
                    ading = true
                    val host = getFoxHost()
                    toFoxPrint("load $placeKey---connect:$connected---id---$id-----")
                    InterstitialAd.load(
                        VpnApp.app,
                        id,
                        AdRequest.Builder().build(),
                        object : InterstitialAdLoadCallback() {
                            override fun onAdLoaded(interAd: InterstitialAd) {
                                toFoxPrint("load $placeKey---connect:$connected---id---$id-----success---")
                                ad = interAd
                                loadHost = host
                                loadSys = System.currentTimeMillis()
                                ading = false
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                toFoxPrint("load $placeKey---connect:$connected---id---$id-----failed---${loadAdError.message}")
                                ading = false
                            }
                        })
                }
            }
        }
    }

    fun foxShowOpInt(activity: BaseActivity, dismiss: () -> Unit) {
        foxShowEnable(activity).apply {
            when(this) {
                false -> { dismiss.invoke() }
                true -> {
                    val callback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            toFoxPrint("show $placeKey---connect:$connected----close---")
                            dismiss.invoke()
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            toFoxPrint("show $placeKey---connect:$connected----failed---${p0.message}")
                            ad = null
                            dismiss.invoke()
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            toFoxPrint("show $placeKey---connect:$connected----success---")
                            ad = null
                        }
                    }
                    when(ad) {
                        is AppOpenAd -> {
                            (ad as AppOpenAd).fullScreenContentCallback = callback
                            (ad as AppOpenAd).show(activity)
                        }
                        is InterstitialAd -> {
                            (ad as InterstitialAd).fullScreenContentCallback = callback
                            (ad as InterstitialAd).show(activity)
                        }
                    }
                }
            }
        }
    }

}