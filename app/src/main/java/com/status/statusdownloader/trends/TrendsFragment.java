package com.status.statusdownloader.trends;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.status.statusdownloader.R;
import com.status.statusdownloader.utility.Connection;
import com.status.statusdownloader.utility.SavedData;

import java.io.File;
import java.util.ArrayList;

public class TrendsFragment extends Fragment {
    private static final int PER_LOAD = 4;
    RecyclerView recyclerView;
    TrendsAdapter adapter;
    ArrayList<Post> arrayList;
    ArrayList<Post> arrayList2;
    private SavedData savedData;
    Connection connection;
    ProgressBar progressBar;
    private ImageView btnDownload;
    private int start=0;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        savedData=new SavedData(getContext());
        recyclerView=view.findViewById(R.id.recycle);
        arrayList=new ArrayList<>();
        arrayList2=new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter=new TrendsAdapter(arrayList2,savedData.getValue("uid")){
            @Override
            public void onBindViewHolder(@NonNull final Holder holder, int i) {
                super.onBindViewHolder(holder, i);
                if(i>arrayList2.size()-PER_LOAD){
                    load();
                }
                final Post post=arrayList.get(holder.getAdapterPosition());
                holder.btnShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        download(post,true);


                    }
                });
                holder.btnDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        savedData.toast("Downloading");
                        download(post,false);
                        btnDownload.setBackgroundColor(Color.parseColor("#ff0000"));


                    }
                });

                holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popUp(holder.getAdapterPosition());
                    }
                });
            }
        };
        connection=new Connection();
        progressBar=view.findViewById(R.id.progress_circular);
        recyclerView.setAdapter(adapter);
        btnDownload= view.findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container,new DownloadTrendsFragment());
                ft.commit();

            }
        });

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
                        connection.getDbPost().child(post.getMonth()).child(post.getKey()).removeValue();
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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trends,container,false);
    }

    @Override
    public void onStart() {
        super.onStart();
        connection.getDbPost().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayList.clear();
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    for(DataSnapshot dataSnapshot2:dataSnapshot1.getChildren()){
                        Post post=dataSnapshot2.getValue(Post.class);
                        post.setKey(dataSnapshot2.getKey());
                        arrayList.add(0,post);
                        if(arrayList.size()%5==0){
                            arrayList.add(new Post("","","","","ads","",""));
                        }
                    }
                }
                load();
                progressBar.setVisibility(View.GONE);
                connection.getDbPost().removeEventListener(this);
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void load() {
        for (int i = start; i < start+PER_LOAD; i++) {
            arrayList2.add(arrayList.get(i));
        }
        start+=PER_LOAD;
        try{
            adapter.notifyDataSetChanged();
        }catch (Exception e){}

    }

    public  void download(final Post post, final boolean share){
        final File rootPath;

        if(share){
            rootPath = new File(Environment.getExternalStorageDirectory(), "Delete/Trends");
            savedData.showAlert("please wait");
        }else{
            rootPath = new File(Environment.getExternalStorageDirectory(), "Status-Downloader/Trends");
        }
        String fileExtension;
        if( post.getType().equals("video")) {
            fileExtension=".mp4";
        }else{
            fileExtension=".jpg";
        }

        try {
            if (!rootPath.exists()) {
                rootPath.mkdirs();
            }
            final File file = new File(rootPath,post.getComment()+post.getName()+fileExtension);

            StorageReference reference= FirebaseStorage.getInstance().getReferenceFromUrl(post.getUrl());
            reference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    btnDownload.setBackgroundColor(Color.parseColor("#ffffff"));
                    if(share){
                        savedData.removeAlert();
                        share(file.getAbsolutePath(),post.getType());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void share(String path, String type) {
        File videoFile = new File(path);
        Uri videoURI =getUri(getContext(),videoFile);
                ShareCompat.IntentBuilder.from(getActivity())
                .setStream(videoURI)
                .setType(type+"/*")
                .setChooserTitle("Share")
                .startChooser();

    }
    public static Uri getUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

}
