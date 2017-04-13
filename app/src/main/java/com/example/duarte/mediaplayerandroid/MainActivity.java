package com.example.duarte.mediaplayerandroid;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer player;
    private MediaPlayer nextPlayer;
    private boolean isPlaying;
    private TextView textViewTime;
    private long duration;
    private long currentTime;

    private int position;
    private String[] items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        items = bundle.getStringArray("items");
        position = bundle.getInt("position");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewTime = (TextView) findViewById(R.id.textViewTime);
        if(savedInstanceState != null){
            duration = savedInstanceState.getLong("duration");
            currentTime = savedInstanceState.getLong("currentTime");
            isPlaying = savedInstanceState.getBoolean("isPlaying");

            if(isPlaying){
                playMusic(null);
            }
        }

    }

    public void onSaveInstanceState(Bundle output){
        super.onSaveInstanceState(output);

        output.putLong("duration", duration);
        output.putLong("currentTime", currentTime);
        output.putBoolean("isPlaying",isPlaying);
    }

    public void onDestroy(){
        super.onDestroy();
        if(player != null){
            player.stop();
            player.release();
            player = null;
        }
    }

    public void onPause(){
        isPlaying = false;
        super.onPause();
        if(player != null){
            duration = player.getDuration();
            currentTime = player.getCurrentPosition();
        }
    }

    public void onStop(){
        isPlaying = false;
        super.onStop();
    }

    public void playMusic(View view){
        if(player == null){
            try {
                //raw
                /*player = MediaPlayer.create(MainActivity.this, R.raw.music1);
                player.start();*/
                //sdCard
                File sdCard = Environment.getExternalStorageDirectory();
                File file = new File(sdCard,items[position]);
                player = new MediaPlayer();
                player.setDataSource(file.getAbsolutePath().toString());
                player.prepareAsync();


                //assets
                /*AssetFileDescriptor asset = getAssets().openFd("Pusho - Te Fuiste ft. Ozuna.mp3");
                player = new MediaPlayer();
                player.setDataSource(asset.getFileDescriptor(), asset.getStartOffset(), asset.getLength());
                player.prepareAsync();*/


                player.setOnCompletionListener(this);
                player.setOnPreparedListener(this);
                player.setOnSeekCompleteListener(this);
                player.setOnErrorListener(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            player.start();
            isPlaying = true;
            updateTimeMusicThred(player, textViewTime);
        }
    }

    public void stopMusic(View view){
        if(player != null){
            player.stop();
            player.release();;
            player = null;
            currentTime = 0;
            textViewTime.setText("");

        }
    }

    public void pauseMusic(View view){
        if(player != null){
            player.pause();
        }
    }

    public void updateTimeMusicThred(final long duration, final long currentTime, final TextView view){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int minute, secund;
                long aux;

                //Duraction
                aux = duration /1000;
                minute = (int) (aux /60);
                secund = (int) (aux %60);
                String sDuraction = minute < 10 ? "0"+minute : minute+"";
                sDuraction += ":"+(secund < 10 ? "0"+secund : secund);

                //CurrentTime
                aux = currentTime /1000;
                minute = (int) (aux /60);
                secund = (int) (aux %60);
                String scurrentTime = minute < 10 ? "0"+minute : minute+"";
                scurrentTime += ":"+(secund < 10 ? "0"+secund : secund);

                view.setText(sDuraction+" / " + scurrentTime);
            }
        });

    }

    public void updateTimeMusicThred(final MediaPlayer mediaPlayer, final TextView view){
        new Thread(){
            public void run(){
                while(isPlaying){
                    try{
                        updateTimeMusicThred(mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), view);
                        Thread.sleep(1000);
                    }
                    catch (IllegalStateException e){e.printStackTrace();}
                    catch (InterruptedException e){e.printStackTrace();}
                }
            }
        }.start();

    }


    //Listners
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.i("scrip", "onCompletion()");
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.i("scrip", "onError()");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        isPlaying = true;
        Log.i("scrip", "onPrepared()");
        mediaPlayer.start();
        mediaPlayer.setLooping(true);
        //mediaPlayer.setNextMediaPlayer(nextPlayer);
        mediaPlayer.seekTo((int)currentTime);
        updateTimeMusicThred(mediaPlayer, textViewTime);

    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        Log.i("scrip", "onSeekComplete()");
    }
}

