package com.example.MusicPlayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import com.squareup.picasso.Picasso;

public class MusicPlayerObserver {

    private static final String ACTION_PLAY = "PLAY";
    private static final String ACTION_PAUSE = "PAUSE";
    private static final String ACTION_NEXT = "NEXT";
    private static final String ACTION_PREVIOUS = "PREVIOUS";
    private static final String CHANNEL_ID = "music_player";
    private static final int PLAYER_ID = 1;

    private Uri uri = Uri.parse("content://media/external/audio/albumart");

    private Audio music;
    private MediaPlayer mediaPlayer = null;
    private boolean isMusicStarting = false;
    private MusicPlayerService musicPlayer=null;
    private NotificationManager notificationManager=null;
    private MusicPlayerNotificationBuilder musicPlayerNotificationBuilder = null;
    private static MusicPlayerObserver observer = null;


    // Singleton
    public static MusicPlayerObserver getInstance(MusicPlayerService musicPlayer, NotificationManager notificationManager){

        if(observer == null)
            observer = new MusicPlayerObserver(musicPlayer,notificationManager);

        return observer;
    }

    private MusicPlayerObserver(MusicPlayerService musicPlayer, NotificationManager notificationManager){
        this.musicPlayer = musicPlayer;
        this.notificationManager = notificationManager;
        this.mediaPlayer = musicPlayer.getMediaPlayer();
    }

    // If State of music player is changed, reset notification
    private class MusicPlayerNotificationBuilder extends AsyncTask<Void,Object, Notification> {

        RemoteViews remoteViews=null;
        PendingIntent pendingIntent=null;
        NotificationCompat.Builder notificationBuilder=null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            remoteViews = createRemoteView();

            Intent notificationIntent = new Intent(musicPlayer,PlayMusicActivity.class);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            notificationIntent.putExtra("MUSIC_POSITION",musicPlayer.getPosition());
            notificationIntent.putExtra("MUSIC_LIST",musicPlayer.getMusicList());
            notificationIntent.putExtra("IS_FIRST_PLAY",false);
            notificationIntent.setAction(Long.toString(System.currentTimeMillis()));
            pendingIntent = PendingIntent.getActivity(musicPlayer,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder = new NotificationCompat.Builder(musicPlayer,CHANNEL_ID);

            if(mediaPlayer.isPlaying())
                notificationBuilder.setSmallIcon(R.drawable.play);
            else
                notificationBuilder.setSmallIcon(R.drawable.pause);

            notificationBuilder.setOngoing(true);
            notificationBuilder.setContentIntent(pendingIntent);
            notificationBuilder.setContent(remoteViews);

            Notification notification = notificationBuilder.build();

            if(!isMusicStarting){
                isMusicStarting = true;
                musicPlayer.startForeground(PLAYER_ID,notification);
            }
        }

        @Override
        protected Notification doInBackground(Void... voids) {
            notificationBuilder.setContent(remoteViews);
            notificationBuilder.setContentIntent(pendingIntent);
            notificationBuilder.setPriority(Notification.PRIORITY_MAX);
            Notification notification = notificationBuilder.build();

            publishProgress(remoteViews,notification);

            return notification;
        }

        @Override
        protected void onProgressUpdate(Object... objects) {

            RemoteViews remoteViews = (RemoteViews)objects[0];
            Notification notification =(Notification)objects[1];

            updateRemoteView(remoteViews,notification);
        }

        @Override
        protected void onPostExecute(Notification notification) {
            super.onPostExecute(notification);
            try {
                notificationManager.notify(PLAYER_ID,notification);
            }catch (Exception e){}
        }

        private RemoteViews createRemoteView(){

            RemoteViews remoteView = new RemoteViews(musicPlayer.getPackageName(),R.layout.notification_layout);

            Intent playIntent = new Intent(musicPlayer,MusicPlayerService.class);
            playIntent.setAction(ACTION_PLAY);

            Intent pauseIntent = new Intent(musicPlayer,MusicPlayerService.class);
            pauseIntent.setAction(ACTION_PAUSE);

            Intent nextIntent = new Intent(musicPlayer,MusicPlayerService.class);
            nextIntent.setAction(ACTION_NEXT);

            Intent previousIntent = new Intent(musicPlayer,MusicPlayerService.class);
            previousIntent.setAction(ACTION_PREVIOUS);

            Intent albumArtIntent = new Intent(musicPlayer,PlayMusicActivity.class);
            albumArtIntent.putExtra("MUSIC_POSITION",musicPlayer.getPosition());
            albumArtIntent.putExtra("MUSIC_LIST",musicPlayer.getMusicList());
            albumArtIntent.putExtra("IS_FIRST_PLAY",false);
            albumArtIntent.setAction(Long.toString(System.currentTimeMillis()));
            albumArtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent playPendingIntent = PendingIntent.getService(musicPlayer,0,playIntent,0);
            PendingIntent pausePendingIntent = PendingIntent.getService(musicPlayer,0,pauseIntent,0);
            PendingIntent nextPendingIntent = PendingIntent.getService(musicPlayer,0,nextIntent,0);
            PendingIntent previousPendingIntent = PendingIntent.getService(musicPlayer,0,previousIntent,0);
            PendingIntent albumArtPendingIntent = PendingIntent.getActivity(musicPlayer,0,albumArtIntent,PendingIntent.FLAG_UPDATE_CURRENT);

            if(mediaPlayer.isPlaying())
                remoteView.setOnClickPendingIntent(R.id.notificationPlayOrNextID,pausePendingIntent);
            else
                remoteView.setOnClickPendingIntent(R.id.notificationPlayOrNextID,playPendingIntent);

            remoteView.setOnClickPendingIntent(R.id.notificationNextID,nextPendingIntent);
            remoteView.setOnClickPendingIntent(R.id.notificationPreviousID,previousPendingIntent);
            remoteView.setOnClickPendingIntent(R.id.notificationAlbumArtID,albumArtPendingIntent);

            return remoteView;
        }

        private synchronized void updateRemoteView(RemoteViews remoteViews,Notification notification){

            music = musicPlayer.getMusic();

            if(mediaPlayer.isPlaying()) {
                remoteViews.setImageViewResource(R.id.notificationPlayOrNextID, R.drawable.pause);
                notifyMusicChanged();
            }
            else {
                remoteViews.setImageViewResource(R.id.notificationPlayOrNextID, R.drawable.play);
                notifyMusicChanged();
            }
            remoteViews.setTextViewText(R.id.notificationTitleID,music.getTitle());

            Uri albumArtUri = ContentUris.withAppendedId(uri,musicPlayer.music.getAlbumId());
            Picasso.get().load(albumArtUri).error(R.drawable.tmpimage).into(remoteViews,R.id.notificationAlbumArtID,PLAYER_ID,notification);
        }
    }

    public void update(){
        cancel();
        musicPlayerNotificationBuilder = new MusicPlayerNotificationBuilder();
        musicPlayerNotificationBuilder.execute();
    }
    public void remove(){
        cancel();
        musicPlayer.stopForeground(true);
        isMusicStarting = false;
    }
    public void cancel(){
        if(musicPlayerNotificationBuilder != null){
            musicPlayerNotificationBuilder.cancel(true);
            musicPlayerNotificationBuilder= null;
        }
    }

    private void notifyMusicChanged(){

        Intent intent = new Intent();
        intent.setAction("CHANGED_MUSIC");
        intent.putExtra("MUSIC_POSITION",musicPlayer.getPosition());
        musicPlayer.sendBroadcast(intent);
    }

}
