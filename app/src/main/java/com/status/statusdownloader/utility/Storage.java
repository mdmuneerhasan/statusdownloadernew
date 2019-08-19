package com.status.statusdownloader.utility;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.status.statusdownloader.BuildConfig;

public class Storage {
    StorageReference reference;
    String path;

    public Storage() {
        path="release";
        if(BuildConfig.DEBUG){
            path="debug";
        }
        reference = FirebaseStorage.getInstance().getReference(path);
    }

    public StorageReference getPostStorage() {
        return reference.child("post");
    }

}
