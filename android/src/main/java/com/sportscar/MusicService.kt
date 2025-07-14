package com.sportscar

import android.os.Bundle
import androidx.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.MediaItem

class MusicService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var player: SimpleExoPlayer

    override fun onCreate() {
        super.onCreate()

        player = SimpleExoPlayer.Builder(this).build()

        mediaSession = MediaSessionCompat(this, "MyMusicService")

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                // handle generic play
            }

            override fun onPause() {
                player.pause()
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
            }

            override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                mediaId?.let {
                    playMediaById(it)
                }
            }
        })

        mediaSession.isActive = true
        sessionToken = mediaSession.sessionToken
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot("root_id", null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        val item = MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId("dummy_id")
                .setTitle("SoundHelix Song 1")
                .setSubtitle("Sample track")
                .build(),
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
        result.sendResult(mutableListOf(item))
    }

    private fun playMediaById(mediaId: String) {
        val url = when (mediaId) {
            "dummy_id" -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            else -> null
        }

        url?.let {
            val mediaItem = MediaItem.fromUri(it)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)

            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "SoundHelix Song 1")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "SoundHelix")
                .build()

            mediaSession.setMetadata(metadata)
        }
    }

    private fun updatePlaybackState(state: Int) {
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
            )
            .setState(state, player.currentPosition, 1.0f)
            .build()

        mediaSession.setPlaybackState(playbackState)
    }
}
