package com.kentchiu.eslpod;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

public class ScriptListAdapter extends ArrayAdapter {

	private List<String>	script;
	private String			richScript;

	public ScriptListAdapter(Context context, int resource, int textViewResourceId, List<String> script) {
		super(context, resource, textViewResourceId, script);
		this.script = script;

	}

	public String getRichScript() {
		return richScript;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		TextView textview = (TextView) view.findViewById(R.id.scriptLine);
		String source = script.get(position);
		CharSequence html = richText(source, extractWord());
		textview.setText(html);
		return view;
	}

	public void setRichScript(String richScript) {
		this.richScript = richScript;
	}

	protected Iterable<String> extractWord() {
		if (StringUtils.isBlank(richScript)) {
			return ImmutableList.of();
		}
		String[] words = StringUtils.substringsBetween(richScript, "<b>", "</b>");
		if (ArrayUtils.isEmpty(words)) {
			return ImmutableList.of();
		} else {
			return ImmutableList.copyOf(words);
		}
	}

	protected CharSequence richText(String source, Iterable<String> words) {
		SpannableStringBuilder style = new SpannableStringBuilder(source);
		for (String each : words) {
			int start = 0;
			int idx = StringUtils.indexOfIgnoreCase(source, each, start);
			if (idx == -1) {
				start = idx;
			} else {
				style.setSpan(new ForegroundColorSpan(Color.RED), idx, idx + each.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return style;
	}

}
