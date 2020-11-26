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

        const val REQUEST_CODE_FLEXIBLE_UPDATE = 17363
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
            fragment = this@SweetPlayAppUpdaterBottomSheet
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
    private fun initAppUpdaterAndCheckForUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(requireActivity())
        registerListener()
        checkUpdateAvailable()

        /**
         * If update is available then start update otherwise show install update button
         */
        binding.btnDownloadInstall.setOnClickListener {
            when (binding.btnDownloadInstall.text) {
                getString(R.string.download) -> startForInAppUpdate()
                getString(R.string.install) -> completeUpdate()
            }
        }
    }

    /**
     * Check Update is available or not
     */
    private fun checkUpdateAvailable() {
        checkForUpdateViewVisibility()
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateInfo = it
                binding.llUpdateAction.visibility = View.VISIBLE
                binding.llCheckingUpdate.visibility = View.GONE
                binding.llNoUpdateAvailable.visibility = View.GONE
                binding.llUpdateDownloadProgress.visibility = View.GONE
            } else {
                noUpdateAvailable()
                unregisterListener()
            }
        }
        appUpdateManager.appUpdateInfo.addOnFailureListener {
            noUpdateAvailable()
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
            .addOnSuccessListener {
                if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    appUpdateInfo = it
                }
                if (it.installStatus() == InstallStatus.DOWNLOADED) {
                    onStateUpdateChange(it.installStatus())
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
                binding.llCheckingUpdate.visibility = View.GONE
                binding.llNoUpdateAvailable.visibility = View.GONE
                binding.llUpdateDownloadProgress.visibility = View.GONE
            }
            InstallStatus.DOWNLOADED -> {
                binding.btnDownloadInstall.text = getString(R.string.install)
                binding.tvUpdateAvailable.text = getString(R.string.sweet_app_update_downloaded)
                binding.llUpdateAction.visibility = View.VISIBLE
                binding.llUpdateDownloadProgress.visibility = View.GONE
                binding.llNoUpdateAvailable.visibility = View.GONE
                binding.llCheckingUpdate.visibility = View.GONE
            }
            InstallStatus.DOWNLOADING -> {
                downloadingViewVisibility()
            }
            InstallStatus.INSTALLING -> {
                binding.tvUpdateProgress.text = getString(R.string.installing_update)
                binding.llUpdateAction.visibility = View.GONE
                binding.llCheckingUpdate.visibility = View.GONE
                binding.llNoUpdateAvailable.visibility = View.GONE
                binding.llUpdateDownloadProgress.visibility = View.VISIBLE
            }
            InstallStatus.INSTALLED -> {
                binding.llNoUpdateAvailable.visibility = View.VISIBLE
            }
            InstallStatus.UNKNOWN -> {
                binding.llNoUpdateAvailable.visibility = View.GONE
                binding.llCheckingUpdate.visibility = View.GONE
            }
        }
    }

    private fun registerListener() = appUpdateManager.registerListener(this)

    private fun unregisterListener() = appUpdateManager.unregisterListener(this)

    // If user ignore the update then re-check update as user may want to install the update later
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FLEXIBLE_UPDATE && resultCode != Activity.RESULT_OK) {
            checkUpdateAvailable()
        }
    }

    private fun noUpdateAvailable() {
        binding.tvUpdateAvailable.text = getString(R.string.no_update_available)
        binding.llUpdateAction.visibility = View.GONE
        binding.llCheckingUpdate.visibility = View.GONE
        binding.llUpdateDownloadProgress.visibility = View.GONE
        binding.llNoUpdateAvailable.visibility = View.VISIBLE
    }

    private fun downloadingViewVisibility() {
        binding.llUpdateAction.visibility = View.GONE
        binding.llCheckingUpdate.visibility = View.GONE
        binding.llNoUpdateAvailable.visibility = View.GONE
        binding.llUpdateDownloadProgress.visibility = View.VISIBLE
    }

    private fun checkForUpdateViewVisibility() {
        binding.llUpdateAction.visibility = View.GONE
        binding.llUpdateDownloadProgress.visibility = View.GONE
        binding.llNoUpdateAvailable.visibility = View.GONE
        binding.llCheckingUpdate.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterListener()
    }
}
