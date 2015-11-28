package com.example.ui;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOBeaconRegionState;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECOMonitoringListener;
import com.perples.recosdk.RECORangingListener;
import com.perples.recosdk.RECOServiceConnectListener;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by delarosa on 2015-11-22.
 */
public class BoothSensingService extends Service implements RECORangingListener, RECOMonitoringListener, RECOServiceConnectListener {

    private long mScanDuration = 1000L;
    private long mSleepDuration = 10000L;

    private RECOBeaconManager mRecoManager;
    private ArrayList<RECOBeaconRegion> mRegions;
    private BoothSensingHistory.UserVisit mVisit;
    private BoothSensingHistory.UserHistory mHistory;
    private ArrayList<BoothSensingHistory.Location> mLocations;
    private BoothDB boothDB;
    private ArrayList<RECOBeacon> mRangedBeacons;
    private BoothSensingHistory.Location userLocation;
    private ArrayList<BoothSensingHistory.Stabilizer> mStabs;
    private int global_count;


    @Override
    public void onCreate() {
        Log.i("BoothSensingService", "onCreate()");
        //Setting Booth Location info
        /*BoothSensingHistory.Location location1 = new BoothSensingHistory.Location();
        BoothSensingHistory.Location location2 = new BoothSensingHistory.Location();
        BoothSensingHistory.Location location3 = new BoothSensingHistory.Location();
        location1.minor = 1;
        location1.x = 100.0;
        location1.y = 150.0;
        location1.name = "Beacon1";
        location1.range = 3.0;
        location2.minor = 2;
        location2.x = 50.0;
        location2.y = 200.0;
        location2.name="Beacon2";
        location2.range = 4.0;
        location3.minor = 3;
        location3.x = 250.0;
        location3.y = 300.0;
        location3.name="Beacon3";
        location3.range = 5.0;
        mLocations = new ArrayList<BoothSensingHistory.Location>();
        mLocations.add(location1);
        mLocations.add(location2);
        mLocations.add(location3);*/
        global_count = 0;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BoothSensingService", "onStartCommand");
        /**
         * Create an instance of RECOBeaconManager (to set scanning target and ranging timeout in the background.)
         * If you want to scan only RECO, and do not set ranging timeout in the backgournd, create an instance:
         * 		mRecoManager = RECOBeaconManager.getInstance(getApplicationContext(), true, false);
         * WARNING: False enableRangingTimeout will affect the battery consumption.
         *
         * RECOBeaconManager 인스턴스틀 생성합니다. (스캔 대상 및 백그라운드 ranging timeout 설정)
         * RECO만을 스캔하고, 백그라운드 ranging timeout을 설정하고 싶지 않으시다면, 다음과 같이 생성하시기 바랍니다.
         * 		mRecoManager = RECOBeaconManager.getInstance(getApplicationContext(), true, false);
         * 주의: enableRangingTimeout을 false로 설정 시, 배터리 소모량이 증가합니다.
         */
        mRecoManager = RECOBeaconManager.getInstance(getApplicationContext(), true, false);
        this.bindRECOService();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.i("BoothSensingService", "onDestroy()");

        this.tearDown();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("BoothSensingService", "onTaskRemoved()");
        super.onTaskRemoved(rootIntent);
    }

    private void bindRECOService() {
        Log.i("BoothSensingService", "bindRECOService()");
        //Initialize storages
        mRegions = new ArrayList<RECOBeaconRegion>();
        mRegions = this.generateBeaconRegion();
        mVisit = null;
        mHistory = new BoothSensingHistory.UserHistory();
        mStabs = new ArrayList<BoothSensingHistory.Stabilizer>();
        mRangedBeacons = new ArrayList<RECOBeacon>();
        mRecoManager.setMonitoringListener(this);
        mRecoManager.setRangingListener(this);
        mRecoManager.bind(this);
    }

    private ArrayList<RECOBeaconRegion> generateBeaconRegion() {
        Log.i("BoothSensingService", "generateBeaconRegion()");
        ArrayList<RECOBeaconRegion> regions = new ArrayList<RECOBeaconRegion>();
        RECOBeaconRegion recoRegion;
        recoRegion = new RECOBeaconRegion(MapActivity.RECO_UUID, "RECO Sample Region");
        regions.add(recoRegion);

        Log.i("BoothSensingService", Integer.toString(mRegions.size()));

        return regions;
    }

