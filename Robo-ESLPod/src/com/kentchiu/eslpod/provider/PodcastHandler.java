package com.kentchiu.eslpod.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.kentchiu.eslpod.EslPodApplication;
import com.kentchiu.eslpod.provider.Podcast.PodcastColumns;

public class PodcastHandler {


	public List<Node> getItemNodes(InputStream inputStream) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		InputSource inputSource = new InputSource(inputStream);
		NodeList nodes = (NodeList) xpath.evaluate("//channel/item/title", inputSource, XPathConstants.NODESET);
		int length = nodes.getLength();
		List<Node> results = Lists.newArrayList();
		for (int i = 0; i < length; i++) {
			Node item = nodes.item(i);
			String content = item.getTextContent();
			String titlePattern = "\\d.\\d?\\d? \\- .*";
			if (content.matches(titlePattern)) {
				Preconditions.checkNotNull(item.getParentNode());
				results.add(item.getParentNode());
			}
		}
		return results;
	}

	public void handleResponse(HttpResponse response, Uri uri) throws IOException {
		//provider.getDatabase().execSQL("delete from podcast");
		List<Node> items;
		try {
			URL url = new URL(uri.toString());
			InputStream is = url.openStream();
			items = getItemNodes(is);
			for (Node item : items) {
				//provider.insert(Podcast.PODCAST_URI, convert(item), provider.getDatabase());
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}

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
				result.put(PodcastColumns.MEDIA_URI, url);
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
				//				Iterable<String> indexs = Iterables.transform(indexInfo, new Function<String, String>() {
				//					public String apply(String input) {
				//						return StringUtils.substringAfter(input, ":").trim();
				//					}
				//				});
				//result.setParagraphIndex(Iterables.toArray(indexs, String.class));
				// script
				//String[] all = Iterables.toArray(lines, String.class);
				//String[] script = (String[]) ArrayUtils.subarray(all, 2, all.length);
				//result.setScript(script);

			}
		}
		Log.d(EslPodApplication.LOG_TAG, result.toString());
		return result;
	}

}
