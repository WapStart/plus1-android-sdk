/**
 * Copyright (c) 2011, Alexander Klestov <a.klestov@co.wapstart.ru>
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
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import ru.wapstart.plus1.sdk.HtmlBannerDownloader.HtmlBannerInfo;
import ru.wapstart.plus1.sdk.Plus1BannerDownloadListener.LoadError;
import ru.wapstart.plus1.sdk.Plus1BannerDownloadListener.BannerAdType;

import android.os.AsyncTask;
import android.util.Log;

final class HtmlBannerDownloader extends AsyncTask<Plus1Request, Void, HtmlBannerInfo> {
	private static final String LOGTAG = "HtmlBannerDownloader";
	private static final Integer BUFFER_SIZE = 8192;

	protected class HtmlBannerInfo {
		private Integer mResponseCode;
		private String mBannerContent;
		private String mBannerAdType;
	}

	protected ArrayList<Plus1BannerDownloadListener> mListenerList;
	protected HashMap<String, String> mRequestPropertyList;

	public HtmlBannerDownloader() {
		mListenerList = new ArrayList<Plus1BannerDownloadListener>();
		mRequestPropertyList = new HashMap<String, String>();
	}

	public HtmlBannerDownloader addDownloadListener(
		Plus1BannerDownloadListener bannerDownloadListener
	) {
		mListenerList.add(bannerDownloadListener);

		return this;
	}

	public HtmlBannerDownloader addRequestProperty(String key, String value) {
		mRequestPropertyList.put(key, value);

		return this;
	}

	@Override
	protected HtmlBannerInfo doInBackground(Plus1Request... requests)
	{
		Plus1Request request = requests[0];
		String requestUrl = request.getUrl();

		HttpURLConnection connection = makeConnection(requestUrl);

		if (connection == null)
			return null;

		HtmlBannerInfo bannerInfo = null;
		String result = "";

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

			bannerInfo = new HtmlBannerInfo();
			bannerInfo.mResponseCode = connection.getResponseCode();
			bannerInfo.mBannerContent = result.toString();
			bannerInfo.mBannerAdType = connection.getHeaderField("X-Adtype");

			Log.d(LOGTAG, "Response code: "		+ bannerInfo.mResponseCode);
			Log.d(LOGTAG, "X-Adtype: "			+ bannerInfo.mBannerAdType);
			Log.d(LOGTAG, "Banner content: "	+ bannerInfo.mBannerContent);
		} catch (IOException e) {
			Log.e(LOGTAG, "URL " + requestUrl + " doesn't exist", e);
		} catch (Exception e) {
			Log.e(LOGTAG, "Exception while downloading banner: " + e.getMessage(), e);
		} finally {
			connection.disconnect();
		}

		return bannerInfo;
	}

	@Override
	protected void onPostExecute(HtmlBannerInfo bannerInfo) {
		if (bannerInfo == null) {
			notifyOnBannerLoadFailed(LoadError.DownloadFailed);
		} else if (bannerInfo.mResponseCode.equals(HttpStatus.SC_NO_CONTENT)) {
			notifyOnBannerLoadFailed(LoadError.NoHaveBanner);
		} else {
			try {
				notifyOnBannerLoaded(
					bannerInfo.mBannerContent,
					BannerAdType.valueOf(bannerInfo.mBannerAdType)
				);
			} catch (IllegalArgumentException e) {
				Log.e(LOGTAG, "Unsupported ad type: " + bannerInfo.mBannerAdType, e);

				notifyOnBannerLoadFailed(LoadError.UnknownAnswer);
			}
		}
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

	private void notifyOnBannerLoaded(String content, BannerAdType adType) {
		for (Plus1BannerDownloadListener listener : mListenerList)
			listener.onBannerLoaded(content, adType);
	}

	private void notifyOnBannerLoadFailed(LoadError loadError) {
		for (Plus1BannerDownloadListener listener : mListenerList)
			listener.onBannerLoadFailed(loadError);
	}
}
