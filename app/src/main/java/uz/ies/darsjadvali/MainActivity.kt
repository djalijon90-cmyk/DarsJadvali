package uz.ies.darsjadvali

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.json.JSONObject
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    // HTML ichidagi oddiy fayl tanlash uchun
    private val filePicker =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            val uris =
                WebChromeClient.FileChooserParams.parseResult(
                    result.resultCode,
                    result.data
                )

            fileCallback?.onReceiveValue(uris)
            fileCallback = null
        }

    // Android uchun rington/audio tanlash
    private val ringtonePicker =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }

                getSharedPreferences("dars_settings", MODE_PRIVATE)
                    .edit()
                    .putString("ringtone_uri", uri.toString())
                    .apply()

                webView.post {
                    webView.evaluateJavascript(
                        "if(window.androidRingtoneSelected){androidRingtoneSelected();}",
                        null
                    )
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (
            Build.VERSION.SDK_INT >= 33 &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                10
            )
        }

        webView = findViewById(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true

        webView.addJavascriptInterface(
            AndroidBridge(),
            "AndroidApp"
        )

        webView.webViewClient = WebViewClient()

        webView.webChromeClient =
            object : WebChromeClient() {

                override fun onShowFileChooser(
                    webView: WebView?,
                    callback: ValueCallback<Array<Uri>>?,
                    params: FileChooserParams?
                ): Boolean {

                    fileCallback?.onReceiveValue(null)
                    fileCallback = callback

                    val intent =
                        params?.createIntent()
                            ?: Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                            }

                    filePicker.launch(intent)
                    return true
                }
            }

        webView.loadUrl(
            "file:///android_asset/index.html"
        )
    }

    inner class AndroidBridge {

        // Telefon xotirasidan musiqa tanlash
        @JavascriptInterface
        fun chooseRingtone() {
            runOnUiThread {
                ringtonePicker.launch(
                    arrayOf("audio/*")
                )
            }
        }

        // Rington tanlanganmi?
        @JavascriptInterface
        fun hasCustomRingtone(): Boolean {
            return getSharedPreferences(
                "dars_settings",
                MODE_PRIVATE
            )
                .getString("ringtone_uri", null) != null
        }

        // Tanlangan ringtonni o‘chirish
        @JavascriptInterface
        fun clearRingtone() {
            getSharedPreferences(
                "dars_settings",
                MODE_PRIVATE
            )
                .edit()
                .remove("ringtone_uri")
                .apply()
        }

        // Dars signalini rejalashtirish
        @JavascriptInterface
        fun scheduleAlarm(json: String) {

            try {
                val obj = JSONObject(json)

                val hour = obj.optInt("hour")
                val minute = obj.optInt("minute")
                val before = obj.optInt("before", 0)

                val title =
                    obj.optString(
                        "title",
                        "Dars eslatmasi"
                    )

                val text =
                    obj.optString(
                        "text",
                        "Dars vaqti yaqinlashdi"
                    )

                val calendar =
                    Calendar.getInstance().apply {

                        set(
                            Calendar.HOUR_OF_DAY,
                            hour
                        )

                        set(
                            Calendar.MINUTE,
                            minute
                        )

                        set(
                            Calendar.SECOND,
                            0
                        )

                        set(
                            Calendar.MILLISECOND,
                            0
                        )

                        add(
                            Calendar.MINUTE,
                            -before
                        )

                        if (
                            timeInMillis <=
                            System.currentTimeMillis()
                        ) {
                            add(
                                Calendar.DAY_OF_YEAR,
                                1
                            )
                        }
                    }

                val receiverIntent =
                    Intent(
                        this@MainActivity,
                        AlarmReceiver::class.java
                    ).apply {

                        putExtra(
                            "title",
                            title
                        )

                        putExtra(
                            "text",
                            text
                        )
                    }

                val requestCode =
                    hour * 10000 +
                    minute * 100 +
                    before

                val pendingIntent =
                    PendingIntent.getBroadcast(
                        this@MainActivity,
                        requestCode,
                        receiverIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                    )

                val alarmManager =
                    getSystemService(
                        ALARM_SERVICE
                    ) as AlarmManager

                if (
                    Build.VERSION.SDK_INT >= 31 &&
                    !alarmManager.canScheduleExactAlarms()
                ) {

                    startActivity(
                        Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse(
                                "package:$packageName"
                            )
                        )
                    )

                    return
                }

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )

            } catch (_: Exception) {
            }
        }
@JavascriptInterface
fun testRingtone() {
    try {
        val player = android.media.MediaPlayer.create(
            this@MainActivity,
            R.raw.school_bell
        )
        player?.setOnCompletionListener { it.release() }
        player?.start()
    } catch (_: Exception) {
    }
}
    }
}
