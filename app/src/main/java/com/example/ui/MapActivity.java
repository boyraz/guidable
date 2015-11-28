package com.example.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnMatrixChangedListener;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.widget.Toast;
/*
* this is map activity
* in this activity, application shows the map with icons for each booth
* also, there are buttons at corners
*
* there are 4 kinds of icons for each booth
* - yellow circle : it is recommended
* - red circle : it is popular
* - green circle : it is visited
* - light blue circle : it has an event
*
* there are 4 buttons
* - list button : go to ListActivity
* - position button : go to current position on the map  ※ it is not implemented yet
* - setting button : for setting ※ it is not implemented yet ※ it is not implemented yet
* - monitoring button : start or stop monitoring beacons*/

public class MapActivity extends Activity {


	//// SJ's work:start
	//This is a default proximity uuid of the RECO
	public static final String RECO_UUID = "24DDF411-8CF1-440C-87CD-E368DAF9C93E";
	public static final boolean SCAN_RECO_ONLY = true;
	public static final boolean ENABLE_BACKGROUND_RANGING_TIMEOUT = false;
	public static final boolean DISCONTINUOUS_SCAN = false;

	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_LOCATION = 10;

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	//// SJ's work:end



	BoothDB boothDB; //BoothDB variable for connecting database of application(※not server)
	private ImageButton getListButton; //variable for list button
	private ImageButton getPositButton; //variable for position button
	private ImageButton getMonitorButton; //variable for monitoring button
	private ImageView mImageView; //variable for map image
	private PhotoViewAttacher mAttacher; //variable for PhotoViewAttacher which manage the pinch-zoom and drag functions for map
	private IconView mIconView; //variable for draw Icons on the map
	private RectF map_size; //save initial map size (this is used for implementing pinch-zoom and drag function for Icons
	private boolean showRecent = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_button);


		//// SJ's work:start


		mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager.getAdapter();

/*
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				Log.i("MainActivity", "The location permission (ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION) is not granted.");
				this.requestLocationPermission();
			} else {
				Log.i("MainActivity", "The location permission (ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION) is already granted.");
			}
		} */

        /*
        * Background service - BoothSensingService starts on creation
        * */



		//// SJ's work:end


		getMonitorButton = (ImageButton)findViewById(R.id.Monitoring);

		getMonitorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MapActivity.this, BoothSensingService.class);
				if(isMonitoring()) {
					stopService(intent);
					Toast.makeText(MapActivity.this,"stop service",Toast.LENGTH_LONG).show();
                    mIconView.showRecent = false;
				}
				else {
					startService(intent);
					Toast.makeText(MapActivity.this,"start service",Toast.LENGTH_LONG).show();
				}
			}
		});
		//if monitoring button clicked, it starts/stops monitoring service if the service is stopped/started.

		boothDB = new BoothDB(this); //make boothDB
		boothDB.setLists(); //set sample lists of recommended,popular booth. It will be erased after finishing implementation

		map_size = new RectF((float)0,(float)0,(float)1047,(float)1831); //set size of map
		
		getListButton = (ImageButton)findViewById(R.id.ListButton);
		getListButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				Intent intent = new Intent(MapActivity.this, ListActivity.class);
				startActivity(intent);
			}
		});
		//if list button is clicked, then it starts list activity
		
		mIconView = (IconView)findViewById(R.id.testicon); //get icon view from layout

	}

	//isMonitoring function return boolean values that it shows whether service is under way or not.
	public boolean isMonitoring() {
		ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.example.ui.BoothSensingService".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	//// SJ's work:start

	/*private void requestLocationPermission() {
		if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
			return;
		}

		Snackbar.make(mLayout, R.string.location_permission_rationale, Snackbar.LENGTH_INDEFINITE)
				.setAction(R.string.ok, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
					}
				})
				.show();
	}*/

	//// SJ's work:start



	@Override
	public void onResume(){
		super.onResume();
		boothDB.open(); //start connection on database
		init(); //rest of code
	}
	private void init(){
	    mImageView = (ImageView) findViewById(R.id.map);

		//put map into imageview
	    Drawable bitmap = ContextCompat.getDrawable(this,R.drawable.testmap);
	    mImageView.setImageDrawable(bitmap);

	    // attach PhotoViewAttacher into mImageView, which manages zooming functionality.
	    mAttacher = new PhotoViewAttacher(mImageView);

		//if user taps booth, InfoActivity will be started
	    mAttacher.setOnPhotoTapListener(new OnPhotoTapListener(){

	    	//click with info activity
			//x,y is coordinates which are represented as a percentage
			@Override
			public void onPhotoTap(View view, float x, float y) {

				//get cursor to get info. of whole booths
				Cursor c = boothDB.getBooths();
				while(c.moveToNext()){
					float bx = (float)c.getLong(c.getColumnIndex(DBTables.booth.LocationX));
					float by = (float)c.getLong(c.getColumnIndex(DBTables.booth.LocationY));
					RectF booth = new RectF(bx,by,
							bx+(float)110,by+(float)110);
					//check x,y is in booth
					if(booth.contains(x*map_size.width(),y*map_size.height())){
						Intent intent = new Intent(MapActivity.this,InfoActivity.class);
						intent.putExtra("id", c.getLong(c.getColumnIndex(DBTables.booth.ID)));
						startActivity(intent);
						break;
					}
				}
				c.close();
			}
	    });

		//set cursor of whole booth on icon view to get locations of whole booths
	    mIconView.setCursor(boothDB.getBooths());

		//initialize icon view
		Matrix mx = new Matrix();
		mx.setRectToRect(map_size, mAttacher.getDisplayRect(), Matrix.ScaleToFit.FILL);
		mIconView.setMatrix(mx);
		mIconView.invalidate();

		//if map is dragged or zoomed, icon view will fit to map.
		mAttacher.setOnMatrixChangeListener(new OnMatrixChangedListener(){

			@Override
			public void onMatrixChanged(RectF rect) {
				// TODO Auto-generated method stub
				Log.i("move", map_size.toString()+" vs "+rect.toString());
				Matrix mx = new Matrix();
				mx.setRectToRect(map_size, rect, Matrix.ScaleToFit.FILL);
				mIconView.setMatrix(mx);
				mIconView.invalidate();
			}
	    	
	    });

	    getPositButton = (ImageButton)findViewById(R.id.PositButton);

		//if position button is clicked, map will be initialized, this will be changed by implementation
	    getPositButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
                if(BoothSensingService.userLocation == null) mIconView.showRecent = false;
                else{
                    mIconView.recentX = (float)BoothSensingService.userLocation.x;
                    mIconView.recentY = (float)BoothSensingService.userLocation.y;
                    mIconView.showRecent = true;

                }
                Log.i("current button",mIconView.showRecent+" ");
			}
		});


		//// SJ's work:start


		if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			//If the request to turn on bluetooth is denied, the app will be finished.
			//사용자가 블루투스 요청을 허용하지 않았을 경우, 어플리케이션은 종료됩니다.
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	//// SJ's work:end

	@Override
	public void onPause(){
		super.onPause();
		boothDB.close(); //disconnect from database
		mIconView.getCursor().close(); //close cursor of IconView
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	@Override
	public void onDestroy() {
		super.onDestroy();
		mAttacher.cleanup();
	}
}
class IconView extends View{
	Paint mPaint;
	Matrix matrix; //matrix of map
	Cursor booths; //cursor for booth info
	boolean showRecent;
    float recentX;
    float recentY;

