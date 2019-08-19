package com.status.statusdownloader.music;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.squareup.picasso.Callback;
import com.status.statusdownloader.R;
import com.status.statusdownloader.trends.Post;

import java.io.IOException;
import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;
import static com.status.statusdownloader.notification.App.CHANNEL_ID;

/**
 * Created by Jerry on 2/15/2018.
 */

public class AudioServiceBinder extends Binder {
    private Uri audioFileUri = null;
    private String audioFileUrl = "";
    private boolean streamAudio = false;
    private MediaPlayer audioPlayer = null;
    private Context context = null;
    private ArrayList<Post> arrayList;
    private Handler audioProgressUpdateHandler;
    NotificationManagerCompat managerCompat;
    private PendingIntent downloadIntent,playIntent,forwardIntent,nextIntent,rewindIntent;
    private NotificationCompat.Builder notification;
    public final int UPDATE_AUDIO_PROGRESS_BAR = 1;


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getAudioFileUrl() {
        return audioFileUrl;
    }

    public void setAudioFileUrl(String audioFileUrl) {
        this.audioFileUrl = audioFileUrl;
        managerCompat=NotificationManagerCompat.from(getContext());
        createPendingIntent();
        notification.setContentText(arrayList.get(find(audioFileUrl)).getComment());
        managerCompat.notify(1,notification.build());
    }
    private void createPendingIntent() {
        downloadIntent = PendingIntent.getBroadcast(getContext(), 100, new Intent("com.status.statusdownloader.ACTION_DOWNLOAD"), 0);
        playIntent = PendingIntent.getBroadcast(getContext(), 100, new Intent("com.status.statusdownloader.ACTION_PLAY"), 0);
        rewindIntent = PendingIntent.getBroadcast(getContext(), 100, new Intent("com.status.statusdownloader.ACTION_REWIND"), 0);
        nextIntent = PendingIntent.getBroadcast(getContext(), 100, new Intent("com.status.statusdownloader.ACTION_NEXT"), 0);
        forwardIntent = PendingIntent.getBroadcast(getContext(), 100, new Intent("com.status.statusdownloader.ACTION_FORWARD"), 0);
        notification = new NotificationCompat.Builder(getContext().getApplicationContext(), CHANNEL_ID);
        notification.setSmallIcon(R.drawable.ic_music_note_black_24dp)
                .setContentTitle("Status Downloader")
                .setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_music_note_black_24dp))
                .addAction(R.drawable.ic_file_download_black_24dp, "download",downloadIntent)
                .addAction(R.drawable.ic_fast_rewind_black_24dp, "rewind", rewindIntent)
                .addAction(R.drawable.ic_play_circle_filled_black_24dp, "play/pause",playIntent)
                .addAction(R.drawable.ic_fast_forward_black_24dp, "forward", forwardIntent)
                .addAction(R.drawable.ic_skip_next_black_24dp, "next", nextIntent)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().
                        setShowActionsInCompactView(0, 2, 4)).build();

    }

    public void notify(int currProgress) {
        managerCompat.notify(1,notification.build());
    }

    public boolean isStreamAudio() {
        return streamAudio;
    }

    public void setStreamAudio(boolean streamAudio) {
        this.streamAudio = streamAudio;
    }

    public Uri getAudioFileUri() {
        return audioFileUri;
    }

    public void setAudioFileUri(Uri audioFileUri) {
        this.audioFileUri = audioFileUri;
    }

    public Handler getAudioProgressUpdateHandler() {
        return audioProgressUpdateHandler;
    }

    public void setAudioProgressUpdateHandler(Handler audioProgressUpdateHandler) {
        this.audioProgressUpdateHandler = audioProgressUpdateHandler;
    }

    // Start play audio.
    public void startAudio(Callback callback)
    {
        initAudioPlayer(callback);
        callback.onError(new Exception());

    }

    // Pause playing audio.
    public void pauseAudio()
    {
        if(audioPlayer!=null) {
            audioPlayer.pause();
        }
    }

    // Stop play audio.
    public void stopAudio()
    {
        if(audioPlayer!=null) {
            audioPlayer.stop();
            destroyAudioPlayer();
        }
    }

    // Initialise audio player.
    private void initAudioPlayer(final Callback callback)
    {
        try {
            if (audioPlayer == null) {
                audioPlayer = MyMediaPlayer.getMedia();
            }
            audioPlayer.stop();
            audioPlayer.reset();
            audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                audioPlayer.setDataSource(getContext().getApplicationContext(), Uri.parse(getAudioFileUrl()));
                audioPlayer.prepareAsync();
                audioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        audioPlayer.start();
                        callback.onSuccess();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
            Thread updateAudioProgressThread = new Thread()
            {
                @Override
                public void run() {
                    while(true)
                    {
                        Message updateAudioProgressMsg = new Message();
                        updateAudioProgressMsg.what = UPDATE_AUDIO_PROGRESS_BAR;
                        audioProgressUpdateHandler.sendMessage(updateAudioProgressMsg);

                        // Sleep one second.
                        try {
                            Thread.sleep(1000);
                        }catch(InterruptedException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }
            };
            // Run above thread object.
            updateAudioProgressThread.start();


        }catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    // Destroy audio player.
    private void destroyAudioPlayer()
    {
        if(audioPlayer!=null)
        {
            if(audioPlayer.isPlaying())
            {
                audioPlayer.stop();
            }

            audioPlayer.release();

            audioPlayer = null;
        }
    }

    // Return current audio play position.
    public int getCurrentAudioPosition()
    {
        int ret = 0;
        if(audioPlayer != null)
        {
            ret = audioPlayer.getCurrentPosition();
        }
        return ret;
    }

    // Return total audio file duration.
    public int getTotalAudioDuration()
    {
        int ret = 0;
        if(audioPlayer != null)
        {
            ret = audioPlayer.getDuration();
        }
        return ret;
    }

    public void seekTo(int progress) {
        if(audioPlayer!=null){
            audioPlayer.seekTo(progress);
        }
    }

    public void backward() {
        if(audioPlayer!=null){
            audioPlayer.seekTo(audioPlayer.getCurrentPosition()-11000);

        }
    }
    public void forward() {
        if(audioPlayer!=null){
            audioPlayer.seekTo(audioPlayer.getCurrentPosition()+10000);

        }
    }

    public void setArrayList(ArrayList<Post> arrayList) {
        this.arrayList=arrayList;
        audioPlayer=MyMediaPlayer.getMedia();
        audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next();
            }
        });
    }

    public void next() {
        int i=find(getAudioFileUrl())+1;
        if(i<arrayList.size()){
            try {
                setAudioFileUrl( arrayList.get(i).getUrl());
                setStreamAudio(true);
                setContext(context.getApplicationContext());
                setAudioProgressUpdateHandler(audioProgressUpdateHandler);
                startAudio(new Callback() {
                    @Override
                    public void onSuccess() {
                        MusicFragment.getMusicFragment().adapter.inflateNext();
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }catch (Exception e){
                Log.e(TAG, "next: "+e.getMessage() );
            }
        }
    }

    private int find(String audioFileUrl) {
        for (int i = 0; i < arrayList.size(); i++) {
            Post post=arrayList.get(i);
            if(post.getUrl().equals(audioFileUrl)){
                return i;
            }
        }
        return 0;
    }



    // Return current audio player progress value.

}