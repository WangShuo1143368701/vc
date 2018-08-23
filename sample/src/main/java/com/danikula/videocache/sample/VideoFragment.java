package com.danikula.videocache.sample;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.widget.media.LiveIjkVideoView;
import org.androidannotations.annotations.*;

import java.io.File;


@EFragment(R.layout.fragment_video)
public class VideoFragment extends Fragment implements CacheListener {

    private static final String LOG_TAG = "VideoFragment";

    @FragmentArg String url;

    @ViewById ImageView cacheStatusImageView;
    @ViewById
    LiveIjkVideoView videoView;
    @ViewById ProgressBar progressBar;

    private boolean isFirst;

    private final VideoProgressUpdater updater = new VideoProgressUpdater();

    public static Fragment build(String url) {
        return VideoFragment_.builder()
                .url(url)
                .build();
    }

    @AfterViews
    void afterViewInjected() {
        checkCachedState();
        //startVideo();
    }

    private void checkCachedState() {
        HttpProxyCacheServer proxy = App.getProxy(getActivity());
        boolean fullyCached = proxy.isCached(url);
        setCachedState(fullyCached);
        if (fullyCached) {
            progressBar.setSecondaryProgress(100);
        }
    }

    private void startVideo() {
        HttpProxyCacheServer proxy = App.getProxy(getActivity());
        proxy.registerCacheListener(this, url);
        //String proxyUrl = proxy.getProxyUrl(url);
        //Log.d(LOG_TAG, "Use proxy url " + proxyUrl + " instead of original url " + url);
        //videoView.setVideoPath(proxyUrl);

        //proxy.startCache(url,0,100*1024);
        videoView.setVideoPath(proxy.startCacheAndPlay(url));
        videoView.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        updater.start();

        checkCachedState();
        startVideo();
        Log.e(LOG_TAG, "======startCache ====");
    }

    @Override
    public void onPause() {
        super.onPause();
        updater.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        videoView.stopPlayback();
        App.getProxy(getActivity()).unregisterCacheListener(this);
    }

    @Override
    public void onCacheAvailable(File file, String url, int percentsAvailable) {
        progressBar.setSecondaryProgress(percentsAvailable);
        setCachedState(percentsAvailable == 100);
        Log.d(LOG_TAG, String.format("onCacheAvailable. percents: %d, file: %s, url: %s", percentsAvailable, file, url));

//        if(percentsAvailable == 10 && !isFirst){
//            HttpProxyCacheServer proxy = App.getProxy(getActivity());
//            proxy.stopCache(url);
//            Log.e(LOG_TAG, "stopCache ====");
//            isFirst = true;
//        }
    }

    private void updateVideoProgress() {
        //int videoProgress = videoView.getCurrentPosition() * 100 / videoView.getDuration();
        //progressBar.setProgress(videoProgress);
    }

    @SeekBarTouchStop(R.id.progressBar)
    void seekVideo() {
        int videoPosition = videoView.getDuration() * progressBar.getProgress() / 100;
        videoView.seekTo(videoPosition);
    }

    private void setCachedState(boolean cached) {
        int statusIconId = cached ? R.drawable.ic_cloud_done : R.drawable.ic_cloud_download;
        cacheStatusImageView.setImageResource(statusIconId);
    }

    private final class VideoProgressUpdater extends Handler {

        public void start() {
            sendEmptyMessage(0);
        }

        public void stop() {
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message msg) {
            updateVideoProgress();
            sendEmptyMessageDelayed(0, 500);
        }
    }
}
