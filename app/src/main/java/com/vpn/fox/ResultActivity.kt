package com.vpn.fox

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.github.shadowsocks.Core
import com.vpn.fox.base.BaseActivity
import com.vpn.fox.ui.CenterColumn
import com.vpn.fox.ui.CenterRow
import com.vpn.fox.ui.Glide
import com.vpn.fox.ui.Text
import com.vpn.fox.ui.TextWhite
import com.vpn.fox.utils.DataManager
import com.vpn.fox.utils.FoxAdManager.IVBACKRST_KEY
import com.vpn.fox.utils.FoxAdManager.IVBACKSV_KEY
import com.vpn.fox.utils.FoxAdManager.foxAdEnable
import com.vpn.fox.utils.FoxAdManager.foxLoad
import com.vpn.fox.utils.FoxAdManager.foxPb
import com.vpn.fox.utils.FoxAdManager.foxShow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

class ResultActivity : BaseActivity() {
    private val current = MutableLiveData(System.currentTimeMillis())
    private val adLoadingDialog = MutableLiveData<Boolean>()

    @Composable
    override fun Content(padding: PaddingValues) {
        val adLoading by adLoadingDialog.observeAsState()
        BackHandler {
            lifecycleScope.launch {
                if (adLoadingDialog.value == true) return@launch
                if (!IVBACKRST_KEY.foxPb()) {
                    adLoadingDialog.postValue(true)
                    IVBACKRST_KEY.foxLoad()
                    for (i in 0 until 50) {
                        delay(100)
                        if (IVBACKRST_KEY.foxAdEnable() && i >= 10) break
                    }
                    if (IVBACKRST_KEY.foxAdEnable()) {
                        adLoadingDialog.postValue(false)
                        IVBACKRST_KEY.foxShow(this@ResultActivity, dismiss = {
                            finish()
                        })
                    } else {
                        adLoadingDialog.postValue(false)
                        finish()
                    }
                } else {
                    adLoadingDialog.postValue(false)
                    finish()
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.padding(padding), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 20.dp)
                ) {
                    Glide(R.drawable.ic_back,
                        Modifier
                            .size(20.dp)
                            .clickable {
                                lifecycleScope.launch {
                                    if (adLoadingDialog.value == true) return@launch
                                    if (!IVBACKRST_KEY.foxPb()) {
                                        adLoadingDialog.postValue(true)
                                        IVBACKRST_KEY.foxLoad()
                                        for (i in 0 until 50) {
                                            delay(100)
                                            if (IVBACKRST_KEY.foxAdEnable() && i >= 10) break
                                        }
                                        if (IVBACKRST_KEY.foxAdEnable()) {
                                            adLoadingDialog.postValue(false)
                                            IVBACKRST_KEY.foxShow(this@ResultActivity, dismiss = {
                                                finish()
                                            })
                                        } else {
                                            adLoadingDialog.postValue(false)
                                            finish()
                                        }
                                    } else {
                                        adLoadingDialog.postValue(false)
                                        finish()
                                    }
                                }
                            })
                    TextWhite(
                        "Result", 18,
                        Modifier
                            .weight(1F)
                            .padding(start = 10.dp, end = 30.dp),
                        textAlign = TextAlign.Center,
                    )
                }
                CenterRow(Modifier.padding(top = 40.dp), horizontalArrangement = Arrangement.Center) {
                    Glide(
                        R.drawable.result_icon,
                        Modifier
                            .padding(end = 12.dp)
                            .size(66.dp)
                    )
                    TextWhite((if (Core.state.canStop) "Connection" else "Disconnection") + " successful", 20)
                }
                val text = if (Core.state.canStop) "Connected to server (IP: ${DataManager.connectIp})"
                else "Thanks for using our vpn, Total connection duration ${formatTime(System.currentTimeMillis())}"
                TextWhite(
                    text, 12,
                    Modifier
                        .padding(start = 30.dp, end = 30.dp, top = 48.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF353C4D))
                        .padding(vertical = 14.dp, horizontal = 20.dp),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Visible
                )
                if (Core.state.canStop) {
                    val time by current.observeAsState()
                    Text("Connection time", 16, Modifier.padding(top = 30.dp, bottom = 20.dp), Color.White.copy(0.6F))
                    Text(formatTime(time!!), 24, color = Color(0xFF41ECFF), fontWeight = 900)
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(1000L)
                            current.postValue(System.currentTimeMillis())
                        }
                    }
                } else {
                    Box(
                        Modifier
                            .padding(top = 48.dp, start = 50.dp, end = 50.dp)
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple()
                            ) {
                                setResult(RESULT_OK, Intent().putExtra("jump_server", true))
                                finish()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Spacer(
                            Modifier
                                .padding(3.dp)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color(0xFF3CCFF8))
                        )
                        Text("Try more servers", 16, color = Color.White, fontWeight = 600)
                    }
                }
            }
            if (adLoading == true) {
                AdLoadingUI()
            }
        }
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
            Modifier.fillMaxSize().background(color = Color(0x33000000)),
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

    private fun formatTime(current: Long): String {
        val time = current - Core.connectTime
        val hours: Long = TimeUnit.MILLISECONDS.toHours(time)
        val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(time) % 60
        val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(time) % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }
}