package com.example.duarte.mediaplayerandroid;

/**
 * Created by duarte on 14/04/2017.
 */
public class playList {

    ///////////////////////////////
    //Define the list structure  //
    ///////////////////////////////

    String MusicTitle;
    int imgResId;


    public String getTitle() {
        return MusicTitle;
    }

    public void setTitle(String MusicTitle) {

        this.MusicTitle = MusicTitle;
    }

    public int getImgResId() {
        return imgResId;
    }

    public void setImgResId(int imgResId) {
        this.imgResId = imgResId;
    }

}
