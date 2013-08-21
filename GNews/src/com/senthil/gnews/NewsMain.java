package com.senthil.gnews;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.senthil.gnews.adapters.ImageLoader;
import com.senthil.gnews.adapters.LinkedListAdapter;
import com.senthil.gnews.parsers.GoogleNewsParser;
import com.senthil.gnews.parsers.GoogleNewsParser.Entry;

public class NewsMain extends Activity {
	public static final String WIFI = "Wi-Fi";
	public static final String ANY = "Any";
	private static final String URL = "https://news.google.com/news/feeds?pz=1&cf=all&ned=in&hl=en&output=rss";
	public static String sPref = null;
	// Whether there is a Wi-Fi connection.
	private static boolean wifiConnected = false;
	// Whether there is a mobile connection.
	private static boolean mobileConnected = false;
	// Whether the display should be refreshed.
	public static boolean refreshDisplay = true;

	// The BroadcastReceiver that tracks network connectivity changes.
	private NetworkReceiver receiver = new NetworkReceiver();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_main);

		// Register BroadcastReceiver to track connection changes.
		IntentFilter filter = new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION);
		receiver = new NetworkReceiver();
		this.registerReceiver(receiver, filter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.news_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_settings:
			openSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void openSettings() {
		// TODO Auto-generated method stub
		Intent settingsActivity = new Intent(getBaseContext(),
				SettingsActivity.class);
		startActivity(settingsActivity);
	}

	// Refreshes the display if the network connection and the
	// pref settings allow it.
	@Override
	public void onStart() {
		super.onStart();
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		sPref = sharedPrefs.getString("listPref", "Wi-Fi");
		updateConnectedFlags();
		if (refreshDisplay) {
			loadPage();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (receiver != null) {
			this.unregisterReceiver(receiver);
		}
	}

	// Checks the network connection and sets the wifiConnected and
	// mobileConnected
	// variables accordingly.
	private void updateConnectedFlags() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
		if (activeInfo != null && activeInfo.isConnected()) {
			wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
			mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
		} else {
			wifiConnected = false;
			mobileConnected = false;
		}
	}

	private void loadPage() {
		if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
				|| ((sPref.equals(WIFI)) && (wifiConnected))) {

			new DownloadXmlTask(this).execute(URL);
		} else {
			showErrorPage();
		}
	}

	// Displays an error if the app is unable to load content.
	private void showErrorPage() {
		WebView myWebView = new WebView(this);
		setContentView(myWebView);
		myWebView.loadData(getResources().getString(R.string.connection_error),
				"text/html", null);
	}

	public class DownloadXmlTask extends AsyncTask<String, Void, List<Entry>> {

		/**
    	 * 
    	 */
		Activity activityContext = null;

		public DownloadXmlTask(Activity actContext) {
			activityContext = actContext;
		}

		@Override
		protected List<Entry> doInBackground(String... urls) {
			try {
				return loadXmlFromNetwork(urls[0]);
			} catch (IOException e) {
				return null;
			} catch (XmlPullParserException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<Entry> result) {
			Entry[] array = {};
			array = result.toArray(array);
			LinearLayout tl = (LinearLayout) findViewById(R.id.myTableLayout);
			tl.removeAllViews();
			for (Entry item : array) {
				TableRow tr = new TableRow(activityContext);
				tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.WRAP_CONTENT));
				if (item != null) {
					// My layout has only one TextView
					TextView itemView = new TextView(activityContext);
					itemView.setLayoutParams(new TableRow.LayoutParams(
							TableRow.LayoutParams.WRAP_CONTENT,
							TableRow.LayoutParams.WRAP_CONTENT));
					itemView.setText(Html.fromHtml(item.toString()));
					itemView.setMovementMethod(LinkMovementMethod.getInstance());
					ImageView imageView = new ImageView(activityContext);
					imageView.setLayoutParams(new TableRow.LayoutParams(
							TableRow.LayoutParams.WRAP_CONTENT,
							TableRow.LayoutParams.WRAP_CONTENT));
					new ImageLoader().execute(imageView, "http:"
							+ item.imageLink);

					/* Add TextView to row. */
					tr.addView(imageView);
					tr.addView(itemView);
					/* Add row to TableLayout. */
					tl.addView(tr,
							new TableLayout.LayoutParams(
									LayoutParams.FILL_PARENT,
									LayoutParams.WRAP_CONTENT));
				}
			}
		}

		// Uploads XML from stackoverflow.com, parses it, and combines it with
		// HTML markup. Returns HTML string.
		private List<Entry> loadXmlFromNetwork(String urlString)
				throws XmlPullParserException, IOException {
			InputStream stream = null;
			GoogleNewsParser googleNewsXmlParser = new GoogleNewsParser();
			List<Entry> entries = null;
			Calendar rightNow = Calendar.getInstance();
			DateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa");

			// Checks whether the user set the preference to include summary
			// text
			SharedPreferences sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(activityContext);
			boolean pref = sharedPrefs.getBoolean("summaryPref", false);

			StringBuilder htmlString = new StringBuilder();
			htmlString.append("<h3>"
					+ this.activityContext.getResources().getString(
							R.string.page_title) + "</h3>");
			htmlString.append("<em>"
					+ this.activityContext.getResources().getString(
							R.string.updated) + " "
					+ formatter.format(rightNow.getTime()) + "</em>");

			try {
				entries = googleNewsXmlParser.parseFromURL(urlString);
			} finally {
				if (stream != null) {
					stream.close();
				}
			}

			return entries;
		}

	}

	/**
	 * 
	 * This BroadcastReceiver intercepts the
	 * android.net.ConnectivityManager.CONNECTIVITY_ACTION, which indicates a
	 * connection change. It checks whether the type is TYPE_WIFI. If it is, it
	 * checks whether Wi-Fi is connected and sets the wifiConnected flag in the
	 * main activity accordingly.
	 * 
	 */
	public class NetworkReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager connMgr = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (WIFI.equals(sPref) && networkInfo != null
					&& networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				refreshDisplay = true;
				Toast.makeText(context, R.string.wifi_connected,
						Toast.LENGTH_SHORT).show();
			} else if (ANY.equals(sPref) && networkInfo != null) {
				refreshDisplay = true;
			} else {
				refreshDisplay = false;
				Toast.makeText(context, R.string.lost_connection,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}
