package com.status.statusdownloader.utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.status.statusdownloader.BuildConfig;

public class SavedData {
    SharedPreferences  sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;
    ProgressDialog progressDialog;
    KeyEvent.Callback callback;
    public SavedData(Context context) {
        this.context = context;
        sharedPreferences=context.getSharedPreferences("store",Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        progressDialog=new ProgressDialog(context);

    }

    public String getValue(String key) {
        return sharedPreferences.getString(key,"Schooly");
    }

    public void setValue(String key,String value) {
        editor.putString(key,value);
        editor.commit();
    }


    public void removeAlert() {
        progressDialog.dismiss();
    }

    public void showAlert(String message) {
        try{
            progressDialog.setMessage(message);
            progressDialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public boolean needHelp() {
        if(getValue("help")!=null && getValue("help").equals("no")){
            return false;
        }
        return true;
    }

    public void toast(String message) {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

    public String getType() {
        if(getValue("deptType")==null){
            return "schoolName";
        }else if(getValue("deptType").equals("schoolName")){
            return "schoolName";
        }else {
            return getValue("uid");
        }
    }

    public void test(String message) {
        if(BuildConfig.DEBUG){
            Toast.makeText(context,message,Toast.LENGTH_LONG).show();
        }
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }


    public boolean haveValue(String key) {
        return !getValue(key).equals(getValue("default"));
    }

    public void log(String message) {
        Log.e(context.getClass().getSimpleName(),message);
    }

}
