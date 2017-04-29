package com.example.duarte.mediaplayerandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class ActivityList extends AppCompatActivity {
    private ListView lv;
    private String[] items;
    private boolean isPlaying = false;
    private Context context = ActivityList.this;
    private ArrayList adapter = new ArrayList();
    private int img = R.drawable.play; //add
    private int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {



        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            isPlaying = bundle.getBoolean("isPlaying");
            position = bundle.getInt("position");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        if (android.os.Build.VERSION.SDK_INT >= 21) { // Jorge
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.sss));
        }


        lv = (ListView) findViewById(R.id.playListLv);
        ArrayList<File> mySongs = findSongs(Environment.getExternalStorageDirectory());
        items = new String[mySongs.size()];
        for(int i = 0; i<mySongs.size(); i++){
            //toast(mySongs.get(i).getName().toString());
            items[i] = mySongs.get(i).getName().toString();
        }

        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,items); retired

        //lv.setAdapter(adapter); retired
        lv.setAdapter(new MyBaseAdapter(context,adapter)); //add
        getDataInList();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(isPlaying == false){
                    startActivity(new Intent(getApplicationContext(),MainActivity.class).putExtra("position",position).putExtra("items",items));
                    finish();
                }
                else
                {
                    Intent returnMainAct =new Intent();
                    returnMainAct.putExtra("position",position).putExtra("items",items).putExtra("isPlaying", isPlaying);
                    setResult(Activity.RESULT_OK,returnMainAct);
                    finish();
                }

            }
        });
    }

    private void getDataInList(){
        for (int i = 0; i<items.length; i++) {
            // Create a new object for each list item
            playList ld = new playList();
            ld.setTitle(items[i]);
            // Add this object into the ArrayList myList
            adapter.add(ld);
        }
        if(isPlaying){
            playList pl = (playList) adapter.get(position);
            pl.setImgResId(img);
        }

    }

    public ArrayList<File> findSongs(File root){
        ArrayList<File> al = new ArrayList<File>();
        File[] files = root.listFiles();
        for(File singleFile : files){
            if(singleFile.isDirectory()&&!singleFile.isHidden()){
                findSongs(singleFile);
            }
            else if(singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wmv") || singleFile.getName().endsWith(".mp4") || singleFile.getName().endsWith(".mov") ){
                al.add(singleFile);
            }
        }
        return al;
    }

    /*public void toast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }*/
}
