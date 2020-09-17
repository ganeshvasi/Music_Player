package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.MediaController.MediaPlayerControl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements MediaPlayerControl {
    private ArrayList<Song>songArrayList;
    private ListView songView;

    private MusicService musicsrv;
    private Intent playIntent;
    private boolean musicBound =false;

    private MusicController controller;

    private boolean paused=false;
    private boolean playbackpaused =false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                return;
            }
        }

        songView =findViewById(R.id.listOfSongs);
        songArrayList=new ArrayList<Song>();

        getSongsList();

        Collections.sort(songArrayList,new Comparator<Song>()
        {
            public int compare(Song a,Song b)
            {
                return a.getTitle().compareTo(b.getTitle());
            }

        });


        SongAdapter adapter=new SongAdapter(this,songArrayList);

        songView.setAdapter(adapter);

        setController();

    }
    public void getSongsList()
    {
        ContentResolver musicResolver=getContentResolver();
        Uri musicUri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor =musicResolver.query(musicUri,null,null,null,null);
        if(musicCursor!=null && musicCursor.moveToFirst())
        {
            int titleColumn=musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn=musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn=musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            do{
                long thisId=musicCursor.getLong(idColumn);
                String thisTitle=musicCursor.getString(titleColumn);
                String thisArtist=musicCursor.getString(artistColumn);
                songArrayList.add(new Song(thisId,thisTitle,thisArtist));
            }while (musicCursor.moveToNext());
        }
    }


    private ServiceConnection musicConnection =new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicsrv =binder.getService();
            musicsrv.setList(songArrayList);
            musicBound=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicsrv.setList(songArrayList);
            musicBound=false;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null)
        {
            playIntent=new Intent(this,MusicService.class);
            bindService(playIntent,musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void songPicked(View  view)
    {

        musicsrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicsrv.playSong();
        if(playbackpaused)
        {
            setController();
            playbackpaused=false;
        }
        controller.show(0);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.shuffle:
                musicsrv.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicsrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        if(musicBound)
            unbindService(musicConnection);
        stopService(playIntent);
        musicsrv=null;
        super.onDestroy();
    }

    @Override
    public void start() {
        musicsrv.start();
    }

    @Override
    public void pause() {
        playbackpaused=true;
        musicsrv.pause();
    }

    @Override
    public int getDuration() {
       if(musicsrv!=null && musicBound && musicsrv.isPlaying())
       {
           return musicsrv.getDuration();
       }else
       {
           return 0;
       }
    }

    @Override
    public int getCurrentPosition() {
        if(musicsrv!=null && musicBound && musicsrv.isPlaying())
        {
            return musicsrv.getCurrentPosition();
        }else {
            return 0;
        }
    }

    @Override
    public void seekTo(int i) {
            musicsrv.seekTo(i);
    }

    @Override
    public boolean isPlaying() {
        if(musicsrv!=null && musicBound)
            return musicsrv.isPlaying();
        return  false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void setController(){
        controller=new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.listOfSongs));
        controller.setEnabled(true);
    }

    private void playNext()
    {
        musicsrv.playNext();
        if(playbackpaused)
        {
            setController();
            playbackpaused=false;
        }
        controller.show(0);
    }

    private void playPrev()
    {

        musicsrv.playPrev();
        if(playbackpaused)
        {
            setController();
            playbackpaused=false;
        }
        controller.show(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(paused)
        {
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop() {

        controller.hide();
        super.onStop();

    }
}