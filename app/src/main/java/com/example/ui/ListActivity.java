package com.example.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class ListActivity extends Activity {
	private BoothDB boothDB;
	private Cursor historyCursor;
	private HistoryListAdapter historyAdapter;
    private RecPopListAdapter recommendAdapter;
    private RecPopListAdapter popularAdapter;

    //initialize tab and boothDB
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		boothDB = new BoothDB(this);		
		TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);
		tabHost.setup();
		TabSpec spec1 = tabHost.newTabSpec("Tab1").setContent(R.id.tabs1).setIndicator("Recomm.");
		tabHost.addTab(spec1);
		TabSpec spec2 = tabHost.newTabSpec("Tab2").setContent(R.id.tabs2).setIndicator("Popular");
		tabHost.addTab(spec2);
		TabSpec spec3 = tabHost.newTabSpec("Tab3").setContent(R.id.tabs3).setIndicator("History");
		tabHost.addTab(spec3);
		TabSpec spec4 = tabHost.newTabSpec("Tab4").setContent(R.id.tabs4).setIndicator("Event");
		tabHost.addTab(spec4);
	}

    //connect to database and make list views
	@Override
	public void onResume(){
		super.onResume();
		boothDB.open();
		createHistory();
        createPopular();
        createRecommend();
        ImageButton refresh= (ImageButton)findViewById(R.id.refresh);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BoothDB.popular = ServerAdapter.getPopular();
                BoothDB.recommendation = ServerAdapter.getRecommendation();

                popularAdapter = new RecPopListAdapter(false, boothDB);
                recommendAdapter = new RecPopListAdapter(true, boothDB);
                Log.i("datasets size",popularAdapter.getCount()+"  "+recommendAdapter.getCount());
                ListView recommendlv = (ListView)findViewById(R.id.tabs1);
                ListView popularlv = (ListView)findViewById(R.id.tabs2);
                recommendlv.setAdapter(recommendAdapter);
                popularlv.setAdapter(popularAdapter);
            }
        });
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list, menu);
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

    //disconnect database and close cursors
	public void onPause(){
		super.onPause();
		boothDB.close();
		historyCursor.close();
	}
	
	//create history view
	private void createHistory(){
		ListView historylv = (ListView)findViewById(R.id.tabs3);
		historyCursor = boothDB.getHistory();
		historyAdapter = new HistoryListAdapter(this, historyCursor, false, boothDB);
		historylv.setAdapter(historyAdapter);

        //if item is clicked, info activity will be started
		historylv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(ListActivity.this,InfoActivity.class);
				Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
				Log.d("cursorrrrrrrrrrrr", " "+cursor.getLong(cursor.getColumnIndex(DBTables.history.BID)));
				intent.putExtra("id", cursor.getLong(cursor.getColumnIndex(DBTables.history.BID)));
				startActivity(intent);
			}
		});
	}

    //create recommend view
	private void createRecommend(){
        ListView recommendlv = (ListView)findViewById(R.id.tabs1);
        recommendAdapter = new RecPopListAdapter(true, boothDB);
        recommendlv.setAdapter(recommendAdapter);

        //if item is clicked, info activity will be started
        recommendlv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListActivity.this, InfoActivity.class);
                Log.d("cursorrrrrrrrrrrr", " " + (Long)parent.getAdapter().getItem(position));
                intent.putExtra("id", (Long)parent.getAdapter().getItem(position));
                startActivity(intent);
            }
        });
    }

    //create popular view
    private void createPopular(){
        ListView popularlv = (ListView)findViewById(R.id.tabs2);
        popularAdapter = new RecPopListAdapter(false, boothDB);
        popularlv.setAdapter(popularAdapter);

        //if item is clicked, info activity will be started
        popularlv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListActivity.this, InfoActivity.class);
                Log.d("cursorrrrrrrrrrrr", " " + (Long)parent.getAdapter().getItem(position));
                intent.putExtra("id", (Long)parent.getAdapter().getItem(position));
                startActivity(intent);
            }
        });
	}
	private void createEvent(){
	}
}
class HistoryListAdapter extends CursorAdapter {
	private BoothDB boothDB;
	public HistoryListAdapter(Context context, Cursor c, boolean autoRequery, BoothDB boothDB) {
		super(context, c, autoRequery);
		this.boothDB = boothDB;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		  LayoutInflater inflater = LayoutInflater.from(context);
		  View v = inflater.inflate(R.layout.item_history, parent, false);
		  return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		//final ImageView image = (ImageView)view.findViewById(R.id.icon_history);
		final TextView nameView = (TextView)view.findViewById(R.id.name_history);
		final TextView timeView = (TextView)view.findViewById(R.id.time_history);
		long id = cursor.getLong(cursor.getColumnIndex(DBTables.history.BID));
		Cursor c = boothDB.getBooth(id);
		nameView.setText(c.getString(c.getColumnIndex(DBTables.booth.NAME)));
		c.close();
		long time = cursor.getLong(cursor.getColumnIndex(DBTables.history.TIME));
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
		timeView.setText(timeFormat.format(new Date(time)));
	}
}

