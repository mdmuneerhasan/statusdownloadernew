package com.status.statusdownloader.status;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.status.statusdownloader.R;
import com.status.statusdownloader.utility.SavedData;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;

public class DownloadFragment extends Fragment {
    private static final int READ_REQUEST = 1234;
    RecyclerView recyclerView;
    ArrayList<Status> arrayList;
    StatusAdapter adapter;
    private SavedData savedData;
    ImageView btnDownload;
    View view;
    Bundle bundle;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view=view;
        bundle=savedInstanceState;
        arrayList=new ArrayList<>();
        savedData=new SavedData(getContext());
        recyclerView=view.findViewById(R.id.recycle);
        adapter=new StatusAdapter(arrayList){
            @Override
            public void onBindViewHolder(@NonNull final Holder holder, int i) {
                super.onBindViewHolder(holder, i);
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDownload.setVisibility(View.GONE);
                holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popUp(arrayList.get(holder.getAdapterPosition()).getPath());
                    }
                });
                holder.btnShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String path = arrayList.get(holder.getAdapterPosition()).getPath();
                            share(path,getActivity());
                        savedData.log(path);
                    }
                });

            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        checkUserPermission();
        btnDownload=view.findViewById(R.id.btnDownload);
        TextView textView =view.findViewById(R.id.title);
        textView.setText("Downloads(Status)");
        btnDownload.setImageDrawable(getResources().getDrawable(getResources().getIdentifier(String.valueOf(R.drawable.ic_arrow_back_black_24dp),null,getContext().getPackageName())));
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container,new StatusFragment());
                ft.commit();
            }
        });

    }

    private void popUp(final String path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure you want to delete?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       File file=new File(path);
                       file.delete();
                       onViewCreated(view,bundle);
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


    private void checkUserPermission(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_REQUEST);
        }
        else{
            loadSong();
        }
    }

    public void loadSong() {
        File file = new File(Environment.getExternalStorageDirectory(), "Status-Downloader/Status");
        File[] pictures = file.listFiles();
        load(pictures);
    }

    private void load(File[] pictures) {
        try{
            for (File file:pictures) {
                Status status=new Status();
                status.setText(file.getName());
                status.setPath(file.getPath());
                status.setUri(Uri.parse(file.getAbsolutePath()));
                arrayList.add(0,status);
            }
            adapter.notifyDataSetChanged();
        }catch (Exception e){
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status,container,false);
    }
}
