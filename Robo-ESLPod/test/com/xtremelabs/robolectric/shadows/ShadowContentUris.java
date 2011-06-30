package com.xtremelabs.robolectric.shadows;

import android.content.ContentUris;
import android.net.Uri;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(ContentUris.class)
public class ShadowContentUris {
	@RealObject
	private static ContentUris	realObject;

	/**
	* Appends the given ID to the end of the path.
	*
	* @param builder to append the ID to
	* @param id to append
	*
	* @return the given builder
	*/
	@Implementation
	public static Uri.Builder appendId(Uri.Builder builder, long id) {
		return builder.appendEncodedPath(String.valueOf(id));
	}

	/**
	 * Converts the last path segment to a long.
	 *
	 * <p>This supports a common convention for content URIs where an ID is
	 * stored in the last segment.
	 *
	 * @throws UnsupportedOperationException if this isn't a hierarchical URI
	 * @throws NumberFormatException if the last segment isn't a number
	 *
	 * @return the long conversion of the last segment or -1 if the path is
	 *  empty
	 */
	@Implementation
	public static long parseId(Uri contentUri) {
		String last = contentUri.getLastPathSegment();
		return last == null ? -1 : Long.parseLong(last);
	}

	/**
	 * Appends the given ID to the end of the path.
	 *
	 * @param contentUri to start with
	 * @param id to append
	 *
	 * @return a new URI with the given ID appended to the end of the path
	 */
	@Implementation
	public static Uri withAppendedId(Uri contentUri, long id) {
		return appendId(contentUri.buildUpon(), id).build();
	}
}
