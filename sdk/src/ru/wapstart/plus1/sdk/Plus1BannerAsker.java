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
	private Plus1BannerRequest mRequest						= null;
	private Plus1BannerView mView							= null;
	private Handler mHandler								= null;
	private HtmlBannerDownloader mDownloaderTask			= null;
	private Runnable mAskerStopper							= null;

	private boolean mDisableAutoDetectLocation				= false;
	private boolean mRemoveBannersOnPause					= false;
	private boolean mDisableWebViewCorePausing				= false;
	private int mTimeout									= 10;
	private int mVisibilityTimeout							= 0;

	private boolean mInitialized							= false;
	private boolean mWebViewCorePaused						= false;

	private LocationManager mLocationManager				= null;
	private Plus1LocationListener mLocationListener			= null;

	private Plus1BannerViewStateListener viewStateListener	= null;
	private Plus1BannerDownloadListener mDownloadListener	= null;

	public static Plus1BannerAsker create(
		Plus1BannerRequest request, Plus1BannerView view
	) {
		return new Plus1BannerAsker(request, view);
	}

	public Plus1BannerAsker(Plus1BannerRequest request, Plus1BannerView view) {
		mRequest = request;
		mView = view;
	}

	public void onPause() {
		stop();

		if (isRemoveBannersOnPause())
			mView.removeAllBanners();
		else
			mView.onPause();

		if (!isDisabledWebViewCorePausing() && !mWebViewCorePaused) {
			new WebView(mView.getContext()).pauseTimers();
			Log.d(LOGTAG, "WebView core thread was PAUSED");

			mWebViewCorePaused = true;
		}
	}

	public void onResume() {
		if (!mView.isExpanded()) {
			if (mTimeout > 0)
				start();
			else
				startOnce();
		}

		mView.onResume();

		if (mWebViewCorePaused) {
			new WebView(mView.getContext()).resumeTimers();
			Log.d(LOGTAG, "WebView core thread was RESUMED");

			mWebViewCorePaused = false;
		}
	}

	public boolean isDisabledAutoDetectLocation() {
		return mDisableAutoDetectLocation;
	}

	public Plus1BannerAsker disableAutoDetectLocation(boolean disable) {
		mDisableAutoDetectLocation = disable;

		return this;
	}

	public boolean isRemoveBannersOnPause() {
		return mRemoveBannersOnPause;
	}

	public Plus1BannerAsker setRemoveBannersOnPause(boolean orly) {
		mRemoveBannersOnPause = orly;

		return this;
	}

	public boolean isDisabledWebViewCorePausing() {
		return mDisableWebViewCorePausing;
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
		mDisableWebViewCorePausing = orly;

		return this;
	}

	public Plus1BannerAsker setTimeout(int timeout) {
		mTimeout = timeout;

		return this;
	}

	public Plus1BannerAsker setVisibilityTimeout(int visibilityTimeout) {
		mVisibilityTimeout = visibilityTimeout;

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
		mDownloadListener = downloadListener;

		return this;
	}

	public Plus1BannerAsker init() {
		if (mInitialized)
			return this;

		if (!isDisabledAutoDetectLocation()) {
			mLocationManager =
				(LocationManager)mView.getContext().getSystemService(
					Context.LOCATION_SERVICE
				);

			mLocationListener = new Plus1LocationListener(mRequest);
		}

		mView.addViewStateListener(
			new Plus1BannerViewStateListener() {
				public void onShowBannerView() {
					if (mAskerStopper != null)
						mHandler.removeCallbacks(mAskerStopper);
				}

				public void onHideBannerView() {
					if (mAskerStopper == null) {
						mAskerStopper =
							new Runnable() {
								public void run() {
									stop();
								}
							};
					}

					mHandler.postDelayed(mAskerStopper, mVisibilityTimeout * 1000);
				}

				public void onCloseBannerView() {
					stop();

					if (mAskerStopper != null)
						mHandler.removeCallbacks(mAskerStopper);
				}

				public void onExpandStateChanged(boolean expanded) {
					if (expanded)
						stop();
					else
						start();
				}
			}
		);

		// TODO: remove this in next release
		if (viewStateListener != null)
			mView.addViewStateListener(viewStateListener);

		if (mVisibilityTimeout == 0)
			mVisibilityTimeout = mTimeout * 3;

		mHandler = new Handler();

		// NOTE: useful in case when timers are paused and activity was destroyed
		new WebView(mView.getContext()).resumeTimers();

		mInitialized = true;

		return this;
	}

	// NOTE: for manual refreshing
	public void refreshBanner() {
		if (!mView.isExpanded()) {
			stop();

			if (mTimeout > 0)
				start();
			else
				startOnce();
		}
	}

	/**
	 * @deprecated this method will be protected in future
	 */
	public void onShowBannerView() {
		if (mAskerStopper != null)
			mHandler.removeCallbacks(mAskerStopper);
	}

	/**
	 * @deprecated this method will be protected in future
	 */
	public void onHideBannerView() {
		if (mAskerStopper != null)
			return;

		mAskerStopper =
			new Runnable() {
				public void run() {
					stop();
				}
			};

		mHandler.postDelayed(mAskerStopper, mVisibilityTimeout * 1000);
	}

	/**
	 * @deprecated this method will be protected in future
	 */
	public void onCloseBannerView() {
		stop();
	}

	private void start() {
		Log.d(LOGTAG, "start() method fired");

		if (mRequest == null || mView == null || mDownloaderTask != null)
			return;

		init();

		if (!isDisabledAutoDetectLocation()) {
			mLocationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				mTimeout * 10000,
				500f,
				mLocationListener
			);
		}

		mDownloaderTask = makeDownloaderTask();

		mDownloaderTask.execute();
	}

	private void startOnce() {
		Log.d(LOGTAG, "startOnce() method fired");

		if (mRequest == null || mView == null || mDownloaderTask != null)
			return;

		init();

		mDownloaderTask = makeDownloaderTask();

		mDownloaderTask.setRunOnce().execute();
	}

	private void stop() {
		Log.d(LOGTAG, "stop() method fired");

		if (mDownloaderTask == null)
			return;

		if (!isDisabledAutoDetectLocation())
			mLocationManager.removeUpdates(mLocationListener);

		mDownloaderTask.cancel(true);
		mDownloaderTask = null;
	}

	private HtmlBannerDownloader makeDownloaderTask()
	{
		HtmlBannerDownloader task = new HtmlBannerDownloader(mView);

		task
			.setRequest(mRequest)
			.setTimeout(mTimeout);

		if (mDownloadListener != null)
			task.setDownloadListener(mDownloadListener);

		return task;
	}
}
