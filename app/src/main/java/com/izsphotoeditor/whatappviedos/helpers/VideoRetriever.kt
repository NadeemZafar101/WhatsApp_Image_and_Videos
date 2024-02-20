package com.izsphotoeditor.whatappviedos.helpers

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.izsphotoeditor.whatappviedos.models.DataModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoRetriever {
    suspend fun getVideosAndImagesFromMediaStore(context: Context): ArrayList<DataModel> = withContext(Dispatchers.IO) {
        val videos = ArrayList<DataModel>()
        val contentResolver: ContentResolver = context.contentResolver
        val uri: Uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE)
        val selection = "${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val selectionArgs = arrayOf("%WhatsApp/Media/WhatsApp Images%", "%WhatsApp/Media/WhatsApp Video%")
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        cursor?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val typeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)?: "Unknown File"
                val type = cursor.getInt(typeColumn)
                val path = cursor.getString(dataColumn)
                Log.d("getVideosAndImages", "getVideosAndImages: $path")
                val size = cursor.getLong(sizeColumn)
                val isVideo = type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                val model = DataModel(
                    path = path,
                    name = name,
                    isVideo = isVideo,
                    videoSize = size
                )
                videos.add(model)
            }
        }
        cursor?.close()
        videos
    }

    suspend fun getVideosAndImagesFromFiles(vararg directories: File): ArrayList<DataModel> = withContext(Dispatchers.IO) {
        val videos = ArrayList<DataModel>()

        for (directory in directories) {
            val files = directory.listFiles()
            files?.forEach { file ->
                if (file.isFile) {
                    val name = file.name
                    val path = file.absolutePath
                    val size = file.length()

                    // Check if the file is an image or a video based on its extension
                    val isVideo = name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mov")
                    val isImage = name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")

                    if (isVideo || isImage) {
                        val model = DataModel(
                            path = path,
                            name = name,
                            isVideo = isVideo,
                            videoSize = size
                        )
                        videos.add(model)
                    }
                }
//                else if (file.isDirectory) {
//                    // Recursively search for videos and images in subdirectories
//                    videos.addAll(getVideosAndImagesFromFiles(file))
//                }
            }
        }

        videos
    }
}


