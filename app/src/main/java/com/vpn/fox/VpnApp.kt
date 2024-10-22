package com.vpn.fox

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.github.shadowsocks.Core
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.vpn.fox.main.MainActivity
import com.vpn.fox.utils.FoxAdManager.foxInitMobileAdLib
import com.vpn.fox.utils.FoxFbManager.foxLoadFb
import com.vpn.fox.utils.NetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VpnApp : Application(), Application.ActivityLifecycleCallbacks {
    companion object {
        lateinit var app: VpnApp
        val net = MutableLiveData(false)

        fun toFoxPrint(message: String) {
            Log.e("fox", message)
        }

        var foxBehindTime: Long = 0
        var foxBehindJob: Job? = null
        var foxForegroundList: MutableList<String> = mutableListOf()
        var foxAdList: MutableList<AdActivity> = mutableListOf()
    }

    private fun vpnToDoInit(todo: () -> Unit, noChange: (String) -> Unit, toLoadFb: () -> Unit) {
        val currentProcessId = android.os.Process.myPid()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfo = activityManager.runningAppProcesses.find { it.pid == currentProcessId }
        when(processInfo?.processName == packageName) {
            false -> noChange.invoke("no change")
            true -> {
                todo.invoke()
                toLoadFb.invoke()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        Core.init(this, MainActivity::class)
        toFoxPrint("app onCreate-------")
        vpnToDoInit(todo = {
            Firebase.initialize(this)
            MobileAds.initialize(this) {
                foxInitMobileAdLib = true
            }
            registerActivityLifecycleCallbacks(this)
            netState()
            NetManager.region()
        }, noChange = {
        }, toLoadFb = {
            CoroutineScope(Dispatchers.IO).launch {
                true.foxLoadFb()
            }
        })
    }

    private fun netState() {
        getSystemService(ConnectivityManager::class.java).requestNetwork(NetworkRequest.Builder().build(),
            object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    net.postValue(true)
                }

                override fun onLost(network: Network) {
                    net.postValue(false)
                }
            })
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        when(activity) {
            is AdActivity -> {
                foxAdList.add(activity)
            }
            else -> {}
        }
    }

    override fun onActivityStarted(activity: Activity) {
        foxForegroundList.add(activity.localClassName)
        foxBehindJob?.cancel()
        when(foxBehindTime >= 100 && foxForegroundList.size == 1) {
            true -> {
                CoroutineScope(Dispatchers.IO).launch {
                    true.foxLoadFb()
                }
                activity.startActivity(Intent(activity, StartActivity::class.java))
            }
            false -> {}
        }
        foxBehindTime = 0
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        when(foxForegroundList.contains(activity.localClassName)) {
            false -> {}
            true -> {
                foxForegroundList.remove(activity.localClassName)
            }
        }
        if (foxForegroundList.isEmpty()) {
            foxBehindJob?.cancel()
            foxBehindJob = CoroutineScope(Dispatchers.IO).launch {
                while (foxForegroundList.isEmpty()) {
                    delay(30)
                    foxBehindTime += 1
                    when(foxBehindTime >= 100) {
                        true -> {
                            if (foxAdList.isNotEmpty()) {
                                foxAdList.forEach {
                                    it.finish()
                                }
                            }
                        }
                        false -> {}
                    }
                }
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        if (foxAdList.contains(activity))
        foxAdList.remove(activity)
    }

}