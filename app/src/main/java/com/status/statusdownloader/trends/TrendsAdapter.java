package com.status.statusdownloader.trends;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.status.statusdownloader.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class TrendsAdapter extends RecyclerView.Adapter<TrendsAdapter.Holder> {
    private AdRequest adRequest;
    ArrayList<Post> arrayList;
    Context context;
    String uid;
    public TrendsAdapter(ArrayList<Post> arrayList,String uid) {
        this.uid=uid;
        this.arrayList = arrayList;
        adRequest = new AdRequest.Builder().build();

    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context=viewGroup.getContext();
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_row_trends,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int i) {
        final Post post=arrayList.get(i);
        holder.tvText.setText(post.getName());
        holder.tvTime.setText(post.getTime());
        holder.tvComment.setText(post.getComment());
        Log.e(TAG, "onBindViewHolder: "+i );
        if( post.getType().startsWith("image")) {
            // delete this line in future to show ads on imageView
            holder.mAdView2.setVisibility(View.GONE);

            holder.videoView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.btnPlay.setVisibility(View.GONE);
            holder.btnStop.setVisibility(View.GONE);
            Picasso.get().load(post.getUrl()).into(holder.imageView);
        }else{
            holder.mAdView2.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.GONE);
            holder.btnStop.setVisibility(View.VISIBLE);
            holder.videoView.setVisibility(View.VISIBLE);
            holder.videoView.setVideoURI(Uri.parse(post.getUrl()));
            holder.btnPlay.setVisibility(View.GONE);
        }

        holder.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        if(post.getLink()!=null){
            if(!post.getLink().startsWith("http")){
                post.setLink("http://"+post.getLink());
            }
            holder.tvComment.setTextColor(Color.parseColor("#0000ff"));
            holder.tvComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(post.getLink()));
                    context.startActivity(intent);
                }
            });
        }else{
            holder.tvComment.setTextColor(Color.parseColor("#666666"));
        }
        if(uid.equals(post.getUid())){
            holder.btnDelete.setVisibility(View.VISIBLE);
        }else{
            holder.btnDelete.setVisibility(View.GONE);
        }
    }


    @Override
    public void onViewDetachedFromWindow(@NonNull Holder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.videoView.stopPlayback();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull final Holder holder) {
        super.onViewAttachedToWindow(holder);
        if(holder.videoView.getVisibility()==View.VISIBLE){
            holder.mAdView.loadAd(adRequest);
            holder.btnStop.setVisibility(View.VISIBLE);
            holder.btnPlay.setVisibility(View.GONE);
            holder.videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    holder.btnStop.setVisibility(View.GONE);
                    holder.btnPlay.setVisibility(View.VISIBLE);

                    return true;
                }
            });
            holder.videoView.seekTo(1);
            holder.videoView.start();
            holder.relativeLayoutAd.setVisibility(View.VISIBLE);
        }else{
            holder.mAdView2.loadAd(adRequest);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        public RelativeLayout relativeLayoutAd;
        ImageView imageView;
        VideoView videoView;
        AdView mAdView,mAdView2;
        public ImageView btnShare,btnDownload,btnPlay,btnStop,btnDelete;
        TextView tvText,tvTime,tvComment;
        public Holder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.image);
            tvComment=itemView.findViewById(R.id.tvComment);
            tvTime=itemView.findViewById(R.id.tvTime);
            videoView=itemView.findViewById(R.id.video);
            relativeLayoutAd=itemView.findViewById(R.id.rlAd);
            mAdView=itemView.findViewById(R.id.adView);
            mAdView2=itemView.findViewById(R.id.adView2);
            btnDownload=itemView.findViewById(R.id.btnDownload);
            btnShare=itemView.findViewById(R.id.btnShare);
            btnPlay=itemView.findViewById(R.id.btnPlay);
            btnDelete=itemView.findViewById(R.id.btnDelete);
            btnStop=itemView.findViewById(R.id.btnStop);
            tvText=itemView.findViewById(R.id.text);
        }
    }

}
