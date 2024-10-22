package com.vpn.fox.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.RemoteException
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.utils.StartService
import com.vpn.fox.R
import com.vpn.fox.ResultActivity
import com.vpn.fox.SettingsActivity
import com.vpn.fox.VpnApp
import com.vpn.fox.VpnApp.Companion.toFoxPrint
import com.vpn.fox.utils.DataManager
import com.vpn.fox.utils.FoxAdManager.IVBACKRST_KEY
import com.vpn.fox.utils.FoxAdManager.IVCNT_KEY
import com.vpn.fox.utils.FoxAdManager.foxAdEnable
import com.vpn.fox.utils.FoxAdManager.foxLoad
import com.vpn.fox.utils.FoxAdManager.foxPb
import com.vpn.fox.utils.FoxAdManager.foxShow
import com.vpn.fox.utils.FoxFbManager
import com.vpn.fox.utils.FoxFbManager.foxServers
import com.vpn.fox.utils.FoxFbManager.foxSmartServers
import com.vpn.fox.utils.NetManager
import com.vpn.fox.utils.ServerItem
import com.vpn.fox.utils.VpnServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

class VpnManager : ViewModel(), ShadowsocksConnection.Callback {
    private lateinit var launcher: ActivityResultLauncher<Void?>
    private lateinit var permission: ActivityResultLauncher<String>
    private lateinit var result: ActivityResultLauncher<Intent>

    @SuppressLint("StaticFieldLeak")
    private lateinit var activity: MainActivity
    private val connection = ShadowsocksConnection(true)
    private var smartServers = arrayListOf<ServerItem>()
    private var connectingJob: Job? = null
    private var disconnectingJob: Job? = null
    private var stop = false

    val state = MutableLiveData<BaseService.State>()
    val nfDialog = MutableLiveData<Boolean>()
    val netDialog = MutableLiveData<Boolean>()
    val noServerDataDialog = MutableLiveData<Boolean>()
    val adLoadingDialog = MutableLiveData<Boolean>()
    val limitDialog = MutableLiveData<Boolean>()
    val currentServer = MutableLiveData<VpnServer>()
    val servers = MutableLiveData<List<VpnServer>>()
    val type = MutableLiveData(MainActivity.TYPE_VPN)
    var selectServer: ServerItem? = null

    lateinit var connectServer: ServerItem

