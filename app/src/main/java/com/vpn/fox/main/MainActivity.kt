package com.vpn.fox.main

import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import com.github.shadowsocks.bg.BaseService
import com.vpn.fox.R
import com.vpn.fox.VpnApp.Companion.toFoxPrint
import com.vpn.fox.base.BaseActivity
import com.vpn.fox.main.ServerUI.Server
import com.vpn.fox.main.VpnUI.Vpn
//import com.vpn.fox.main.SpeedUI.Speed
import com.vpn.fox.ui.CenterColumn
import com.vpn.fox.ui.Glide
import com.vpn.fox.ui.Text
import com.vpn.fox.ui.TextWhite
import com.vpn.fox.ui.theme.IconTint
import com.vpn.fox.utils.FoxAdManager.IVBACKSV_KEY
import com.vpn.fox.utils.FoxAdManager.foxAdEnable
import com.vpn.fox.utils.FoxAdManager.foxLoad
import com.vpn.fox.utils.FoxAdManager.foxPb
import com.vpn.fox.utils.FoxAdManager.foxShow
import com.vpn.fox.utils.FoxFbManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {
    companion object {
        const val TYPE_VPN = "vpn"
        const val TYPE_SERVER = "server"
//        const val TYPE_SPEED = "speed"
//        const val TYPE_STATISTICS = "Statistics"
    }

    private val model by viewModels<VpnManager>()
//    private val speed by viewModels<SpeedManager>()

    override fun initData() {
        model.init(this)
    }

    override fun onBackPressed() {
        if (model.state.value == BaseService.State.Connecting) return
//        super.onBackPressed()
        moveTaskToBack(true)
        if (false)  super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        model.doRegion(Locale.getDefault().country)
    }

    @Composable
    override fun Content(padding: PaddingValues) {
        val bottom = padding.calculateBottomPadding()
        val permission by model.nfDialog.observeAsState()
        val net by model.netDialog.observeAsState()
        val limit by model.limitDialog.observeAsState()
        val type by model.type.observeAsState()
        val noServer by model.noServerDataDialog.observeAsState()
        val adLoading by model.adLoadingDialog.observeAsState()
        CenterColumn(Modifier.padding(top = padding.calculateTopPadding())) {
            Box(
                Modifier
                    .weight(1F)
                    .fillMaxWidth()
            ) {
                when (type) {
                    TYPE_VPN -> Vpn(model) {
                        model.type.postValue(TYPE_SERVER)
                    }

                    TYPE_SERVER -> Server(model) {
                        model.type.postValue(TYPE_VPN)
                        model.startVpn()
                    }

//                    TYPE_SPEED -> Speed(model, speed)
//                    TYPE_STATISTICS -> {}
                }
                if (permission == true) {
                    PermissionUI()
                }
                if (net == true) {
                    NoNetUI()
                }
                if (limit == true) {
                    LimitUI()
                }
                if (noServer == true) {
                    NoServerDataUI()
                }
                if (adLoading == true) {
                    AdLoadingUI()
                }
            }

            Row(
                Modifier
                    .height(84.dp + bottom)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Brush.verticalGradient(arrayListOf(Color(0xFF2A313F), Color(0xFF141927))))
                    .padding(bottom = bottom, start = 50.dp, end = 50.dp)
                    .fillMaxWidth(),
                Arrangement.SpaceAround, Alignment.CenterVertically
            ) {
                BottomTab(R.drawable.main_vpn, TYPE_VPN, type) {
                    lifecycleScope.launch {
                        if (type == TYPE_SERVER && !IVBACKSV_KEY.foxPb()) {
                            IVBACKSV_KEY.foxLoad()
                            model.adLoadingDialog.postValue(true)
                            for (i in 0 until 50) {
                                delay(100)
                                if (IVBACKSV_KEY.foxAdEnable() && i >= 10) break
                            }
                            if (IVBACKSV_KEY.foxAdEnable()) {
                                model.adLoadingDialog.postValue(false)
                                IVBACKSV_KEY.foxShow(this@MainActivity, dismiss = {
                                    model.type.postValue(it)
                                })
                            } else {
                                model.adLoadingDialog.postValue(false)
                                model.type.postValue(it)
                            }
                        } else {
                            model.adLoadingDialog.postValue(false)
                            model.type.postValue(it)
                        }
                    }
                }
                BottomTab(R.drawable.main_server, TYPE_SERVER, type) {
                    model.type.postValue(it)
                    IVBACKSV_KEY.foxLoad()
                }
//                BottomTab(R.drawable.main_speed, TYPE_SPEED, type) { model.type.postValue(it) }
//                BottomTab(R.drawable.main_statistics, TYPE_STATISTICS, type) { model.type.postValue(it) }
            }
        }
    }

    @Composable
    private fun BottomTab(icon: Int, type: String, currentType: String?, block: (String) -> Unit) {
        Glide(
            icon,
            Modifier
                .size(40.dp)
                .clickable {
                    if (model.state.value == BaseService.State.Connecting ||
                        model.state.value == BaseService.State.Stopping
                    ) return@clickable
                    block(type)
                }, colorFilter = if (currentType == type) ColorFilter.tint(IconTint) else null
        )
    }

    @Composable
    private fun PermissionUI() {
        AlertDialog(
            onDismissRequest = {
                model.nfDialog.postValue(false)
                model.startVpn()
            },
            confirmButton = {
                Text("OK", fontSize = 14, Modifier.clickable {
                    model.nfDialog.postValue(false)
                    model.startVpn()
                })
            },
            title = {
                Text("No nf permission", fontSize = 14)
            },
            properties = DialogProperties(dismissOnBackPress = false, false)
        )
    }

    @Composable
    private fun NoNetUI() {
        AlertDialog(
            onDismissRequest = {
                model.netDialog.postValue(false)
            },
            confirmButton = {
                Text("OK", fontSize = 14, Modifier.clickable {
                    model.netDialog.postValue(false)
                })
            },
            title = {
                Text(
                    "Network request timed out... \n" +
                            "Verify that the network is connected", fontSize = 14,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            properties = DialogProperties(dismissOnBackPress = false, false)
        )
    }

    @Composable
    private fun NoServerDataUI() {
        AlertDialog(
            onDismissRequest = {
                model.noServerDataDialog.postValue(false)
            },
            confirmButton = {
                Text("OK", fontSize = 14, Modifier.clickable {
                    model.noServerDataDialog.postValue(false)
                })
            },
            title = {
                Text(
                    "No data..,", fontSize = 14,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            properties = DialogProperties(dismissOnBackPress = true, true)
        )
    }

    @Composable
    private fun LimitUI() {
        AlertDialog(
            onDismissRequest = {
                exitProcess(0)
            },
            confirmButton = {
                Text("OK", fontSize = 14, Modifier.clickable {
                    exitProcess(0)
                })
            },
            title = {
                Text(
                    "Sorry, due to the policy reason, this service is not available in your country", fontSize = 14,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            properties = DialogProperties(dismissOnBackPress = false, false)
        )
    }

    @Composable
    private fun AdLoadingUI() {
        var target by remember { mutableFloatStateOf(0F) }
        val rotation by animateFloatAsState(
            target,
            infiniteRepeatable(
                animation = tween(1400, 0, easing = LinearEasing)
            ),
            label = "start rotation"
        )
        CenterColumn(
            Modifier
                .fillMaxSize().background(color = Color(0x33000000)),
        ) {
            Spacer(Modifier.weight(1f))
            Glide(
                R.drawable.ic_loading,
                Modifier
                    .width(50.dp)
                    .aspectRatio(36F/37F)
                    .rotate(rotation)
            )
            Spacer(Modifier.height(33.dp))
            Text(
                "Ad coming soon...", fontSize = 12,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            Spacer(Modifier.weight(1f))
        }
        LaunchedEffect(Unit) {
            target = 360F
        }
    }

    override fun onStart() {
        super.onStart()
        model.onStart()
    }

    override fun onStop() {
        model.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        model.onDestroy(this)
    }
}