    private void startMonitoring() {
        Log.i("BoothSensingService", "startMonitoring()");

        mRecoManager.setScanPeriod(this.mScanDuration);
        mRecoManager.setSleepPeriod(this.mSleepDuration);

        Log.i("BoothSensingService", Integer.toString(mRegions.size()));

        for(RECOBeaconRegion region : mRegions) {
            try {
                //mRecoManager.startMonitoringForRegion(region);
                mRecoManager.startRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                Log.e("BoothSensingService", "RemoteException has occured while executing RECOManager.startMonitoringForRegion()");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.e("BoothSensingService", "NullPointerException has occured while executing RECOManager.startMonitoringForRegion()");
                e.printStackTrace();
            }
        }
    }

    private void stopMonitoring() {
        Log.i("BoothSensingService", "stopMonitoring()");

        for(RECOBeaconRegion region : mRegions) {
            try {
                //mRecoManager.stopMonitoringForRegion(region);
                mRecoManager.stopRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                Log.e("BoothSensingService", "RemoteException has occured while executing RECOManager.stopMonitoringForRegion()");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.e("BoothSensingService", "NullPointerException has occured while executing RECOManager.stopMonitoringForRegion()");
                e.printStackTrace();
            }
        }
    }

    private void startRangingWithRegion(RECOBeaconRegion region) {
        Log.i("BoothSensingService", "startRangingWithRegion()");

        /**
         * There is a known android bug that some android devices scan BLE devices only once. (link: http://code.google.com/p/android/issues/detail?id=65863)
         * To resolve the bug in our SDK, you can use setDiscontinuousScan() method of the RECOBeaconManager.
         * This method is to set whether the device scans BLE devices continuously or discontinuously.
         * The default is set as FALSE. Please set TRUE only for specific devices.
         *
         * mRecoManager.setDiscontinuousScan(true);
         */

        try {
            mRecoManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e("BoothSensingService", "RemoteException has occured while executing RECOManager.startRangingBeaconsInRegion()");
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e("BoothSensingService", "NullPointerException has occured while executing RECOManager.startRangingBeaconsInRegion()");
            e.printStackTrace();
        }
    }

    private void stopRangingWithRegion(RECOBeaconRegion region) {
        Log.i("BoothSensingService", "stopRangingWithRegion()");

        try {
            mRecoManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e("BoothSensingService", "RemoteException has occured while executing RECOManager.stopRangingBeaconsInRegion()");
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e("BoothSensingService", "NullPointerException has occured while executing RECOManager.stopRangingBeaconsInRegion()");
            e.printStackTrace();
        }
    }

