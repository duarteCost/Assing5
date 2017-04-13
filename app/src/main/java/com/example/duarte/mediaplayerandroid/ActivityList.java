package com.example.duarte.mediaplayerandroid;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class ActivityList extends AppCompatActivity {
    private ListView lv;
    private String[] items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        lv = (ListView) findViewById(R.id.playListLv);
        ArrayList<File> mySongs = findSongs(Environment.getExternalStorageDirectory());
        items = new String[mySongs.size()];
        for(int i = 0; i<mySongs.size(); i++){
            //toast(mySongs.get(i).getName().toString());
            items[i] = mySongs.get(i).getName().toString();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,items);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class).putExtra("position",position).putExtra("items",items));
            }
        });
    }

    public ArrayList<File> findSongs(File root){
        ArrayList<File> al = new ArrayList<File>();
        File[] files = root.listFiles();
        for(File singleFile : files){
            if(singleFile.isDirectory()&&!singleFile.isHidden()){
                findSongs(singleFile);
            }
            else if(singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wmv") ){
                al.add(singleFile);
            }
        }
        return al;
    }

    /*public void toast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }*/
}
