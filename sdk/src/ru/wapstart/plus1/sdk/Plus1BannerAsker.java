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
	private int mRefreshDelay								= 10;
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

		// NOTE: bc
		view.setOnAutorefreshChangeListener(
			new Plus1BannerView.OnAutorefreshStateListener() {
				public void onAutorefreshStateChanged(Plus1BannerView view) {
					if (view.getAutorefreshEnabled() && !view.isExpanded())
						start();
					else
						stop();
				}
			}
		);
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
			if (mRefreshDelay > 0)
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

	public Plus1BannerAsker setRefreshDelay(int delay) {
		mRefreshDelay = delay;

		return this;
	}

	/**
	 * @deprecated please use setRefreshDelay() method
	 */
	public Plus1BannerAsker setTimeout(int timeout) {
		return setRefreshDelay(timeout);
	}

	public Plus1BannerAsker setVisibilityTimeout(int visibilityTimeout) {
		mVisibilityTimeout = visibilityTimeout;

		return this;
	}

	/**
	 * @deprecated please use inner listener interfaces like Plus1BannerView::OnShowListener
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

		mView
			.addListener(new Plus1BannerView.OnShowListener() {
				public void onShow(Plus1BannerView view) {
					onShowBannerView();
				}
			})
			.addListener(new Plus1BannerView.OnHideListener() {
				public void onHide(Plus1BannerView view) {
					onHideBannerView();
				}
			})
			.addListener(new Plus1BannerView.OnCloseButtonListener() {
				public void onCloseButton(Plus1BannerView view) {
					onCloseBannerView();
				}
			})
			.addListener(new Plus1BannerView.OnExpandListener() {
				public void onExpand(Plus1BannerView view) {
					stop();
				}
			})
			.addListener(new Plus1BannerView.OnCollapseListener() {
				public void onCollapse(Plus1BannerView view) {
					start();
				}
			});

		// NOTE: bc
		if (viewStateListener != null)
			mView.setViewStateListener(viewStateListener);

		if (mVisibilityTimeout == 0)
			mVisibilityTimeout = mRefreshDelay * 3;

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

			if (mRefreshDelay > 0)
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

		if (mAskerStopper != null)
			mHandler.removeCallbacks(mAskerStopper);
	}

	private void start() {
		Log.d(LOGTAG, "start() method fired");

		if (mRequest == null || mView == null || mDownloaderTask != null)
			return;

		init();

		if (!isDisabledAutoDetectLocation()) {
			mLocationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				mRefreshDelay * 10000,
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
			.setTimeout(mRefreshDelay);

		if (mDownloadListener != null)
			task.setDownloadListener(mDownloadListener);

		return task;
	}
}
