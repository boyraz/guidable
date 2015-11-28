package com.example.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class InfoActivity extends Activity {
	BoothDB boothDB;
	long boothId;
	Cursor booth;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		boothDB = new BoothDB(this);
		boothId = getIntent().getLongExtra("id", -1);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Log.d("idddddddddddddd", "    "+boothId);
		boothDB.open();
		booth = boothDB.getBooth(boothId);
		Log.d("columns", booth.getColumnIndex(DBTables.booth.NAME)+"    "+booth.getColumnIndex(DBTables.booth.DESCRIPTION));
		TextView tv = (TextView)findViewById(R.id.textView1);
		tv.setText(booth.getString(booth.getColumnIndex(DBTables.booth.NAME))
				+ System.getProperty("line.separator")
				+ booth.getString(booth.getColumnIndex(DBTables.booth.DESCRIPTION)));
		booth.close();
	}
	
	public void onPause(){
		super.onPause();
		boothDB.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.info, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
