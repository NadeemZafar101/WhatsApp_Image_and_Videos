package com.izsphotoeditor
import android.annotation.SuppressLint
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.VideoView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.izsphotoeditor.whatappviedos.adapters.SliderAdapter
import com.izsphotoeditor.whatappviedos.databinding.ActivityMediaControlBinding
import com.izsphotoeditor.whatappviedos.helpers.VideoPlayCallback
import com.izsphotoeditor.whatappviedos.models.DataModel
class MediaControlActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMediaControlBinding
    private var arrayList = ArrayList<DataModel>()
    private lateinit var viewPager2: ViewPager2
    private var position = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaControlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewPager2 = binding.imageViewPagerContainer
        arrayList = (intent.getSerializableExtra("dataList") as? ArrayList<DataModel>)!!
        position = intent.getIntExtra("position",0)
        Log.d("positionccc", "onCreate: position $position ,,,, list  $arrayList" )
        setViewPager()
    }
    private fun setViewPager() {
        val adopter = SliderAdapter(arrayList, viewPager2,object:VideoPlayCallback{
            override fun getCallback(
                videoPath: String,
                videoView: VideoView,
                mainContainer: ConstraintLayout) {
                videoView.visibility = VISIBLE
                mainContainer.isClickable = false
                playVideo(videoPath,videoView)
            }

        })
        viewPager2.adapter = adopter
        viewPager2.setCurrentItem(position, false)
        val viewPagerChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onPageSelected(position: Int) {
                adopter.notifyDataSetChanged()
            }
        }
        viewPager2.registerOnPageChangeCallback(viewPagerChangeCallback)

    }
    fun playVideo(videoPath: String, videoView: VideoView) {
        // Set the media controller buttons
        if (videoView.isPlaying) {
            videoView.stopPlayback()
        }
        val mediaController = android.widget.MediaController(videoView.context)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
        // Set video URI
        videoView.setVideoURI(Uri.parse(videoPath))
        // Start playing the video
        videoView.start()
    }
}