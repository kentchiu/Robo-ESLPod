package com.kentchiu.eslpod;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
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
		Spanned html = Html.fromHtml(script.get(position));
		textview.setText(html);
		return view;

	}
}