	public IconView(Context context){
		super(context);
		mPaint = new Paint();
	}
	public IconView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
	}
	public IconView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPaint = new Paint();
	}
	public void setMatrix(Matrix mx){
		matrix = mx;
	}
	public void setCursor(Cursor c){
		this.booths = c;
	}
	public Cursor getCursor(){
		return this.booths;
	}

	@Override
	public void onDraw(Canvas canvas){
		canvas.concat(matrix);
		drawIcons(canvas);
	}

	//draw icons for each booth
	private void drawIcons(Canvas canvas){
		if(booths.isClosed()) return;
		booths.moveToPosition(-1);
		while(booths.moveToNext()){
			long x = booths.getLong(booths.getColumnIndex(DBTables.booth.LocationX));
			long y = booths.getLong(booths.getColumnIndex(DBTables.booth.LocationY));
			if(showRecent){
				mPaint.setColor(Color.RED);
                Log.i("current Position",recentX+" "+recentY);
				canvas.drawCircle(recentX, recentY, 22, mPaint);
				mPaint.setColor(Color.BLACK);
				canvas.drawCircle(recentX, recentY, 20 ,mPaint);
			}
			if(booths.getLong(booths.getColumnIndex(DBTables.booth.RECOMMENDATION)) != 0){
				mPaint.setColor(0xFFFFD541);
				canvas.drawCircle(x + 20, y + 20, 10, mPaint);
			}

			if(booths.getLong(booths.getColumnIndex(DBTables.booth.POPULAR)) != 0){
				mPaint.setColor(0xFFFF0000);
				canvas.drawCircle(x + 45, y + 20, 10, mPaint);
			}

			if(booths.getLong(booths.getColumnIndex(DBTables.booth.NumOfVisit)) != 0){
				mPaint.setColor(0xFF2CD100);
				canvas.drawCircle(x + 70, y + 20, 10, mPaint);
			}

			if(booths.getLong(booths.getColumnIndex(DBTables.booth.EVENT)) != 0){
				mPaint.setColor(0xFF20EAE8);
				canvas.drawCircle(x + 95, y + 20, 10, mPaint);
			}
		}
	}
}
