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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.json.JSONObject;
import org.json.JSONException;

import android.os.AsyncTask;
import android.util.Log;
import java.util.EnumMap;

public abstract class BaseRequestLoader<T> extends AsyncTask<Plus1Request, Void, T> {
	private static final String LOGTAG = "BaseRequestLoader";
	private static final Integer BUFFER_SIZE = 8192;
	private static final String SDK_PARAMETERS_HEADER = "X-Plus1-SDK-Parameters";
	private static final String SDK_ACTION_HEADER = "X-Plus1-SDK-Action";

	private List<ChangeSdkPropertiesListener> mChangeSdkPropertiesListenerList =
			new ArrayList<ChangeSdkPropertiesListener>();
	private Map<String, String> mRequestPropertyList =
			new HashMap<String, String>();

	public static enum SdkParameter {refreshDelay, reInitDelay, openIn};
	public static enum SdkAction {openLink};

	abstract protected String getRequestUrl(Plus1Request request);
	abstract protected UrlEncodedFormEntity getUrlEncodedFormEntity(Plus1Request request) throws UnsupportedEncodingException;
	abstract protected T makeResult(String content, HttpURLConnection connection) throws IOException;

	public void addChangeSdkPropertiesListener(ChangeSdkPropertiesListener listener) {
		mChangeSdkPropertiesListenerList.add(listener);
	}

	public void addRequestProperty(String key, String value) {
		mRequestPropertyList.put(key, value);
	}

	protected HttpURLConnection makeConnection(String url)
	{
		HttpURLConnection connection = null;

		try {
			connection = (HttpURLConnection) new URL(url).openConnection();

			for (Entry<String, String> entry : mRequestPropertyList.entrySet())
				connection.setRequestProperty(entry.getKey(), entry.getValue());

			connection.connect();
		} catch (MalformedURLException e) {
			Log.e(LOGTAG, "URL parsing failed: " + url, e);
		} catch (Exception e) {
			Log.e(LOGTAG, "Unexpected exception", e);
		}

		return connection;
	}

	@Override
	final protected T doInBackground(Plus1Request... requests)
	{
		Plus1Request request = requests[0];
		String requestUrl = getRequestUrl(request);

		HttpURLConnection connection = makeConnection(requestUrl);

		if (connection == null)
			return null;

		T result = null;
		String content = "";

		try {
			UrlEncodedFormEntity postEntity = getUrlEncodedFormEntity(request);

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

				content += new String(buffer, 0, count);
			}

			bufStream.close();

			EnumMap<SdkParameter, String> parameters =
				getSdkParametersByJson(connection.getHeaderField(SDK_PARAMETERS_HEADER));
			EnumMap<SdkAction, String> actions =
				getSdkActionsByJson(connection.getHeaderField(SDK_ACTION_HEADER));

			if (!(null == parameters || parameters.isEmpty()))
				notifyOnSdkParametersLoaded(parameters);
			if (!(null == actions || actions.isEmpty()))
				notifyOnSdkActionsLoaded(actions);

			result = makeResult(content.toString(), connection);

		} catch (IOException e) {
			Log.e(LOGTAG, "URL " + requestUrl + " doesn't exist", e);
		} catch (Exception e) {
			Log.e(LOGTAG, "Exception while loading request: " + e.getMessage(), e);
		} finally {
			connection.disconnect();
		}

		return result;
	}

	// FIXME: refactor and simplify
	private EnumMap<SdkParameter, String> getSdkParametersByJson(String json) {
		Map<String, Object> map = getMapByJson(json);

		if (null == map)
			return null;

		EnumMap<SdkParameter, String> result =
			new EnumMap<SdkParameter, String>(SdkParameter.class);

		for (SdkParameter val : SdkParameter.values()) {
			if (map.containsKey(val.toString())) {
				result.put(
					val,
					String.valueOf(
						map.get(val.toString())
					)
				);
			}
		}

		return result;
	}

	private EnumMap<SdkAction, String> getSdkActionsByJson(String json) {
		Map<String, Object> map = getMapByJson(json);

		if (null == map)
			return null;

		EnumMap<SdkAction, String> result =
			new EnumMap<SdkAction, String>(SdkAction.class);

		for (SdkAction val : SdkAction.values()) {
			if (map.containsKey(val.toString())) {
				result.put(
					val,
					String.valueOf(
						map.get(val.toString())
					)
				);
			}
		}

		return result;
	}

	private Map<String, Object> getMapByJson(String json) {
		if (null != json) {
			try {
				return JsonHelper.toMap(new JSONObject(json));
			} catch (JSONException e) {
				Log.e(LOGTAG, "Found not compatible json: " + json, e);
			}
		}

		return null;
	}

	private void notifyOnSdkParametersLoaded(EnumMap<SdkParameter, String> parameters) {
		for (ChangeSdkPropertiesListener listener : mChangeSdkPropertiesListenerList)
			listener.onSdkParametersLoaded(parameters);
	}

	private void notifyOnSdkActionsLoaded(EnumMap<SdkAction, String> actions) {
		for (ChangeSdkPropertiesListener listener : mChangeSdkPropertiesListenerList)
			listener.onSdkActionsLoaded(actions);
	}

	public interface ChangeSdkPropertiesListener {
		public void onSdkParametersLoaded(EnumMap<SdkParameter, String> parameters);
		public void onSdkActionsLoaded(EnumMap<SdkAction, String> actions);
	}
}
