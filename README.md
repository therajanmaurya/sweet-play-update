[![](https://jitpack.io/v/therajanmaurya/sweet-play-update.svg)](https://jitpack.io/#therajanmaurya/sweet-play-update)

Usage
-----

In order to use the library

**1. Gradle dependency** 

  -  Add the following to your project level `build.gradle`:
 
```gradle
allprojects {
	repositories {
		 maven { url 'https://jitpack.io' }
	}
}
```
  -  Add this to your app `build.gradle`:
 
```gradle
dependencies {
    Note use latest version on `JitPack`
	implementation 'com.github.therajanmaurya:sweet-play-update:1.5.4'
}
```

**2. Usage** 

For Sweet Play Update using Bottom sheet 

```kotlin
 val typeface = ResourcesCompat.getFont(this, R.font.nunito)
SweetPlayAppUpdaterBottomSheet.newInstant(
            "App Update Available",
            "We have fixed some issues and added some cool feature in this update",
            R.drawable.ic_android_black_24dp,
            R.drawable.shp_header_background,
            TextFont(
                title = typeface,
                desc = typeface,
                progressTitle = typeface,
                msg = typeface,
                button = typeface
            )
        ).apply { isCancelable = false }
         .show(supportFragmentManager, "Check Update")
```

For Sweet Play Update on somewhere dashboard

```xml
<androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">

        <include layout="@layout/layout_sweet_update" />

        <---- Add your layout here --->
 
    </androidx.constraintlayout.widget.ConstraintLayout>
```

```kotlin
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
```

For proper example, please checkout [this](https://github.com/therajanmaurya/sweet-play-update/blob/master/app/src/main/java/com/github/sweet/play/update/demo/MainActivity.kt)

### Google Play Store
<a href='https://play.google.com/store/apps/details?id=com.github.sweet.play.update.demo'><img alt='Get it on Google Play'
src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width='180'/></a>

 A Simple android library to handle google play udpate.

## Sweet Play Designs
<table>
  <td><img src="https://raw.githubusercontent.com/therajanmaurya/Sweet-Play-Update/master/art/main_dashboard.png"></td>
  <td><img src="https://raw.githubusercontent.com/therajanmaurya/Sweet-Play-Update/master/art/bottom_sheet_dashboard.png"></td>
  <td><img src="https://raw.githubusercontent.com/therajanmaurya/Sweet-Play-Update/master/art/bottom_sheet_play_update.png"></td>
</table>
<table>
 <td><img src="https://raw.githubusercontent.com/therajanmaurya/Sweet-Play-Update/master/art/bottom_sheet_download_progress.png"></td>
  <td><img src="https://raw.githubusercontent.com/therajanmaurya/Sweet-Play-Update/master/art/bottom_sheet_install_update.png"></td>
  <td><img src="https://raw.githubusercontent.com/therajanmaurya/Sweet-Play-Update/master/art/bottom_sheet_install.png"></td>
</table>

Test with internal app-sharing
------------------------------

With [internal app sharing](https://support.google.com/googleplay/android-developer/answer/9303479), you can quickly share an app bundle or APK with your internal team and testers by uploading the app bundle you want to test to the Play Console.

You can also use internal app sharing to test in-app updates, as follows:

1. On your test device, make sure you've already installed a version of your app that meets the following requirements:
   - The app was installed using an internal app sharing URL
   - Supports in-app updates
   - Uses a version code that's lower than the updated version of your app
   
2. Follow the Play Console instructions on how to [share your app internally](https://support.google.com/googleplay/android-developer/answer/9303479). Make sure you upload a version of your app that uses a version code that's higher than the one you have already installed on the test device.

3. On the test device, only click the internal app-sharing link for the updated version of your app. Do not install the app from the Google Play Store page you see after clicking the link.

4. Open the app from the device's app drawer or home screen. The update should now be available to your app, and you can test your implementation of in-app updates.

# Design Inspiration

Self developing projects

# Developers

* [Rajan Maurya](https://github.com/therajanmaurya)

# License

```
Copyright 2020 Rajan Maurya

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

```



