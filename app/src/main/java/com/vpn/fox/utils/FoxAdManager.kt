package com.vpn.fox.utils

import com.github.shadowsocks.Core
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.vpn.fox.base.BaseActivity
import com.vpn.fox.main.FoxAdPresenter

object FoxAdManager {
    var foxInitMobileAdLib: Boolean = false //是否已经初始化过广告库

    fun BaseActivity.showConsentInfoUpdateDialog(start: (Boolean) -> Unit) {
        val consentInformation: ConsentInformation by lazy {
            UserMessagingPlatform.getConsentInformation(this)
        }
        if (!foxInitMobileAdLib) MobileAds.initialize(this)
        when (DataManager.consentDebugSettingEnable) {
            true -> {
                start.invoke(true)
            }

            false -> {
                val debugSettings = ConsentDebugSettings.Builder(this)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .addTestDeviceHashedId("6EEF561A3820F4CA78CE200330CAB1D8")
                    .build()

                val params = ConsentRequestParameters
                    .Builder()
                    .setConsentDebugSettings(debugSettings)
                    .build()
                consentInformation.requestConsentInfoUpdate(
                    this,
                    params,
                    {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            this
                        ) {
                            DataManager.consentDebugSettingEnable = true
                            start.invoke(true)
                        }
                    }
                ) {
                    DataManager.consentDebugSettingEnable = true
                    start.invoke(true)
                }
            }
        }
    }

    /**
     * 获取当前ip
     */
    fun getFoxHost(): String {
        return when (Core.state.canStop) {
            false -> "fox_not_on"
            true -> ProfileManager.getProfile(DataStore.profileId)?.host ?: ""
        }
    }

    const val ONP_KEY = "onp"
    const val IVCNT_KEY = "ivcnt"
    const val IVBACKSV_KEY = "ivbacksv"
    const val IVBACKRST_KEY = "ivbackrst"

    var onp: FoxAdPresenter = FoxAdPresenter(ONP_KEY, false)
    var onp1: FoxAdPresenter = FoxAdPresenter(ONP_KEY, true)

    var ivcnt: FoxAdPresenter = FoxAdPresenter(IVCNT_KEY, false)
    var ivcnt1: FoxAdPresenter = FoxAdPresenter(IVCNT_KEY, true)

    var ivbacksv: FoxAdPresenter = FoxAdPresenter(IVBACKSV_KEY, false)
    var ivbacksv1: FoxAdPresenter = FoxAdPresenter(IVBACKSV_KEY, true)

    var ivbackrst: FoxAdPresenter = FoxAdPresenter(IVBACKRST_KEY, false)
    var ivbackrst1: FoxAdPresenter = FoxAdPresenter(IVBACKRST_KEY, true)

    fun String.foxAdEnable(): Boolean {
        val connected = Core.state.canStop
        return when (this) {
            ONP_KEY -> {
                if (!connected) onp.foxAdEnable()
                else onp1.foxAdEnable()
            }

            IVCNT_KEY -> {
                if (!connected) ivcnt.foxAdEnable()
                else ivcnt1.foxAdEnable()
            }

            IVBACKSV_KEY -> {
                if (!connected) ivbacksv.foxAdEnable()
                else ivbacksv1.foxAdEnable()
            }

            IVBACKRST_KEY -> {
                if (!connected) ivbackrst.foxAdEnable()
                else ivbackrst1.foxAdEnable()
            }

            else -> false
        }
    }

    fun String.foxPb(): Boolean {
        val connected = Core.state.canStop
        return when (this) {
            ONP_KEY -> {
                if (!connected) onp.foxAdPb()
                else onp1.foxAdPb()
            }

            IVCNT_KEY -> {
                if (!connected) ivcnt.foxAdPb()
                else ivcnt1.foxAdPb()
            }

            IVBACKSV_KEY -> {
                if (!connected) ivbacksv.foxAdPb()
                else ivbacksv1.foxAdPb()
            }

            IVBACKRST_KEY -> {
                if (!connected) ivbackrst.foxAdPb()
                else ivbackrst1.foxAdPb()
            }

            else -> true
        }
    }

    fun String.foxLoad() {
        val connected = Core.state.canStop
        when (this) {
            ONP_KEY -> {
                if (!connected) onp.foxLoadOpen(true)
                else onp1.foxLoadOpen(true)
            }

            IVCNT_KEY -> {
                if (!connected) ivcnt.foxLoadInter()
                else ivcnt1.foxLoadInter()
            }

            IVBACKSV_KEY -> {
                if (!connected) ivbacksv.foxLoadInter()
                else ivbacksv1.foxLoadInter()
            }

            IVBACKRST_KEY -> {
                if (!connected) ivbackrst.foxLoadInter()
                else ivbackrst1.foxLoadInter()
            }
        }
    }

    fun String.foxShow(activity: BaseActivity, dismiss: () -> Unit) {
        val connected = Core.state.canStop
        when (this) {
            ONP_KEY -> {
                if (!connected) onp.foxShowOpInt(activity, dismiss)
                else onp1.foxShowOpInt(activity, dismiss)
            }

            IVCNT_KEY -> {
                if (!connected) ivcnt.foxShowOpInt(activity, dismiss)
                else ivcnt1.foxShowOpInt(activity, dismiss)
            }

            IVBACKSV_KEY -> {
                if (!connected) ivbacksv.foxShowOpInt(activity, dismiss)
                else ivbacksv1.foxShowOpInt(activity, dismiss)
            }

            IVBACKRST_KEY -> {
                if (!connected) ivbackrst.foxShowOpInt(activity, dismiss)
                else ivbackrst1.foxShowOpInt(activity, dismiss)
            }
        }
    }
}