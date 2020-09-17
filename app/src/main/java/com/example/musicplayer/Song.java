package com.example.musicplayer;

public class Song {

    private long id;

    private String title;

    private String artist;

    public Song(long SongId ,String songTitle ,String songArtist)
    {
        id=SongId;
        title=songTitle;
        artist=songArtist;
    }

    public long getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public String getArtist(){
        return artist;
    }


}
