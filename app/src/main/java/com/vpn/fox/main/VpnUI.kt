package com.vpn.fox.main

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.shadowsocks.bg.BaseService
import com.vpn.fox.R
import com.vpn.fox.ui.CenterColumn
import com.vpn.fox.ui.CenterRow
import com.vpn.fox.ui.Glide
import com.vpn.fox.ui.Text
import com.vpn.fox.ui.TextWhite

object VpnUI {

    @Composable
    fun Vpn(model: VpnManager, block: () -> Unit) {
        val state by model.state.observeAsState()
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(4) { index ->
                when (index) {
                    0 -> Title(model)
                    1 -> Message(state)
                    2 -> Image(model, state)
                    3 -> Server(model, block)
                }
            }
        }
    }

    @Composable
    private fun Title(model: VpnManager) {
        CenterRow(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
            TextWhite(stringResource(R.string.app_name), 20, Modifier.weight(1F))
            Glide(R.drawable.main_settings,
                Modifier
                    .size(40.dp)
                    .clickable {
                        if (model.state.value == BaseService.State.Stopping ||
                            model.state.value == BaseService.State.Connecting
                        ) return@clickable
                        model.startSettings()
                    })
        }
    }

    @Composable
    private fun Message(state: BaseService.State?) {
        val text = when (state) {
            BaseService.State.Connecting -> "Connecting Vpn..."
            BaseService.State.Connected -> "Your connection is now secure\nand private"
            BaseService.State.Stopping -> "Disconnecting Vpn..."
            else -> "Tap to establish a secure VPN\nconnection"
        }
        Text(
            text, 20,
            Modifier
                .padding(top = 20.dp, bottom = 60.dp)
                .fillMaxWidth()
                .height(50.dp), Color.White.copy(0.8F), textAlign = TextAlign.Center
        )
    }

    @Composable
    private fun Image(model: VpnManager, state: BaseService.State?) {
        val text = when (state) {
            BaseService.State.Connecting -> "Connecting..."
            BaseService.State.Connected -> "Connected"
            BaseService.State.Stopping -> "Disconnecting..."
            else -> "Connect"
        }
        Box(contentAlignment = Alignment.TopCenter) {
            when (state) {
                BaseService.State.Connecting, BaseService.State.Stopping -> RotatingUI()
                BaseService.State.Connected -> Glide(R.drawable.main_connected, Modifier.size(206.dp))
                else -> {
                    Glide(R.drawable.main_disconnect,
                        Modifier
                            .size(206.dp)
                            .clickable {
                                model.startVpn()
                            })
                }
            }
            Box(
                Modifier
                    .padding(top = 170.dp)
                    .size(183.dp, 52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White)
                    .clickable {
                        if (state == BaseService.State.Connected) {
                            model.stopVpn()
                        } else {
                            model.startVpn()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Spacer(
                    Modifier
                        .padding(3.dp)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFF3CCFF8))
                )
                Text(text, 16, color = Color.White, fontWeight = 600)
            }
        }
    }

    @Composable
    private fun RotatingUI() {
        var target by remember { mutableFloatStateOf(0F) }
        val rotation by animateFloatAsState(
            target,
            infiniteRepeatable(tween(1200, easing = LinearEasing)),
            label = "connecting anim"
        )
        Box(Modifier.size(206.dp)) {
            Glide(R.drawable.ic_connect_bg, Modifier.size(206.dp))
            Glide(
                R.drawable.ic_loading,
                Modifier
                    .padding(60.dp)
                    .size(36.dp)
                    .rotate(rotation)
            )
        }
        LaunchedEffect(Unit) {
            target = 360F
        }
    }

    @Composable
    private fun Server(model: VpnManager, block: () -> Unit) {
        val server by model.currentServer.observeAsState()
        val state by model.state.observeAsState()
        CenterColumn(
            Modifier
                .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            CenterRow(
                Modifier
                    .width(256.dp)
                    .height(60.dp)
                    .background(Color(0xFF4B5161))
                    .padding(horizontal = 15.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    ) {
                        if (model.state.value == BaseService.State.Stopping ||
                            model.state.value == BaseService.State.Connecting
                        ) return@clickable
                        block()
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Glide(server?.icon?:R.drawable.ic_server, Modifier.size(36.dp))
                TextWhite(server?.name?:"Auto", 12, Modifier.padding(horizontal = 8.dp))
                Glide(R.drawable.ic_refresh, Modifier.size(24.dp))
            }
            if (state == BaseService.State.Connected) {
                CenterRow(
                    Modifier
                        .width(256.dp)
                        .background(Color(0xFF353C4D))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextWhite("Country:", 12)
                    TextWhite(model.connectServer.country, 12)
                }
                CenterRow(
                    Modifier
                        .width(256.dp)
                        .background(Color(0xFF353C4D))
                        .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextWhite("City:", 12)
                    TextWhite(model.connectServer.city, 12)
                }
            }
        }
    }
}