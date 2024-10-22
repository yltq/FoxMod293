package com.vpn.fox

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpn.fox.base.BaseActivity
import com.vpn.fox.main.MainActivity
import com.vpn.fox.ui.CenterColumn
import com.vpn.fox.ui.CenterRow
import com.vpn.fox.ui.Glide
import com.vpn.fox.ui.TextWhite
import com.vpn.fox.ui.theme.TextGray
import com.vpn.fox.utils.DataManager

class GuideActivity : BaseActivity() {
    @Composable
    override fun Content(padding: PaddingValues) {
        CenterColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CenterRow(
                Modifier.padding(top = 56.dp)
            ) {
                Glide(
                    R.drawable.start_icon,
                    Modifier
                        .padding(end = 8.dp)
                        .size(104.dp, 126.dp)
                )
                TextWhite("Welcome To\n${getString(R.string.app_name)}!", 24)
            }
            Box(
                Modifier
                    .padding(vertical = 20.dp, horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF4B5161))
                    .padding(vertical = 18.dp, horizontal = 14.dp)
                    .fillMaxWidth()
                    .weight(1F),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    "We're thrilled to have you join us on a journey towards a safer and more private online experience. To ensure the best possible service, we need to ask for your permission to access certain features on your Android device. Here's a quick overview of the permissions we require and why we need them:\n" +
                            "\n" +
                            "1. Internet Permission:\n" +
                            "  - Why We Need It: To establish a secure VPN connection, we need to access the internet. This permission allows us to manage your network connections and ensure a stable VPN service.\n" +
                            "\n" +
                            "2. VPN Service:\n" +
                            "  - Why We Need It: To protect your data and privacy, we create a virtual private network (VPN) connection. This permission is essential for encrypting your internet traffic.\n" +
                            "\n" +
                            "3. System Settings:\n" +
                            "  - Why We Need It: Sometimes, we might need to adjust system settings to optimize the VPN connection's performance and stability.\n" +
                            "\n" +
                            "4. Storage Access:\n" +
                            "  - Why We Need It: We may require access to your device's storage to save necessary configuration files or to download updates to keep your app running smoothly.\n" +
                            "\n" +
                            "5. Device Information:\n" +
                            "  - Why We Need It: To ensure compatibility and provide the best possible experience, we collect some basic device information.\n" +
                            "\n" +
                            "6. Location Information:\n" +
                            "  - Why We Need It: While we don't track your actual location, we may use approximate location data to optimize VPN server selection and provide a better connection.\n" +
                            "\n" +
                            "7. Background Running:\n" +
                            "  - Why We Need It: To keep your VPN connection active even when you're not actively using the app, we need to run in the background.\n" +
                            "\n" +
                            "8. Notification Access:\n" +
                            "  - Why We Need It: We'll send you important updates, alerts, and information about your VPN connection status.\n" +
                            "\n" +
                            "We promise to handle your data with the utmost care and respect your privacy. We do not sell your data to third parties, and we adhere to strict privacy policies.\n" +
                            "\n" +
                            "How to Grant Permissions:\n" +
                            "- During the setup process, you will be prompted to grant the necessary permissions. Simply click \"Allow\" to give us the access we need to provide you with a seamless service.\n" +
                            "\n" +
                            "Your Consent:\n" +
                            "By using FoxMod, you agree to our privacy policy and terms of service, which you can find in the app's settings or on our website.\n" +
                            "\n" +
                            "Opting Out:\n" +
                            "You can always change your permissions in your device's settings. However, please note that some features of the app may not work as intended without the necessary permissions.\n" +
                            "\n" +
                            "We appreciate your trust in us and are committed to providing you with a secure, private, and hassle-free VPN experience. If you have any questions or concerns, feel free to reach out to our support team.\n" +
                            "\n" +
                            "Welcome aboard, and enjoy a safer internet!\n" +
                            "\n" +
                            "Best regards,\n" +
                            "FoxMod Team",
                    Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    color = Color.White,
                    16.sp,
                    fontWeight = FontWeight(400),
                    overflow = TextOverflow.Visible
                )
            }

            Box(
                Modifier
                    .padding(horizontal = 50.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .clickable {
                        toMain()
                    },
                contentAlignment = Alignment.Center
            ) {
                Spacer(
                    Modifier
                        .padding(2.dp)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF3CCFF8))
                )
                Text("Agree And Enter", fontSize = 16.sp, fontWeight = FontWeight.W600, color = Color.White)
            }

            Text(
                "Privacy Policy", fontSize = 12.sp, color = TextGray,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                textAlign = TextAlign.End,
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 30.dp)
                    .clickable { toWeb("https://www.baidu.com") } // TODO privacy policy url
            )
        }
    }

    private fun toMain() {
        DataManager.completeGuide = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun toWeb(url: String) {
        startActivity(Intent(this, WebActivity::class.java).putExtra("url", url))
    }
}