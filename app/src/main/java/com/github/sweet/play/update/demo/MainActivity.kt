package com.github.sweet.play.update.demo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.github.sweet.play.update.SweetPlayAppUpdater
import com.github.sweet.play.update.SweetPlayAppUpdater.Companion.REQUEST_CODE_FLEXIBLE_UPDATE
import com.github.sweet.play.update.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var sweetAppUpdater: SweetPlayAppUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
                .apply {
                    lifecycleOwner = this@MainActivity
                    mainActivity = this@MainActivity
                }
        sweetAppUpdater = SweetPlayAppUpdater(this, binding.root).apply {
            initAppUpdaterAndCheckForUpdate()
        }
    }

    fun checkUpdatesInBottomSheet() {
        Log.d("MainActivity", "Check Update")
    }

    override fun onResume() {
        super.onResume()
        // Check all update is already downloaded or not if then show install update ui only
        sweetAppUpdater.ifUpdateDownloadedThenInstall()
    }

    // If user ignore the update then re-check update as user may want to install the update later
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FLEXIBLE_UPDATE
            && resultCode != Activity.RESULT_OK
        ) {
            sweetAppUpdater.checkUpdateAvailable()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sweetAppUpdater.unregisterListener()
    }
}
