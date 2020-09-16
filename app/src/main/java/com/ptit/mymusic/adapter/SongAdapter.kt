package com.ptit.mymusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ptit.mymusic.R
import com.ptit.mymusic.model.Song

class SongAdapter(
    private val onClickListener: (Int) -> Unit
) : RecyclerView.Adapter<SongViewHolder>() {

    private val songs = mutableListOf<Song>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder =
        SongViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_song, parent, false),
            onClickListener
        )

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) =
        holder.bind(songs[position])

    override fun getItemCount(): Int = songs.size

    fun updateData(newData: List<Song>) {
        if (songs.isNotEmpty()) {
            songs.clear()
        }
        songs.addAll(newData)
        notifyDataSetChanged()
    }
}