    private void tearDown() {
        Log.i("BoothSensingService", "tearDown()");
        this.stopMonitoring();

        try {
            mRecoManager.unbind();
        } catch (RemoteException e) {
            Log.e("BoothSensingService", "RemoteException has occured while executing unbind()");
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceConnect() {
        Log.i("BoothSensingService", "onServiceConnect()");
        mRecoManager.setDiscontinuousScan(MapActivity.DISCONTINUOUS_SCAN);
        this.startMonitoring();
        //Write the code when RECOBeaconManager is bound to RECOBeaconService
    }

    @Override
    public void didDetermineStateForRegion(RECOBeaconRegionState state, RECOBeaconRegion region) {
        Log.i("BoothSensingService", "didDetermineStateForRegion()");
        //Write the code when the state of the monitored region is changed
    }

    @Override
    public void didEnterRegion(RECOBeaconRegion region, Collection<RECOBeacon> beacons) {
        /**
         * For the first run, this callback method will not be called.
         * Please check the state of the region using didDetermineStateForRegion() callback method.
         *
         * 최초 실행시, 이 콜백 메소드는 호출되지 않습니다.
         * didDetermineStateForRegion() 콜백 메소드를 통해 region 상태를 확인할 수 있습니다.
         */

        //Get the region and found beacon list in the entered region
        Log.i("BoothSensingService", "didEnterRegion() - " + region.getUniqueIdentifier());
        //   this.popupNotification("Inside of " + region.getUniqueIdentifier());
        //Write the code when the device is enter the region

        this.startRangingWithRegion(region); //start ranging to get beacons inside of the region
        //from now, stop ranging after 10 seconds if the device is not exited
    }

    @Override
    public void didExitRegion(RECOBeaconRegion region) {
        /**
         * For the first run, this callback method will not be called.
         * Please check the state of the region using didDetermineStateForRegion() callback method.
         *
         * 최초 실행시, 이 콜백 메소드는 호출되지 않습니다.
         * didDetermineStateForRegion() 콜백 메소드를 통해 region 상태를 확인할 수 있습니다.
         */

        Log.i("BoothSensingService", "didExitRegion() - " + region.getUniqueIdentifier());
        //  this.popupNotification("Outside of " + region.getUniqueIdentifier());
        //Write the code when the device is exit the region

        this.stopRangingWithRegion(region); //stop ranging because the device is outside of the region from now
    }

    @Override
    public void didStartMonitoringForRegion(RECOBeaconRegion region) {
        Log.i("BoothSensingService", "didStartMonitoringForRegion() - " + region.getUniqueIdentifier());
        //Write the code when starting monitoring the region is started successfully
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> beacons, RECOBeaconRegion region) {
        Log.i("BoothSensingService", "didRangeBeaconsInRegion() - " + region.getUniqueIdentifier() + " with " + beacons.size() + " beacons");
        //Write the code when the beacons inside of the region is received
        synchronized (beacons) {
            mRangedBeacons = new ArrayList<RECOBeacon>(beacons);
        }

        stabilizeDistance();


        if(global_count > 9){
            //Among monitored beacons, filter them which are inside the range of each beacon.
            boothDB = new BoothDB(this);
            boothDB.open();

            rangeFilter();

            //Approximate user location with nearby monitored beacon locations and distance from them.
            findUser();

            //Update where the user visits right now and if user left from a booth, save recent visit history into somewhere.
            updateHistory();

            boothDB.close();

            global_count = 0;
            mStabs = new ArrayList<BoothSensingHistory.Stabilizer>();
        }

        else{
            global_count++;
        }

    }

    private void stabilizeDistance() {
        boolean found = false;
        BoothSensingHistory.Stabilizer Stab = null;
        for(int i = 0; i < mRangedBeacons.size(); i++){
            for(int j = 0; j < mStabs.size(); j++){
                if(mRangedBeacons.get(i).getMinor() == mStabs.get(j).minor){
                    mStabs.get(j).txpower += mRangedBeacons.get(i).getTxPower();
                    mStabs.get(j).rssi += mRangedBeacons.get(i).getRssi();
                    mStabs.get(j).distance += mRangedBeacons.get(i).getAccuracy();
                    mStabs.get(j).count += 1;
                    found = true;
                    break;
                }
            }
            if(!found){
                Stab = new BoothSensingHistory.Stabilizer();
                Stab.minor = mRangedBeacons.get(i).getMinor();
                Stab.distance = mRangedBeacons.get(i).getAccuracy();
                Stab.txpower = mRangedBeacons.get(i).getTxPower();
                Stab.rssi = mRangedBeacons.get(i).getRssi();
                Stab.count = 1;
                mStabs.add(Stab);
            }
            found = false;
        }
    }

    private void sendExitingInfo(){
        //send mHistoryInfo to server;
        int sensor_id = mHistory.minor;
        long time_enter = mHistory.startTime;
        long duration = mHistory.duration;
        //Fill here to send data to server;
        //local storage
        Log.i("Updating DB", "finished");
        Cursor c = boothDB.getBeacon((long)sensor_id);
        boothDB.insertHistory(time_enter,c.getLong(c.getColumnIndex(DBTables.beacon.BID)),duration);
        c.close();
        //



        //Empty mHistory
        mHistory = null;
    }

    private void sendEnteringInfo(){
        //send visit information;
        int sensor_id = mVisit.minor;
        long time_enter = mVisit.startTime;
        //Fill here to send data to server;


    }

    /*
       Calculate distance from txpower and rssi.
       Reference : http://stackoverflow.com/questions/20416218/understanding-ibeacon-distancing
    */
    private double calculateAccuracy(double txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }

    private void rangeFilter() {

        /*
        * Calculate approximate distance from beacon to user with average txpower and rssi value.
        * If those valued counted less than 5 times, let assume beacon is far away from user and do not take care about it.
        * */
        for(int i = 0; i < mStabs.size(); ){
            if(mStabs.get(i).count <= 5){
                mStabs.remove(i);
            }
            else{
                mStabs.get(i).txpower = mStabs.get(i).txpower / mStabs.get(i).count;
                mStabs.get(i).rssi = mStabs.get(i).rssi / mStabs.get(i).count;
                mStabs.get(i).distance = calculateAccuracy(mStabs.get(i).txpower, mStabs.get(i).rssi);
                i++;
            }
        }

        for(int i = 0; i < mStabs.size(); i++){

            Cursor c = boothDB.getBeacon((long)mStabs.get(i).minor);
            Log.i("Visit info","minor "+(long)mStabs.get(i).minor + " : " +c.getDouble(c.getColumnIndex(DBTables.beacon.RANGE)) + ", " + mStabs.get(i).distance);
            if(mStabs.get(i).distance > c.getDouble(c.getColumnIndex(DBTables.beacon.RANGE))){

                mStabs.remove(i);
                i--;
            }
            c.close();
            /*
            for(int j = 0; j < mLocations.size(); j++){
                if(mRangedBeacons.get(i).getMinor() == mLocations.get(i).minor){
                    if(mRangedBeacons.get(i).getAccuracy() > mLocations.get(i).range){
                        mRangedBeacons.remove(i);
                        break;
                    }
                }
                else{
                    i++;
                    break;
                }
            }*/
        }
    }

    private void updateHistory() {
        int nearest = -1;
        double tmp, distance = 99999;
        /*
        * First, find nearest booth among monitored beacon. If there is no monitored beacons, nearest set to -1.
        * */
        for(int i = 0; i < mStabs.size(); i++){
            tmp = mStabs.get(i).distance;
            if(tmp < distance){
                distance = tmp;
                nearest = i;
            }
        }
        Log.i("Visit info", "nearest one  "+nearest);
        /*
        * If there user visited no booth recently but visiting new booth right now, push the booth information into mVisit, which stores current visiting booth information
        * */
        if(mVisit == null){
            if(nearest >= 0){
                addVisit(nearest, distance);
            }
        }

        else{
            BoothSensingHistory.UserHistory userHistory = new BoothSensingHistory.UserHistory();
            if(nearest < 0){
                addHistory(userHistory);
                mVisit = null;
            }
            /* If nearest booth detected is the booth user visited recently, the app just update the distance between user and current visiting booth
             */
            else if(mVisit.minor == mStabs.get(nearest).minor){
                    mVisit.distance = distance;
                Log.i("Visit info", mVisit.minor+"   "+mVisit.distance);
            }
            // If the nearest booth is different from recently visited booth, update history. As new visiting booth exists, send that information to server
            else{
                addHistory(userHistory);
                addVisit(nearest, distance);
            }
        }
    }

    private void addVisit(int nearest, double distance) {
        mVisit = new BoothSensingHistory.UserVisit();
        mVisit.minor = mStabs.get(nearest).minor;
        mVisit.startTime = System.currentTimeMillis();
        mVisit.distance = distance;
        sendEnteringInfo();
    }

    private void addHistory(BoothSensingHistory.UserHistory userHistory) {
        mHistory = new BoothSensingHistory.UserHistory();
        userHistory.minor = mVisit.minor;
        userHistory.startTime = mVisit.startTime;
        userHistory.duration = System.currentTimeMillis() - userHistory.startTime;
        Log.i("BoothSensingService", "User History added: " + userHistory.minor + ", startTime: " + userHistory.startTime);
        mHistory = userHistory;
        sendExitingInfo();
    }

    private void findUser() {
        double x1, x2, y1, y2, m1, m2, d1, d2;

        userLocation = new BoothSensingHistory.Location();
        userLocation.minor = -1;
        userLocation.name = "Myself";

        if(mVisit != null){
            Cursor c = boothDB.getBeacon((long)mVisit.minor);
            userLocation.x = c.getLong(c.getColumnIndex(DBTables.beacon.LocationX));
            userLocation.y = c.getLong(c.getColumnIndex(DBTables.beacon.LocationY));
            c.close();
        }
        //If there is no monitored beacons nearby, the app cannot detect user location. Thus, let it default value(0,0).
        else if(mRangedBeacons.size() == 0){
            userLocation.x = 0;
            userLocation.y = 0;
        }
        //If there is only one monitored beacons nearby, the app assumes that user is in the booth where the beacon exists.
        else if(mRangedBeacons.size() == 1){
            Cursor c = boothDB.getBeacon((long)mStabs.get(0).minor);
            userLocation.x = c.getLong(c.getColumnIndex(DBTables.beacon.LocationX));
            userLocation.y = c.getLong(c.getColumnIndex(DBTables.beacon.LocationY));
            c.close();
            /*for(int i = 0; i < mLocations.size(); i++){
                if(mLocations.get(i).minor == mRangedBeacons.get(0).getMinor()){
                    userLocation.x = mLocations.get(i).x;
                    userLocation.y = mLocations.get(i).y;
                }
            }*/
        }
        /*If there are more than two monitored beacons nearby, use two beacon locations and distance from them, by drawing two circles we approximate user location as midpoint of two intersections of the circles.*
        / But as beacon sensing is not accurate, there would be no intersection between two circles such that center of the circle is location of the booth and radius is measured distance between device and booth location.
        / In that case,we draw a line between two points which are locations of two beacons, and assume the point between this line as user location, where distance to two beacons are proportional to measured distance between two booths.
        / Maybe using three beacons give more accuracy, but I decided to do it as optional goal because as it measured distance is not that accurate, we cannot sure that three circles drawn like above has common space. If it isn't, it would be
        / very complicated problem.
         */
        else{
            x1 = x2 = y1 = y2 = 0;
            // Find two beacons monitored and save its id value and accuracy
            m1 = mStabs.get(0).minor;
            d1 = mStabs.get(0).distance;
            m2 = mStabs.get(1).minor;
            d2 = mStabs.get(1).distance;
            // Find location of beacons
            Cursor c1 = boothDB.getBeacon((long)mStabs.get(0).minor);
            userLocation.x = c1.getLong(c1.getColumnIndex(DBTables.beacon.LocationX));
            userLocation.y = c1.getLong(c1.getColumnIndex(DBTables.beacon.LocationY));
            c1.close();

            Cursor c2 = boothDB.getBeacon((long)mStabs.get(1).minor);
            userLocation.x = c2.getLong(c2.getColumnIndex(DBTables.beacon.LocationX));
            userLocation.y = c2.getLong(c2.getColumnIndex(DBTables.beacon.LocationY));
            c2.close();
            /*for(int j = 0; j < mLocations.size(); j++){
                if(mLocations.get(j).minor == m1){
                    x1 = mLocations.get(j).x;
                    y1 = mLocations.get(j).y;
                }
                if(mLocations.get(j).minor == m2){
                    x2 = mLocations.get(j).x;
                    y2 = mLocations.get(j).y;
                }
            }*/
            //Approximate user location with data we gathered
            userLocation.x = (x1+x2)/2;
            userLocation.y = (y1+y2)/2;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //This method is not used
        return null;
    }

    @Override
    public void onServiceFail(RECOErrorCode errorCode) {
        //Write the code when the RECOBeaconService is failed.
        //See the RECOErrorCode in the documents.
        return;
    }

    @Override
    public void monitoringDidFailForRegion(RECOBeaconRegion region, RECOErrorCode errorCode) {
        //Write the code when the RECOBeaconService is failed to monitor the region.
        //See the RECOErrorCode in the documents.
        return;
    }

    @Override
    public void rangingBeaconsDidFailForRegion(RECOBeaconRegion region, RECOErrorCode errorCode) {
        //Write the code when the RECOBeaconService is failed to range beacons in the region.
        //See the RECOErrorCode in the documents.
        return;
    }
}
