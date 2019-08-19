package com.status.statusdownloader.music;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MusicService extends Service {
    public MusicService() {
    }
    private AudioServiceBinder audioServiceBinder = new AudioServiceBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return audioServiceBinder;
    }

}
