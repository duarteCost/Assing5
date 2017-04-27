package com.example.duarte.mediaplayerandroid;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;



import android.widget.SeekBar; // Jorge
import android.widget.VideoView;

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

    int GLOBAL_TOUCH_POSITION_X = 0;// Jorge
    int GLOBAL_TOUCH_CURRENT_POSITION_X = 0;// Jorge
    VideoView mVideoView2;  // Jorge

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


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                File sdCard = Environment.getExternalStorageDirectory();
                File file = new File(sdCard,items[position]);
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

        Intent returnListAct =new Intent(this, ActivityList.class);
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
                   mVideoView2.seekTo(progress * 1000);
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


        File sdCard = Environment.getExternalStorageDirectory();
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
            Uri uri2 = Uri.parse(uriPath);
            mVideoView2.setVideoURI(uri2);
            mVideoView2.requestFocus();
            mVideoView2.start();
          //  player.stop();
        }
             else
            {

               // String uriPathCD = "android.resource://"+ getPackageName() + "/"+R.raw.giphyCD;
//giphyCD.3g2"
                // Disc_Tunnel_4K_Motion_Background_Loop-3.3gp
                String uriPathCD = "/sdcard/giphyCD.3g2";
                Uri uri = Uri.parse(uriPathCD);
                mVideoView2.setVideoURI(uri);
                mVideoView2.requestFocus();
                mVideoView2.start();




            }
        }



    public void videoStop ()
    {
        File sdCard = Environment.getExternalStorageDirectory();
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
        File sdCard = Environment.getExternalStorageDirectory();
        final File file = new File(sdCard,items[position]);

        VideoView mVideoView2 = (VideoView) findViewById(R.id.videoView1);
        String uriPath = file.getAbsolutePath().toString();

        if(uriPath.endsWith(".mp4")) {

            mVideoView2.pause();
        }
    }


    private String TAG = "Gesto";
    float initialX, initialY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //int action = MotionEventCompat.getActionMasked(event);

        //Number of touches
        int pointerCount = event.getPointerCount();
        if(pointerCount > 1){
            player.stop();
            return true;
        }
        else
        if(pointerCount == 1){
            int action = event.getActionMasked();
            int actionIndex = event.getActionIndex();
            String actionString =  "333";
            TextView tv = (TextView) findViewById(R.id.editText);
            switch (action)
            {
                /*case MotionEvent.ACTION_DOWN:
                    GLOBAL_TOUCH_POSITION_X = (int) m.getX(1);
                    actionString = "DOWN"+" current "+GLOBAL_TOUCH_CURRENT_POSITION_X+" prev "+GLOBAL_TOUCH_POSITION_X;
                    tv.setText(actionString);
                    break;
                case MotionEvent.ACTION_UP:
                    GLOBAL_TOUCH_CURRENT_POSITION_X = 0;
                    actionString = "UP"+" current "+GLOBAL_TOUCH_CURRENT_POSITION_X+" prev "+GLOBAL_TOUCH_POSITION_X;
                    tv.setText(actionString);

                    break;
                case MotionEvent.ACTION_MOVE:
                    GLOBAL_TOUCH_CURRENT_POSITION_X = (int) m.getX(1);
                    int diff = GLOBAL_TOUCH_POSITION_X-GLOBAL_TOUCH_CURRENT_POSITION_X;
                    actionString = "Diff "+diff+" current "+GLOBAL_TOUCH_CURRENT_POSITION_X+" prev "+GLOBAL_TOUCH_POSITION_X;
                    tv.setText(actionString);

                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    GLOBAL_TOUCH_POSITION_X = (int) m.getX(1);
                    actionString = "DOWN"+" current "+GLOBAL_TOUCH_CURRENT_POSITION_X+" prev "+GLOBAL_TOUCH_POSITION_X;
                    tv.setText(actionString);
                    player.stop();

                    break;
                default:
                    actionString = "";
                    return true;*/

                case MotionEvent.ACTION_DOWN:
                    initialX = event.getX();
                    initialY = event.getY();

                    Log.d(TAG, "Action was DOWN");
                    tv.setText("Action was DOWN");
                    break;

                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "Action was MOVE");
                    tv.setText("Action was MOVE");
                    break;

                case MotionEvent.ACTION_UP:
                    float finalX = event.getX();
                    float finalY = event.getY();

                    Log.d(TAG, "Action was UP");
                    tv.setText("Action was UP");

                    if (initialX < finalX) {
                        Log.d(TAG, "Left to Right swipe performed");
                        tv.setText("Left to Right swipe performed");
                        nextMusic(null);
                    }

                    if (initialX > finalX) {
                        Log.d(TAG, "Right to Left swipe performed");
                        tv.setText("Right to Left swipe performed");
                        prevMusic(null);

                    }

                    if (initialY < finalY) {
                        Log.d(TAG, "Up to Down swipe performed");
                        tv.setText("Up to Down swipe performed");
                    }

                    if (initialY > finalY) {
                        Log.d(TAG, "Down to Up swipe performed");
                        tv.setText("Down to Up swipe performed");
                    }

                    break;

                case MotionEvent.ACTION_CANCEL:
                    Log.d(TAG,"Action was CANCEL");
                    tv.setText("Action was CANCEL");
                    break;

                case MotionEvent.ACTION_OUTSIDE:
                    Log.d(TAG, "Movement occurred outside bounds of current screen element");
                    tv.setText("Movement occurred outside bounds of current screen element");

                    break;
            }

            pointerCount = 0;
            return true;
        }
        else {
            GLOBAL_TOUCH_POSITION_X = 0;
            GLOBAL_TOUCH_CURRENT_POSITION_X = 0;
            return true;
        }

    }
// Jorge



}

