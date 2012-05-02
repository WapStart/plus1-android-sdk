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

import ru.wapstart.plus1.sdk.Plus1BannerDownloadListener.LoadError;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * @author Alexander Klestov <a.klestov@co.wapstart.ru>
 * @copyright Copyright (c) 2011, Wapstart
 */
final class HtmlBannerDownloader extends AsyncTask<Void, Void, Void> {
	private static final String LOGTAG = "HtmlBannerDownloader";
	private static final Integer BUFFER_SIZE = 8192;
	private static final String NO_BANNER = "<!-- i4jgij4pfd4ssd -->";
	
	protected Plus1BannerView view			= null;
	protected Plus1BannerRequest request	= null;
	protected int timeout					= 0;
	protected boolean runOnce               = false;
	
	private boolean running = true;

	protected Plus1BannerDownloadListener bannerDownloadListener = null;
	
	public HtmlBannerDownloader(Plus1BannerView view) {
		this.view = view;
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
		this.runOnce = true;

		return this;
	}

	public HtmlBannerDownloader setRunOnce(boolean runOnce) {
		this.runOnce = runOnce;

		return this;
	}	
	
	public HtmlBannerDownloader setDownloadListener(
		Plus1BannerDownloadListener bannerDownloadListener
	) {
		this.bannerDownloadListener = bannerDownloadListener;

		return this;
	}
	
    public void stop()
    {	
        running = false;
    }

	@Override	
	protected Void doInBackground(Void... voids)
	{
		while( running ) {
			if (view.isClosed())
				return null;

			updateBanner();

			if (runOnce)
				return null;
			
			try {
				Thread.sleep(1000 * timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

	protected void updateBanner()
	{
		final String result = getBannerData();

		if (!result.equals("") && !result.equals(NO_BANNER)) {
			view.post(new Runnable() {
				public void run() {
					view.loadAd(result.toString(), "");//connection.getHeaderField("X-Adtype"));
				}
			});
		}
	}

	/*protected Plus1Banner getBanner()
	{
		final String result = getBannerData();
		
		Log.d(getClass().getName(), "answer: " + result.toString());
		
		Plus1Banner banner = null;

		if (result.equals("")) {
			if (bannerDownloadListener != null)
				bannerDownloadListener.onBannerLoadFailed(
					LoadError.UnknownAnswer
				);
		} else if (result.equals(NO_BANNER)) { 
			if (bannerDownloadListener != null)
				bannerDownloadListener.onBannerLoadFailed(
					LoadError.NoHaveBanner
				);
		} else {
			banner = parse(result);

			if ((banner != null) && (banner.getId() > 0)) {
				if (bannerDownloadListener != null)
					bannerDownloadListener.onBannerLoaded();
			} else {
				if (bannerDownloadListener != null)
					bannerDownloadListener.onBannerLoadFailed(
						LoadError.UnknownAnswer
					);
			}
		}
		
		return banner;
	}*/
	
	protected String getBannerData()
	{
		InputStream stream = getStream(request.getRequestUri());
		
		String result = "";
		
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int count = 0;
			
			BufferedInputStream bufStream = 
				new BufferedInputStream (
					stream,
					BUFFER_SIZE
				);

			while ((count = bufStream.read(buffer)) != -1)
				result += new String(buffer, 0, count);
			
			bufStream.close();
		} catch (Exception e) {
			Log.e(
				getClass().getName(),
				"Exception while downloading banner: " + e.getMessage()
			);

			if (bannerDownloadListener != null)
				bannerDownloadListener.onBannerLoadFailed(
					LoadError.DownloadFailed
				);
		}
		
		Log.d(LOGTAG, "answer: " + result.toString());

		return result;
	}
	
	protected void modifyConnection(HttpURLConnection connection) {
		connection.setRequestProperty(
			"User-Agent", 
			Plus1Helper.getUserAgent()
		);
		
		connection.setRequestProperty(
			"Cookies", 
			"wssid="+Plus1Helper.getClientSessionId(view.getContext())
		);
		
		connection.setRequestProperty(
			"x-display-metrics", 
			getDisplayMetrics()
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
	
	protected InputStream getStream(String url)
	{
		InputStream stream = null;
		HttpURLConnection connection;
		
		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
			modifyConnection(connection);
			connection.connect();
			
			stream = connection.getInputStream();
		} catch (MalformedURLException e) {
			Log.e(getClass().getName(), "URL parsing failed: " + url);
		} catch (IOException e) {
			Log.d(getClass().getName(), "URL " + url + " doesn't exist");
		} catch (Exception e) {
			Log.d(getClass().getName(), "Unexpected exception: " + e.getMessage());
		}
		
		return stream;
	}	
}
