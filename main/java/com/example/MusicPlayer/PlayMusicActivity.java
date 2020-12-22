package com.example.MusicPlayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PlayMusicActivity extends AppCompatActivity {

    private static final String ACTION_PLAY = "PLAY";
    private static final String ACTION_PAUSE = "PAUSE";
    private static final String ACTION_NEXT = "NEXT";
    private static final String ACTION_PREVIOUS = "PREVIOUS";
    private static final String START_FOREGROUND_ACTION = "START_FOREGROUND_ACTION";
    private Uri uri = Uri.parse("content://media/external/audio/albumart");

    private int position;
    private Intent intent;
    private Intent serviceIntent;

    private boolean isFirstCreated = true;
    private boolean isBounding = false;
    private boolean isMusicOn = true;

    private ArrayList<Audio> musicList;

    private ImageView albumArt;
    private ImageView playOrPauseIcon;
    private TextView title;
    private TextView nowTime;
    private TextView totalTime;
    private ProgressBar progressBar;

    private IntentFilter intentFilter;
    private IMusicPlayerService mBinder=null;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBinder = IMusicPlayerService.Stub.asInterface(iBinder);
            isBounding = true;

            try {
                if(!mBinder.getCurrentState()) {
                    playOrPauseIcon.setImageResource(R.drawable.play);

                    try {
                        progressBar.setProgress(mBinder.getCurrentPosition());
                        nowTime.setText(DateFormat.format("mm:ss",mBinder.getCurrentPosition()));
                    }catch (RemoteException e){}

                    isMusicOn = false;
                }
                else
                    isMusicOn = true;
                progressBarHandler.sendEmptyMessage(0);
            }catch (RemoteException e){}
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBounding = false;
        }
    };


    Handler progressBarHandler = new Handler() {
        public void handleMessage(Message msg) {

            if (isBounding&&isMusicOn) {
                try {
                    progressBar.setProgress(mBinder.getCurrentPosition());
                    nowTime.setText(DateFormat.format("mm:ss",mBinder.getCurrentPosition()));
                }catch (RemoteException e){}
            }
            progressBarHandler.sendEmptyMessageDelayed(0, 200);
        }
    };

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(isBounding) {
                try {
                    if (!mBinder.getCurrentState())
                        isMusicOn = false;
                    else
                        isMusicOn = true;
                } catch (RemoteException e) {
                }
            }

            paintMusic(musicList, intent.getIntExtra("MUSIC_POSITION", 0),isFirstCreated);
            position = intent.getIntExtra("MUSIC_POSITION", 0);
            progressBarHandler.sendEmptyMessage(0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_play_music);
        setLayout();

        intentFilter = new IntentFilter();
        intentFilter.addAction("CHANGED_MUSIC");
        registerReceiver(receiver,intentFilter);

        intent = getIntent();
        position = intent.getIntExtra("MUSIC_POSITION",0);
        musicList = (ArrayList<Audio>)intent.getSerializableExtra("MUSIC_LIST");

        paintMusic(musicList,position,isFirstCreated);
        isFirstCreated=false;

        serviceIntent = new Intent(this, MusicPlayerService.class);
        serviceIntent.setAction(START_FOREGROUND_ACTION);
        serviceIntent.putExtra("MUSIC_POSITION", position);
        serviceIntent.putExtra("MUSIC_LIST", musicList);

        // If this is first playing, start foreground service
        // else, notification click -> service is already starting

        new Thread(){
            public void run(){
                bindService(serviceIntent,connection,BIND_AUTO_CREATE);

                if(intent.getBooleanExtra("IS_FIRST_PLAY",true)) {
                    startService(serviceIntent);
                }
                else {
                    isMusicOn=true;
                    progressBarHandler.sendEmptyMessage(0);
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unbindService(connection);
        serviceIntent=null;
        finish();
    }

    public void onClick(View view) {
        Intent commandIntent;

        switch (view.getId()){

            case R.id.playMusicPreviousID:

                if(!isMusicOn)
                    isMusicOn = true;

                commandService(ACTION_PREVIOUS);
                progressBarHandler.sendEmptyMessage(0);
                break;

            case R.id.playMusicPlayOrPauseID:

                if(isMusicOn){
                    isMusicOn= false;
                    playOrPauseIcon.setImageResource(R.drawable.play);
                    commandService(ACTION_PAUSE);
                }else{
                    isMusicOn = true;
                    playOrPauseIcon.setImageResource(R.drawable.pause);

                    commandService(ACTION_PLAY);
                    progressBarHandler.sendEmptyMessage(0);
                }
                break;

            case R.id.playMusicNextID:

                if(!isMusicOn)
                    isMusicOn = true;

                commandService(ACTION_NEXT);
                progressBarHandler.sendEmptyMessage(0);
                break;
        }
    }

    public synchronized void paintMusic(ArrayList<Audio> audios, int musicPosition,boolean isFirstCreate){

        Audio music = audios.get(musicPosition);

        if((position != musicPosition) || isFirstCreated){
            Uri albumArtUri = ContentUris.withAppendedId(uri, music.getAlbumId());
            Picasso.get().load(albumArtUri).error(R.drawable.tmpimage).into(albumArt);

            title.setText(music.getTitle());
            progressBar.setMax((int)music.getDuration());
            totalTime.setText(DateFormat.format("mm:ss",music.getDuration()));
        }
        title.setText(music.getTitle());
        progressBar.setMax((int)music.getDuration());
        totalTime.setText(DateFormat.format("mm:ss",music.getDuration()));
        if(isBounding) {
            try {
                if (isMusicOn)
                    playOrPauseIcon.setImageResource(R.drawable.pause);
                else
                    playOrPauseIcon.setImageResource(R.drawable.play);

                progressBar.setProgress(mBinder.getCurrentPosition());
                nowTime.setText(DateFormat.format("mm:ss",mBinder.getCurrentPosition()));
            }catch (RemoteException e){}
        }
        else {
            progressBar.setProgress(0);
            nowTime.setText("00:00");
            playOrPauseIcon.setImageResource(R.drawable.pause);
        }
    }

    private void setLayout(){
        albumArt = (ImageView)findViewById(R.id.playMusicAlbumArtID);
        playOrPauseIcon = (ImageView)findViewById(R.id.playMusicPlayOrPauseID);
        title = (TextView)findViewById(R.id.playMusictitleID);
        nowTime = (TextView)findViewById(R.id.playMusicNowTimeID);
        totalTime = (TextView)findViewById(R.id.playMusicTotalTimeID);
        progressBar =(ProgressBar)findViewById(R.id.playMusicProgressID);
    }

    private void commandService(String ACTION){

        Intent command;
        command = new Intent(this,MusicPlayerService.class);
        command.setAction(ACTION);
        startService(command);
    }
}
