package com.example.ui;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Environment;
import android.database.sqlite.SQLiteOpenHelper;

//
public class BoothDB {
	private static final String DB_NAME = "guidableData.db";
	private static final int DB_VER = 1;
	public static ArrayList<Long> recommendation;
	public static ArrayList<Long> popular;

	private SQLiteDatabase db;
	private DatabaseHelper dbHelper;
	private Context context;
	private class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(DBTables.booth._CREATE);
			db.execSQL(DBTables.history._CREATE);
			db.execSQL(DBTables.beacon._CREATE);
			for(int i = 0; i < 6; i++){
				ContentValues values = new ContentValues();
				values.put(DBTables.booth.ID, (long)i);
				values.put(DBTables.booth.NAME, BoothInfo.name[i]);
				values.put(DBTables.booth.LocationX, BoothInfo.x[i]);
				values.put(DBTables.booth.LocationY, BoothInfo.y[i]);
				values.put(DBTables.booth.DESCRIPTION, BoothInfo.name[i] + " is a good booth");
                if(i==2) values.put(DBTables.booth.NumOfVisit, 0);
                else values.put(DBTables.booth.NumOfVisit, 1);
                if(i == 4 || i == 5 || i == 2) values.put(DBTables.booth.POPULAR, true);
                else values.put(DBTables.booth.POPULAR, false);
                if(i == 1 || i == 2 || i ==4) values.put(DBTables.booth.RECOMMENDATION, true);
                else values.put(DBTables.booth.RECOMMENDATION, false);
                values.put(DBTables.booth.EVENT, false);

				db.insert(DBTables.booth._TABLENAME, null, values);
			}
			for(int i = 0; i < 5; i++){
				ContentValues values = new ContentValues();
				values.put(DBTables.history.TIME, BoothInfo.time[i]);
				values.put(DBTables.history.BID, BoothInfo.id[i]);
				values.put(DBTables.history.DURATION, BoothInfo.duration[i]);
				db.insert(DBTables.history._TABLENAME, null, values);
			}
			for(int i = 0; i < 6; i++){
				ContentValues values = new ContentValues();
				values.put(DBTables.beacon.MINOR, BoothInfo.minor[i]);
				values.put(DBTables.beacon.BID, (long)i);
				values.put(DBTables.beacon.LocationX, BoothInfo.x[i]+55);
				values.put(DBTables.beacon.LocationY, BoothInfo.y[i]+55);
				values.put(DBTables.beacon.RANGE, BoothInfo.range[i]);
				db.insert(DBTables.beacon._TABLENAME, null, values);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
		}
		
	}
	
	public BoothDB(Context context){
		this.context = context;
	}
    public static void setLists(){
        recommendation = new ArrayList<Long>();
        popular = new ArrayList<Long>();
        recommendation.add(2L);
        recommendation.add(1L);
        recommendation.add(4L);
        popular.add(4L);
        popular.add(5L);
        popular.add(2L);
    }

	public BoothDB open() throws SQLException{
		dbHelper = new DatabaseHelper(context,Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+DB_NAME, null, DB_VER);
		db = dbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		db.close();
	}
	
	/*public long insertBooth(long id, String name, long locationX, long locationY, String desc){
		ContentValues values = new ContentValues();
		values.put(DBTables.booth.NAME, name);
		values.put(DBTables.booth.LocationX, locationX);
		values.put(DBTables.booth.LocationY, locationY);
		values.put(DBTables.booth.DESCRIPTION, desc);
		return db.insert(DBTables.booth._TABLENAME, null, values);
	}
	*/
    public boolean updateRecommend(){
        //update arraylist recommendation by connecting server
        //blank

        //update DB according to changed array
        return true;
    }
    public boolean updatePopular(){
        //update arraylist popular by connecting server
        //blank

        //update DB according to changed array
        return true;
    }
	private boolean addOneToVisit(long id){
		ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(getBooth(id), values);
        long n = values.getAsLong(DBTables.booth.NumOfVisit);
        values.remove(DBTables.booth.NumOfVisit);
        values.put(DBTables.booth.NumOfVisit,n+1);
		return db.update(DBTables.booth._TABLENAME, values, "_id=" + id, null) > 0;
	}

	public boolean insertHistory(long time, long bid, long duration){
		ContentValues values = new ContentValues();
		values.put(DBTables.history.TIME, time);
		values.put(DBTables.history.BID, bid);
		values.put(DBTables.history.DURATION, duration);
		return (db.insert(DBTables.history._TABLENAME, null, values) > 0) && (addOneToVisit(bid));
	}
	public Cursor getHistory(){
		return  db.query(DBTables.history._TABLENAME, null, null, null, null, null, null);
	}
	
	public Cursor getBooths() {
		return db.query(DBTables.booth._TABLENAME, null, null, null, null, null, null);
	}

	public Cursor getBeacons() {
		return db.query(DBTables.beacon._TABLENAME, null, null, null, null, null, null);
	}
	
	public Cursor getBooth(long id){
		Cursor c = db.query(DBTables.booth._TABLENAME, null, DBTables.booth.ID+ "=" + id, null, null, null, null);
		if(c != null && c.getCount() != 0) c.moveToFirst();
		return c;
	}
	public Cursor getBeacon(long minor){
		Cursor c = db.query(DBTables.beacon._TABLENAME, null, DBTables.beacon.MINOR+ "=" + minor, null, null, null, null);
		if(c != null && c.getCount() != 0) c.moveToFirst();
		return c;
	}
}
