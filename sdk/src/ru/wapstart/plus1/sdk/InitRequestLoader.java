/**
 * Copyright (c) 2014, Alexander Zaytsev <a.zaytsev@co.wapstart.ru>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the "Wapstart" nor the names
 *     of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ru.wapstart.plus1.sdk;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import android.os.AsyncTask;
import android.util.Log;

final class InitRequestLoader extends AsyncTask<Plus1Request, Void, String> {
	private static final String LOGTAG = "InitRequestLoader";
	private static final Integer BUFFER_SIZE = 8192;

	protected ArrayList<InitRequestLoadListener> mListenerList;
	protected HashMap<String, String> mRequestPropertyList;

	public InitRequestLoader() {
		mListenerList = new ArrayList<InitRequestLoadListener>();
		mRequestPropertyList = new HashMap<String, String>();
	}

	public InitRequestLoader addLoadListener(InitRequestLoadListener listener) {
		mListenerList.add(listener);

		return this;
	}

	public InitRequestLoader addRequestProperty(String key, String value) {
		mRequestPropertyList.put(key, value);

		return this;
	}

	@Override
	protected String doInBackground(Plus1Request... requests)
	{
		Plus1Request request = requests[0];
		String requestUrl = request.getUrl(Plus1Request.RequestType.init);

		HttpURLConnection connection = makeConnection(requestUrl);

		if (connection == null)
			return null;

		String result = "";
		String uniqueId = null;

		try {
			UrlEncodedFormEntity postEntity = request.getUrlEncodedFormEntity();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(
				"Content-Type",
				"application/x-www-form-urlencoded"
			);
			connection.setRequestProperty(
				"Content-Length",
				Integer.toString((int)postEntity.getContentLength())
			);
			postEntity.writeTo(connection.getOutputStream());

			InputStream stream = connection.getInputStream();

			byte[] buffer = new byte[BUFFER_SIZE];
			int count = 0;

			BufferedInputStream bufStream =
				new BufferedInputStream(stream, BUFFER_SIZE);

			while ((count = bufStream.read(buffer)) != -1) {
				if (isCancelled())
					return null;

				result += new String(buffer, 0, count);
			}

			bufStream.close();

			uniqueId = result.toString();

			Log.d(LOGTAG, "Unique identifier: " + uniqueId);

		} catch (IOException e) {
			Log.e(LOGTAG, "URL " + requestUrl + " doesn't exist", e);
		} catch (Exception e) {
			Log.e(LOGTAG, "Exception while downloading banner: " + e.getMessage(), e);
		} finally {
			connection.disconnect();
		}

		return uniqueId;
	}

	@Override
	protected void onPostExecute(String uid) {
		if (null == uid)
			notifyOnUniqueIdLoadFailed();

		notifyOnUniqueIdLoaded(uid);
	}

	protected void modifyConnection(HttpURLConnection connection) {
		for (Entry<String, String> entry : mRequestPropertyList.entrySet())
			connection.setRequestProperty(entry.getKey(), entry.getValue());
	}

	protected HttpURLConnection makeConnection(String url)
	{
		HttpURLConnection connection = null;

		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
			modifyConnection(connection);
			connection.connect();
		} catch (MalformedURLException e) {
			Log.e(LOGTAG, "URL parsing failed: " + url, e);
		} catch (Exception e) {
			Log.e(LOGTAG, "Unexpected exception", e);
		}

		return connection;
	}

	private void notifyOnUniqueIdLoaded(String uid) {
		for (InitRequestLoadListener listener : mListenerList)
			listener.onUniqueIdLoaded(uid);
	}

	private void notifyOnUniqueIdLoadFailed() {
		for (InitRequestLoadListener listener : mListenerList)
			listener.onUniqueIdLoadFailed();
	}

	public interface InitRequestLoadListener {
		public void onUniqueIdLoaded(String uid);
		public void onUniqueIdLoadFailed();
	}
}
