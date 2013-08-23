package com.senthil.gnews;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class NewsActivity extends Activity {

	// private final static String feedsBaseURL =
	// "https://news.google.com/news/feeds?cf=all&output=rss";
	public static boolean			refreshDisplay	= true;

	// private String feedsEditionURL = feedsBaseURL;

	private DrawerLayout			mDrawerLayout;
	private ListView				mDrawerList;
	private ActionBarDrawerToggle	mDrawerToggle;
	private CharSequence			mDrawerTitle;
	private CharSequence			mTitle;
	DrawerListEntry[]				topics			= {};
	private int						selectedChannel	= 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		retriveTopics();

		setupDrawer();

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		if (savedInstanceState == null) {
			selectItem(selectedChannel);
		}
	}

	private void setupDrawer() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		mDrawerList.setAdapter(new ArrayAdapter<DrawerListEntry>(this,
				R.layout.drawer_list_item, topics));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		mTitle = mDrawerTitle = getTitle();
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description */
		R.string.drawer_close /* "close drawer" description */
		) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void retriveTopics() {
		String topicsJSONArrayStr = getResources().getString(
				R.string.topics_json_array);
		JSONArray topicsJSONArray = null;
		ArrayList<DrawerListEntry> dle = new ArrayList<DrawerListEntry>();
		try {
			JSONObject json = new JSONObject(topicsJSONArrayStr);
			topicsJSONArray = json.getJSONArray("Topics");

			for (int i = 0; i < topicsJSONArray.length(); i++) {
				JSONObject json_data = topicsJSONArray.getJSONObject(i);
				DrawerListEntry topic = new DrawerListEntry();
				topic.newsName = json_data.getString("Name");
				topic.topicID = json_data.getString("Code");
				dle.add(topic);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		topics = dle.toArray(topics);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_settings:
			openSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void openSettings() {
		Intent settingsActivity = new Intent(getBaseContext(),
				SettingsActivity.class);
		startActivity(settingsActivity);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void setTitle(CharSequence title) {
		// TODO Auto-generated method stub
		getActionBar().setTitle(title);
	}

	private void selectItem(int position) {
		selectedChannel = position;
		Fragment fragment = new NewsFragment();
		DrawerListEntry dle = topics[position];
		Bundle args = new Bundle();
		args.putString("TopicCode", dle.topicID);
		args.putString("Name", dle.newsName);
		fragment.setArguments(args);

		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment).commit();

		mTitle = mDrawerTitle = dle.newsName;
		setTitle(dle.newsName);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	public class DrawerItemClickListener implements
			ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView parent, View view, int position,
				long id) {
			refreshDisplay = true;
			selectItem(position);
		}

	}

	private class DrawerListEntry {
		public String	newsName;
		public String	topicID;

		@Override
		public String toString() {
			if (newsName != null)
				return newsName;
			else
				return "";
		}
	}

}