    fun init(activity: MainActivity) {
        this.activity = activity
        initServers()
        launcher = activity.registerForActivityResult(StartService()) {
            if (it) start()
        }
        permission = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) launcher.launch(null) else if (activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS).not()) {
                nfDialog.postValue(true)
            }
        }
        result = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data?.getBooleanExtra("jump_server", false) == true) {
                type.postValue(MainActivity.TYPE_SERVER)
            }/* else if (it.data?.getBooleanExtra("jump_speed", false) == true) {
                type.postValue(MainActivity.TYPE_SPEED)
            }*/
        }
        connection.connect(activity, this)
        NetManager.netRegion.observe(activity) {
            doRegion(it)
        }
        doRegion(Locale.getDefault().country)
    }


    fun onStart() {
        connection.bandwidthTimeout = 500
    }

    fun onStop() {
        stop = false
        connection.bandwidthTimeout = 0
        if (state.value == BaseService.State.Stopping) {
            disconnectingJob?.cancel()
            state.postValue(BaseService.State.Connected)
            if (DataManager.connectIp.isEmpty()) {
                currentServer.value = servers.value?.first()
                selectServer = null
            } else {
                servers.value?.forEach { vpnServer ->
                    vpnServer.servers.forEach {
                        if (DataManager.connectIp == it.ip) {
                            selectServer = it
                            currentServer.value = vpnServer
                        }
                    }
                }
            }
        } else if (state.value == BaseService.State.Connecting) {
            connectingJob?.cancel()
            state.postValue(BaseService.State.Stopped)
        }
    }

    fun onDestroy(activity: MainActivity) {
        connection.disconnect(activity)
    }

    fun startVpn() {
        if (state.value == BaseService.State.Stopping ||
            state.value == BaseService.State.Connecting
        ) return
        val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }
        val enable = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        if (!enable) {
            netDialog.postValue(true)
        } else if(foxServers.isEmpty()) {
            noServerDataDialog.postValue(true)
        } else {
            checkPermission()
        }
    }

    fun stopVpn() {
        if (state.value == BaseService.State.Stopping ||
            state.value == BaseService.State.Connecting
        ) return
        stop = true
        disconnectingJob?.cancel()
        disconnectingJob = CoroutineScope(Dispatchers.Main).launch {
            state.postValue(BaseService.State.Stopping)
            delay(1000L)

            IVCNT_KEY.foxLoad()
            for (i in 0 until 90) {
                delay(100)
                if (IVCNT_KEY.foxAdEnable()) break
            }
            if (IVCNT_KEY.foxAdEnable()) {
                IVCNT_KEY.foxShow(activity, dismiss = {
                    Core.stopService()
                })
            } else {
                Core.stopService()
            }
        }
    }

    fun doRegion(region: String?) {
        region ?: return
        if (region.uppercase() in "CN,HK,MO,IR") {
            limitDialog.postValue(true)
        }
    }

    fun startSettings() {
        activity.startActivity(Intent(activity, SettingsActivity::class.java))
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (DataManager.requestNf
                && ContextCompat.checkSelfPermission(VpnApp.app, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    launcher.launch(null)
                }
            } else if (DataManager.requestNf.not()) {
                DataManager.requestNf = true
                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                launcher.launch(null)
            }
        } else {
            launcher.launch(null)
        }
    }

    private fun initServers() {
        val all = buildAllServer()
        smartServers = foxSmartServers
        if (DataManager.connectIp.isEmpty()) {
            currentServer.value = all.first()
        } else {
            all.forEach { vpnServer ->
                vpnServer.servers.forEach {
                    if (DataManager.connectIp == it.ip) {
                        connectServer = it
                        selectServer = it
                        currentServer.value = vpnServer
                    }
                }
            }
        }
        currentServer.value?.select?.value = true
        servers.value = all
    }

    private fun buildAllServer(): ArrayList<VpnServer> {
        val list: ArrayList<VpnServer> = arrayListOf()
        if (foxServers.isNotEmpty()) {
            val map: MutableMap<String, ArrayList<ServerItem>> = mutableMapOf()
            foxServers.forEach {
                if (map.containsKey(it.country)) {
                    (map[it.country]?: arrayListOf()).add(it)
                } else {
                    map.put(it.country, arrayListOf(it))
                }
            }
            if (map.isNotEmpty()) {
                list.add(VpnServer(R.drawable.ic_server, "Auto", arrayListOf()))
                map.forEach { t, u ->
                    val expand = list.size == 1
                    val vpnServer: VpnServer = VpnServer(
                        t.foxCountry2Icon(),
                        t,
                        u,
                        mutableStateOf(expand)
                    )
                    list.add(vpnServer)
                }
            }
        }
        return list
    }

    private fun String.foxCountry2Icon(): Int {
        val country = replace("\\s".toRegex(), "").toLowerCase()
        return when (country) {
            "arabia" -> R.drawable.flag_arabia
            "australia" -> R.drawable.flag_australia
            "france" -> R.drawable.flag_france
            "germany" -> R.drawable.flag_germany
            "japan" -> R.drawable.flag_japan
            "koreasouth" -> R.drawable.flag_korea
            "singapore" -> R.drawable.flag_singapore
            "switzerland" -> R.drawable.flag_switzerland
            "unitedkingdom" -> R.drawable.flag_unitedkingdom
            "unitedstates" -> R.drawable.flag_unitedstates
            "austria" -> R.drawable.flg_austria
            else -> R.drawable.ic_server
        }
    }

    private fun start() {
        connectingJob?.cancel()
        connectingJob = CoroutineScope(Dispatchers.IO).launch {
            if (state.value == BaseService.State.Connected) {
                state.postValue(BaseService.State.Stopping)
                delay(500L)
                Core.stopService()
                delay(200L)
            }
            state.postValue(BaseService.State.Connecting)
            withTimeoutOrNull(1500L) {
                launch { delay(1000L) }
                determineServer()
            }

            Core.startService()
        }
    }

    private fun determineServer() {
        connectServer = selectServer ?: smartServers.shuffled().random()
        Core.updateProfile(connectServer.ip, connectServer.city, connectServer.method, connectServer.pwd, connectServer.port)
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        changeState(state)
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        changeState(
            try {
                BaseService.State.entries[service.state]
            } catch (_: RemoteException) {
                BaseService.State.Idle
            }
        )
    }

    override fun onServiceDisconnected() {
        changeState(BaseService.State.Idle)
    }

    private fun changeState(state: BaseService.State) {
        if (Core.state != state) {
            Core.state = state
            runCatching {
                if (state.canStop) {
                    IVBACKRST_KEY.foxLoad()
                    IVCNT_KEY.foxPb().apply {
                        when(this) {
                            true -> {
                                this@VpnManager.state.postValue(state)
                                Core.connectTime = System.currentTimeMillis()
                                DataManager.connectIp = connectServer.ip
                                activity.startActivity(Intent(activity, ResultActivity::class.java))
                            }
                            false -> {
                                activity.lifecycleScope.launch {
                                    IVCNT_KEY.foxLoad()
                                    for (i in 0 until 90) {
                                        delay(100)
                                        if (IVCNT_KEY.foxAdEnable()) break
                                    }
                                    if (IVCNT_KEY.foxAdEnable()) {
                                        IVCNT_KEY.foxShow(activity, dismiss = {
                                            activity.lifecycleScope.launch {
                                                IVCNT_KEY.foxLoad()
                                                this@VpnManager.state.postValue(state)
                                                Core.connectTime = System.currentTimeMillis()
                                                DataManager.connectIp = connectServer.ip
                                                delay(300)
                                                if (activity.baseFoxVisible) activity.startActivity(Intent(activity, ResultActivity::class.java))
                                            }
                                        })
                                    } else {
                                        this@VpnManager.state.postValue(state)
                                        Core.connectTime = System.currentTimeMillis()
                                        DataManager.connectIp = connectServer.ip
                                        if (activity.baseFoxVisible) activity.startActivity(Intent(activity, ResultActivity::class.java))
                                    }
                                }
                            }
                        }
                    }

                } else if (state == BaseService.State.Stopped && stop) {
                    stop = false
                    IVBACKRST_KEY.foxLoad()
                    when(activity.baseFoxVisible) {
                        false -> {}
                        true -> {
                            result.launch(Intent(activity, ResultActivity::class.java))
                        }
                    }
                } else {
                    this.state.postValue(state)
                }
            }
        } else {
            this.state.postValue(state)
        }
    }
}