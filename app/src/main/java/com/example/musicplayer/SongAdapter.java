package com.example.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    public SongAdapter(Context c,ArrayList<Song> theSongs)
    {
        songs=theSongs;
        songInf=LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LinearLayout songlayout=(LinearLayout)songInf.inflate(R.layout.song_layout,viewGroup,false);
        TextView songView=(TextView)songlayout.findViewById(R.id.title);
        TextView artistView=(TextView)songlayout.findViewById(R.id.artistName);
        Song currSong=songs.get(i);
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        songlayout.setTag(i);
        return songlayout;
    }
}
