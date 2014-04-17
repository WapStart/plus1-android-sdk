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

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import android.os.AsyncTask;
import android.util.Log;

public abstract class BaseRequestLoader<T> extends AsyncTask<Plus1Request, Void, T> {
	private static final String LOGTAG = "BaseRequestLoader";

	protected ArrayList<BaseRequestLoadListener> mListenerList;
	protected HashMap<String, String> mRequestPropertyList;

	public BaseRequestLoader() {
		mListenerList = new ArrayList<BaseRequestLoadListener>();
		mRequestPropertyList = new HashMap<String, String>();
	}

	public void addLoadListener(BaseRequestLoadListener listener) {
		mListenerList.add(listener);
	}

	public void addRequestProperty(String key, String value) {
		mRequestPropertyList.put(key, value);
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

	public abstract interface BaseRequestLoadListener {
		// FIXME: add sdk parameters and sdk action event handlers here
	}
}
