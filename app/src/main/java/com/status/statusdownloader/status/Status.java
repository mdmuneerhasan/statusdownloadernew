package com.status.statusdownloader.status;

import android.net.Uri;

public class Status {
    private String text,path;
    private Uri uri;

    public Status() {
    }

    public Status(String text, String path, Uri uri) {
        this.text = text;
        this.path = path;
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getText() {
        return text;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setText(String text) {
        this.text = text;
    }
}
