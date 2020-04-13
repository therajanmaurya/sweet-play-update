package com.github.sweet.play.update.demo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.sweet.play.update.SweetPlayAppUpdater
import com.github.sweet.play.update.SweetPlayAppUpdater.Companion.REQUEST_CODE_FLEXIBLE_UPDATE

class MainActivity : AppCompatActivity() {

    private lateinit var sweetAppUpdater: SweetPlayAppUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sweetAppUpdater = SweetPlayAppUpdater(this, findViewById(android.R.id.content)).apply {
            initAppUpdaterAndCheckForUpdate()
        }
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
