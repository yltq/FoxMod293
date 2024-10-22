package com.vpn.fox.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vpn.fox.R
import com.vpn.fox.VpnApp.Companion.toFoxPrint
import com.vpn.fox.ui.CenterRow
import com.vpn.fox.ui.Glide
import com.vpn.fox.ui.TextWhite
import com.vpn.fox.utils.FoxFbManager

object ServerUI {
    private const val HIGH_QUALITY = "High quality"
    private const val ALL = "All"
    private const val HISTORY = "history"

    @Composable
    fun Server(model: VpnManager, block: () -> Unit) {
        var type by remember { mutableStateOf(HIGH_QUALITY) }
        TextWhite("Servers", 20, Modifier.padding(top = 40.dp, start = 20.dp))
        when (type) {
            HIGH_QUALITY -> QualityServer(model, block)
            HISTORY -> {}
            ALL -> {}
        }
    }

    @Composable
    private fun QualityServer(model: VpnManager, block: () -> Unit) {
        val servers by model.servers.observeAsState()
        LazyColumn(
            Modifier
                .padding(top = 80.dp)
                .wrapContentHeight()
        ) {
            items(servers?.size ?: 0) { index ->
                val server = servers!![index]
                val outSelect by remember { server.select }
                val expand by remember { server.expand }
                Column(
                    Modifier
                        .padding(vertical = 4.dp, horizontal = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    CenterRow(
                        Modifier
                            .background(Color(0xFF4B5161))
                            .height(60.dp)
                            .padding(horizontal = 14.dp)
                            .clickable {
                                if (model.adLoadingDialog.value == true) return@clickable
                                server.expand.value = !server.expand.value
                            }
                    ) {
                        Glide(server.icon, Modifier.size(36.dp))
                        TextWhite(
                            server.name, 15,
                            Modifier
                                .padding(horizontal = 8.dp)
                                .weight(1F)
                        )
                        Glide(if (outSelect) R.drawable.ic_selected else R.drawable.ic_not_select,
                            Modifier
                                .size(20.dp)
                                .clickable {
                                    if (model.adLoadingDialog.value == true) return@clickable
                                    if (!outSelect) {
                                        model.selectServer = server.servers.firstOrNull()
                                        model.currentServer.value?.select?.value = false
                                        server.select.value = true
                                        model.currentServer.value = server
                                        block()
                                    }
                                })
                    }
                    if (server.servers.isNotEmpty() && expand) {
                        val size = server.servers.size
                        LazyColumn(
                            Modifier
                                .background(Color(0xFF353C4D))
                                .height((50 * size + size - 1).dp)
                        ) {
                            items(size) { index ->
                                val serverItem = server.servers[index]
                                CenterRow(
                                    Modifier
                                        .height(50.dp)
                                        .padding(horizontal = 14.dp)
                                ) {
                                    TextWhite(
                                        serverItem.city, 12,
                                        Modifier
                                            .padding(horizontal = 10.dp)
                                            .weight(1F)
                                    )
                                    Glide(if (outSelect && model.selectServer == serverItem) R.drawable.ic_selected else R.drawable.ic_not_select,
                                        Modifier
                                            .size(20.dp)
                                            .clickable {
                                                if (model.adLoadingDialog.value == true) return@clickable
                                                if (model.selectServer != serverItem) {
                                                    model.selectServer = serverItem
                                                    model.currentServer.value?.select?.value = false
                                                    server.select.value = true
                                                    model.currentServer.value = server
                                                    block()
                                                }
                                            })
                                }
                                if (index != size - 1) HorizontalDivider(
                                    Modifier.padding(start = 14.dp, end = 4.dp),
                                    color = Color.White.copy(0.1F)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}