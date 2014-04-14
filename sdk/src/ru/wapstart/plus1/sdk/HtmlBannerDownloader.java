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
import java.util.Locale;
import java.util.ArrayList;

import ru.wapstart.plus1.sdk.Plus1BannerDownloadListener.LoadError;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;

final class HtmlBannerDownloader extends AsyncTask<Plus1Request, Void, Boolean> {
	private static final String LOGTAG = "HtmlBannerDownloader";
	private static final Integer BUFFER_SIZE = 8192;
	private static final Integer RESPONSE_CODE_NO_CONTENT = 204;

	public static enum BannerAdType {plus1, mraid};

	protected Plus1BannerView mView;

	protected Integer mResponseCode;
	protected String mBannerContent;
	protected String mBannerAdType;

	protected ArrayList<Plus1BannerDownloadListener> mListenerList;

	public HtmlBannerDownloader(Plus1BannerView view) {
		mView = view;

		mListenerList = new ArrayList<Plus1BannerDownloadListener>();
	}

	public HtmlBannerDownloader addDownloadListener(
		Plus1BannerDownloadListener bannerDownloadListener
	) {
		mListenerList.add(bannerDownloadListener);

		return this;
	}

	@Override
	protected Boolean doInBackground(Plus1Request... requests)
	{
		Plus1Request request = requests[0];

		HttpURLConnection connection = makeConnection(request.getRequestUri());

		if (connection == null)
			return false;

		String result = "";
		boolean fetched = false;

		try {
			InputStream stream = connection.getInputStream();

			byte[] buffer = new byte[BUFFER_SIZE];
			int count = 0;

			BufferedInputStream bufStream =
				new BufferedInputStream(stream, BUFFER_SIZE);

			while ((count = bufStream.read(buffer)) != -1) {
				if (isCancelled())
					return false;

				result += new String(buffer, 0, count);
			}

			bufStream.close();

			mResponseCode = connection.getResponseCode();
			mBannerContent = result.toString();
			mBannerAdType = connection.getHeaderField("X-Adtype");

			Log.d(LOGTAG, "Response code: " + mResponseCode);
			Log.d(LOGTAG, "Banner content: " + mBannerContent);
			Log.d(LOGTAG, "X-Adtype: " + mBannerAdType);

			fetched = true;
		} catch (IOException e) {
			Log.w(LOGTAG, "URL " + request.getRequestUri() + " doesn't exist");
		} catch (Exception e) {
			Log.e(LOGTAG, "Exception while downloading banner: " + e.getMessage());
		} finally {
			connection.disconnect();
		}

		return fetched;
	}

	@Override
	protected void onPostExecute(Boolean fetched) {
		if (!fetched) {
			notifyOnBannerLoadFailed(LoadError.DownloadFailed);
		} else if (mResponseCode.equals(RESPONSE_CODE_NO_CONTENT)) {
			notifyOnBannerLoadFailed(LoadError.NoHaveBanner);
		} else {
			try {
				mView.loadAd(
					mBannerContent,
					BannerAdType.valueOf(mBannerAdType)
				);

				notifyOnBannerLoaded();
			} catch (IllegalArgumentException e) {
				Log.e(LOGTAG, "Unsupported ad type: " + mBannerAdType, e);

				notifyOnBannerLoadFailed(LoadError.UnknownAnswer);
			}
		}
	}

	// FIXME: move data to post
	protected void modifyConnection(HttpURLConnection connection) {
		connection.setRequestProperty(
			"User-Agent",
			Plus1Helper.getUserAgent()
		);

		connection.setRequestProperty(
			"Cookie",
			"wssid="+Plus1Helper.getClientSessionId(mView.getContext())
		);

		connection.setRequestProperty(
			"x-original-user-agent",
			mView.getWebViewUserAgent()
		);

		connection.setRequestProperty(
			"x-display-metrics",
			getDisplayMetrics()
		);

		connection.setRequestProperty(
			"x-container-metrics",
			getContainerMetrics()
		);

		connection.setRequestProperty(
			"x-application-type",
			"android"
		);

		connection.setRequestProperty(
			"x-preferred-locale",
			Locale.getDefault().getDisplayName(Locale.US)
		);
	}

	protected String getDisplayMetrics()
	{
		DisplayMetrics metrics = new DisplayMetrics();

		((Activity)mView.getContext()).
			getWindowManager().
			getDefaultDisplay().
			getMetrics(metrics);

		return
			String.valueOf(metrics.widthPixels) + "x"
			+ String.valueOf(metrics.heightPixels);
	}

	protected String getContainerMetrics()
	{
		float density =
			((Activity)mView.getContext())
				.getResources()
				.getDisplayMetrics()
				.density;

		return
			String.valueOf(
				(int) (mView.getLayoutParams().width / density + 0.5f)
			)
			+ "x"
			+ String.valueOf(
				(int) (mView.getLayoutParams().height / density + 0.5f)
			);
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

	private void notifyOnBannerLoaded() {
		for (Plus1BannerDownloadListener listener : mListenerList)
			listener.onBannerLoaded();
	}

	private void notifyOnBannerLoadFailed(LoadError loadError) {
		for (Plus1BannerDownloadListener listener : mListenerList)
			listener.onBannerLoadFailed(loadError);
	}
}
