package com.example.duarte.mediaplayerandroid;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class ListFiles extends AppCompatActivity {
    private ListView lv; // list view
    private String[] items; // array files player
    private static boolean isPlaying = false; // satate player
    private Context context = ListFiles.this; // context
    private ArrayList adapter = new ArrayList(); // adaptar
    private int img = R.drawable.play; //add
    private int position; // position row clicked
    private EditText editText; // Var lable Search
    private int tabSelected; // tab selected
    private int auxtabSelected; // tab selected main activity
    ArrayList<String> auxItems =  new ArrayList(); // array aux
    ArrayList<String> filesALll =  new ArrayList(); // array all files
    ArrayList<String> filesMusic =  new ArrayList(); // array mucics
    ArrayList<String> filesVideo =  new ArrayList(); // array video


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras(); // get variables main activity
        tabSelected = 0; // tab selected 0

        if(bundle != null){ // If any variables variables main activity
            isPlaying = bundle.getBoolean("isPlaying");
            position = bundle.getInt("position");
            auxtabSelected = bundle.getInt("tabSelected");
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

       tabLayout.getTabAt(auxtabSelected).select(); // change tab selected
       


        // if change tab
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int tab) {
                     switch (tab) {
                            case 0:
                                tabSelected = 0; // change variable
                                setFilesList();

                                break;
                            case 1:
                                tabSelected = 1; // change variable
                                setFilesList();

                                break;
                            case 2:
                                tabSelected = 2; // change variable
                                setFilesList();

                                break;
                        }

                    }
                });


        if (android.os.Build.VERSION.SDK_INT >= 21) { // change color bar
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.Black_F2));
        }

        lv = (ListView) findViewById(R.id.playListLv); // get layout list view

        editText = (EditText) findViewById(R.id.txtsearch);// get text search label
        getFiles("all"); // get files sd

        initSearch();// start Search


        // On Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if(isPlaying == false){ // if play begin the MainActivity
                    // send variables
                    startActivity(new Intent(getApplicationContext(),MainActivity.class).putExtra("position",searchPosition(position)).putExtra("tabSelected",tabSelected).putExtra("items",items));
                    finish();
                }
                else // if not play return to the MainActivity
                {
                    Intent returnMainAct = new Intent();
                    // send variables
                    returnMainAct.putExtra("position",searchPosition(position)).putExtra("items",items).putExtra("tabSelected",tabSelected).putExtra("isPlaying", isPlaying);
                    setResult(Activity.RESULT_OK,returnMainAct);
                    finish();
                }

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_files, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }



        return super.onOptionsItemSelected(item);
    }

    

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_list_files, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "ALl";

                case 1:

                    return "Music";
                case 2:
                    return "Video";
            }
            return null;
        }
    }






    ///////////////////////////
    //                       //
    // Function findFiles()  //
    //      find files sd    //
    ///////////////////////////
    public ArrayList<File> findFiles(File root, String type){
        ArrayList<File> al = new ArrayList<File>();
        File[] files = root.listFiles();
        for(File singleFile : files){
            if(singleFile.isDirectory()&&!singleFile.isHidden()){
                findFiles(singleFile , type);
            }// if music or video
            else if((singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wmv") || singleFile.getName().endsWith(".mp4") || singleFile.getName().endsWith(".mov"))&& !singleFile.getName().contains("._")  ){
                    al.add(singleFile); // Adds the file array



            }

        }
        return al;
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


    //////////////////////////////////
    //                              //
    //       Function getFiles()    //
    //      separating the files    //
    //////////////////////////////////
    public  void getFiles(String type){


        ArrayList<File> mySongs = findFiles(getStoragePath(), type); // get files sd

       items = new String[mySongs.size()];
        for(int i = 0; i<mySongs.size(); i++){

            if(mySongs.get(i).getName().contains(".mp3") || mySongs.get(i).getName().contains(".mp4")){ // if file is all
                filesALll.add(mySongs.get(i).getName().toString()); // add array all

            }
            if(mySongs.get(i).getName().contains(".mp3") ){ // if file is music
                filesMusic.add(mySongs.get(i).getName().toString()); // add array music
            }
            if(mySongs.get(i).getName().contains(".mp4") ){ // if file is video
                filesVideo.add(mySongs.get(i).getName().toString());// add array video
            }

        }



        setFilesList(); // set files list view


    }


    ////////////////////////////////////
    //                                //
    //       Function initSearch()    //
    //      on changed label search   //
    ////////////////////////////////////
    public void initSearch()
    {

        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
                String text = editText.getText().toString().toLowerCase(Locale.getDefault()); // get text

                search(text); // search text

                editText.setImeActionLabel("Custom text", KeyEvent.KEYCODE_ENTER);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {


                // TODO Auto-generated method stub

            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                // TODO Auto-generated method stub

            }
        });


    }



    ////////////////////////////////////
    //                                //
    //       Function initSearch()    //
    //      on changed label search   //
    ////////////////////////////////////
    public  void search(String... params){

        ArrayList<String> namesFiles =  new ArrayList(); // create array file names

        auxItems.clear(); // clear array aux
        lv.clearFocus(); // clear list view
        adapter.clear(); // clear adapter

        switch (tabSelected){ // select array
            case 0:
                namesFiles = filesALll;
                break;
            case 1:
                namesFiles = filesMusic;
                break;
            case 2:
                namesFiles = filesVideo;
                break;

        }

        for (String param : params){ // for params

                for (String nameFile : namesFiles) { // for names
                    String nameFile2 = nameFile.toLowerCase(); // name toLowerCase

                    if (nameFile2.contains(param.toLowerCase())) { // if contains
                        playList ld = new playList(); // creat new playlist
                        ld.setTitle(nameFile); // add name
                        auxItems.add(nameFile); // Adds the name in the array aux

                        // Add this object into the ArrayList myList
                        adapter.add(ld);


                    }
                }
            }

        lv.setAdapter(new MyBaseAdapter(context,adapter)); //add names list view

    }


    ////////////////////////////////////
    //                                //
    //       Function setFilesList()  //
    //        set files list view     //
    ////////////////////////////////////
    public void setFilesList(){
        lv.clearFocus();// clear list view
        adapter.clear();// clear adapter
        auxItems.clear();// clear array aux

        ArrayList<String> namesFiles =  new ArrayList(); // create array file names

        switch (tabSelected){// select array
            case 0:
                namesFiles = filesALll;
                break;
            case 1:
                namesFiles = filesMusic;
                break;
            case 2:
                namesFiles = filesVideo;
                break;

        }

        items = new String[namesFiles.size()]; // create an array of items
        for(int i = 0; i<namesFiles.size(); i++){

            items[i] = namesFiles.get(i); // add names
        }


        for (String nameFile : namesFiles) { // for names

                    playList ld = new playList(); // creat new playlist
                    ld.setTitle(nameFile);// add name

                    auxItems.add(nameFile);// Adds the name in the array aux

                    // Add this object into the ArrayList myList
                    adapter.add(ld);



        }

        if(isPlaying && (tabSelected == auxtabSelected)){ // if play and auxtabSelected

            playList pl = (playList) adapter.get(position); // get position
            pl.setImgResId(img); // set image play
        }
        lv.setAdapter(new MyBaseAdapter(context,adapter)); //add names and image list view


    }

    ///////////////////////////////////////////
    //                                       //
    //     Function searchPosition()         //
    //   search the position in the array    //
    ///////////////////////////////////////////
    public int searchPosition(int auxPosition){
        for (int i = 0; i<items.length; i++) { // for items array

           if(items[i].contains(auxItems.get(auxPosition))){ // if array contains file name listview
              return i; // return position
            }
        }
        return 0; // default position
    }



}

