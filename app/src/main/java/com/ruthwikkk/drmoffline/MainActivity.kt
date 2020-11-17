package com.ruthwikkk.drmoffline

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.drm.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private var exoPlayer: SimpleExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    var drmSessionManager: DefaultDrmSessionManager<FrameworkMediaCrypto?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        trackSelector = DefaultTrackSelector(this)
        exoPlayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector!!)
            .build()

        player_view.player = exoPlayer

        // playVideo("file:///android_asset/big_buck_bunny.mp4", "WEYnAyXfbYdWs5f7tTwH2w", "oIUkxBK-0IQSRqeHmSKGPg")
       playVideo("file:///android_asset/drm.mp4", "WEYnAyXfbYdWs5f7tTwH2w", "oIUkxBK-0IQSRqeHmSKGPg")

    }

    fun playVideo(url: String, id: String, value: String){
        try {
            drmSessionManager =
                Util.getDrmUuid(C.CLEARKEY_UUID.toString())?.let { buildDrmSessionManager(
                    it,
                    true,
                    id,
                    value
                ) }
        } catch (e: UnsupportedDrmException) {
            e.printStackTrace()
        }
        exoPlayer?.prepare(buildDashMediaSource(Uri.parse(url)))
        exoPlayer?.playWhenReady = true
    }

    private fun buildDashMediaSource(uri: Uri): MediaSource {
        val dashChunkSourceFactory = DefaultDataSourceFactory(this, "agent")
        return ProgressiveMediaSource.Factory(dashChunkSourceFactory)
            //.setDrmSessionManager(drmSessionManager ?: DrmSessionManager.DUMMY)
            .createMediaSource(uri)
    }

    @Throws(UnsupportedDrmException::class)
    private fun buildDrmSessionManager(uuid: UUID, multiSession: Boolean, id: String, value: String): DefaultDrmSessionManager<FrameworkMediaCrypto?> {
        val drmCallback = LocalMediaDrmCallback("{\"keys\":[{\"kty\":\"oct\",\"k\":\"${value}\",\"kid\":\"${id}\"}],\"type\":\"temporary\"}".toByteArray())
        val mediaDrm = FrameworkMediaDrm.newInstance(uuid)
        return DefaultDrmSessionManager(uuid, mediaDrm, drmCallback, null, multiSession)
    }
}