package com.status.statusdownloader.music;

import android.media.MediaPlayer;

public class MyMediaPlayer {
    private static final MediaPlayer media=new MediaPlayer();
    public static MediaPlayer getMedia() {
        return media;
    }
    private MyMediaPlayer() {
    }
}
