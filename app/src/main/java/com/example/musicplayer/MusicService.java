package com.example.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener,MediaPlayer.OnCompletionListener  {

    private final IBinder musicbind=new MusicBinder();

    private MediaPlayer player;

    private ArrayList<Song>songs;

    private int songPosition;

    private String songTitle;

    private static final int NOTIFY_ID=1;

    private boolean shuffle =false;

    private Random rand;

    @Override
    public void onCreate() {
        super.onCreate();
        songPosition=0;
        player=new MediaPlayer();
        initMusicPlayer();
        rand=new Random();
    }

    public void initMusicPlayer()
    {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs)
    {
        songs=theSongs;
    }

    public class MusicBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicbind;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(player.getCurrentPosition()>0)
        {
            mediaPlayer.reset();
            playNext();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        Intent notIntent =new Intent(this,MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt=PendingIntent.getActivity(this,0,notIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O)
        {
            String NOTIFICATION_CHANNEL_ID="com.example.musicplayer";
            String channelName="My Background Service";
            NotificationChannel chan=new NotificationChannel(NOTIFICATION_CHANNEL_ID,channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager!=null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuider=new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
            Notification notification=notificationBuider.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_baseline_play_circle_filled_24)
                    .setContentTitle("Playing")
                    .setContentText(songTitle)
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2,notification);
        }else
        {
            startForeground(1,new Notification());
        }

    }

    public void playSong()
    {
        player.reset();
        Song playSong =songs.get(songPosition);
        songTitle =playSong.getTitle();
        long currsong=playSong.getId();
        Uri tractUri= ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,currsong);

        try{
            player.setDataSource(getApplicationContext(),tractUri);
        }
        catch (Exception e)
        {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();

    }

    public void setSong(int songIndex)
    {
        songPosition=songIndex;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public int getCurrentPosition()
    {
        return player.getCurrentPosition();
    }

    public int getDuration()
    {
        return player.getDuration();
    }

    public boolean isPlaying()
    {
        return player.isPlaying();
    }

    public void pause()
    {
        player.pause();
    }

    public void seekTo(int posn)
    {
        player.seekTo(posn);
    }

    public void start()
    {
        player.start();
    }

    public void playPrev()
    {
        songPosition--;
        if (songPosition < 0)
        {
            songPosition=songs.size()-1;
            playSong();
        }
    }

    public void playNext()
    {
        if(shuffle)
        {
            int newSong=songPosition;
            while(newSong==songPosition)
            {
                newSong=rand.nextInt(songs.size());
            }
            songPosition=newSong;
        }
        else {
            songPosition++;
            if (songPosition >= songs.size())
                songPosition = 0;
        }
        playSong();

    }

    public void setShuffle()
    {
        if(shuffle)
            shuffle=false;
        else
            shuffle=true;
    }


}
