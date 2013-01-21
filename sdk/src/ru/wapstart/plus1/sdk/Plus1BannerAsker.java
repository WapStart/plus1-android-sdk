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

import android.content.Context;
import android.location.LocationManager;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;

public class Plus1BannerAsker {
	private static final String LOGTAG = "Plus1BannerAsker";
	private Plus1BannerRequest request						= null;
	private Plus1BannerView view							= null;
	private Handler handler									= null;
	private HtmlBannerDownloader downloaderTask				= null;
	private Runnable askerStopper							= null;

	private boolean disableAutoDetectLocation				= false;
	private boolean removeBannersOnPause					= false;
	private boolean disableWebViewCorePausing				= false;
	private int timeout										= 10;
	private int visibilityTimeout							= 0;

	private boolean initialized								= false;
	private boolean mWebViewCorePaused						= false;

	private LocationManager locationManager					= null;
	private Plus1LocationListener locationListener			= null;

	private Plus1BannerViewStateListener viewStateListener	= null;
	private Plus1BannerDownloadListener downloadListener	= null;

	public static Plus1BannerAsker create(
		Plus1BannerRequest request, Plus1BannerView view
	) {
		return new Plus1BannerAsker(request, view);
	}

	public Plus1BannerAsker(Plus1BannerRequest request, Plus1BannerView view) {
		this.request = request;
		this.view = view;
	}

	public void onPause() {
		stop();

		if (isRemoveBannersOnPause())
			view.removeAllBanners();
		else
			view.onPause();

		if (!isDisabledWebViewCorePausing() && !mWebViewCorePaused) {
			new WebView(view.getContext()).pauseTimers();
			Log.d(LOGTAG, "WebView core thread was PAUSED");

			mWebViewCorePaused = true;
		}
	}

	public void onResume() {
		if (!view.isExpanded()) {
			if (view.getAutorefreshEnabled())
				start();
			else
				startOnce();
		}

		view.onResume();

		if (mWebViewCorePaused) {
			new WebView(view.getContext()).resumeTimers();
			Log.d(LOGTAG, "WebView core thread was RESUMED");

			mWebViewCorePaused = false;
		}
	}

	public boolean isDisabledAutoDetectLocation() {
		return disableAutoDetectLocation;
	}

	public Plus1BannerAsker disableAutoDetectLocation(boolean disable) {
		this.disableAutoDetectLocation = disable;

		return this;
	}

	public boolean isRemoveBannersOnPause() {
		return removeBannersOnPause;
	}

	public Plus1BannerAsker setRemoveBannersOnPause(boolean orly) {
		this.removeBannersOnPause = orly;

		return this;
	}

	public boolean isDisabledWebViewCorePausing() {
		return disableWebViewCorePausing;
	}

	/**
	 * NOTE: This method is useful when you are using WebView instances
	 *       in another activities of your application. Please note, when
	 *       WebView core thread is running, all banners of activity still
	 *       working in background. The important thing is whether they are
	 *       consuming CPU cycles. To reduce this effect, please remove all
	 *       banners when you pausing activity. Use setRemoveBannersOnPause()
	 *       method to remove them automatically on pausing asker.
	 * @see setRemoveBannersOnPause() method
	 */
	public Plus1BannerAsker setDisabledWebViewCorePausing(boolean orly) {
		this.disableWebViewCorePausing = orly;

		return this;
	}

	public Plus1BannerAsker setTimeout(int timeout) {
		this.timeout = timeout;

		return this;
	}

	public Plus1BannerAsker setVisibilityTimeout(int visibilityTimeout) {
		this.visibilityTimeout = visibilityTimeout;

		return this;
	}

	/**
	 * @deprecated use Plus1BannerView::setViewStateListener() method
	 */
	public Plus1BannerAsker setViewStateListener(
		Plus1BannerViewStateListener viewStateListener
	) {
		this.viewStateListener = viewStateListener;

		return this;
	}

	public Plus1BannerAsker setDownloadListener(
		Plus1BannerDownloadListener downloadListener
	) {
		this.downloadListener = downloadListener;

		return this;
	}

	public Plus1BannerAsker init() {
		if (initialized)
			return this;

		if (!isDisabledAutoDetectLocation()) {
			this.locationManager =
				(LocationManager) view.getContext().getSystemService(
					Context.LOCATION_SERVICE
				);

			this.locationListener = new Plus1LocationListener(request);
		}

		view.setViewStateListener( // FIXME: change to add* method
			new Plus1BannerViewStateListener() {
				public void onShowBannerView() {
					onShowBannerView();
				}

				public void onHideBannerView() {
					onHideBannerView();
				}

				public void onCloseBannerView() {
					onCloseBannerView();
				}

				public void onExpandStateChanged(boolean expanded) {
					if (expanded)
						stop();
					else
						start();
				}
			}
		);

		if (viewStateListener != null)
			view.setViewStateListener(viewStateListener); // FIXME: change to add* method

		if (visibilityTimeout == 0)
			visibilityTimeout = timeout * 3;

		handler = new Handler();

		// NOTE: useful in case when timers are paused and activity was destroyed
		new WebView(view.getContext()).resumeTimers();

		initialized = true;

		return this;
	}

	// NOTE: for manual refreshing
	public void refreshBanner() {
		if (!view.isExpanded()) {
			stop();

			if (view.getAutorefreshEnabled())
				start();
			else
				startOnce();
		}
	}

	/**
	 * @deprecated this method will be protected in future
	 */
	public void onShowBannerView() {
		if (askerStopper != null)
			handler.removeCallbacks(askerStopper);
	}

	/**
	 * @deprecated this method will be protected in future
	 */
	public void onHideBannerView() {
		if (askerStopper != null)
			return;

		askerStopper =
			new Runnable() {
				public void run() {
					stop();
				}
			};

		handler.postDelayed(askerStopper, visibilityTimeout * 1000);
	}

	/**
	 * @deprecated this method will be protected in future
	 */
	public void onCloseBannerView() {
		stop();
	}

	private void start() {
		Log.d(LOGTAG, "start() method fired");

		if (request == null || view == null || downloaderTask != null)
			return;

		init();

		if (!isDisabledAutoDetectLocation()) {
			locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				timeout * 10000,
				500f,
				locationListener
			);
		}

		downloaderTask = makeDownloaderTask();

		downloaderTask.execute();
	}

	private void stop() {
		Log.d(LOGTAG, "stop() method fired");

		if (downloaderTask == null)
			return;

		if (!isDisabledAutoDetectLocation())
			locationManager.removeUpdates(locationListener);

		downloaderTask.cancel(true);
		downloaderTask = null;
	}

	private void startOnce() {
		Log.d(LOGTAG, "startOnce() method fired");

		if (request == null || view == null || downloaderTask != null)
			return;

		init();

		downloaderTask = makeDownloaderTask();

		downloaderTask.setRunOnce().execute();
	}

	private HtmlBannerDownloader makeDownloaderTask()
	{
		HtmlBannerDownloader task = new HtmlBannerDownloader(view);

		task
			.setRequest(request)
			.setTimeout(timeout);

		if (downloadListener != null)
			task.setDownloadListener(downloadListener);

		return task;
	}
}
