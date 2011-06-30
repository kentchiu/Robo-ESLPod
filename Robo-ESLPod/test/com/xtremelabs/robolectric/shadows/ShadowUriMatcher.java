package com.xtremelabs.robolectric.shadows;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.content.UriMatcher;
import android.net.Uri;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(UriMatcher.class)
public class ShadowUriMatcher {
	public static final int				NO_MATCH			= -1;

	static final Pattern				PATH_SPLIT_PATTERN	= Pattern.compile("/");

	private static final int			EXACT				= 0;

	private static final int			NUMBER				= 1;

	private static final int			TEXT				= 2;

	private int							mCode;

	private int							mWhich;
	private String						mText;
	private ArrayList<ShadowUriMatcher>	mChildren;

	public ShadowUriMatcher() {
		mCode = NO_MATCH;
		mWhich = -1;
		mChildren = new ArrayList<ShadowUriMatcher>();
		mText = null;
	}

	/**
	 * Creates the root node of the URI tree.
	 *
	 * @param code the code to match for the root URI
	 */
	public ShadowUriMatcher(int code) {
		mCode = code;
		mWhich = -1;
		mChildren = new ArrayList<ShadowUriMatcher>();
		mText = null;
	}

	/**
	 * Add a URI to match, and the code to return when this URI is
	 * matched. URI nodes may be exact match string, the token "*"
	 * that matches any text, or the token "#" that matches only
	 * numbers.
	 *
	 * @param authority the authority to match
	 * @param path the path to match. * may be used as a wild card for
	 * any text, and # may be used as a wild card for numbers.
	 * @param code the code that is returned when a URI is matched
	 * against the given components. Must be positive.
	 */
	@Implementation
	public void addURI(String authority, String path, int code) {
		if (code < 0) {
			throw new IllegalArgumentException("code " + code + " is invalid: it must be positive");
		}
		String[] tokens = path != null ? PATH_SPLIT_PATTERN.split(path) : null;
		int numTokens = tokens != null ? tokens.length : 0;
		ShadowUriMatcher node = this;
		for (int i = -1; i < numTokens; i++) {
			String token = i < 0 ? authority : tokens[i];
			ArrayList<ShadowUriMatcher> children = node.mChildren;
			int numChildren = children.size();
			ShadowUriMatcher child;
			int j;
			for (j = 0; j < numChildren; j++) {
				child = children.get(j);
				if (token.equals(child.mText)) {
					node = child;
					break;
				}
			}
			if (j == numChildren) {
				// Child not found, create it
				child = new ShadowUriMatcher();
				if (token.equals("#")) {
					child.mWhich = NUMBER;
				} else if (token.equals("*")) {
					child.mWhich = TEXT;
				} else {
					child.mWhich = EXACT;
				}
				child.mText = token;
				node.mChildren.add(child);
				node = child;
			}
		}
		node.mCode = code;
	}

	/**
	 * Try to match against the path in a url.
	 *
	 * @param uri       The url whose path we will match against.
	 *
	 * @return  The code for the matched node (added using addURI),
	 * or -1 if there is no matched node.
	 */
	@Implementation
	public int match(Uri uri) {
		final List<String> pathSegments = uri.getPathSegments();
		final int li = pathSegments.size();

		ShadowUriMatcher node = this;

		if (li == 0 && uri.getAuthority() == null) {
			return mCode;
		}

		for (int i = -1; i < li; i++) {
			String u = i < 0 ? uri.getAuthority() : pathSegments.get(i);
			ArrayList<ShadowUriMatcher> list = node.mChildren;
			if (list == null) {
				break;
			}
			node = null;
			int lj = list.size();
			for (int j = 0; j < lj; j++) {
				ShadowUriMatcher n = list.get(j);
				which_switch: switch (n.mWhich) {
				case EXACT:
					if (n.mText.equals(u)) {
						node = n;
					}
					break;
				case NUMBER:
					int lk = u.length();
					for (int k = 0; k < lk; k++) {
						char c = u.charAt(k);
						if (c < '0' || c > '9') {
							break which_switch;
						}
					}
					node = n;
					break;
				case TEXT:
					node = n;
					break;
				}
				if (node != null) {
					break;
				}
			}
			if (node == null) {
				return NO_MATCH;
			}
		}

		return node.mCode;
	}
}
