package com.status.statusdownloader.sharefilter;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.status.statusdownloader.MainActivity;
import com.status.statusdownloader.music.MyMediaPlayer;
import com.status.statusdownloader.R;
import com.status.statusdownloader.music.MusicAdapter;
import com.status.statusdownloader.trends.Post;
import com.status.statusdownloader.utility.Connection;
import com.status.statusdownloader.utility.SavedData;
import com.status.statusdownloader.utility.Storage;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ShareMusicActivity extends AppCompatActivity {

    MusicAdapter musicAdapter;
    RecyclerView recyclerView;
    ArrayList<Uri> arrayList;
    SavedData savedData;
    EditText edtName,edtText,edtLink;
    private Connection connection;
    private Storage storage;
    private boolean terminated=false;
    private Date date;
    ArrayList<Post> arrayList2;
    MediaPlayer mediaPlayer;
    private int last=-1;
    private String totalDuration;
    private String TAG="none";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_music);
            recyclerView=findViewById(R.id.recycle);
            edtName=findViewById(R.id.edtName);
            mediaPlayer= MyMediaPlayer.getMedia();
            edtLink=findViewById(R.id.edtLink);
            edtText=findViewById(R.id.edtText);
            storage=new Storage();
            date=new Date(System.currentTimeMillis());
            connection=new Connection();
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            savedData=new SavedData(this);
            arrayList2=new ArrayList<>();
            arrayList= getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            musicAdapter =new MusicAdapter(arrayList2,savedData.getValue("uid")){
                @Override
                public void onBindViewHolder(@NonNull Holder holder, int i) {
                    super.onBindViewHolder(holder, i);
                    set(holder,i);
                    holder.tvExtra.setVisibility(View.GONE);
                    holder.btnDownload.setImageDrawable(getResources().getDrawable(getResources()
                            .getIdentifier(String.valueOf(R.drawable.ic_delete_black_24dp),null,getPackageName())));

                }
            };
            recyclerView.setAdapter(musicAdapter);
            if(!savedData.haveValue("uid")){
                savedData.setValue("uid", String.valueOf(System.currentTimeMillis()));
            }
            if(savedData.haveValue("name")){
                edtName.setText(savedData.getValue("name"));
            }
            if(savedData.haveValue("link")){
                edtLink.setText(savedData.getValue("link"));
            }
            for(Uri uri:arrayList){
                File file=new File(uri.getPath());
                Post status=new Post();
                status.setComment(getName(uri));
                status.setUrl(file.getPath());
                DecimalFormat decimalFormat=new DecimalFormat("#.##");
                status.setTime(decimalFormat.format(file.length()/1024/1024.0)+" Mb");
                arrayList2.add(status);
            }
        }


    private void set(final MusicAdapter.Holder holder, final int i) {
        holder.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                Log.e(TAG, "onClick: playing" );
                holder.progressBar.setVisibility(View.VISIBLE);
                mediaPlayer.reset();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(getApplicationContext(), arrayList.get(i));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    mediaPlayer.seekTo(10000);
                    totalDuration=time(mediaPlayer.getDuration());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onClick: "+e.getMessage() );
                }
            }
        });
        holder.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUp(holder.getAdapterPosition());
            }
        });
    }


    private void popUp(final int position) {
        arrayList2.remove(position);
        arrayList.remove(position);
        musicAdapter.notifyItemRemoved(position);
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



    public void share(View view) {
            String name=edtName.getText().toString();
            if(name.trim().length()<1){
                edtName.setError("please enter name");
                return;
            }
            String link=edtLink.getText().toString();
            savedData.setValue("link",link);
            savedData.setValue("name",name);
            savedData.showAlert("uploading...");
            for(Uri uri:arrayList){
                File file=new File(uri.getPath());
                final Post post = new Post("", edtText.getText().toString(), name, new SimpleDateFormat("hh:mm a dd-MMM-yyyy").format(date), "image", "", new SimpleDateFormat("MM").format(date));
                post.setComment(getName(uri));
                post.setUrl(file.getPath());
                DecimalFormat decimalFormat=new DecimalFormat("#.##");
                post.setTime(decimalFormat.format(file.length()/1024/1024.0)+" Mb");
                post.setLink(link);
                if(uri!=null){
                    storage.getPostStorage().child("music").child(post.getMonth())
                            .child(post.getName()+"."+getExtension(uri)).putFile(uri)
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
                                            connection.getDbMusic().child(post.getMonth()).push().setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    savedData.toast("posted");
                                                    if(!terminated){
                                                        terminated=true;
                                                        finish();
                                                        startActivity(new Intent(getBaseContext(), MainActivity.class).putExtra("trends","music"));
                                                    }
                                                }
                                            });
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
        }

    private String time(int what) {
        int min=what/(60*1000);
        int sec=(what/1000)%60;
        return String.format("%02d",min)+":"+String.format("%02d",sec);
    }


    private String getExtension(Uri uri) {
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
        }
    }
