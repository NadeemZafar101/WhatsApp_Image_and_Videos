package com.izsphotoeditor.whatappviedos.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.intuit.sdp.BuildConfig
import com.izsphotoeditor.whatappviedos.R
import com.izsphotoeditor.whatappviedos.ads.NativeAdManager
import com.izsphotoeditor.whatappviedos.databinding.AdViewLayoutBinding
import com.izsphotoeditor.whatappviedos.databinding.ItemViewBinding
import com.izsphotoeditor.whatappviedos.helpers.ClickCallback
import com.izsphotoeditor.whatappviedos.models.DataModel
import com.squareup.picasso.Picasso
import java.io.File
import javax.sql.DataSource
class ItemListAdapter(
    private val context: Context,
    private val nativeAdManager: NativeAdManager,
    private val viewList: ArrayList<DataModel>,
    private val clickCallback: ClickCallback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ITEM_VIEW_TYPE_NORMAL = 0
    private val ITEM_VIEW_TYPE_AD = 1
    inner class ViewHolder(val binding: ItemViewBinding) : RecyclerView.ViewHolder(binding.root)
    inner class AdViewHolder(val binding: AdViewLayoutBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_NORMAL -> {
                val binding = ItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolder(binding)
            }
            ITEM_VIEW_TYPE_AD -> {
                val binding = AdViewLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = viewList[position]
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_NORMAL -> {
                val viewHolder = holder as ViewHolder
                viewHolder.binding.apply {
                    if (model.isVideo) {
                        videoIcon.visibility = VISIBLE
                    } else {
                        videoIcon.visibility = GONE
                    }
                    Glide.with(context)
                        .load(model.path)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground)
                        .listener(object : RequestListener<Drawable> {
                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target:Target<Drawable>?,
                                dataSource: com.bumptech.glide.load.DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("GlideLoading", "loading image success:")
                                return false
                            }

                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("GlideLoading", "Error loading image: $e")
                                return false
                            }
                        })
                        .into(imageView)

                    mainContainer.setOnClickListener { clickCallback.getItemClick(position) }
                    textViewOptions.setOnClickListener { showPopupMenu(textViewOptions,position) }
                    val getSize =formatFileSize(model.videoSize)
                    fileSize.text = "Size :  $getSize"
                }
            }
            ITEM_VIEW_TYPE_AD -> {
                val viewHolder = holder as AdViewHolder
                nativeAdManager.showNativeDashboardAd(viewHolder.binding.nativeAdView)
            }
        }
    }

    override fun getItemCount(): Int {
        // Calculate count including the ad layout
        return viewList.size
        //+ (viewList.size / 6)
    }

    override fun getItemViewType(position: Int): Int {
        // Return the view type based on position
        return if ((position + 1) % 7 == 0) ITEM_VIEW_TYPE_AD else ITEM_VIEW_TYPE_NORMAL
    }

    private fun showPopupMenu(view: View, position:Int) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menu.clear()
        popupMenu.menuInflater.inflate(R.menu.item_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.openMenuItem -> {
                    clickCallback.getItemClick(position)
                    true
                }
                R.id.deleteMenuItem -> {
                    openDialog(position)
                    true
                }
                R.id.shareMenuItem -> {
                    shareImage(viewList[position].path)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun openDialog(position:Int) {
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Delete Data")
        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete file")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes"){dialogInterface, which ->
           if(deleteFile(viewList[position].path)){
               viewList.removeAt(position)
               notifyDataSetChanged()
               Toast.makeText(context, "file deleted", Toast.LENGTH_SHORT).show()
           }else{
               Toast.makeText(context, " not file deleted", Toast.LENGTH_SHORT).show()
           }
        }
        builder.setNegativeButton("No"){dialogInterface, which -> }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }


    private fun deleteFile(filePath: String): Boolean {
        Log.d("FileDeletion", "filePath  $filePath")
        val file = File(filePath)
        return try {
            val deleted = file.delete()
            if (deleted) {
                Log.d("FileDeletion", "File deleted successfully.")
            } else {
                Log.e("FileDeletion", "Failed to delete file.")
            }
            deleted
        } catch (e: Exception) {
            Log.e("FileDeletion", "Error deleting file: ${e.message}")
            false
        }
    }

    private fun shareImage(path:String) {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
            val uri = Uri.parse(path)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
            context.startActivity(Intent.createChooser(shareIntent, "Share file"))
        }else{
            val file = File(path)
            val share = Intent("android.intent.action.SEND")
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            share.putExtra("android.intent.extra.STREAM",
                FileProvider.getUriForFile(
                    context,
                    "com.izsphotoeditor.whatappviedos.provider",
                    file
                )
            )
            share.putExtra(Intent.EXTRA_TEXT,"")
            share.type = "image/*"
           context.startActivity(Intent.createChooser(share, "Share Image"))

        }
    }

    private fun formatFileSize(size: Long): String {
        val kilobyte = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024

        return when {
            size < kilobyte -> "$size B"
            size < megabyte -> String.format("%.2f KB", size.toFloat() / kilobyte)
            size < gigabyte -> String.format("%.2f MB", size.toFloat() / megabyte)
            else -> String.format("%.2f GB", size.toFloat() / gigabyte)
        }
    }


}


