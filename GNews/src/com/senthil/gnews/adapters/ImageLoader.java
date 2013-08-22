package com.senthil.gnews.adapters;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.senthil.gnews.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

public class ImageLoader extends AsyncTask<Object, String, Bitmap> {

    private ImageView imageView;
    private Bitmap bitmap = null;

    @Override
    protected Bitmap doInBackground(Object... parameters) {

        // Get the passed arguments here
        imageView = (ImageView) parameters[0];
        String uri = (String)parameters[1];
        InputStream is;
		try {
			URL imageUrl = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            is=conn.getInputStream();
	        bitmap = BitmapFactory.decodeStream(is);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null && imageView != null) {
           	imageView.setImageBitmap(bitmap);
           	imageView.setTag(bitmap);
        }
    }
}
