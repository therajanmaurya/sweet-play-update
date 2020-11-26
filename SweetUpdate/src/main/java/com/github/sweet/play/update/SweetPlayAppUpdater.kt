package com.github.sweet.play.update

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 *  SweetAppUpdater Utils class
 */
class SweetPlayAppUpdater constructor(private val context: Activity, private val view: View) : InstallStateUpdatedListener {

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var appUpdateInfo: AppUpdateInfo

    private lateinit var llDownloadUpdate: LinearLayout
    private lateinit var btnDownloadInstall: MaterialButton
    private lateinit var btnLater: MaterialButton
    private lateinit var tvUpdateAvailable: TextView
    private lateinit var tvUpdateAvailableMessage: TextView
    private lateinit var llUpdateAction: LinearLayout
    private lateinit var llUpdateDownloadProgress: LinearLayout
    private lateinit var tvUpdateProgress: TextView

    private fun initializeUIComponent(view: View) {
        tvUpdateAvailable = view.findViewById(R.id.tvUpdateAvailable)
        llDownloadUpdate = view.findViewById(R.id.llDownloadUpdate)
        btnDownloadInstall = view.findViewById(R.id.btnDownloadInstall)
        btnLater = view.findViewById(R.id.btnLater)
        tvUpdateAvailableMessage = view.findViewById(R.id.tvUpdateAvailableMessage)
        llUpdateAction = view.findViewById(R.id.llUpdateAction)
        llUpdateDownloadProgress = view.findViewById(R.id.llUpdateDownloadProgress)
        tvUpdateProgress = view.findViewById(R.id.tvUpdateProgress)
    }

    /**
     * Initialize AppUpdateManager and check update
     */
    fun initAppUpdaterAndCheckForUpdate() {
        initializeUIComponent(view)
        appUpdateManager = AppUpdateManagerFactory.create(context)
        registerListener()
        checkUpdateAvailable()

        /**
         * If update is available then start update otherwise show install update button
         */
        btnDownloadInstall.setOnClickListener {
            when (btnDownloadInstall.text) {
                context.getString(R.string.download) -> startForInAppUpdate()
                context.getString(R.string.install) -> completeUpdate()
            }
        }
        // Hide the In-app-update UI if user selects later
        btnLater.setOnClickListener { llDownloadUpdate.visibility = View.GONE }
    }

    /**
     * Check Update is available or not
     */
    fun checkUpdateAvailable() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateInfo = it
                llDownloadUpdate.visibility = View.VISIBLE
            } else {
                llDownloadUpdate.visibility = View.GONE
                unregisterListener()
            }
        }
    }

    /**
     * As In-App-Update status changes It is being called
     */
    override fun onStateUpdate(state: InstallState) {
        onStateUpdateChange(state.installStatus())
    }

    // If the update is downloaded but not installed,
    // notify the user to complete the update.
    fun ifUpdateDownloadedThenInstall() {
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    onStateUpdateChange(appUpdateInfo.installStatus())
                    //onStateUpdateChange.invoke(appUpdateInfo.installStatus())
                }
            }
    }

    /**
     * Start downloading updates
     */
    private fun startForInAppUpdate() {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.FLEXIBLE,
            context,
            REQUEST_CODE_FLEXIBLE_UPDATE
        )
    }

    /**
     * Install the update
     */
    private fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    // As the In-App-Update status changes update the UI to show Download, Install or later button
    private fun onStateUpdateChange(installStatus: Int) {
        when (installStatus) {
            InstallStatus.PENDING -> {
                llDownloadUpdate.visibility = View.VISIBLE
            }
            InstallStatus.DOWNLOADED -> {
                btnDownloadInstall.text = context.getString(R.string.install)
                tvUpdateAvailable.text = context.getString(R.string.app_update_downloaded)
                llDownloadUpdate.visibility = View.VISIBLE
                llUpdateAction.visibility = View.VISIBLE
                llUpdateDownloadProgress.visibility = View.GONE
            }
            InstallStatus.DOWNLOADING -> {
                llDownloadUpdate.visibility = View.VISIBLE
                llUpdateAction.visibility = View.GONE
                llUpdateDownloadProgress.visibility = View.VISIBLE
            }
            InstallStatus.INSTALLING -> {
                tvUpdateProgress.text = context.getString(R.string.installing_update)
                llDownloadUpdate.visibility = View.VISIBLE
                llUpdateAction.visibility = View.GONE
                llUpdateDownloadProgress.visibility = View.VISIBLE
            }
            InstallStatus.INSTALLED, InstallStatus.UNKNOWN -> {
                llDownloadUpdate.visibility = View.GONE
            }
            else -> {
                llDownloadUpdate.visibility = View.GONE
            }
        }
    }

    private fun registerListener() = appUpdateManager.registerListener(this)

    fun unregisterListener() = appUpdateManager.unregisterListener(this)

    companion object {
        const val REQUEST_CODE_FLEXIBLE_UPDATE = 17362
    }
}
