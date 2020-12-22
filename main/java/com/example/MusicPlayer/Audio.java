package com.example.MusicPlayer;

import java.io.Serializable;

public class Audio implements Serializable {

    private long id;
    private long albumId;
    private long duration;
    private String title;

    public long getId(){
        return id;
    }

    public long getAlbumId(){
        return albumId;
    }

    public long getDuration(){
        return duration;
    }

    public String getTitle(){
        return title;
    }

    public void setId(long id){
        this.id=id;
    }

    public void setAlbumId(long albumId){
        this.albumId = albumId;
    }

    public void setDuration(long duration){
        this.duration= duration;
    }

    public void setTitle(String title){
        this.title = title;
    }
}