class RecPopListAdapter extends BaseAdapter{
	private ArrayList<Long> bidList;
    private BoothDB boothDB;
	RecPopListAdapter(boolean isRec, BoothDB db){
		if(isRec) bidList = BoothDB.recommendation;
		else bidList = BoothDB.popular;
        boothDB = db;
	}
	@Override
	public int getCount() {
		return bidList.size();
	}

	@Override
	public Object getItem(int position) {
		return bidList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        BoothViewHolder bvHolder;
        Context context = parent.getContext();
        if(convertView != null) bvHolder = (BoothViewHolder)convertView.getTag();
        else{
            Log.i("ListAdapter","1 dddddddddddddddddddddddddd");
            bvHolder = new BoothViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_booth, parent, false);
            Log.i("ListAdapter","2 dddddddddddddddddddddddddd");

            bvHolder.icon = (ImageView) convertView.findViewById(R.id.icon_booth);
            bvHolder.name = (TextView) convertView.findViewById(R.id.name_booth);
            bvHolder.recommend = (ImageView) convertView.findViewById(R.id.recommend_booth);
            bvHolder.popular = (ImageView) convertView.findViewById(R.id.popular_booth);
            bvHolder.visit = (ImageView) convertView.findViewById(R.id.visit_booth);
            bvHolder.event = (ImageView) convertView.findViewById(R.id.event_booth);

            convertView.setTag(bvHolder);
            Log.i("ListAdapter", "3 dddddddddddddddddddddddddd");
        }
        long bid = bidList.get(position);
        Cursor c = boothDB.getBooth(bid);
        Log.i("ListAdapter","4 dddddddddddddddddddddddddd");
        bvHolder.name.setText(c.getString(c.getColumnIndex(DBTables.booth.NAME)));
        if(c.getLong(c.getColumnIndex(DBTables.booth.RECOMMENDATION)) == 0){
            bvHolder.recommend.setVisibility(View.INVISIBLE);
        }
        else bvHolder.recommend.setVisibility(View.VISIBLE);

        if(c.getLong(c.getColumnIndex(DBTables.booth.POPULAR)) == 0){
            bvHolder.popular.setVisibility(View.INVISIBLE);
        }
        else bvHolder.popular.setVisibility(View.VISIBLE);

        if(c.getLong(c.getColumnIndex(DBTables.booth.NumOfVisit)) == 0){
            bvHolder.visit.setVisibility(View.INVISIBLE);
        }
        else bvHolder.visit.setVisibility(View.VISIBLE);

        if(c.getLong(c.getColumnIndex(DBTables.booth.EVENT)) == 0){
            bvHolder.event.setVisibility(View.INVISIBLE);
        }
        else bvHolder.event.setVisibility(View.VISIBLE);

        c.close();
		return convertView;
	}
}
class BoothViewHolder {
	public ImageView icon;
	public TextView name;
	public ImageView recommend;
	public ImageView popular;
	public ImageView visit;
	public ImageView event;
}

