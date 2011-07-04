package com.kentchiu.eslpod;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ScriptListAdapter extends ArrayAdapter {

	private List<String>	script;

	public ScriptListAdapter(Context context, int resource, int textViewResourceId, List<String> script) {
		super(context, resource, textViewResourceId, script);
		this.script = script;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//		SpannableStringBuilder style = new SpannableStringBuilder("test the style");
		//		style.setSpan(new ForegroundColorSpan(Color.RED), 1, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		//		System.out.println(style);
		View view = super.getView(position, convertView, parent);
		TextView textview = (TextView) view.findViewById(R.id.scriptLine);
		String source = script.get(position);
		String style = newStyle(source);
		Log.d(EslPodApplication.LOG_TAG, style);
		Spanned html = Html.fromHtml(style);
		Log.d(EslPodApplication.LOG_TAG, html.toString());
		textview.setText(html);
		return view;

	}

	protected String newStyle(String html) {
		String str1 = html.replaceAll("</b>", "</font>");
		return str1.replaceAll("<b>", "<font color='red'>");
	}


}
