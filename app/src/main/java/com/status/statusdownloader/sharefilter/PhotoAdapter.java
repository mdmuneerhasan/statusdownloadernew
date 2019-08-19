package com.status.statusdownloader.sharefilter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.status.statusdownloader.R;

import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.Holder>{
    ArrayList<Uri> arrayList;
    Context context;
    public PhotoAdapter(ArrayList<Uri> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context=viewGroup.getContext();
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_row_photo,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int i) {
        Picasso.get().load(arrayList.get(i)).into(holder.imageView);
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class  Holder extends RecyclerView.ViewHolder{
        public ImageView imageView,btnDelete;
    public Holder(@NonNull View itemView) {
        super(itemView);
        imageView=itemView.findViewById(R.id.imageView);
        btnDelete=itemView.findViewById(R.id.btnDelete);
    }
}
}
