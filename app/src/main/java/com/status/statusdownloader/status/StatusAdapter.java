package com.status.statusdownloader.status;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.status.statusdownloader.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.Holder> {
    ArrayList<Status> arrayList;
    Context context;
    AdRequest adRequest;
    Uri uri;
    public StatusAdapter(ArrayList<Status> arrayList) {
        adRequest = new AdRequest.Builder().build();
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context=viewGroup.getContext();
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_row_status,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int i) {
        final Status status=arrayList.get(i);
        holder.tvText.setText(status.getText());
        uri=status.getUri();
        String mimeType = URLConnection.guessContentTypeFromName(String.valueOf(uri));
        if( mimeType != null && mimeType.startsWith("video")) {

            holder.videoView.setVisibility(View.VISIBLE);
            holder.mAdView2.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.GONE);
            holder.videoView.setVideoPath(status.getPath());
            holder.videoView.seekTo(1);
            holder.btnPlay.setVisibility(View.VISIBLE);
            holder.btnStop.setVisibility(View.GONE);

        }else{
            // delete this line in future to show ads on imageView
            holder.mAdView2.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.btnPlay.setVisibility(View.GONE);
            holder.btnStop.setVisibility(View.GONE);
            holder.imageView.setImageURI(uri);
        }
        holder.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorRed();
                copyFileOrDirectory(status.getPath(),"/storage/emulated/0/Status-Downloader/Status");
                Toast.makeText(context,status.getText()+" downloaded successfully",Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.relativeLayoutAd.setVisibility(View.VISIBLE);
                holder.videoView.setVisibility(View.VISIBLE);
                holder.imageView.setVisibility(View.GONE);
                holder.btnPlay.setVisibility(View.GONE);
                holder.btnStop.setVisibility(View.VISIBLE);
                holder.videoView.start();
                holder.videoView.resume();

            }
        });
        holder.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.btnPlay.setVisibility(View.VISIBLE);
                holder.btnStop.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.VISIBLE);
                holder.imageView.setVisibility(View.GONE);
                holder.videoView.stopPlayback();
            }
        });
    }

    public void colorRed() {
    }

    public void share(String path, Activity activity) {
        String type ;
        File videoFile = new File(path);
        Uri videoURI=getUri(context,videoFile);
        String mimeType = URLConnection.guessContentTypeFromName(path);
        if( mimeType != null && mimeType.startsWith("video")) {
            type="video";
        }else if(mimeType != null && mimeType.startsWith("image")){
            type="image";
        }else{
            type="audio";
        }

        ShareCompat.IntentBuilder.from(activity)
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
    @Override
    public void onViewDetachedFromWindow(@NonNull Holder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.videoView.stopPlayback();
        holder.relativeLayoutAd.setVisibility(View.GONE);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull Holder holder) {
        super.onViewAttachedToWindow(holder);
        holder.mAdView.loadAd(adRequest);
        holder.mAdView2.loadAd(adRequest);
        if(holder.btnStop.getVisibility()==View.VISIBLE){
            holder.btnStop.setVisibility(View.GONE);
            holder.btnPlay.setVisibility(View.VISIBLE);
        }
        holder.videoView.seekTo(1);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class Holder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public VideoView videoView;
        public ImageView btnShare,btnDownload,btnPlay,btnStop,btnDelete;
        public TextView tvText;
        AdView mAdView,mAdView2;
        RelativeLayout relativeLayoutAd;
        public Holder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.image);
            videoView=itemView.findViewById(R.id.video);
            relativeLayoutAd=itemView.findViewById(R.id.rlAd);
            btnDownload=itemView.findViewById(R.id.btnDownload);
            mAdView=itemView.findViewById(R.id.adView);
            mAdView2=itemView.findViewById(R.id.adView2);
            btnShare=itemView.findViewById(R.id.btnShare);
            btnPlay=itemView.findViewById(R.id.btnPlay);
            btnDelete=itemView.findViewById(R.id.btnDelete);
            btnStop=itemView.findViewById(R.id.btnStop);
            tvText=itemView.findViewById(R.id.text);
        }
    }

    public void copyFileOrDirectory(String srcDir, String dstDir) {

        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);
                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
            colorWhite();
        }
    }

    public void colorWhite() {
    }
}
