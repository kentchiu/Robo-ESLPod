package com.kentchiu.eslpod.cmd;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import roboguice.util.Ln;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastCommand implements Runnable {

	public static final String	RSS_URI					= "http://feeds.feedburner.com/EnglishAsASecondLanguagePodcast";
	public static final String	ACTION_NEW_PODCAST		= "com.kentchiu.eslpod.NEW";
	public static final int		START_GET_ITEM_NODES	= 1;
	public static final int		ADD_ITEM_NODE			= 2;
	public static final int		END_GET_ITEM_NODES		= 3;
	public static final int		START_IMPORT			= 4;
	public static final int		IMPORTING				= 5;
	public static final int		END_IMPORT				= 6;
	private static final int	MAX_COUNT				= 10;

	private InputStream			inputStream;
	private Context				context;
	private Handler				handler;

	public PodcastCommand(Context context, InputStream inputStream, Handler handler) {
		super();
		this.context = context;
		this.inputStream = inputStream;
		this.handler = handler;
	}

	ContentValues convert(Node item) {
		NodeList children = item.getChildNodes();
		ContentValues result = new ContentValues();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (StringUtils.equals(PodcastColumns.TITLE, node.getNodeName())) {
				result.put(PodcastColumns.TITLE, node.getTextContent());
			} else if (StringUtils.equals(PodcastColumns.DURATION, node.getNodeName())) {
				result.put(PodcastColumns.DURATION, node.getTextContent());
			} else if (StringUtils.equals("pubDate", node.getNodeName())) {
				result.put(PodcastColumns.PUBLISHED, node.getTextContent());
			} else if (StringUtils.equals("link", node.getNodeName())) {
				result.put(PodcastColumns.LINK, node.getTextContent());
			} else if (StringUtils.endsWith("enclosure", node.getNodeName())) {
				NamedNodeMap attributes = node.getAttributes();
				String url = attributes.getNamedItem("url").getNodeValue();
				String length = attributes.getNamedItem("length").getNodeValue();
				result.put(PodcastColumns.MEDIA_URL, url);
				result.put(PodcastColumns.MEDIA_LENGTH, length);
			} else if (StringUtils.equals("description", node.getNodeName())) {
				String desc = node.getTextContent();
				String cleanText = desc.replaceAll("\n", "").replaceAll("<br />", ",").replaceAll("<p>", "");
				// subtitle
				Iterable<String> lines = Splitter.on("</p>").split(cleanText);
				String subtitle = Iterables.get(lines, 0);
				result.put(PodcastColumns.SUBTITLE, subtitle.trim());
				Splitter.on(",").split(Iterables.get(lines, 1));
				result.put(PodcastColumns.PARAGRAPH_INDEX, Iterables.get(lines, 1));
				List<String> scripts = Lists.newArrayList(lines).subList(2, Iterables.size(lines));
				result.put(PodcastColumns.SCRIPT, Joiner.on("\n").join(scripts).trim());
			}
		}
		return result;
	}

	public List<Node> getItemNodes() throws XPathExpressionException {
		sendMessage(START_GET_ITEM_NODES, 0, 0);
		XPath xpath = XPathFactory.newInstance().newXPath();
		InputSource inputSource = new InputSource(inputStream);
		NodeList nodes = (NodeList) xpath.evaluate("//channel/item/title", inputSource, XPathConstants.NODESET); // FIXME this will take a long time
		int length = nodes.getLength();
		Ln.d("Count of nodes is %d", length);
		List<Node> results = Lists.newArrayList();
		for (int i = 0; i < length; i++) {
			Node item = nodes.item(i);
			String content = item.getTextContent();
			String titlePattern = "\\d.\\d?\\d? \\- .*";
			if (content.matches(titlePattern)) {
				Preconditions.checkNotNull(item.getParentNode());
				results.add(item.getParentNode());
				sendMessage(ADD_ITEM_NODE, i, length);
			}
			if (results.size() >= MAX_COUNT) {
				break;
			}
		}
		sendMessage(END_GET_ITEM_NODES, results.size(), length);
		return results;
	}

	@Override
	public void run() {
		try {
			Cursor c = context.getContentResolver().query(PodcastColumns.PODCAST_URI, new String[] { PodcastColumns.TITLE }, null, null, null);
			Set<String> titles = Sets.newHashSet();
			while (c.moveToNext()) {
				titles.add(c.getString(c.getColumnIndex(PodcastColumns.TITLE)));
			}
			List<Node> nodes = getItemNodes();
			Ln.d("%d need to be saved", nodes.size());
			int count = 1;
			sendMessage(START_IMPORT, 0, nodes.size());
			for (Node item : nodes) {
				ContentValues cv = convert(item);
				String title = cv.getAsString(PodcastColumns.TITLE);
				if (StringUtils.isNotBlank(title) && !titles.contains(title)) {
					try {
						Uri uri = context.getContentResolver().insert(PodcastColumns.PODCAST_URI, cv);
						sendMessage(IMPORTING, count, nodes.size());
						Intent intent = new Intent("com.kentchiu.eslpod.NEW_PODCAST", uri);
						context.sendBroadcast(intent);
						count++;
					} catch (Exception e) {
						Ln.w("insert podcast fail %s", title);
					}
				}
			}
			sendMessage(END_IMPORT, count, nodes.size());

			Ln.i(count + " podcast saved");
		} catch (XPathExpressionException e) {
			Ln.w("rss parse fail", e);
		} catch (IllegalStateException e) {
			Ln.w("get podcast rss fail", e);
		}
	}

	private void sendMessage(int what, int arg1, int arg2) {
		if (handler != null) {
			Message m = handler.obtainMessage(what, arg1, arg2);
			handler.sendMessage(m);
		}
	}

}
