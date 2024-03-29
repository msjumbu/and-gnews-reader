/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.senthil.gnews.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * This class parses XML feeds from stackoverflow.com. Given an InputStream
 * representation of a feed, it returns a List of entries, where each list
 * element represents a single entry (post) in the XML feed.
 */
public class GoogleNewsParser {
	private static final String ns = null;

	// We don't use namespaces

	public List<Entry> parse(InputStream in) throws XmlPullParserException,
			IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readFeed(parser);
		} finally {
			in.close();
		}
	}

	private List<Entry> readFeed(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		List<Entry> entries = new ArrayList<Entry>();

		parser.require(XmlPullParser.START_TAG, ns, "rss");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("item")) {
				entries.add(readEntry(parser));
			} else if (name.equals("channel")) {
				continue;
			} else {
				skip(parser);
			}
		}
		return entries;
	}

	// This class represents a single entry (post) in the XML feed.
	// It includes the data members "title," "link," and "summary."
	public static class Entry {
		public final String title;
		public final String link;
		public final String summary;
		public final String imageLink;

		private Entry(String title, String summary, String link, String imageLink) {
			this.title = title;
			this.summary = summary;
			this.link = link;
			this.imageLink = imageLink;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			StringBuilder htmlString = new StringBuilder();
			htmlString.append(this.title);
			htmlString.append("<br/><a href='");
			htmlString.append(this.link);
			htmlString.append("'>Full Story</a>");
			return htmlString.toString();
		}
	}

	// Given a string representation of a URL, sets up a connection and gets
	// an input stream.
	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		// Starts the query
		conn.connect();
		InputStream stream = conn.getInputStream();
		return stream;
	}

	// Parses the contents of an entry. If it encounters a title, summary, or
	// link tag, hands them
	// off
	// to their respective &quot;read&quot; methods for processing. Otherwise,
	// skips the tag.
	private Entry readEntry(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "item");
		String title = null;
		String summary = null;
		String link = null;
		String imageLink = null;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("title")) {
				title = readTitle(parser);
			} else if (name.equals("description")) {
				summary = readSummary(parser);
				imageLink = readImageLink(summary);
			} else if (name.equals("link")) {
				link = readLink(parser);
			} else {
				skip(parser);
			}
		}
		return new Entry(title, summary, link, imageLink);
	}

	// Processes title tags in the feed.
	private String readTitle(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "title");
		String title = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "title");
		return title;
	}

	// Processes link tags in the feed.
	private String readLink(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String link = "";
		parser.require(XmlPullParser.START_TAG, ns, "link");
		link = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "link");
		return link;
	}

	// Processes summary tags in the feed.
	private String readSummary(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "description");
		String summary = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "description");
		return summary;
	}

	private String readImageLink(String summary) {
		String imgString = "<img src=\"";
		if (summary.contains(imgString)) {
			int startPos = summary.indexOf(imgString) + imgString.length();
			return summary.substring(startPos, summary.indexOf("\"", startPos));
		}
		return "";
	}

	// For the tags title and summary, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	// Skips tags the parser isn't interested in. Uses depth to handle nested
	// tags. i.e.,
	// if the next tag after a START_TAG isn't a matching END_TAG, it keeps
	// going until it
	// finds the matching END_TAG (as indicated by the value of "depth" being
	// 0).
	private void skip(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

	public List<Entry> parseFromURL(String urlString) throws IOException,
			XmlPullParserException {
		// TODO Auto-generated method stub
		InputStream stream = downloadUrl(urlString);
		return this.parse(stream);
	}
}
