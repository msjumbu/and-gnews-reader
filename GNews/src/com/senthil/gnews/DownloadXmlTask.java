package com.senthil.gnews;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.TextView;

import com.senthil.gnews.adapters.LinkedListAdapter;
import com.senthil.gnews.parsers.GoogleNewsParser;
import com.senthil.gnews.parsers.GoogleNewsParser.Entry;

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
    	if (result == null) {
    		TextView tv = (TextView) this.activityContext.findViewById(R.id.textView1);
    		tv.setText("No Connection");
    		return;
    	}
		ListView listView = (ListView) this.activityContext.findViewById(android.R.id.list);

		Entry[] array = {};
		array = result.toArray(array);

		LinkedListAdapter<Entry> adapter = new LinkedListAdapter<Entry>(activityContext, 
		        android.R.layout.simple_list_item_1, array);
		listView.setAdapter(adapter);
    }
    
    // Uploads XML from stackoverflow.com, parses it, and combines it with
    // HTML markup. Returns HTML string.
    private List<Entry> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        GoogleNewsParser googleNewsXmlParser = new GoogleNewsParser();
        List<Entry> entries = null;
        
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