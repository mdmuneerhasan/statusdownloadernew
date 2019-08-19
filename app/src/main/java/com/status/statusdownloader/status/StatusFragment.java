package com.status.statusdownloader.status;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.ImageView;

import com.status.statusdownloader.R;
import com.status.statusdownloader.utility.SavedData;

import java.io.File;
import java.util.ArrayList;

public class StatusFragment extends Fragment {
    private static final int READ_REQUEST = 1234;
    RecyclerView recyclerView;
    ArrayList<Status> arrayList;
    StatusAdapter adapter;
    private SavedData savedData;
    private ImageView btnDownload;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        arrayList=new ArrayList<>();
        savedData=new SavedData(getContext());
        recyclerView=view.findViewById(R.id.recycle);
        adapter=new StatusAdapter(arrayList){
            @Override
            public void colorRed() {
                btnDownload.setBackgroundColor(Color.parseColor("#ff0000"));
            }

            @Override
            public void colorWhite() {
                btnDownload.setBackgroundColor(Color.parseColor("#ffffff"));
            }

            @Override
            public void onBindViewHolder(@NonNull Holder holder, int i) {
                super.onBindViewHolder(holder, i);
                final Status status=arrayList.get(i);
                holder.btnShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            copyFileOrDirectory(status.getPath(),"/storage/emulated/0/Delete/status");
                            String path = ("/storage/emulated/0/Delete/status/"+status.getText());
                            savedData.log(path);
                            share(path,getActivity());

                    }
                });
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        checkUserPermission();
        btnDownload= view.findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container,new DownloadFragment());
                ft.commit();

            }
        });

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
        File file = new File(Environment.getExternalStorageDirectory(), "WhatsApp/Media/.Statuses");
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
                arrayList.add(status);

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
