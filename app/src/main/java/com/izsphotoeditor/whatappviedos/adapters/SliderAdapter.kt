package com.izsphotoeditor.whatappviedos.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.izsphotoeditor.whatappviedos.R
import com.izsphotoeditor.whatappviedos.databinding.ItemViewBinding
import com.izsphotoeditor.whatappviedos.databinding.SlideItemContainerBinding
import com.izsphotoeditor.whatappviedos.helpers.VideoPlayCallback
import com.izsphotoeditor.whatappviedos.models.DataModel

class SliderAdapter(
    private val arrayList: ArrayList<DataModel>,
    private val viewPager2: ViewPager2,
    private val videoPlayCallback: VideoPlayCallback
    ) : RecyclerView.Adapter<SliderAdapter.ViewHolder>() {
    inner class ViewHolder( val binding:SlideItemContainerBinding) : RecyclerView.ViewHolder(binding.root)
        var context:Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
     val binding = SlideItemContainerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        context = parent.context
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = arrayList[position]
        holder.binding.apply {
            if(model.isVideo){
                videoIcon.visibility = VISIBLE
                mainContainer.setOnClickListener {
                    videoPlayCallback.getCallback(model.path,videoViewContainer,mainContainer)
                }
            }  else videoIcon.visibility = GONE
            Glide.with(context!!).load(model.path).into(imageSlide)
        }
        if (position == arrayList.size - 1) {
            viewPager2.post(runable)
        }
    }
    override fun getItemCount(): Int = arrayList.size
    @SuppressLint("NotifyDataSetChanged")
    private val runable = Runnable {
        arrayList.addAll(arrayList)
        notifyDataSetChanged()
    }

}