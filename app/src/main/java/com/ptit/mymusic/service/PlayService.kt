package com.ptit.mymusic.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.Message
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.ptit.mymusic.R
import com.ptit.mymusic.ui.MainActivity
import com.ptit.mymusic.ui.PlayActivity
import kotlin.concurrent.thread

class PlayService : Service() {

    private var binder = LocalBinder()
    private var currentPosition: Int = -1
    private var isPlaying: Boolean = false
    private var mediaPlayer: MediaPlayer? = null

    inner class LocalBinder : Binder() {

        fun getService(): PlayService = this@PlayService
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action.toString()) {
            ACTION_PREVIOUS -> onClickPrevious()
            ACTION_PAUSE -> onClickPause(isPlaying)
            ACTION_NEXT -> onClickNext()
        }
        return START_STICKY
    }

    fun createSong(songPosition: Int) {
        this.currentPosition = songPosition
        val song = MainActivity.songs[currentPosition]
        if (isPlaying) {
            mediaPlayer?.apply {
                stop()
                release()
            }
            isPlaying = false
        }
        mediaPlayer = MediaPlayer.create(
            applicationContext,
            song.uri
        )
        mediaPlayer?.start()
        isPlaying = true

        setSongTitle(song.title)
        setSeekBarValueMax(mediaPlayer?.duration)
        updateSeekBarProgress()
        initNotify(song.title)
    }

    fun onClickPrevious() {
        currentPosition =
            if (currentPosition - 1 < 0) {
                MainActivity.songs.size - 1
            } else {
                (currentPosition - 1)
            }
        createSong(currentPosition)
    }

    fun onClickPause(isPlayValue: Boolean) {
        this.isPlaying = isPlayValue
        if (isPlaying) {
            mediaPlayer?.start()
        } else {
            isPlaying = false
            mediaPlayer?.pause()
        }
    }

    fun onClickNext() {
        currentPosition = (currentPosition + 1) % MainActivity.songs.size
        createSong(currentPosition)
    }

    fun onSeekBarChanged(currentProgress: Int) {
        mediaPlayer?.seekTo(currentProgress)
        if (isPlaying) {
            mediaPlayer?.start()
        }
    }

    private fun initNotify(title: String) {
        val intentPlay = createIntent(ACTION_PLAY)
        val pendingIntentPlay = createPendingIntent(intentPlay)

        val intentPause = createIntent(ACTION_PAUSE)
        val pendingIntentPause = createPendingIntent(intentPause)

        val intentPrevious = createIntent(ACTION_PREVIOUS)
        val pendingIntentPrevious = createPendingIntent(intentPrevious)

        val intentNext = createIntent(ACTION_NEXT)
        val pendingIntentNext = createPendingIntent(intentNext)

        val builder = NotificationCompat.Builder(this, PlayActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(title)
            .setContentText(R.string.value_playing.toString())
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    this.resources,
                    R.drawable.ic_music
                )
            )
            .addAction(
                R.drawable.ic_skip_previous, R.string.value_previous.toString(),
                pendingIntentPrevious
            )
            .addAction(R.drawable.ic_pause, R.string.value_pause.toString(), pendingIntentPause)
            .addAction(R.drawable.ic_skip_next, R.string.value_next.toString(), pendingIntentNext)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
        PlayActivity.notifyManager?.notify(VALUE_NOTIFY_ID_DEFAULT, builder)
    }

    private fun createIntent(valueAction: String) =
        Intent(this, PlayService::class.java).apply {
            action = valueAction
        }

    private fun createPendingIntent(intent: Intent) =
        PendingIntent.getService(
            this,
            VALUE_PENDING_CODE_DEFAULT,
            intent,
            VALUE_PENDING_FLAG_DEFAULT
        )

    private fun updateSeekBarProgress() {
        thread {
            mediaPlayer?.let {
                val totalDuration = it.duration
                var currentDuration = 0
                while (currentDuration < totalDuration) {
                    try {
                        Thread.sleep(500)
                        currentDuration = it.currentPosition
                        setSeekBarProgress(currentDuration)
                    } catch (e: Exception) {
                    }
                }

                nextRandomSong()
            }
        }
    }

    private fun nextRandomSong() {
        currentPosition = (0 until MainActivity.songs.size).random()
        createSong(currentPosition)
    }

    private fun setSeekBarProgress(currentDuration: Int) =
        PlayActivity.handler?.sendMessage(
            createMessage(VALUE_CURRENT_PROGRESS, currentDuration, VALUE_MESSAGE_CURRENT_PROGRESS)
        )

    private fun setSeekBarValueMax(duration: Int?) =
        PlayActivity.handler?.sendMessage(
            createMessage(VALUE_SEEKBAR_MAX, duration, VALUE_MESSAGE_SEEKBAR_MAX)
        )

    private fun setSongTitle(title: String) =
        PlayActivity.handler?.sendMessage(
            createMessage(VALUE_SONG_TITLE, title, VALUE_MESSAGE_SONG_TITLE)
        )

    private fun createMessage(bundleTAG: String, bundleValue: Any?, messageTAG: Int): Message {
        val bundle = bundleOf(bundleTAG to bundleValue)
        return Message().apply {
            what = messageTAG
            data = bundle
        }
    }

    companion object {
        const val VALUE_SONG_TITLE = "com.ptit.mymusic.service.VALUE_SONG_TITLE"
        const val VALUE_MESSAGE_SONG_TITLE = 1

        const val VALUE_SEEKBAR_MAX = "com.ptit.mymusic.service.VALUE_SEEKBAR_MAX"
        const val VALUE_MESSAGE_SEEKBAR_MAX = 2

        const val VALUE_CURRENT_PROGRESS = "com.ptit.mymusic.service.VALUE_CURRENT_PROGRESS"
        const val VALUE_MESSAGE_CURRENT_PROGRESS = 3

        const val ACTION_PLAY = "action_play"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_PREVIOUS = "action_previous"
        const val ACTION_NEXT = "action_next"

        private const val VALUE_PENDING_CODE_DEFAULT = 0
        private const val VALUE_PENDING_FLAG_DEFAULT = 0
        private const val VALUE_NOTIFY_ID_DEFAULT = 10

        fun getIntent(context: Context) = Intent(context, PlayService::class.java)
    }
}
