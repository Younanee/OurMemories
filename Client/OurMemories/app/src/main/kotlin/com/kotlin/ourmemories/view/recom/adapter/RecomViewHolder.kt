package com.kotlin.ourmemories.view.recom.adapter

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.VideoView
import com.kotlin.ourmemories.R
import com.kotlin.ourmemories.data.jsondata.ReComMemoryResult
import com.kotlin.ourmemories.view.recom.RecomActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropSquareTransformation
import kotlinx.android.synthetic.main.item_card.view.*

/**
 * Created by kimmingyu on 2017. 12. 6..
 */
class RecomViewHolder(parent: ViewGroup?):RecyclerView.ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_card,parent,false)) {
    fun bindView(activity: RecomActivity, item:ReComMemoryResult){
        with(itemView){
            when{
                item.media.contains("jpg") -> {
                    val photo = ImageView(activity)
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    photo.layoutParams = params
                    photo.scaleType = ImageView.ScaleType.FIT_START
                    photo.adjustViewBounds = true

                    Picasso.with(activity)
                            .load(item.media)
                            .transform(CropSquareTransformation())
                            .into(photo)
                    reComMedia.addView(photo)
                }
                item.media.contains("mp4") -> {
                    val video = VideoView(activity)
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, this.resources.getDimension(R.dimen.video_height).toInt())
                    video.layoutParams = params
                    val controller = MediaController(activity)
                    video.setMediaController(controller)

                    video.setVideoURI(Uri.parse(item.media))
                    video.requestFocus()
                    reComMedia.addView(video)
                }
            }
            reComTitleText.text = item.title
            reComText.text = item.contents
            reComAddress.text = item.address
        }
    }
}