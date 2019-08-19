package com.status.statusdownloader.music;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.status.statusdownloader.R;
import com.status.statusdownloader.trends.Post;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.Holder> {
    ArrayList<Post> arrayList;
    ArrayList<View> arrayListView;
    Context context;
    String uid;
    AdRequest adRequest;

    public MusicAdapter(ArrayList<Post> arrayList, String uid) {
        this.uid=uid;
        this.arrayList = arrayList;
        arrayListView=new ArrayList<>();
        adRequest= new AdRequest.Builder().build();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context=viewGroup.getContext();
        View view=LayoutInflater.from(context).inflate(R.layout.item_row_music,viewGroup,false);
        arrayListView.add(view);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int i) {
        final Post post=arrayList.get(i);
        holder.tvTitle.setText(post.getComment());
        if(post.getName()!=null){
            holder.tvSize.setText("uploaded by:- "+post.getName());
        }
        if(uid.equals(post.getUid())){
            holder.btnDelete.setVisibility(View.VISIBLE);
        }else{
            holder.btnDelete.setVisibility(View.GONE);
        }
        holder.imageView.setImageDrawable(context.getResources().getDrawable(context.getResources()
                .getIdentifier(String.valueOf(R.drawable.ic_music_note_black_24dp),null,context.getPackageName())));

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull Holder holder) {
        super.onViewAttachedToWindow(holder);
        holder.mAdView.loadAd(adRequest);

    }

    public void update(Holder holder) {
        for(View view:arrayListView){
            view.findViewById(R.id.rlAd).setVisibility(View.GONE);
            view.findViewById(R.id.rl1).setVisibility(View.GONE);
            view.findViewById(R.id.rl2).setVisibility(View.GONE);
            view.findViewById(R.id.btnPlay).setVisibility(View.VISIBLE);
            view.findViewById(R.id.btnPause).setVisibility(View.GONE);
            view.findViewById(R.id.progress_circular).setVisibility(View.GONE);
            view.findViewById(R.id.ll1).setVisibility(View.GONE);
            SeekBar seekBar=view.findViewById(R.id.seekBar);
            SeekBar seekBar2=view.findViewById(R.id.seekBar2);
            seekBar.setProgress(0);seekBar2.setProgress(0);
        }
        if(holder!=null){
            holder.btnStop.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.btnPlay.setVisibility(View.GONE);
            holder.ll1.setVisibility(View.VISIBLE);
            holder.relativeLayoutAd.setVisibility(View.VISIBLE);
            holder.relativeLayout.setVisibility(View.VISIBLE);
            holder.relativeLayout2.setVisibility(View.VISIBLE);
        }
    }

    public void update() {
        for(View view:arrayListView){
            view.findViewById(R.id.rlAd).setVisibility(View.GONE);
            view.findViewById(R.id.rl1).setVisibility(View.GONE);
            view.findViewById(R.id.rl2).setVisibility(View.GONE);
            view.findViewById(R.id.btnPlay).setVisibility(View.VISIBLE);
            view.findViewById(R.id.btnPause).setVisibility(View.GONE);
            view.findViewById(R.id.progress_circular).setVisibility(View.GONE);
            view.findViewById(R.id.ll1).setVisibility(View.GONE);
            SeekBar seekBar=view.findViewById(R.id.seekBar);
            SeekBar seekBar2=view.findViewById(R.id.seekBar2);
            seekBar.setFocusable(false);seekBar.setClickable(false);
            seekBar.setProgress(0);seekBar2.setProgress(0);
        }
    }

    public void inflateNext() {
        boolean makeVisible=false;
        for (View view:arrayListView){
            if(view.findViewById(R.id.progress_circular).getVisibility()==View.VISIBLE){
                view.findViewById(R.id.rlAd).setVisibility(View.GONE);
                view.findViewById(R.id.rl1).setVisibility(View.GONE);
                view.findViewById(R.id.rl2).setVisibility(View.GONE);
                view.findViewById(R.id.btnPlay).setVisibility(View.VISIBLE);
                view.findViewById(R.id.btnPause).setVisibility(View.GONE);
                view.findViewById(R.id.progress_circular).setVisibility(View.GONE);
                view.findViewById(R.id.ll1).setVisibility(View.GONE);
                SeekBar seekBar=view.findViewById(R.id.seekBar);
                SeekBar seekBar2=view.findViewById(R.id.seekBar2);
                seekBar.setProgress(0);seekBar2.setProgress(0);
                makeVisible=true;
            }else if(makeVisible){
                view.findViewById(R.id.rlAd).setVisibility(View.VISIBLE);
                view.findViewById(R.id.rl1).setVisibility(View.GONE);
                view.findViewById(R.id.rl2).setVisibility(View.VISIBLE);
                view.findViewById(R.id.btnPlay).setVisibility(View.GONE);
                view.findViewById(R.id.progress_circular).setVisibility(View.VISIBLE);
                view.findViewById(R.id.btnPause).setVisibility(View.VISIBLE);
                view.findViewById(R.id.ll1).setVisibility(View.VISIBLE);
                MusicFragment.getMusicFragment().createAudioProgressbarUpdater(new Holder(view));
                break;
            }
        }
    }


    public class Holder extends RecyclerView.ViewHolder{
        public ImageView imageView,btnDownload,btnDelete,btnPlay,btnStop,btnForward,btnBackward;
        public ProgressBar progressBar;
        public TextView tvTitle,tvSize,tvExtra,tvTime,tvTime2;
        public LinearLayout ll1;
        public RelativeLayout relativeLayout,relativeLayout2,relativeLayoutAd;
        public SeekBar seekBar,seekBar2;
        AdView mAdView;
        public Holder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.imageView);
            btnDelete=itemView.findViewById(R.id.btnDelete);
            btnDownload=itemView.findViewById(R.id.btnDownload);
            mAdView = itemView.findViewById(R.id.adView);
            btnPlay=itemView.findViewById(R.id.btnPlay);
            btnStop=itemView.findViewById(R.id.btnPause);
            btnForward=itemView.findViewById(R.id.btnForward);
            btnBackward=itemView.findViewById(R.id.btnBackward);
            progressBar=itemView.findViewById(R.id.progress_circular);
            tvExtra=itemView.findViewById(R.id.tvExtra);
            tvTitle=itemView.findViewById(R.id.tvTitle);
            tvSize=itemView.findViewById(R.id.tvSize);
            tvTime=itemView.findViewById(R.id.tvTime);
            tvTime2=itemView.findViewById(R.id.tvTime2);
            ll1=itemView.findViewById(R.id.ll1);
            relativeLayout=itemView.findViewById(R.id.rl1);
            relativeLayoutAd=itemView.findViewById(R.id.rlAd);
            relativeLayout2=itemView.findViewById(R.id.rl2);
            seekBar=itemView.findViewById(R.id.seekBar);
            seekBar2=itemView.findViewById(R.id.seekBar2);
        }
    }
}
