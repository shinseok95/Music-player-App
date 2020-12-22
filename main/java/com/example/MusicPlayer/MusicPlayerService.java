package com.example.MusicPlayer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MusicPlayerService extends Service{

    private static final String ACTION_PLAY = "PLAY";
    private static final String ACTION_PAUSE = "PAUSE";
    private static final String ACTION_NEXT = "NEXT";
    private static final String ACTION_PREVIOUS = "PREVIOUS";
    private static final String START_FOREGROUND_ACTION = "START_FOREGROUND_ACTION";

    private static final String CHANNEL_ID = "music_player";
    private static final int PLAYER_ID = 1;

    Audio music=null;
    ArrayList<Audio> musicList=null;
    private int position = 0;
    private boolean isMusicOn = false;
    private MediaPlayer mediaPlayer=null;

    private NotificationManager notificationManager=null;
    private MusicPlayerObserver observer = null;

    private IMusicPlayerService.Stub mBinder = new IMusicPlayerService.Stub() {
        @Override
        public int getCurrentPosition() throws RemoteException {
            return mediaPlayer.getCurrentPosition();
        }

        @Override
        public boolean getCurrentState() throws RemoteException{
            return mediaPlayer.isPlaying();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                isMusicOn = true;
                mediaPlayer.start();
                observer.update();

                notifyMusicChanged();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(isMusicOn) {
                    next();
                    observer.update();
                }
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                isMusicOn = false;
                observer.update();
                return false;
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mediaPlayer) {
                play();
                observer.update();
            }
        });

        // Create channel for notification
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "MUSIC_PLAYER", NotificationManager.IMPORTANCE_MIN);
            notificationManager.createNotificationChannel(notificationChannel);
          }

        observer = MusicPlayerObserver.getInstance(this,notificationManager);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getAction().equals(START_FOREGROUND_ACTION)){

            if(!isMusicOn) {

                position = intent.getIntExtra("MUSIC_POSITION", 0);
                musicList = (ArrayList<Audio>) intent.getSerializableExtra("MUSIC_LIST");
                music = musicList.get(position);
                prepare();

            }else{

                try{
                    position = intent.getIntExtra("MUSIC_POSITION", 0);
                    music = musicList.get(position);
                    stop();

                    Uri musicURI = Uri.withAppendedPath(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + music.getId());
                    mediaPlayer.setDataSource(this,musicURI);

                    mediaPlayer.prepareAsync();

                }catch (Exception e){}
            }
        }
        else if(intent.getAction().equals(ACTION_PLAY)){
            if(isMusicOn)
                play();
        }
        else if(intent.getAction().equals(ACTION_PAUSE)){
            if(isMusicOn)
                pause();

        }
        else if(intent.getAction().equals(ACTION_NEXT)){
            if(isMusicOn)
                next();

        }
        else if(intent.getAction().equals(ACTION_PREVIOUS)){
            if(isMusicOn)
                previous();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        observer.remove();
        observer = null;

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void play(int position){
        this.position = position;
        music = musicList.get(position);
        stop();
        prepare();
    }

    private void play(){
        if(isMusicOn) {
            mediaPlayer.start();
            observer.update();
        }
    }

    private void prepare(){
        try{
            notifyMusicChanged();

            Uri musicURI = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + music.getId());
            mediaPlayer.setDataSource(this,musicURI);

            mediaPlayer.prepareAsync();

        }catch (Exception e){ }
    }

    private void stop(){
        mediaPlayer.stop();
        mediaPlayer.reset();
    }

    private void pause(){
        if(isMusicOn) {
            mediaPlayer.pause();
            observer.update();
        }
    }

    private void next(){
        if(position < musicList.size()-1)
            position++;
        else
            position=0;
        play(position);
    }
    private void previous() {
        if(position>0 && position<musicList.size())
            position--;
        else
            position = musicList.size()-1;
        play(position);
    }

    private void notifyMusicChanged(){
        Intent intent = new Intent();
        intent.setAction("CHANGED_MUSIC");
        intent.putExtra("MUSIC_POSITION",position);
        sendBroadcast(intent);
    }

    MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }

    ArrayList<Audio> getMusicList(){
        return musicList;
    }

    Audio getMusic(){
        return music;
    }

    int getPosition(){
        return position;
    }
}
