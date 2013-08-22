package com.senthil.gnews;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NewsFragment extends Fragment {

	private String URL = "https://news.google.com/news/feeds?pz=1&cf=all&ned=in&hl=en&output=rss";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_news_main, container,
				false);
		this.URL = getArguments().getString("URL");
		getActivity().setTitle(getArguments().getString("Name"));
		return view;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		loadPage();
	}

	private void loadPage() {
		Activity activityContext = getActivity();
		new DownloadXmlTask(activityContext).execute(URL);
	}


}
