package com.kentchiu.eslpod.view.adapter;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kentchiu.eslpod.R;
import com.kentchiu.eslpod.cmd.RichScriptCommand;

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
		CharSequence html = richText(source, RichScriptCommand.extractWord(richScript));
		textview.setText(html);
		return view;
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

	public void setRichScript(String richScript) {
		this.richScript = richScript;
	}

}
