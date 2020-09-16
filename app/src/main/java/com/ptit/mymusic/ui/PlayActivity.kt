package com.ptit.mymusic.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.ptit.mymusic.R
import com.ptit.mymusic.service.PlayService
import kotlinx.android.synthetic.main.activity_play.*

class PlayActivity
    : AppCompatActivity(), SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private var songPosition: Int = DEFAULT_VALUE_NULL
    private var mService: PlayService? = null
    private var isBound = false
    private var isPlaying = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlayService.LocalBinder
            mService = binder.getService()
            isBound = true
            mService?.createSong(songPosition)
            isPlaying = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        seekBar?.let{ mService?.onSeekBarChanged(it.progress) }
    }

    override fun onClick(v: View?) {
        when(v) {
            buttonNext -> onClickNext()
            buttonPause -> onClickPause()
            buttonPrevious -> onClickPrevious()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        initListeners()
        getSongPosition()
        initHandler()
    }

    override fun onStart() {
        super.onStart()
        initNotificationChannel()
        createService()
    }

    override fun onStop() {
        super.onStop()
        destroyService()
    }

    private fun createService() {
        val serviceIntent = PlayService.getIntent(this)
        startService(serviceIntent)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun destroyService() {
        unbindService(connection)
        isBound = false
    }

    private fun initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                R.string.app_name.toString(),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifyManager?.createNotificationChannel(notificationChannel)

        }
    }

    private fun initHandler() {
        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    PlayService.VALUE_MESSAGE_SONG_TITLE -> updateSongTitle(msg)
                    PlayService.VALUE_MESSAGE_SEEKBAR_MAX -> updateSeekBarMax(msg)
                    PlayService.VALUE_MESSAGE_CURRENT_PROGRESS -> updateSeekBarProgress(msg)
                }
            }
        }
    }

    private fun updateSongTitle(msg: Message) {
        val strSongTitle = msg.data.getString(PlayService.VALUE_SONG_TITLE)
        textViewSongTitle.text = strSongTitle
    }

    private fun updateSeekBarMax(msg: Message) {
        val seekbarMax = msg.data.getInt(PlayService.VALUE_SEEKBAR_MAX)
        seekBarSongProcess.max = seekbarMax
    }

    private fun updateSeekBarProgress(msg: Message) {
        val progress = msg.data.getInt(PlayService.VALUE_CURRENT_PROGRESS)
        seekBarSongProcess.progress = progress
    }

    private fun initListeners() {
        seekBarSongProcess.setOnSeekBarChangeListener(this)
        buttonNext.setOnClickListener(this)
        buttonPause.setOnClickListener(this)
        buttonPrevious.setOnClickListener(this)
    }

    private fun getSongPosition() {
        songPosition = intent.getIntExtra(EXTRA_SONG_POSITION, DEFAULT_VALUE_NULL)
    }

    private fun onClickNext() = mService?.onClickNext()

    private fun onClickPrevious() = mService?.onClickPrevious()

    private fun onClickPause() {
        isPlaying = when (isPlaying) {
            true -> {
                buttonPause.setImageResource(R.drawable.ic_play)
                false
            }
            false -> {
                buttonPause.setImageResource(R.drawable.ic_pause)
                true
            }
        }
        mService?.onClickPause(isPlaying)
    }

    companion object {

        var handler: Handler? = null
        var notifyManager: NotificationManager? = null

        private const val DEFAULT_VALUE_NULL = -1
        private const val EXTRA_SONG_POSITION = "com.ptit.mymusic.ui.EXTRA_SONG_POSITION"
        const val CHANNEL_ID = "com.ptit.mymusic.ui.CHANNEL_ID"

        fun getIntent(context: Context, songPosition: Int): Intent =
            Intent(context, PlayActivity::class.java).putExtra(EXTRA_SONG_POSITION, songPosition)
    }
}
