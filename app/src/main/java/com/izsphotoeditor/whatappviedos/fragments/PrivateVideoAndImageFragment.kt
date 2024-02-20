package com.izsphotoeditor.whatappviedos.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.storage.StorageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.izsphotoeditor.MediaControlActivity
import com.izsphotoeditor.whatappviedos.MainActivity
import com.izsphotoeditor.whatappviedos.adapters.ItemListAdapter
import com.izsphotoeditor.whatappviedos.databinding.FragmentVideoAndImageBinding
import com.izsphotoeditor.whatappviedos.helpers.ClickCallback
import com.izsphotoeditor.whatappviedos.helpers.Connection
import com.izsphotoeditor.whatappviedos.helpers.GetImagePath
import com.izsphotoeditor.whatappviedos.helpers.MySp
import com.izsphotoeditor.whatappviedos.helpers.VideoRetriever
import com.izsphotoeditor.whatappviedos.models.DataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PrivateVideoAndImageFragment : Fragment() {
    private lateinit var binding: FragmentVideoAndImageBinding
    private lateinit var videoRetriever: VideoRetriever
    private lateinit var adapter: ItemListAdapter
    private lateinit var  mySp : MySp
    private lateinit var getImagePath: GetImagePath
    private val PICK_WHATSAPP_IMAGES_REQUEST = 123
    private lateinit var handler: Handler
    private val destinationDirectory = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoAndImageBinding.inflate(inflater, container, false)
        mySp = MySp(requireContext())
        getImagePath = GetImagePath()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler = Handler()
        adTesting()
        videoRetriever = VideoRetriever()
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if ((position + 1) % 7 == 0) 3 else 1
            }
        }
        binding.listRecyclerView.layoutManager = gridLayoutManager
        loadRecyclerView()
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            loadRecyclerView()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
    private fun setupRecyclerView(listOfImages:ArrayList<DataModel>) {
        adapter = ItemListAdapter(requireContext(),(activity as MainActivity).nativeAdManager,listOfImages, object : ClickCallback {
            override fun getItemClick(position: Int) {
                val intent = Intent(requireActivity(), MediaControlActivity::class.java)
                intent.putExtra("position", position)
                intent.putExtra("dataList", listOfImages)
                startActivity(intent)
            }
        })
        binding.listRecyclerView.adapter = adapter
    }
    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
    private fun loadRecyclerView(){
        if(Build.VERSION.SDK_INT >Build.VERSION_CODES.Q){
//            requestStorageAccess()
            val checkSp = mySp.getSp()
            if(checkSp.isNotEmpty()){
                if(checkSp == destinationDirectory){
                    val myPath = "%2FWhatsApp%20Images%2FPrivate"
                    val getUri =   Uri.parse(checkSp+myPath)
                    val arrayL =    traverseDirectory(getUri)
                    setupRecyclerView(arrayL)
                }else{
                    requestStorageAccess()
                }
            }else{
                Log.d("tracingSp", "sp is empty")
                requestStorageAccess()
            }
        }else{
            val whatsappImagesDirectory = File(Environment.getExternalStorageDirectory(), "WhatsApp/Media/WhatsApp Images/Private")
            val whatsappVideosDirectory = File(Environment.getExternalStorageDirectory(), "WhatsApp/Media/WhatsApp Video/Private")
            retrieveVideosAndImagesFromFiles(whatsappImagesDirectory, whatsappVideosDirectory)

        }
    }
    private fun retrieveVideosAndImagesFromFiles(vararg directories: File) {
        lifecycleScope.launch {
            val videos = withContext(Dispatchers.IO) {
                videoRetriever.getVideosAndImagesFromFiles(*directories)
            }
            setupRecyclerView(videos)
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestStorageAccess() {
        try {
            val storageManager = requireContext().getSystemService(AppCompatActivity.STORAGE_SERVICE) as StorageManager
            val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
            val targetDirectory = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia"
            var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
            var scheme = uri.toString()
            scheme = scheme.replace("/root/", "/document/")
            scheme += "%3A$targetDirectory"
            uri = Uri.parse(scheme)
            intent.putExtra("android.provider.extra.INITIAL_URI", uri)
            startActivityForResult(intent, PICK_WHATSAPP_IMAGES_REQUEST)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Can't find an app to select media. Please activate your 'Files' app and/or update your phone Google Play services.", Toast.LENGTH_SHORT).show()
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_WHATSAPP_IMAGES_REQUEST) {
            data?.data?.also { uri ->
                 Log.d("imageUri", "uri: $uri")
                 requireContext().contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)
                 mySp.setSp(uri.toString())
                val imagePath = "%2FWhatsApp%20Images%2FPrivate"
                val videoPath = "%2FWhatsApp%20Video%2FPrivate"
                val getImageUri =   Uri.parse(uri.toString()+imagePath)
                val getVideoUri =   Uri.parse(uri.toString()+videoPath)
                val arrayL =   traverseDirectories(arrayOf(getImageUri, getVideoUri))
                Log.d("loadRecyclerView", "loadRecyclerView: $arrayL")
                setupRecyclerView(arrayL)
            }
        }
    }
    private fun traverseDirectories(uris: Array<Uri>): ArrayList<DataModel> {
        val arrayList = ArrayList<DataModel>()

        for (uri in uris) {
            val fileDoc = DocumentFile.fromTreeUri(requireContext(), uri)
            fileDoc?.listFiles()?.forEach { document ->
                val fileName = document.name
                val isDirectory = document.isDirectory
                val fileUri = document.uri
                val fileSize = if (!isDirectory) document.length() else 0
                Log.d("fileSize", "fileSize: $fileSize")
                val filePath = getImagePath.getPathFromUri(requireContext(), fileUri)
                Log.d("isDirectoryName", "Directory name: $filePath")
                val isVideoFile = filePath.endsWith(".mp4", ignoreCase = true)
                val model = DataModel(
                    path = filePath,
                    name = fileName ?: "Unknown File",
                    isVideo = isVideoFile,
                    videoSize = fileSize
                )
                arrayList.add(model)
                // If the current document is a directory, recursively traverse it
                if (isDirectory) {
                    val directoryName = document.name
                }
            }
        }

        return arrayList
    }



    private fun traverseDirectory(uri: Uri) :ArrayList<DataModel>{
        Log.d("imageUri", "uri: $uri")
        val arrayList = ArrayList<DataModel>()
        val fileDoc = DocumentFile.fromTreeUri(requireContext(), uri)
        fileDoc?.listFiles()?.forEach { document ->
            val fileName = document.name
            val isDirectory = document.isDirectory
            val fileUri = document.uri
            val filePath = getImagePath.getPathFromUri(requireContext(),fileUri)
            Log.d("isDirectoryName", "Directory name: $filePath")
            val isVideoFile = filePath.endsWith(".mp4", ignoreCase = true)
            val model = DataModel(
                path = filePath,
                name = fileName?: "Unknown File",
                isVideo = isVideoFile,
                videoSize = 0
            )
            arrayList.add(model)
            //   Log.d("onActivityResult", "onActivityResult: fileName  $fileName,,,,   fileUri  $fileUri")
            // If the current document is a directory, recursively traverse it
            if (isDirectory) {
                val directoryName = document.name
            }
        }
       return arrayList
    }

    private fun traverseDirectory(uri: Uri, visitedDirectories: HashSet<Uri>) {
        if (visitedDirectories.contains(uri)) {
            // Already visited this directory, skip
            return
        }

        val fileDoc = DocumentFile.fromTreeUri(requireContext(), uri)
        fileDoc?.listFiles()?.forEach { document ->
            val fileName = document.name
            val isDirectory = document.isDirectory
            Log.d("traverseDirectory", "traverseDirectory: $isDirectory")
            val fileUri = document.uri
            Log.d("onActivityResult", "onActivityResult: fileName  $fileName,,,,   fileUri  $fileUri")

            if (isDirectory) {
                // Recursively traverse the subdirectory
                traverseDirectory(document.uri, visitedDirectories.apply { add(uri) })
            }
        }
    }

    private fun adTesting(){
        if(Connection.isInternetWorking(requireContext())){
            Log.d("adTesting", "adTesting: function calls")
            handler.postDelayed({
                if(MainActivity.isAdLoaded){
                    Log.d("adTesting", "ad loaded")
                    loadRecyclerView()
                    handler.removeCallbacksAndMessages(null)
                }else{
                    Log.d("adTesting", "else state")
                    adTesting()
                }
            },3000)
        }
    }



}