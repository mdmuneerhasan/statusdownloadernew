package com.status.statusdownloader.music;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.status.statusdownloader.R;
import com.status.statusdownloader.status.DownloadFragment;
import com.status.statusdownloader.trends.Post;
import com.status.statusdownloader.utility.Connection;
import com.status.statusdownloader.utility.SavedData;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

public  class MusicFragment extends Fragment {
    private AudioServiceBinder binder = null;
    private Handler audioProgressUpdateHandler = null;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // Cast and assign background service's onBind method returned iBander object.
            binder = (AudioServiceBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @SuppressLint("ValidFragment")
    private MusicFragment() {
    }

    public static MusicFragment getMusicFragment() {
        return music;
    }
    private static final MusicFragment music=new MusicFragment();
    private static boolean loaded;
    RecyclerView recyclerView;
    MusicAdapter adapter;
    private SavedData savedData;
    Connection connection;
    static ArrayList<Post> arrayList;
    ProgressBar progressBar;
    String totalDuration;
    MusicAdapter.Holder holder;
    static Context context;
    private ImageView btnDownload;
    private TextView textView;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context=getContext();
        bindAudioService();
        savedData=new SavedData(getContext());
        recyclerView=view.findViewById(R.id.recycle);
        progressBar=view.findViewById(R.id.progress);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        arrayList=new ArrayList<>();
        textView =view.findViewById(R.id.title);
        textView.setText("Music");

        if(!savedData.haveValue("disable")){
            showLockScreenAlert();
        }
        adapter=new MusicAdapter(arrayList,savedData.getValue("uid")){
            @Override
            public void onBindViewHolder(@NonNull final Holder holder, final int i) {
                super.onBindViewHolder(holder, i);
                set(holder,i);
            }
        };
        adapter.update(holder);
        recyclerView.setAdapter(adapter);
        connection=new Connection();

        btnDownload=view.findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container, MusicFragmentDownload.getMusicFragment());
                ft.commit();

            }
        });
    }
    private void bindAudioService() {
        if(binder == null) {
            Intent intent = new Intent(context, MusicService.class);
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }


    private void showLockScreenAlert() {
        final CheckBox checkBox=new CheckBox(getContext());
        checkBox.setText("Don't show again ");
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(checkBox);
        builder.setTitle("Allow lock screen notification")
                .setMessage("Click open setting->notification->enable Show on lock screen")
                .setCancelable(false)
                .setPositiveButton("Open setting", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        dialog.cancel();
                        if(checkBox.isChecked()){
                            savedData.setValue("disable","disable");
                        }
                    }
                })
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        if(checkBox.isChecked()){
                            savedData.setValue("disable","disable");
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public  void download(final Post post){
        savedData.toast("downloading "+post.getComment());
        final File rootPath;
            rootPath = new File(Environment.getExternalStorageDirectory(), "Status-Downloader/Music");
        String fileExtension=".mp3";
        try {
            if (!rootPath.exists()) {
                rootPath.mkdirs();
            }
            final File file = new File(rootPath,post.getComment()+fileExtension);

            StorageReference reference= FirebaseStorage.getInstance().getReferenceFromUrl(post.getUrl());
            reference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    savedData.toast(post.getComment()+" Downloaded successfully");
                    btnDownload.setBackgroundColor(Color.parseColor("#ffffff"));

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void popUp(final int position) {
        final Post post=arrayList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure you want to delete?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(post.getUrl());
                        ref.delete();
                        connection.getDbMusic().child(post.getMonth()).child(post.getKey()).removeValue();
                        arrayList.remove(post);
                        adapter.notifyItemRemoved(position);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void set(final MusicAdapter.Holder holder, final int i) {
        final Post post=arrayList.get(i);
        holder.tvExtra.setText("time:- "+post.getTime());
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    popUp(holder.getAdapterPosition());
            }
        });
            holder.seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    holder.seekBar.setProgress(progress);
                    binder.seekTo(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

        holder.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download(post);
                btnDownload.setBackgroundColor(Color.parseColor("#ff0000"));
            }
        });
        holder.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    binder.setContext(context.getApplicationContext());
                    binder.setAudioFileUrl( post.getUrl());
                    binder.setStreamAudio(true);
                    createAudioProgressbarUpdater(holder);
                    binder.setAudioProgressUpdateHandler(audioProgressUpdateHandler);
                    binder.startAudio(new Callback() {
                        @Override
                        public void onSuccess() {
                            totalDuration=time(binder.getTotalAudioDuration());
                            holder.seekBar.setMax(binder.getTotalAudioDuration());
                            holder.seekBar2.setMax(binder.getTotalAudioDuration());
                            adapter.update(holder);
                        }

                        @Override
                        public void onError(Exception e) {
                            holder.progressBar.setVisibility(View.VISIBLE);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e( "onClick: ",e.getMessage() );
                }
            }
        });
        holder.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binder.pauseAudio();
                adapter.update();
             }
        });
        holder.btnBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binder.backward();
            }
        });
        holder.btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binder.forward();
            }
        });
    }

    private void popUp2(final String path, final int i) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure you want to delete?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        File file=new File(path);
                        file.delete();
                        arrayList.remove(i);
                        adapter.notifyItemRemoved(i);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    @SuppressLint("HandlerLeak")
    public void createAudioProgressbarUpdater(final MusicAdapter.Holder holder) {
        /* Initialize audio progress handler. */
        if(true) {
            audioProgressUpdateHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == binder.UPDATE_AUDIO_PROGRESS_BAR) {
                        if( binder != null) {
                            int currProgress = binder.getCurrentAudioPosition();
                            holder.seekBar.setProgress(currProgress);
                            holder.tvTime2.setText(totalDuration);
                            holder.tvTime.setText(time(currProgress));
                            if(currProgress%10==0){
                                binder.notify(currProgress);
                            }

                        }
                    }
                }
            };
        }

    }

    private String time(int what) {
        int min=what/(60*1000);
        int sec=(what/1000)%60;
        return String.format("%02d",min)+":"+String.format("%02d",sec);
    }


    @Override
    public void onStart() {
        super.onStart();
        if(!loaded){
            connection.getDbMusic().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    arrayList.clear();
                    for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                        for(DataSnapshot  dataSnapshot2:dataSnapshot1.getChildren()){
                            Post post=dataSnapshot2.getValue(Post.class);
                            post.setKey(dataSnapshot2.getKey());
                            arrayList.add(0,post);
                        }
                    }
                    loaded=true;
                    binder.setArrayList(arrayList);
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    connection.getDbMusic().removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music,container,false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (arrayList.size()>0){
            progressBar.setVisibility(View.GONE);
        }
    }
}
