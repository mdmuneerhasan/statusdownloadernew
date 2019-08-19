package com.status.statusdownloader.utility;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.status.statusdownloader.BuildConfig;

public class Connection {
    DatabaseReference reference;
    String path;

    public Connection() {
        path="release";
        if(BuildConfig.DEBUG){
            path="debug";
        }
        reference = FirebaseDatabase.getInstance().getReference(path);
    }

    public DatabaseReference getDbPost() {
        return reference.child("post");
    }

    public DatabaseReference getDbMusic() {
        return reference.child("music");
    }



}
