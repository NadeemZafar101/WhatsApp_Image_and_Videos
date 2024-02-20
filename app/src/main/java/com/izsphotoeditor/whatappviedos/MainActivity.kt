package com.izsphotoeditor.whatappviedos

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayoutMediator
import com.izsphotoeditor.whatappviedos.adapters.ViewPagerAdapter
import com.izsphotoeditor.whatappviedos.ads.NativeAdCallback
import com.izsphotoeditor.whatappviedos.ads.NativeAdManager
import com.izsphotoeditor.whatappviedos.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var nativeAdManager: NativeAdManager
    private val galleryPermissionCode = 111
    companion object{
        var isAdLoaded = false
    }

    private val TAG = "tracingMainActivityLog"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

         nativeAdManager = NativeAdManager.getInstance(this)
        nativeAdManager.loadNativeDashboardAd(object:NativeAdCallback{
            override fun adLoaded() {
                isAdLoaded = true
            }
        })
        isGalleryPermissionGranted()
    }

    private fun setTabAndViewPager(){
        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabView, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "videos"
                1 -> tab.text = "Private Videos"
            }
        }.attach()
    }

 private  fun isGalleryPermissionGranted(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                setTabAndViewPager()
            }
            else { ActivityCompat.requestPermissions(this, PERMISSIONS, galleryPermissionCode) }
        }else{
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    setTabAndViewPager()
            } else { ActivityCompat.requestPermissions(this, PERMISSIONS, galleryPermissionCode) }
        }
    }
    private val PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
//            android.Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            galleryPermissionCode->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setTabAndViewPager()
                } else {
                    Toast.makeText(this, "gallery permission no granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}