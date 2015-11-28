package com.example.ui;

import java.util.ArrayList;

/**
 * Created by lim on 2015-11-28.
 */
public class ServerAdapter {
    private Client c;
    private int uid;
    
    public ServerAdapter(int uid) {
        this.uid = uid;
        c = new Client();
    }
    //get recommended booth (arraylist of boothid)
    public static ArrayList<Long> getRecommendation(){
        String[] args = {"1",uid};
        return c.send(Client.REQUEST, args);
    }

    //get popular booth (arraylist of boothid)
    public static ArrayList<Long> getPopular(){
        String[] args = {"0"};
        return c.getPopular(Client.REQUEST, args);
    }
    public static void sendLog(int booth){
        String[] args = {"0",uid, booth};
        c.send(Client.VISIT,args);
    }
}
