package com.example.duarte.mediaplayerandroid;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
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
import static android.R.attr.button;
import static android.R.attr.delay;


public class MainActivity extends Activity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer player;  //music and video reproducer
    private boolean isPlaying;  // control if the player is playing
    private TextView textViewTime; //show the music/video time's
    private TextView musicTitle;
    private long duration;     //music/video duration
    private long currentTime;   //music/video current time
    private int maxPosition;    //number of list elements
    private volatile Thread playingMusic;  //control the players status (i.e music/video current time)
    private volatile Thread changeVolume;  // change the volume according the environment noise
    private volatile Thread verifyNoise;   //control the environment noise
    private MediaRecorder recorder;    //allow mic acess
    private double amplitudeDb;     //value of noise amplitude in one certain moment
    private SeekBar volumeSeekbar = null;
    private AudioManager audioManager = null;
    private SeekBar seekBar; // seekBar time
    private final int  REQ_CODE_SPEECH_OUTPUT = 0; //SPEECH OUTPUT
    private static boolean removed = false;
    private LinearLayout.LayoutParams  layout;
    private  boolean stateRecorder = true; // state recorder
    private boolean auxBtnToOpenMic = false; // state SPEECH


    private VideoView mVideoView2;  // video
    private boolean videoPlay; // video state

    private volatile Thread setVolume;


    private float x1,x2; // aux gestures
    private static final int MIN_DISTANCE = 500; //swipe DISTANCE

    private Button playPause;
    private int position;   //Control the current item
    private int countT = 0;  //number of noise measurements
    private String[] items;  //list of items in sd_card
    private double amplitudeDbC = 0;  // sum of noise values
    private int tabSelected; // tab Selected on tableview
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        //get some variables from the ListFiles
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        items = bundle.getStringArray("items");
        position = bundle.getInt("position");
        isPlaying = bundle.getBoolean("isPlaying");
        tabSelected = bundle.getInt("tabSelected");
        maxPosition = items.length;


        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setVolume();

        mVideoView2 = (VideoView) findViewById(R.id.videoView1); // get layout video
        if (android.os.Build.VERSION.SDK_INT >= 21) { // change color bar
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.Black_F2));
        }

        textViewTime = (TextView) findViewById(R.id.textViewTime); initializeViews();  // initialize functions
        handleSeekbar(); // initialize time Seekbar
        seekBar.setMax((int) 20 / 1000); // initialize length Seekbar

        /*
        if(savedInstanceState != null){
            if(savedInstanceState != null){
                duration = savedInstanceState.getLong("duration");
                currentTime = savedInstanceState.getLong("currentTime");
                isPlaying = savedInstanceState.getBoolean("isPlaying");
                maxPosition = savedInstanceState.getInt("maxPosition");
                position = savedInstanceState.getInt("position");
                //countT = savedInstanceState.getInt("countT");
                //amplitudeDb = savedInstanceState.getDouble("amplitudeDb");
                //amplitudeDbC = savedInstanceState.getDouble("amplitudeDbC");
                if(isPlaying){
                    playMusic(null);
                }

            }
        }else
        {
            playMusic(null);
        }*/
        playMusic(null);

        //Initialize the recorder and allow mic access
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile("/dev/null");
        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int orientation=this.getResources().getConfiguration().orientation; // save orientation
        if(orientation==Configuration.ORIENTATION_LANDSCAPE){ // if orientation horizontal
            ORIENTATION_LANDSCAPE(); // change layout
        }
    }



    /*public void onSaveInstanceState(Bundle output){
        super.onSaveInstanceState(output);
        output.putLong("duration", duration);
        output.putLong("currentTime", currentTime);
        output.putBoolean("isPlaying", true);
        output.putInt("maxPosition", maxPosition);
        output.putInt("position", position);
        //output.putInt("countT", countT);
        //output.putDouble("amplitudeDb", amplitudeDb);
        //output.putDouble("amplitudeDbC", amplitudeDbC);

       // recorder.stop();
    }*/

    //////////////////////////
    // When the activity is //
    // destroyed            //
    //////////////////////////
    public void onDestroy(){
        super.onDestroy();
        if(player != null){
            player.stop();
            videoStop(); // Jorge
            player.release();
            player = null;
        }
    }

    //////////////////////////
    // When the activity is //
    // paused               //
    //////////////////////////
    public void onPause(){
        isPlaying = false;
        super.onPause();
        if(player != null){
            duration = player.getDuration();
            currentTime = player.getCurrentPosition();
        }
    }

    //////////////////////////
    // When the activity is //
    // stoped               //
    //////////////////////////
    public void onStop(){
        isPlaying = false;
        super.onStop();
    }

    //////////////////////////
    // Begins the player    //
    //////////////////////////
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
                player = new MediaPlayer();
                isPlaying = true;
                player.setDataSource(file.getAbsolutePath().toString());


                playPause = (Button)findViewById(R.id.playPause);
                playPause.setBackgroundResource(R.drawable.pause);
                player.prepareAsync(); player.setOnCompletionListener(this);
                player.setOnPreparedListener(this);
                player.setOnSeekCompleteListener(this);
                player.setOnErrorListener(this);
                videoPlay ();
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

    //////////////////////////
    // Stop's music and     //
    // with some threads    //
    //////////////////////////
    public void stopMusic(View view){
        if(player != null){
            player.stop();
            videoStop(); // Jorge
            player.release();
            playingMusic.interrupt();
           verifyNoise.interrupt();
            changeVolume.interrupt();
            player = null;
            currentTime = 0;
            isPlaying = false;
            playPause = (Button)findViewById(R.id.playPause);
            playPause.setBackgroundResource(R.drawable.play2);
            //playPause.setText("Play");
            textViewTime.setText("");

        }
    }

    //////////////////////////
    // Pause music          //
    //////////////////////////
    public void pauseMusic(View view){
        if(player != null){
            player.pause();

        }
    }

    //////////////////////////
    // Pause music and      //
    // change the play/     //
    // pause button         //
    //////////////////////////
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
            videoPlay();// Jorge
            playMusic(null);
        }
    }

    ///////////////////////////
    // Check if the next/prev//
    // exists                //
    ///////////////////////////
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

    //////////////////////////
    // Change item to the   //
    // next                 //
    //////////////////////////
    public void nextMusic(View view){

        if(isPlaying){
            stopMusic(null);
            isPlaying = false;
            verifyArray("next");
        }
        else
        {
            verifyArray("next");
        }
        playPause = (Button)findViewById(R.id.playPause);
        playPause.setBackgroundResource(R.drawable.pause);
        //playPause.setText("Pause");
        playMusic(null);
    }

    //////////////////////////
    // Change item to the   //
    // prev                //
    //////////////////////////
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

    //////////////////////////
    // Go back to the List  //
    //////////////////////////
    public void returnMenu(View view){
        Intent returnListAct = new Intent(this, ListFiles.class);
        returnListAct.putExtra("isPlaying", isPlaying).putExtra("tabSelected",tabSelected).putExtra("position", position);
        startActivityForResult(returnListAct, 1);
        //btnToOpenMic();
    }

    //////////////////////////
    // Update music status  //
    // and change automa.   //
    //////////////////////////
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

    //////////////////////////
    // Control the update   //
    // status thread        //
    //////////////////////////
    public void updateTimeMusicThred(final MediaPlayer mediaPlayer, final TextView view){

        this.playingMusic = new Thread(new Runnable() {
            @Override
            public void run() {
                while(isPlaying){
                    try{
                        updateTimeMusicThred(mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), view);
                        Thread.sleep(1000);
                    }
                    catch (IllegalStateException e){e.printStackTrace();}
                    catch (InterruptedException e){e.printStackTrace();}
                }
            }
        });
        playingMusic.start();

    }

    //////////////////////////
    // Respond the volume   //
    // change               //
    //////////////////////////
    private void setVolume() {
        new Thread(new Runnable() {
            @Override
            public void run() {
            while (true) {
                try {

                    volumeSeekbar = (SeekBar) findViewById(R.id.soundSeekbar);
                    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    volumeSeekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                    volumeSeekbar.setProgress(audioManager
                                    .getStreamVolume(AudioManager.STREAM_MUSIC));
                    volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onStopTrackingTouch(SeekBar arg0) {
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar arg0) {
                        }

                        @Override
                        public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {

                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                    progress, 0);
                        }
                    });
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            }
        }).start();

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

    /////////////////////////////////////////
    // Measures the occurrence of noise    //
    // and filter the -infinity values     //
    /////////////////////////////////////////
    public void verifyNoiseThread(){
        this.verifyNoise = new Thread(new Runnable() {
            @Override
            public void run () {
                while(true){
                    try {
                        int amplitude = recorder.getMaxAmplitude();
                        amplitudeDb = 20 * Math.log10((double) Math.abs(amplitude));
                        if(amplitudeDb>=0) {
                            amplitudeDbC = amplitudeDbC + amplitudeDb;
                            countT ++;
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        verifyNoise.start();
    }

    ////////////////////////////////////////////////////////
    // Calculate the noise media and update the volume    //
    ////////////////////////////////////////////////////////
    public void changeVolumeWithNoise(){

        this.changeVolume = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if(amplitudeDbC!=0){
                            double amplitudeNewScale = amplitudeDbC / countT;
                            amplitudeDbC = 0;
                            countT = 0;
                            amplitudeNewScale = amplitudeNewScale / 10;
                            int amplitudeM = (int) Math.round(amplitudeNewScale);
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, amplitudeM, 0);
                            Thread.sleep(8000);
                        }
                        else
                        {
                            Thread.sleep(1000);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        changeVolume.start();
    }

    ////////////////////////////////////////////////////////
    // Initializes the player and all necessary thread's  //
    ////////////////////////////////////////////////////////
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        isPlaying = true;
        Log.i("scrip", "onPrepared()");
        mediaPlayer.start();
        mediaPlayer.seekTo((int)currentTime);
        updateTimeMusicThred(mediaPlayer, textViewTime);
        mediaPlayer.seekTo((int)currentTime);
        updateTimeMusicThred(mediaPlayer, textViewTime);
        if(verifyNoise==null){
            verifyNoiseThread();
        }
        if(changeVolume==null){
            changeVolumeWithNoise();
        }

    }


    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        Log.i("scrip", "onSeekComplete()");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        /////////////////////////////////////////////////
        // when the listFiles return to Main Activity  //
        /////////////////////////////////////////////////
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                isPlaying=data.getBooleanExtra("isPlaying",isPlaying);
                items = data.getStringArrayExtra("items");
                tabSelected = data.getIntExtra("tabSelected", tabSelected);

                maxPosition = items.length;
                position = data.getIntExtra("position", position);
                if(isPlaying){
                    stopMusic(null);
                    playMusic(null);
                }
            }
        }


        //////////////////////////
        // Case btnToOpenMic()  //
        //////////////////////////
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_OUTPUT && resultCode == RESULT_OK) { // result of speech ok

            // array potential speech
            final ArrayList<String> voiceInText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // message command
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            voiceInText.get(0),
                            Toast.LENGTH_LONG)
                            .show();
                }
            });

            executeCommand(voiceInText); // verify word command

            // init recorder
            try {
                recorder.prepare();
                recorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            verifyNoiseThread();
            changeVolumeWithNoise();

            // chnage states
            auxBtnToOpenMic = false;
            stateRecorder = true;


        }else if(auxBtnToOpenMic){ // didn't you get the speech result

            // error message
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Fail",
                            Toast.LENGTH_LONG)
                            .show();
                }
            });

            checkState(null); // play media player


            // init recorder
            try {
                recorder.prepare();
                recorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // chnage states
            auxBtnToOpenMic = false;
            stateRecorder = false;

        }

    }//onActivityResult




    ///////////////////////////////////
    //                               //
    //     Function btnToOpenMic()   //
    //   change state seekbar time   //
    ///////////////////////////////////
    private void handleSeekbar(){
      seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           @Override
           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               if (player != null && fromUser) { // if music
                   player.seekTo(progress * 1000);

               }
              if (videoPlay && fromUser){ // if video
                  VideoView mVideoView2 = (VideoView) findViewById(R.id.videoView1);
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


    //////////////////////////////////////
    //                                  //
    //     Function initializeViews()   //
    //      start functions view        //
    //////////////////////////////////////

    private void initializeViews(){

        seekBar = (SeekBar) findViewById(R.id.seekbar);
        getWindow().setFormat(PixelFormat.UNKNOWN);

    }


    //////////////////////////////////
    //                              //
    //     Function videoPlay()     //
    //    play video or animation   //
    //////////////////////////////////
    public void videoPlay ()
    {


        File sdCard = getStoragePath(); // get path sd card
        final File file = new File(sdCard,items[position]); // get file sd card
        getWindow().setFormat(PixelFormat.UNKNOWN);
        final VideoView mVideoView2 = (VideoView) findViewById(R.id.videoView1); // get VideoView the layout


        mVideoView2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                // restart on completion

                if(isPlaying) {
                    mVideoView2.start();
                }
            }
        });


        String uriPath = file.getAbsolutePath().toString(); // string extension

         if(uriPath.endsWith(".mp4") ) {    // if video
             videoPlay = true;              // change status
             player.setVolume(0,0);         // Player mute
            Uri uri2 = Uri.parse(uriPath);
            mVideoView2.setVideoURI(uri2);
            mVideoView2.requestFocus();
            mVideoView2.start();            // start video
             mVideoView2.seekTo(player.getCurrentPosition()); // set video position player

        } else // if animation
            {

                player.setVolume(100, 100);     // Player not mute

                videoPlay = false;              // change status
                String uriPathCD = "android.resource://"+ getPackageName() + "/"+R.raw.giphycd; // path animation
                Uri uri = Uri.parse(uriPathCD);
                mVideoView2.setVideoURI(uri);
                mVideoView2.requestFocus();
                mVideoView2.start();           // start animation

            }
        }


    //////////////////////////////////
    //                              //
    //     Function videoStop()     //
    //          stop video          //
    //////////////////////////////////
    public void videoStop ()
    {
        File sdCard = getStoragePath(); // get path sd card
        final File file = new File(sdCard,items[position]); // get file sd card

        VideoView mVideoView2 = (VideoView) findViewById(R.id.videoView1);// get VideoView the layout
        String uriPath = file.getAbsolutePath().toString();// string extension


        if(uriPath.endsWith(".mp4")) { // if video
            Uri uri2 = Uri.parse(uriPath);
            mVideoView2.setVideoURI(uri2);
            mVideoView2.requestFocus();
            mVideoView2.stopPlayback();// stop video
        }
    }


    //////////////////////////////////
    //                              //
    //     Function videoPause()    //
    //    pause video or animation  //
    //////////////////////////////////
    public void videoPause ()
    {
        VideoView mVideoView2 = (VideoView) findViewById(R.id.videoView1);// get VideoView the layout
        mVideoView2.pause();// pause video

    }





    //////////////////////////////////
    //                              //
    //     Function onTouchEvent()  //
    //             gestures         //
    //////////////////////////////////
    @Override
    public boolean onTouchEvent(MotionEvent event) {


        if(event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) { // if touch
            if(event.getPointerCount() == 2 && auxBtnToOpenMic == false) // touch with two fingers I can do play or pause
            {
                checkState(null); // chnage state palyer
            }
            else
            if(event.getPointerCount() > 2) { // When I touch with more than two fingers it starts the speech/voice recognizer
                btnToOpenMic(); // call speech/voice recognizer
            }

        }

        if(event.getPointerCount() == 1) { // touch with one finger
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    x2 = event.getX();
                    float deltaX = x2 - x1;
                    if (Math.abs(deltaX) > MIN_DISTANCE) { // if swipe

                        if (deltaX < 0) { // swipe left to right it goes to the next song or video
                            nextMusic(null);
                            return true;
                        }
                        if (deltaX > 0) { // swipe right to left it goes to the previous song or video
                            prevMusic(null);
                            return true;
                        }

                    }


                    break;
            }
        }
        return super.onTouchEvent(event);

    }


    //////////////////////////////////
    //                              //
    //   Function getStoragePath()  //
    //   get psth sd card lg g3     //
    //////////////////////////////////
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



    /////////////////////////////////////
    //                                 //
    //       Function onKeyDown()      //
    //   on click bottun headphones    //
    /////////////////////////////////////
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_HEADSETHOOK){ // if click bottun headphones

            btnToOpenMic(); // call speech/voice recognizer


            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    ////////////////////////////////////////
    //                                    //
    //       Function btnToOpenMic()      //
    //       speech/voice recognizer      //
    ////////////////////////////////////////
    private void btnToOpenMic() {
        auxBtnToOpenMic = true; // change state

        if(stateRecorder) {
            recorder.stop(); // stop recorder
        }

        if(isPlaying){ // if play goes to stop
            checkState(null); // stop player
        }


        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH); // LANGUAGE
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Change your music or video"); // mensage

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_OUTPUT); // REQ_CODE_SPEECH_OUTPUT
        } catch (ActivityNotFoundException tim) {



        }
    }


    ////////////////////////////////////////
    //                                    //
    //       Function executeCommand()    //
    //       search the words command     //
    ////////////////////////////////////////
    public void executeCommand(ArrayList<String> voiceInText){

        if(voiceInText.get(0).contains("next")){ // if word is "next"
            nextMusic(null); // next
        }else if(voiceInText.get(0).contains("prev")){// if word is "prev"
            prevMusic(null); // prev
        }else if(voiceInText.get(0).contains("play")){ // if word is "play"
            search(voiceInText.get(0)); // search file name
        }else if(voiceInText.get(0).contains("stop")){ // if word is "stop"
            checkState(null); // stop
        }else {
            if(isPlaying){
                checkState(null);
            }
        }

    }


    ///////////////////////////////
    //                           //
    //      Function search()    //
    //      search file name     //
    ///////////////////////////////
    public  void search(String text) {

        text = text.substring(5, text.length()); // substring name file
        ArrayList<String> mySongs2 = new ArrayList(); // creat array
        int filePosition = 0;     // creat position
        text = text.toLowerCase(); // text toLowerCase

        for (int i = 0; i < items.length; i++) {
            String name = items[i].toLowerCase(); // name toLowerCase

            if (name.contains(text)) { // if contains
                mySongs2.add(items[i]); // add array
                filePosition=i; // set position
            }
        }

        if(mySongs2.size()==1){ // If the array has only one element
            position = filePosition; // set position
            stopMusic(null); // stop
            playMusic(null); // play file name
        }
    }



    ///////////////////////////////////////////////
    //                                           //
    //      Function onConfigurationChanged()    //
    //           ORIENTATION change              //
    ///////////////////////////////////////////////
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) // if vertical
        {
            ORIENTATION_PORTRAIT();
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) // if horizontal
        {
            ORIENTATION_LANDSCAPE();
        }
    }


    ///////////////////////////////////////////////
    //                                           //
    //      Function ORIENTATION_PORTRAIT()      //
    //           ORIENTATION vertical            //
    ///////////////////////////////////////////////
public void ORIENTATION_PORTRAIT(){ // Adds buttons and NOT FULLSCREEN
    LinearLayout layoutVideo = (LinearLayout) findViewById(R.id.layoutVideo);

    layoutVideo.setLayoutParams(layout);

    if(removed) {
        final LinearLayout list = (LinearLayout) findViewById(R.id.listLine);
        final LinearLayout fullLayout = (LinearLayout) findViewById(R.id.fullLayout);
        final Button n = (Button) findViewById(R.id.MenuButton);

        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        list.setLayoutParams(lp1);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);



        removed = false;
    }

}

    ///////////////////////////////////////////////
    //                                           //
    //      Function ORIENTATION_LANDSCAPE()     //
    //           ORIENTATION horizontal          //
    ///////////////////////////////////////////////
    public void ORIENTATION_LANDSCAPE(){// remove buttons and  FULLSCREEN

        removed = true;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final LinearLayout list = (LinearLayout)findViewById(R.id.listLine);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, 0);
        list.setLayoutParams(lp1);

        LinearLayout layoutVideo = (LinearLayout) findViewById(R.id.layoutVideo);

        layout = (LinearLayout.LayoutParams) layoutVideo.getLayoutParams();

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutVideo.setLayoutParams(lp);

    }

}



