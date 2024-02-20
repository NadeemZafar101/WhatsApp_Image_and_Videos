package com.izsphotoeditor.whatappviedos.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.izsphotoeditor.MediaControlActivity
import com.izsphotoeditor.whatappviedos.MainActivity
import com.izsphotoeditor.whatappviedos.MainActivity.Companion.isAdLoaded
import com.izsphotoeditor.whatappviedos.adapters.ItemListAdapter
import com.izsphotoeditor.whatappviedos.databinding.FragmentVideoAndImageBinding
import com.izsphotoeditor.whatappviedos.helpers.ClickCallback
import com.izsphotoeditor.whatappviedos.helpers.Connection
import com.izsphotoeditor.whatappviedos.helpers.VideoRetriever
import com.izsphotoeditor.whatappviedos.models.DataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
class VideoAndImageFragment : Fragment() {
    private lateinit var binding: FragmentVideoAndImageBinding
    private lateinit var videoRetriever: VideoRetriever
    private var arrayList = ArrayList<DataModel>()
    private lateinit var adapter: ItemListAdapter
    private lateinit var handler: Handler
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoAndImageBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler = Handler()
        adTesting()
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if ((position + 1) % 7 == 0) 3 else 1
            }
        }
        binding.listRecyclerView.layoutManager = gridLayoutManager
        videoRetriever = VideoRetriever()
        refreshList()
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshList()
        }
    }
    private fun setupRecyclerView() {
        adapter = ItemListAdapter(requireContext(),(activity as MainActivity).nativeAdManager,arrayList, object : ClickCallback {
            override fun getItemClick(position: Int) {
                val intent = Intent(requireActivity(), MediaControlActivity::class.java)
                intent.putExtra("position", position)
                intent.putExtra("dataList", arrayList)
                startActivity(intent)
            }
        })
        binding.listRecyclerView.adapter = adapter
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun refreshList() {
        binding.swipeRefreshLayout.isRefreshing = true
        Log.d("whatislist", "refreshList: $arrayList")
         if(SDK_INT>Build.VERSION_CODES.Q){
            lifecycleScope.launch {
                arrayList = withContext(Dispatchers.IO) {
                    videoRetriever.getVideosAndImagesFromMediaStore(requireContext())
                }
                setupRecyclerView()
            }
        }else{
            val whatsappImagesDirectory = File(Environment.getExternalStorageDirectory(), "WhatsApp/Media/WhatsApp Images")
            val whatsappVideosDirectory = File(Environment.getExternalStorageDirectory(), "WhatsApp/Media/WhatsApp Video")
             retrieveVideosAndImagesFromFiles(whatsappImagesDirectory, whatsappVideosDirectory)
        }
        setupRecyclerView()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun retrieveVideosAndImagesFromFiles(vararg directories: File) {
        lifecycleScope.launch {
            arrayList = withContext(Dispatchers.IO) {
                videoRetriever.getVideosAndImagesFromFiles(*directories)
            }
        }
    }

    private fun adTesting(){
        if(Connection.isInternetWorking(requireContext())){
            Log.d("adTesting", "adTesting: function calls")
            handler.postDelayed({
                if(isAdLoaded){
                    Log.d("adTesting", "ad loaded")
                    refreshList()
                    handler.removeCallbacksAndMessages(null)
                }else{
                    Log.d("adTesting", "else state")
                    adTesting()
                }
            },3000)
        }
    }


}
