package com.status.statusdownloader.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import com.status.statusdownloader.utility.SavedData;

public class AudioPlayerBroadcastReceiver extends BroadcastReceiver {
    MediaPlayer mediaPlayer;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        mediaPlayer= MyMediaPlayer.getMedia();
        if(action.equalsIgnoreCase("com.status.statusdownloader.ACTION_DOWNLOAD")){
            new SavedData(context).toast("no action defined");
        }else if(action.equalsIgnoreCase("com.status.statusdownloader.ACTION_REWIND")){
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
        }else if(action.equalsIgnoreCase("com.status.statusdownloader.ACTION_PLAY")){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }else {
                mediaPlayer.start();
            }
        }else if(action.equalsIgnoreCase("com.status.statusdownloader.ACTION_FORWARD")){
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
        }else if(action.equalsIgnoreCase("com.status.statusdownloader.ACTION_NEXT")){
            mediaPlayer.seekTo(mediaPlayer.getDuration());
        }
    }
}
