package com.status.statusdownloader.sharefilter;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.status.statusdownloader.R;
import com.status.statusdownloader.trends.Post;
import com.status.statusdownloader.utility.Connection;
import com.status.statusdownloader.utility.SavedData;
import com.status.statusdownloader.utility.Storage;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class SharePhotosActivity extends AppCompatActivity {
    PhotoAdapter photoAdapter;
    RecyclerView recyclerView;
    ArrayList<Uri> arrayList;
    SavedData savedData;
    EditText edtName,edtText,edtLink;
    private Connection connection;
    private Storage storage;
    private boolean terminated=false;
    private Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_photos);
        recyclerView=findViewById(R.id.recycle);
        edtName=findViewById(R.id.edtName);
        edtLink=findViewById(R.id.edtLink);
        edtText=findViewById(R.id.edtText);
        storage=new Storage();
        date=new Date(System.currentTimeMillis());
        connection=new Connection();
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,3);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
        savedData=new SavedData(this);
        arrayList= getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        photoAdapter=new PhotoAdapter(arrayList);
        recyclerView.setAdapter(photoAdapter);
        if(!getIntent().getType().startsWith("image")){
            Intent intent = new Intent(this,ShareMusicActivity.class);
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayList);
            startActivity(intent);
            finish();
        }
        if(!savedData.haveValue("uid")){
            savedData.setValue("uid", String.valueOf(System.currentTimeMillis()));
        }
        if(savedData.haveValue("name")){
            edtName.setText(savedData.getValue("name"));
        }
        if(savedData.haveValue("link")){
            edtLink.setText(savedData.getValue("link"));
        }
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
            final Post post = new Post("", edtText.getText().toString(), name, new SimpleDateFormat("hh:mm a dd-MMM-yyyy").format(date), "image", "", new SimpleDateFormat("MM").format(date));
            post.setLink(link);
            if(uri!=null){
                storage.getPostStorage().child("delete").child(post.getMonth())
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
                                        connection.getDbPost().child(post.getMonth()).push().setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                savedData.toast("posted");
                                                if(!terminated){
                                                    terminated=true;
                                                    finish();
                                                    startActivity(new Intent(getBaseContext(), MainActivity.class).putExtra("trends","trends"));
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

    private String getExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }
}
