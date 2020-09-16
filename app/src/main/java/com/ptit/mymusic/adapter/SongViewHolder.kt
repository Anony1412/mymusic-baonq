package com.ptit.mymusic.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ptit.mymusic.model.Song
import kotlinx.android.synthetic.main.item_song.view.*

class SongViewHolder(
    view: View,
    onItemClickListener: (Int) -> Unit
) : RecyclerView.ViewHolder(view) {

    private var song: Song? = null

    init {
        itemView.setOnClickListener {
            song?.let {
                onItemClickListener(adapterPosition)
            }
        }
    }

    fun bind(song: Song) {
        this.song = song
        itemView.textSongTitle.text = song.title
    }
}
