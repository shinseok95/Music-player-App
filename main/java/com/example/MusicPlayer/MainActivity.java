package com.example.MusicPlayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_FOREGROUND_SERVICE=1000;
    private static final int PERMISSION_READ_EXTERNAL_STORAGE=1001;

    private Cursor c = null;
    private ArrayList<Audio> musicList =null;
    private ListView listView =null;
    private BaseAdapter adapter = null;

    private String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        listView = (ListView)findViewById(R.id.listViewID);
        musicList = new ArrayList<Audio>();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_READ_EXTERNAL_STORAGE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                runMediaStoreProvider();
            else
                Toast.makeText(MainActivity.this,"미디어 접근이 필요합니다.",Toast.LENGTH_SHORT).show();
        }
    }

    public void checkPermission(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, PERMISSION_FOREGROUND_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_EXTERNAL_STORAGE);
        else
            runMediaStoreProvider();
    }

    private void runMediaStoreProvider(){
        // MediaStore Provider
        new Thread() {
            public void run() {

                c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, selection, null, null);

                if (c !=null && c.getCount() >0) {

                    while (c.moveToNext()) {

                        Audio audio = new Audio();
                        audio.setId(c.getLong(c.getColumnIndex(MediaStore.Audio.AudioColumns._ID)));
                        audio.setAlbumId(c.getLong(c.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID)));
                        audio.setDuration(c.getLong(c.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));
                        audio.setTitle(c.getString(c.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)));
                        musicList.add(audio);
                    }
                }

                adapter = new Adapter(MainActivity.this,musicList);
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(adapter);
                    }
                });
            }
        }.start();
    }
}
