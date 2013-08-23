package com.senthil.gnews.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.senthil.gnews.R;
import com.senthil.gnews.parsers.GoogleNewsParser.Entry;

public class LinkedListAdapter<T> extends ArrayAdapter<T> {

	private Context context;
//    private final ImageDownloader imageDownloader = new ImageDownloader();

	public LinkedListAdapter(Context context, int resource, T[] objects) {
		super(context, resource, objects);
		this.context = context;
		SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder = null;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.item, null);
			holder = new ViewHolder();
			holder.tvNews = (TextView) view.findViewById(R.id.textView1);
			//holder.ivNewsImage = (ImageView) view.findViewById(R.id.imageView1);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		Entry item = (Entry) getItem(position);
		if (item != null) {
			holder.tvNews.setText(Html.fromHtml(item.toString()));
			holder.tvNews.setMovementMethod(LinkMovementMethod.getInstance());
			//imageDownloader.download("http:" + item.imageLink, holder.ivNewsImage);
		}

		return view;
	}

	static class ViewHolder {
		TextView tvNews;
		ImageView ivNewsImage;
	}
}
