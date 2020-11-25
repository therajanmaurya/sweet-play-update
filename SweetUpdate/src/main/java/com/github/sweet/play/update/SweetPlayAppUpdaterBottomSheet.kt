package com.github.sweet.play.update

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.github.sweet.play.update.databinding.LayoutSweetUpdateBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class SweetPlayAppUpdaterBottomSheet(
    private val title: String,
    private val description: String,
    private val headerImage: Int
) : BottomSheetDialogFragment(), InstallStateUpdatedListener {

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var appUpdateInfo: AppUpdateInfo

    lateinit var binding: LayoutSweetUpdateBottomSheetBinding

    companion object {
        fun newInstant(
            title: String,
            description: String,
            headerImage: Int
        ) = SweetPlayAppUpdaterBottomSheet(title, description, headerImage)

        const val REQUEST_CODE_FLEXIBLE_UPDATE = 17362
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_sweet_update_bottom_sheet,
            null,
            false
        )
        binding.apply {
            title = this@SweetPlayAppUpdaterBottomSheet.title
            description = this@SweetPlayAppUpdaterBottomSheet.description
            headerImage = this@SweetPlayAppUpdaterBottomSheet.headerImage
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAppUpdaterAndCheckForUpdate()
    }

    override fun onResume() {
        super.onResume()
        // Check all update is already downloaded or not if then show install update ui only
        ifUpdateDownloadedThenInstall()
    }

    /**
     * Initialize AppUpdateManager and check update
     */
    fun initAppUpdaterAndCheckForUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(context)
        registerListener()
        checkUpdateAvailable()

        /**
         * If update is available then start update otherwise show install update button
         */
        binding.btnDownloadInstall.setOnClickListener {
            when (binding.btnDownloadInstall.text) {
                getString(R.string.update) -> startForInAppUpdate()
                getString(R.string.install) -> completeUpdate()
            }
        }
        // Hide the In-app-update UI if user selects later
        binding.btnLater.setOnClickListener { dismiss() }
    }

    /**
     * Check Update is available or not
     */
    private fun checkUpdateAvailable() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateInfo = it
                binding.llUpdateAction.visibility = View.VISIBLE
            } else {
                binding.llCheckingUpdate.visibility = View.GONE
                binding.llNoUpdateAvailable.visibility = View.VISIBLE
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
    private fun ifUpdateDownloadedThenInstall() {
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    onStateUpdateChange(appUpdateInfo.installStatus())
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
            requireActivity(),
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
                binding.llUpdateAction.visibility = View.VISIBLE
            }
            InstallStatus.DOWNLOADED -> {
                binding.btnDownloadInstall.text = getString(R.string.install)
                binding.tvUpdateAvailable.text = getString(R.string.app_update_downloaded)
                binding.llUpdateAction.visibility = View.VISIBLE
                binding.llUpdateAction.visibility = View.VISIBLE
                binding.llUpdateDownloadProgress.visibility = View.GONE
            }
            InstallStatus.DOWNLOADING -> {
                binding.llUpdateAction.visibility = View.VISIBLE
                binding.llUpdateAction.visibility = View.GONE
                binding.llUpdateDownloadProgress.visibility = View.VISIBLE
            }
            InstallStatus.INSTALLING -> {
                binding.tvUpdateProgress.text = getString(R.string.installing_update)
                binding.llUpdateAction.visibility = View.VISIBLE
                binding.llUpdateAction.visibility = View.GONE
                binding.llUpdateDownloadProgress.visibility = View.VISIBLE
            }
            InstallStatus.INSTALLED, InstallStatus.UNKNOWN -> {
                binding.llNoUpdateAvailable.visibility = View.GONE
            }
            else -> {
                binding.llNoUpdateAvailable.visibility = View.GONE
            }
        }
    }

    private fun registerListener() = appUpdateManager.registerListener(this)

    private fun unregisterListener() = appUpdateManager.unregisterListener(this)

    // If user ignore the update then re-check update as user may want to install the update later
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SweetPlayAppUpdater.REQUEST_CODE_FLEXIBLE_UPDATE
            && resultCode != Activity.RESULT_OK
        ) {
            checkUpdateAvailable()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterListener()
    }
}
