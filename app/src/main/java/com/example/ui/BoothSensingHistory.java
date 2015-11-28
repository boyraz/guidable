package com.example.ui;

/**
 * Created by delarosa on 2015-11-22.
 */
public class BoothSensingHistory {
    //Class UserVisit is used to manage current visiting booth information. It stores booth id(minor), when the user started to visit the booth, and distance between booth sensor and the user.
    static class UserVisit{
        int minor;
        Long startTime;
        Double distance;
    }
    //Class UserHistory stores single visit history. Similar to UserVisit, it stores booth id and when the user entered the booth and duration of visitation.
    static class UserHistory{
        int minor;
        Long startTime;
        Long duration;
    }
    //Class Location stores where booth sensor exists. This structure also used for user location.
    static class Location{
        int minor;  // -1 for usr
        double x;
        double y;
        double range; // Amount of distance which assumed to users which are inside that range are visited.
        String name;
    }

    //Class Stabilizer stores total txpower and rssi value accumulated through 10 seconds. Instance count counts number of times beacon detected in 10 seconds.
    static class Stabilizer{
        int minor;
        int count;
        double txpower;
        double rssi;
        double distance;
    }
}
