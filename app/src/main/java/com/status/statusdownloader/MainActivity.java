package com.status.statusdownloader;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.status.statusdownloader.music.MusicFragment;
import com.status.statusdownloader.status.StatusFragment;
import com.status.statusdownloader.trends.TrendsFragment;
import com.status.statusdownloader.utility.SavedData;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_status:
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, statusFragment);
                    ft.commit();
                    return true;
                case R.id.navigation_trends:
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container,trendsFragment);
                    ft.commit();


                    return true;
                case R.id.navigation_music:
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container,musicFragment);
                    ft.commit();
                    return true;
            }
            return false;
        }
    };

    private StatusFragment statusFragment;
    private MusicFragment musicFragment;
    private TrendsFragment trendsFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        statusFragment=new StatusFragment();
        trendsFragment=new TrendsFragment();
        musicFragment= MusicFragment.getMusicFragment();


        try {
            if(getIntent().getExtras().getString("trends").equals("trends")){
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container,trendsFragment);
                ft.commit();

            }else if(getIntent().getExtras().getString("trends").equals("music")){
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container,musicFragment);
                ft.commit();

            } else{
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container,statusFragment);
                ft.commit();
            }
                }catch (Exception e){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container,statusFragment);
            ft.commit();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            statusFragment.loadSong();
        }else{
            new SavedData(this).toast("please grant permission");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        File rootPath = new File(Environment.getExternalStorageDirectory(), "Delete");
        deleteRecursive(rootPath);
    }
    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}
