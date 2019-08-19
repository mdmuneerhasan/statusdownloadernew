package com.status.statusdownloader.sharefilter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.graphics.PathUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.status.statusdownloader.MainActivity;
import com.status.statusdownloader.R;
import com.status.statusdownloader.trends.Post;
import com.status.statusdownloader.utility.Connection;
import com.status.statusdownloader.utility.SavedData;
import com.status.statusdownloader.utility.Storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class ShareActivity extends AppCompatActivity {
    VideoView videoView;
    ImageView imageView,btnPlay,btnStop,btnPlayMusic,btnStopMusic;
    SeekBar seekBar3,seekBar2;
    private Uri uri;
    EditText edtName,edtText,edtLink;
    SavedData savedData;
    Storage storage;
    String type;
    Connection connection;
    Date date;
    private Post post;
    private MediaPlayer mediaPlayer;
    TextView title;
    private String location="delete";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        imageView = findViewById(R.id.imageView);
        title=findViewById(R.id.title);
        videoView = findViewById(R.id.video);
        btnPlay = findViewById(R.id.btnPlay);
        btnStop= findViewById(R.id.btnStop);
        btnPlayMusic = findViewById(R.id.btnPlayMusic);
        btnStopMusic= findViewById(R.id.btnStopMusic);
        seekBar3 = findViewById(R.id.seekBar);
        seekBar2 = findViewById(R.id.seekBar2);
        Intent intent = getIntent();
        type=intent.getType();
        uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        edtName=findViewById(R.id.edtName);
        edtText=findViewById(R.id.edtText);
        edtLink=findViewById(R.id.edtLink);
        savedData=new SavedData(this);
        connection=new Connection();
        storage=new Storage();
        date=new Date(System.currentTimeMillis());


        if(!savedData.haveValue("uid")){
            savedData.setValue("uid", String.valueOf(System.currentTimeMillis()));
        }


        if(savedData.haveValue("name")){
            edtName.setText(savedData.getValue("name"));
        }
        if(savedData.haveValue("link")){
            edtLink.setText(savedData.getValue("link"));
        }

        if( type.startsWith("image")) {
            imageView.setImageURI(uri);
            imageView.setVisibility(View.VISIBLE);
        }else if(type.startsWith("video")){
            videoView.setVideoURI(uri);
            videoView.start();
            videoView.setVisibility(View.VISIBLE);
            btnStop.setVisibility(View.VISIBLE);
        }else{
            type="audio";
            setupAudio();
        }
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBar3.setProgress(progress);
                mediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        btnStopMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMusic();
            }
        });
        btnPlayMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.seekTo(1);
                btnStop.setVisibility(View.GONE);
                btnPlay.setVisibility(View.VISIBLE);
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
    }


    private void setupAudio() {
        edtText.setText(getName(uri));
        seekBar3.setVisibility(View.VISIBLE);
        seekBar2.setVisibility(View.VISIBLE);
        btnStopMusic.setVisibility(View.VISIBLE);
        mediaPlayer=new MediaPlayer();
        title.setText("Share to music");
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        location="music";
        type="audio";

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mediaPlayer!=null){
                    Message message=new Message();
                    message.what=mediaPlayer.getCurrentPosition();
                    handler.sendMessage(message);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        seekBar3.setMax(mediaPlayer.getDuration());
        seekBar2.setMax(mediaPlayer.getDuration());
    }

    private String getName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            seekBar3.setProgress(msg.what);
        }
    };

    private void playMusic() {
        mediaPlayer.start();
        btnPlayMusic.setVisibility(View.GONE);
        btnStopMusic.setVisibility(View.VISIBLE);

    }

    private void stopMusic() {
        mediaPlayer.pause();
        btnStopMusic.setVisibility(View.GONE);
        btnPlayMusic.setVisibility(View.VISIBLE);

    }

    private void play() {
        videoView.start();
        videoView.resume();
        btnPlay.setVisibility(View.GONE);
        btnStop.setVisibility(View.VISIBLE);
    }

    private void stop() {
        videoView.stopPlayback();
        btnStop.setVisibility(View.GONE);
        btnPlay.setVisibility(View.VISIBLE);
    }

    public void share(View view) {
        String name=edtName.getText().toString();
        if(name.trim().length()<1){
            edtName.setError("please enter name");
            return;
        }
        String link=edtLink.getText().toString();
        if(link.trim().length()>0){
            savedData.setValue("link",link);
        }

        savedData.setValue("name",name);
        savedData.showAlert("uploading...");
        post=new Post("",edtText.getText().toString(),name,new SimpleDateFormat("hh:mm a dd-MMM-yyyy").format(date),type,"",new SimpleDateFormat("MM").format(date));
        post.setLink(link);
        if(uri!=null){
            storage.getPostStorage().child(location).child(post.getMonth())
                    .child(System.currentTimeMillis()+"."+getExtension(uri)).putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();
                            downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();
                                    savedData.toast("Upload Successful");
                                    savedData.removeAlert();
                                    post.setUrl(url);
                                    post.setUid(savedData.getValue("uid"));
                                    if(type.equals("audio")){
                                        connection.getDbMusic().child(post.getMonth()).push().setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                savedData.toast("posted");
                                                finish();
                                                startActivity(new Intent(getBaseContext(), MainActivity.class).putExtra("trends","music"));
                                            }
                                        });
                                    }else{

                                        connection.getDbPost().child(post.getMonth()).push().setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                savedData.toast("posted");
                                                finish();
                                                startActivity(new Intent(getBaseContext(), MainActivity.class).putExtra("trends","trends"));
                                            }
                                        });
                                    }
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    savedData.toast("Upload Failed");
                    savedData.removeAlert();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    //       savedData.showAlert(String.valueOf(100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount()));
                }
            });
        }
    }

    private String getExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }

}