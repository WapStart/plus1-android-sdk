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
import java.net.HttpURLConnection;
import java.util.Locale;

import ru.wapstart.plus1.sdk.Plus1BannerDownloadListener.LoadError;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * @author Alexander Klestov <a.klestov@co.wapstart.ru>
 * @copyright Copyright (c) 2011, Wapstart
 */
abstract class BaseBannerDownloader extends BaseDownloader {
	private static final Integer BUFFER_SIZE = 8192;
	private static final String NO_BANNER = "<!-- i4jgij4pfd4ssd -->";
	
	protected Plus1BannerView view			= null;
	protected Plus1BannerRequest request	= null;
	protected String deviceId				= null;
	protected int timeout					= 0;

	protected Plus1BannerDownloadListener bannerDownloadListener = null;
	
	public BaseBannerDownloader(Plus1BannerView view) {
		this.view = view;
	}
	
	public BaseBannerDownloader setDeviceId(String deviceId) {
		this.deviceId = deviceId;
		
		return this;
	}
	
	public BaseBannerDownloader setRequest(Plus1BannerRequest request) {
		this.request = request;
		
		return this;
	}
	
	public BaseBannerDownloader setTimeout(int timeout) {
		this.timeout = timeout;
		
		return this;
	}

	public BaseBannerDownloader setDownloadListener(
		Plus1BannerDownloadListener bannerDownloadListener
	) {
		this.bannerDownloadListener = bannerDownloadListener;

		return this;
	}
	
	@Override	
	protected Void doInBackground(Void... params)
	{
		if (view.isClosed())
			return null;
		
		if (request != null)
			this.url = request.getRequestUri();
		
		final String result = getData();
		
		Log.d(getClass().getName(), "answer: " + result.toString());
		
		view.post(new Runnable() {
			public void run() {
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
				
				view.setBanner(banner);
			}
		});
		
		return null;
	}
	
	protected String getData()
	{
		openConnection();
		
		String result = new String();
		
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int count = 0;
			
			BufferedInputStream bufStream = 
				new BufferedInputStream (
					stream,
					BUFFER_SIZE
				);
			
			if (bufStream != null)
				while ((count = bufStream.read(buffer)) != -1)
					result += new String(buffer, 0, count);
			
			bufStream.close();
		} catch (IOException e) {
			Log.e(getClass().getName(), "IOException in InputStream");

			if (bannerDownloadListener != null)
				bannerDownloadListener.onBannerLoadFailed(
					LoadError.DownloadFailed
				);
		}
		
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
		
		if ((deviceId != null) && !deviceId.equals(""))
			connection.setRequestProperty("x-device-imei", deviceId);
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
	
	abstract protected Plus1Banner parse(String answer);
}
