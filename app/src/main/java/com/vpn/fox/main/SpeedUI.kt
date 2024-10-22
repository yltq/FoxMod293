//package com.vpn.fox.main
//
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.layout.padding
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.runtime.mutableFloatStateOf
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.airbnb.lottie.LottieProperty
//import com.airbnb.lottie.compose.LottieAnimation
//import com.airbnb.lottie.compose.LottieCompositionSpec
//import com.airbnb.lottie.compose.animateLottieCompositionAsState
//import com.airbnb.lottie.compose.rememberLottieAnimatable
//import com.airbnb.lottie.compose.rememberLottieComposition
//import com.airbnb.lottie.compose.rememberLottieDynamicProperties
//import com.airbnb.lottie.compose.rememberLottieDynamicProperty
//import com.vpn.fox.ui.TextWhite
//import kotlinx.coroutines.delay
//
//object SpeedUI {
//    @Composable
//    fun Speed(model: VpnManager, speed: SpeedManager) {
//        TextWhite("Speed Test", 20, Modifier.padding(top = 40.dp, start = 20.dp))
//        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("data.json"), imageAssetsFolder = "images")
//        var progress by remember { mutableFloatStateOf(0F) }
//        val pAnim by animateFloatAsState(progress, tween(500, 0), label = "")
//
//        LottieAnimation(
//            composition = composition,
//            progress = { pAnim },
//        )
//
//        LaunchedEffect(Unit) { // TODO
//            delay(2000L)
//            progress = 0.5F
//        }
//    }
//}