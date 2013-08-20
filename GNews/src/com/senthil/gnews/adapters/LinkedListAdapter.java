package com.senthil.gnews.adapters;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import com.senthil.gnews.R;
import com.senthil.gnews.parsers.GoogleNewsParser.Entry;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LinkedListAdapter<T> extends ArrayAdapter<T> {

	private Context context;

	public LinkedListAdapter(Context context, int resource, T[] objects) {
		super(context, resource, objects);
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item, null);
        }

        Entry item = (Entry) getItem(position);
        if (item!= null) {
            // My layout has only one TextView
            TextView itemView = (TextView) view.findViewById(R.id.textView1);
            if (itemView != null) {
                // do whatever you want with your string and long
                itemView.setText(Html.fromHtml(item.toString()));
                itemView.setMovementMethod(LinkMovementMethod.getInstance());
            }
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView1);
            if (imageView != null) {
            	//URI uri = new URI("http:" + item.imageLink);
            	new ImageLoader().execute(view, "http:" + item.imageLink);
            	
            }
         }

        return view;
    }
}
