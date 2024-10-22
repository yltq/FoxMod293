package com.vpn.fox.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class VpnServer(
    val icon: Int,
    val name: String,
    val servers: List<ServerItem>,
    var expand: MutableState<Boolean> = mutableStateOf(false),
    var select: MutableState<Boolean> = mutableStateOf(false)
)

data class ServerItem(
    val ip: String,
    val method: String,
    val pwd: String,
    val port: Int,
    val city: String,
    val country: String,
    var select: MutableState<Boolean> = mutableStateOf(false),
)