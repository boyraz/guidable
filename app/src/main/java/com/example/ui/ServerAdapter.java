package com.example.ui;

import java.util.ArrayList;

/**
 * Created by lim on 2015-11-28.
 */
public class ServerAdapter {
    private GableClient c;
    private int uid;
    
    public ServerAdapter(int uid) {
        this.uid = uid;
        c = new GableClient();
    }
    //get recommended booth (arraylist of boothid)
    public static ArrayList<Integer> getRecommendation(){
        int[] args = {1,uid};
        c.send(GableClient.REQUEST, args);
        return c.response();
    }

    //get popular booth (arraylist of boothid)
    public static ArrayList<Integer> getPopular(){
        int[] args = {0};
        c.send(GableClient.REQUEST, args);
        return c.response();
    }
    
    public static void sendLog(int direction, int booth){
        int[] args = {direction,uid,booth};
        c.send(GableClient.VISIT,args);
    }
}
