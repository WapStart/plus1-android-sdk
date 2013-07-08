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

final class HtmlBannerDownloader extends AsyncTask<Void, Void, Void> {
	private static final String LOGTAG = "HtmlBannerDownloader";
	private static final Integer BUFFER_SIZE = 8192;
	private static final String NO_BANNER = "<!-- i4jgij4pfd4ssd -->";

	protected Plus1BannerView view			= null;
	protected Plus1BannerRequest request	= null;
	protected int timeout					= 0;
	protected boolean runOnce               = false;

	protected String mBannerData			= null;
	protected String mBannerAdType			= null;

	protected ArrayList<Plus1BannerDownloadListener> mListenerList;

	public HtmlBannerDownloader(Plus1BannerView view) {
		this.view = view;

		mListenerList = new ArrayList<Plus1BannerDownloadListener>();
	}

	public HtmlBannerDownloader setRequest(Plus1BannerRequest request) {
		this.request = request;

		return this;
	}

	public HtmlBannerDownloader setTimeout(int timeout) {
		this.timeout = timeout;

		return this;
	}

	public HtmlBannerDownloader setRunOnce() {
		return setRunOnce(true);
	}

	public HtmlBannerDownloader setRunOnce(boolean runOnce) {
		this.runOnce = runOnce;

		return this;
	}

	public HtmlBannerDownloader addDownloadListener(
		Plus1BannerDownloadListener bannerDownloadListener
	) {
		mListenerList.add(bannerDownloadListener);

		return this;
	}

	/**
	 * @deprecated please use addDownloadListener method
	 */
	public HtmlBannerDownloader setDownloadListener(
		Plus1BannerDownloadListener bannerDownloadListener
	) {
		return addDownloadListener(bannerDownloadListener);
	}

	@Override
	protected Void doInBackground(Void... voids)
	{
		while (!isCancelled()) {
			if (view.isClosed() || view.isExpanded())
				return null;

			updateBanner();

			if (runOnce)
				return null;

			try {
				Thread.sleep(1000 * timeout);
			} catch (InterruptedException e) {
				return null;
			}
		}

		return null;
	}

	protected void updateBanner()
	{
		fetchBanner();

		view.post(new Runnable() {
			public void run() {
				if (mBannerData == null) {
					notifyOnBannerLoadFailed(LoadError.DownloadFailed);
				} else if (NO_BANNER.equals(mBannerData)) {
					notifyOnBannerLoadFailed(LoadError.NoHaveBanner);
				} else if (
					!"plus1".equals(mBannerAdType)
					&& !"mraid".equals(mBannerAdType)
				) {
					notifyOnBannerLoadFailed(LoadError.UnknownAnswer);
				} else {
					notifyOnBannerLoaded();

					if (!isCancelled())
						view.loadAd(mBannerData, mBannerAdType);
				}
			}
		});
	}

	protected boolean fetchBanner()
	{
		HttpURLConnection connection = makeConnection(request.getRequestUri());

		if (connection == null)
			return false;

		String result = "";
		boolean fetched = false;

		mBannerData = null;
		mBannerAdType = null;

		try {
			InputStream stream = connection.getInputStream();

			byte[] buffer = new byte[BUFFER_SIZE];
			int count = 0;

			BufferedInputStream bufStream =
				new BufferedInputStream (
					stream,
					BUFFER_SIZE
				);

			while ((count = bufStream.read(buffer)) != -1) {
				if (isCancelled())
					return false;

				result += new String(buffer, 0, count);
			}

			bufStream.close();

			mBannerData = result.toString().trim();
			mBannerAdType = connection.getHeaderField("X-Adtype");

			Log.d(LOGTAG, "Answer: " + mBannerData);
			Log.d(LOGTAG, "X-Adtype: " + mBannerAdType);

			fetched = true;
		} catch (IOException e) {
			Log.w(getClass().getName(), "URL " + request.getRequestUri() + " doesn't exist");
		} catch (Exception e) {
			Log.e(
				getClass().getName(),
				"Exception while downloading banner: " + e.getMessage()
			);
		} finally {
			connection.disconnect();
		}

		return fetched;
	}

	protected void modifyConnection(HttpURLConnection connection) {
		connection.setRequestProperty(
			"User-Agent",
			Plus1Helper.getUserAgent()
		);

		connection.setRequestProperty(
			"Cookie",
			"wssid="+Plus1Helper.getClientSessionId(view.getContext())
		);

		connection.setRequestProperty(
			"x-original-user-agent",
			view.getWebViewUserAgent()
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

		((Activity)view.getContext()).
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
			((Activity)view.getContext())
				.getResources()
				.getDisplayMetrics()
				.density;

		return
			String.valueOf(
				(int) (view.getLayoutParams().width / density + 0.5f)
			)
			+ "x"
			+ String.valueOf(
				(int) (view.getLayoutParams().height / density + 0.5f)
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
			Log.e(getClass().getName(), "URL parsing failed: " + url);
		} catch (Exception e) {
			Log.e(getClass().getName(), "Unexpected exception", e);
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
