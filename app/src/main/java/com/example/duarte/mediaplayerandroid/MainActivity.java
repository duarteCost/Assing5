package com.example.duarte.mediaplayerandroid;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;


import android.widget.SeekBar; // Jorge
import android.widget.VideoView;

import static android.R.attr.action;

public class MainActivity extends Activity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer player;
    private boolean isPlaying;
    private TextView textViewTime;
    private TextView musicTitle;
    private long duration;
    private long currentTime;
    private int maxPosition;
    private volatile Thread playingMusic;

    private SeekBar seekBar; // Jorge
    Button clk;// Jorge
    VideoView videov;// Jorge

    private int GLOBAL_TOUCH_POSITION_X = 0;// Jorge
    private int GLOBAL_TOUCH_CURRENT_POSITION_X = 0;// Jorge
    private VideoView mVideoView2;  // Jorge
    private boolean videoPlay;


    private float x1,x2;
    private static final int MIN_DISTANCE = 500;

    private Button playPause;
    private int position;
    private String[] items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        items = bundle.getStringArray("items");
        position = bundle.getInt("position");
        isPlaying = bundle.getBoolean("isPlaying");
        maxPosition = items.length;

        startService(new Intent(this, ServicePlayer.class));// Jorge


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT >= 21) { // Jorge
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.Black_F2));
        }

        textViewTime = (TextView) findViewById(R.id.textViewTime);
        /*if(savedInstanceState != null){
            duration = savedInstanceState.getLong("duration");
            currentTime = savedInstanceState.getLong("currentTime");
            isPlaying = savedInstanceState.getBoolean("isPlaying");

            if(!isPlaying){
                playMusic(null);
            }
        }*/



        initializeViews();  // Jorge
        handleSeekbar(); // Jorge
        seekBar.setMax((int) 20 / 1000); // Jorge

        if(isPlaying){
            stopMusic(null);
        }
        else
        {
            playMusic(null);
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
            videoStop(); // Jorge
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
                File sdCard = getStoragePath();
                File file = new File(sdCard, items[position]);
                musicTitle = (TextView)findViewById(R.id.musicTitle);
                musicTitle.setText(items[position]);
                musicTitle.setHorizontallyScrolling(true);
                /*if(isPlaying){
                    player.stop();
                    player.release();
                }*/
                player = new MediaPlayer();
                player.setDataSource(file.getAbsolutePath().toString());
                isPlaying = true;
                playPause = (Button)findViewById(R.id.playPause);
                playPause.setBackgroundResource(R.drawable.pause);
              //  playPause.setText("Pause");


                videoPlay ();// Jorge
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
            videoPlay ();// Jorge
            updateTimeMusicThred(player, textViewTime);




        }
    }

    public void stopMusic(View view){
        if(player != null){
            player.stop();
            videoStop(); // Jorge
            player.release();;
            player = null;
            currentTime = 0;
            isPlaying = false;
            playPause = (Button)findViewById(R.id.playPause);
            playPause.setBackgroundResource(R.drawable.play2);
            //playPause.setText("Play");
            textViewTime.setText("");

        }
    }

    public void pauseMusic(View view){
        if(player != null){
            player.pause();

        }
    }

    public void checkState(View view){
        if(isPlaying){
            isPlaying = false;
            playPause = (Button)findViewById(R.id.playPause);
            playPause.setBackgroundResource(R.drawable.play2);
            //playPause.setText("Play");
            videoPause();// Jorge
            pauseMusic(null);
        }
        else
        {
            isPlaying = true;
            playPause = (Button)findViewById(R.id.playPause);
            playPause.setBackgroundResource(R.drawable.pause);
           // playPause.setText("Pause");
            videoPause();// Jorge
            playMusic(null);
        }
    }

    public void verifyArray(String state){
        if(state.equals("next"))
        {
            if(position == maxPosition-1)
            {
                position = 0;
                return;
            }
            else
            {
                position ++;
                return;
            }
        }else if(state.equals("prev"))
        {
            if(position == 0)
            {
                position = maxPosition-1;
                return;
            }
            else
            {
                position--;
                return;
            }
        }
    }

    public void nextMusic(View view){
        if(isPlaying){
            isPlaying = false;
            verifyArray("next");
        }
        else
        {
            verifyArray("next");
        }
        stopMusic(null);
        playPause = (Button)findViewById(R.id.playPause);
        playPause.setBackgroundResource(R.drawable.pause);
        //playPause.setText("Pause");
        playMusic(null);
    }

    public void prevMusic(View view){
        if(isPlaying){
            isPlaying = false;
            verifyArray("prev");
        }
        else
        {
            verifyArray("prev");
        }
        stopMusic(null);
        playPause = (Button)findViewById(R.id.playPause);
        playPause.setBackgroundResource(R.drawable.pause);
       // playPause.setText("Pause");
        playMusic(null);
    }

    public void returnMenu(View view){
        //startActivity(new Intent(getApplicationContext(),ActivityList.class).putExtra("isPlaying", isPlaying));

        Intent returnListAct =new Intent(this, ListFiles.class);
        returnListAct.putExtra("isPlaying", isPlaying).putExtra("position", position);
        startActivityForResult(returnListAct, 1);
        //returnListAct.putExtra("isPlaying", isPlaying);
        //setResult(Activity.RESULT_OK,returnListAct);
        //finish();
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
                secund = (int) (aux %60)-1;
                String sDuraction = minute < 10 ? "0"+minute : minute+"";
                sDuraction += ":"+(secund < 10 ? "0"+secund : secund);



                //CurrentTime
                aux = currentTime /1000;
                minute = (int) (aux /60);
                secund = (int) (aux %60);
                String scurrentTime = minute < 10 ? "0"+minute : minute+"";
                scurrentTime += ":"+(secund < 10 ? "0"+secund : secund);

                view.setText(position+1+" / "+maxPosition +"    "+sDuraction+" / " + scurrentTime);

                //Jorge
                seekBar.setMax((int) duration / 1000);
                int mCurrentPosition = player.getCurrentPosition() / 1000;
                seekBar.setProgress(mCurrentPosition);
                // Fim Jorge


                if((duration/1000)<=((currentTime/1000)+1))
                {
                    nextMusic(null);
                }


            }
        });

    }

    public void updateTimeMusicThred(final MediaPlayer mediaPlayer, final TextView view){

        this.playingMusic = new Thread(){
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
        };
        playingMusic.start();

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
        //mediaPlayer.setLooping(true);
        //mediaPlayer.setNextMediaPlayer(nextPlayer);
        mediaPlayer.seekTo((int)currentTime);
        updateTimeMusicThred(mediaPlayer, textViewTime);




    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        Log.i("scrip", "onSeekComplete()");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                isPlaying=data.getBooleanExtra("isPlaying",isPlaying);
                items = data.getStringArrayExtra("items");
                maxPosition = items.length;
                position = data.getIntExtra("position", position);
                if(isPlaying){
                    stopMusic(null);
                    playMusic(null);
                }
            }
        }
    }//onActivityResult


    // Jorge
    private void handleSeekbar(){
      seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           @Override
           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               if (player != null && fromUser) {
                   player.seekTo(progress * 1000);
                //   mVideoView2.seekTo(progress * 1000);
               }
              if (videoPlay && fromUser){
                   // mVideoView2.seekTo(progress * 1000);
               }

            }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {

          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {

          }


        });
    }

    private void initializeViews(){

        seekBar = (SeekBar) findViewById(R.id.seekbar);


        getWindow().setFormat(PixelFormat.UNKNOWN);
        //displays a video file


    }

    public void videoPlay ()
    {


        File sdCard = getStoragePath();
        final File file = new File(sdCard,items[position]);
        getWindow().setFormat(PixelFormat.UNKNOWN);
        final VideoView mVideoView2 = (VideoView) findViewById(R.id.videoView1); // Jorge


        mVideoView2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                // restart on completion

                if(isPlaying) {
                    mVideoView2.start();
                }
            }
        });


        String uriPath = file.getAbsolutePath().toString();

         if(uriPath.endsWith(".mp4")) {
             videoPlay = true;
            Uri uri2 = Uri.parse(uriPath);
            mVideoView2.setVideoURI(uri2);
            mVideoView2.requestFocus();
            mVideoView2.start();
          //  player.stop();
        } else
            {
                videoPlay = false;
               // String uriPathCD = "android.resource://"+ getPackageName() + "/"+R.raw.giphyCD2;
//giphyCD.3gp"
                // Disc_Tunnel_4K_Motion_Background_Loop-3.3gp
                String uriPathCD = getStoragePath()+"/giphyCD.3gp";
                Uri uri = Uri.parse(uriPathCD);
                mVideoView2.setVideoURI(uri);
                mVideoView2.requestFocus();
                mVideoView2.start();




            }
        }



    public void videoStop ()
    {
        File sdCard = getStoragePath();
        final File file = new File(sdCard,items[position]);

        VideoView mVideoView2 = (VideoView) findViewById(R.id.videoView1);
        String uriPath = file.getAbsolutePath().toString();

        if(uriPath.endsWith(".mp4")) {

            Uri uri2 = Uri.parse(uriPath);
            mVideoView2.setVideoURI(uri2);
            mVideoView2.requestFocus();
            mVideoView2.stopPlayback();
        }
    }



    public void videoPause ()
    {
        File sdCard = getStoragePath();
        final File file = new File(sdCard,items[position]);

        VideoView mVideoView2 = (VideoView) findViewById(R.id.videoView1);
        String uriPath = file.getAbsolutePath().toString();

            mVideoView2.pause();

    }







    @Override
    public boolean onTouchEvent(MotionEvent event) {


        if(event.getPointerCount() > 1){
            player.pause();

        }
        if(event.getPointerCount() == 1) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    player.pause();
                    x1 = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    x2 = event.getX();
                    float deltaX = x2 - x1;
                    if (Math.abs(deltaX) > MIN_DISTANCE) {

                        if (deltaX < 0) {
                            nextMusic(null);
                            return true;
                        }
                        if (deltaX > 0) {
                            prevMusic(null);
                            return true;
                        }

                    }


                    break;
            }
        }
        return super.onTouchEvent(event);

    }
// Jorge

    public File getStoragePath() {
        String removableStoragePath;
        File fileList[] = new File("/storage/").listFiles();
        for (File file : fileList) {
            if(!file.getAbsolutePath().equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath()) && file.isDirectory() && file.canRead()) {
                return file;
            }
        }
        return Environment.getExternalStorageDirectory();
    }





    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_HEADSETHOOK){
            //handle click
           // btnToOpenMic();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    private void btnToOpenMic() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Change your music");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_OUTPUT);
        } catch (ActivityNotFoundException tim) {

        }
    }
    private Button openMic;
    private TextView showVoiceText;
    private final int REQ_CODE_SPEECH_OUTPUT = 0;

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_OUTPUT && resultCode == RESULT_OK) {
            ArrayList<String> voiceInText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            showVoiceText.setText(voiceInText.get(0));
            btnToOpenMic();
        }

    }*/

}



