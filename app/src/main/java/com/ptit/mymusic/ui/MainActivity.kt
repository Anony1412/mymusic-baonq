package com.ptit.mymusic.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ptit.mymusic.R
import com.ptit.mymusic.adapter.SongAdapter
import com.ptit.mymusic.model.Song
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

private const val REQUEST_EXTERNAL_STORAGE = 1

class MainActivity : AppCompatActivity() {

    private val songAdapter: SongAdapter by lazy {
        SongAdapter {
            onItemClick(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showSongs()
        checkPermissions()
    }

    private fun showSongs() {
        recyclerSongs.adapter = songAdapter
    }

    private fun initSongs() {
        val data = findSongs(Environment.getExternalStorageDirectory())
        for (i in data.indices) {
            songs.add(Song(data[i].name, Uri.parse(data[i].toString())))
        }
        songAdapter.updateData(songs)
    }

    private fun findSongs(file: File): List<File> {
        val result = mutableListOf<File>()
        val files = file.listFiles()
        for (singleFile in files) {
            if (singleFile.isDirectory && !singleFile.isHidden) {
                result.addAll(findSongs(singleFile))
            } else {
                if (singleFile.name.endsWith(".mp3")) {
                    result.add(singleFile)
                }
            }
        }
        return result
    }

    private fun onItemClick(songPosition: Int) {
        startActivity(PlayActivity.getIntent(this, songPosition))
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_EXTERNAL_STORAGE
        )
    }

    private fun checkPermissions() {
        if (!checkSelfPermission()) {
            requestPermission()
        } else {
            initSongs()
        }
    }

    private fun solveRequestResult(grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (checkSelfPermission()) {
                initSongs()
            } else {
                requestPermission()
            }
        }
    }

    private fun checkSelfPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                solveRequestResult(grantResults)
                return
            }
        }
    }

    companion object {
        val songs = mutableListOf<Song> ()
    }
}
