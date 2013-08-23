package com.senthil.gnews;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NewsFragment extends Fragment {

	private final static String feedsBaseURL = "https://news.google.com/news/feeds?cf=all&output=rss";
	private String feedsEditionURL = feedsBaseURL;
	private String topicCode ="";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_news_main, container,
				false);
//		this.URL = getArguments().getString("URL");
		this.topicCode = getArguments().getString("TopicCode");
		getActivity().setTitle(getArguments().getString("Name"));
		return view;
	}

	private void setupFeedURL() {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		String edition = sharedPrefs.getString("pref_news_edition", "in");
		feedsEditionURL = feedsBaseURL + "&ned=" + edition + "&topic=" + topicCode;
	}

	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		setupFeedURL();
		if (MainActivity.refreshDisplay) {
			loadPage();
			MainActivity.refreshDisplay = false;
		}
	}

	private void loadPage() {
		Activity activityContext = getActivity();
		new DownloadXmlTask(activityContext).execute(feedsEditionURL);
	}


}
