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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import ru.wapstart.plus1.sdk.Plus1BannerDownloadListener.LoadError;
import ru.wapstart.plus1.sdk.Plus1BannerDownloadListener.BannerAdType;
import ru.wapstart.plus1.sdk.InitRequestLoader.InitRequestLoadListener;
import ru.wapstart.plus1.sdk.BaseRequestLoader.ChangeSdkPropertiesListener;
import ru.wapstart.plus1.sdk.BaseRequestLoader.SdkAction;
import ru.wapstart.plus1.sdk.BaseRequestLoader.SdkParameter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;

public class Plus1BannerAsker {
	private static final String LOGTAG = "Plus1BannerAsker";
	private static final String VALUE_OPEN_IN_BROWSER = "browser";
	private static final String VALUE_OPEN_IN_APPLICATION = "application";
	private static final String VALUE_RESTORE_LAST = "-1";

	private Plus1Request mRequest							= null;
	private Plus1BannerView mView							= null;
	private HtmlBannerDownloader mDownloaderTask			= null;

	private boolean mDisabledAutoDetectLocation				= false;
	private boolean mRemoveBannersOnPause					= false;
	private boolean mDisabledWebViewCorePausing				= false;
	private int mRefreshDelay								= 10;
	private int mLocationRefreshDelay						= 300;
	private int mRefreshRetryNum							= 3;
	private int mReInitDelay								= 60;
	private int mFacebookInfoDelay							= 60;
	private int mTwitterInfoDelay							= 60;

	private boolean mInitialized							= false;
	private boolean mWebViewCorePaused						= false;
	private int mRefreshRetryCount							= 0;

	private LocationManager mLocationManager				= null;
	private LocationListener mLocationListener				= null;

	private Plus1BannerDownloadListener mDownloadListener	= null;

	private EnumMap<SdkParameter, String> mLastSdkParameterMap =
		new EnumMap<SdkParameter, String>(SdkParameter.class);
	private EnumMap<SdkAction, String> mLastSdkActionMap =
		new EnumMap<SdkAction, String>(SdkAction.class);

	Runnable mExecuteDownloadTask = new Runnable() {
		public void run() {
			if (!(mView.isClosed() || mView.isExpanded())) {
				modifyRequest(mRequest);

				mDownloaderTask = makeDownloaderTask();
				mDownloaderTask.execute(mRequest);
			}

			if (isAutoRefreshEnabled())
				mExecuteDownloadHandler.postDelayed(this, mRefreshDelay * 1000);
		}
	};
	private Handler mExecuteDownloadHandler					= new Handler();

	private Runnable mReInitRequestTask = new Runnable() {
		public void run() {
			modifyRequest(mRequest);

			makeInitRequestTask().execute(mRequest);

			mReInitHandler.postDelayed(this, mReInitDelay * 1000);
		}
	};
	private Handler mReInitHandler = new Handler();

	private Runnable mFacebookInfoTask = new Runnable() {
		public void run() {
			try {
				final Class sessionCls = Class.forName("com.facebook.Session");
				final Class userCls = Class.forName("com.facebook.model.GraphUser");
				final Class requestCls = Class.forName("com.facebook.Request");
				final Class callbackCls = Class.forName("com.facebook.Request$GraphUserCallback");

				final Object session = sessionCls.getMethod("getActiveSession").invoke(null);

				if (session != null && (Boolean)sessionCls.getMethod("isOpened").invoke(session) == true) {
					final Object callbackInstance = Proxy.newProxyInstance(callbackCls.getClassLoader(), new Class[]{callbackCls}, new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							if(method.getName().equals("onCompleted")) {
								if (session == sessionCls.getMethod("getActiveSession").invoke(null) && args[0] != null) {
									mRequest.setFacebookUserHash(
										Plus1Helper.getHash(
											String.valueOf(
												userCls.getMethod("getId").invoke(args[0])
											)
										)
									);

									Log.d(LOGTAG, "Facebook user hash was updated: " + mRequest.getFacebookUserHash());
								}

								return 1;
							}

							return -1;
						}
					});

					requestCls.getMethod("executeMeRequestAsync", sessionCls, callbackCls)
						.invoke(null, session, callbackInstance);
				}

				// NOTE: loop only if no exceptions
				mFacebookInfoHandler.postDelayed(this, mFacebookInfoDelay * 1000);
			} catch (ClassNotFoundException e) {
				Log.d(LOGTAG, "Application is not using facebook sdk");
			} catch (SecurityException e) {
				Log.w(LOGTAG, "Security exception using facebook sdk", e);
			} catch (NoSuchMethodException e) {
				Log.w(LOGTAG, "No such method exception in facebook sdk", e);
			} catch (IllegalAccessException e) {
				Log.w(LOGTAG, "Illegal access exception in facebook sdk", e);
			} catch (InvocationTargetException e) {
				Log.w(LOGTAG, "Invocation target exception in facebook sdk", e);
			} catch (IllegalStateException e) {
				Log.w(LOGTAG, "Illegal state exception in facebook sdk", e);
			}
		}
	};
	private Handler mFacebookInfoHandler = new Handler();

	private Runnable mTwitterInfoTask = new Runnable() {
		public void run() {
			try {
				final Class factoryCls = Class.forName("twitter4j.TwitterFactory");
				final Class twitterCls = Class.forName("twitter4j.TwitterImpl");
				final Class configCls = Class.forName("twitter4j.conf.Configuration");

				// TODO: search another way to get configuration
				Field hackedMap = twitterCls.getDeclaredField("implicitParamsMap");
				hackedMap.setAccessible(true);
				HashMap hashMap = (HashMap)hackedMap.get(null);

				for (Object conf : hashMap.keySet()) {
					boolean allowAuth =
						configCls.getMethod("getOAuthConsumerKey").invoke(conf) != null
						&& configCls.getMethod("getOAuthConsumerSecret").invoke(conf) != null;

					if (allowAuth) {
						if ((Boolean)configCls.getMethod("isApplicationOnlyAuthEnabled").invoke(conf)) {
							allowAuth =
								configCls.getMethod("getOAuth2TokenType").invoke(conf) != null
								&& configCls.getMethod("getOAuth2AccessToken").invoke(conf) != null;
						} else {
							allowAuth =
								configCls.getMethod("getOAuthAccessToken").invoke(conf) != null
								&& configCls.getMethod("getOAuthAccessTokenSecret").invoke(conf) != null;
						}
					} else {
						allowAuth =
							configCls.getMethod("getUser").invoke(conf) != null
							&& configCls.getMethod("getPassword").invoke(conf) != null;
					}

					if (allowAuth) {
						Object twitter = factoryCls.getMethod("getInstance").invoke(
							factoryCls.getConstructor(configCls).newInstance(conf)
						);

						mRequest.setTwitterUserHash(
							Plus1Helper.getHash(
								String.valueOf(
									twitterCls.getMethod("getId").invoke(twitter)
								)
							)
						);

						Log.d(LOGTAG, "Twitter user hash was updated: " + mRequest.getTwitterUserHash());

						break;
					}
				}

				// NOTE: loop only if no exceptions
				mTwitterInfoHandler.postDelayed(this, mTwitterInfoDelay * 1000);
			} catch (ClassNotFoundException e) {
				Log.d(LOGTAG, "Application is not using twitter4j sdk");
			} catch (NoSuchMethodException e) {
				Log.w(LOGTAG, "No such method exception in twitter4j sdk", e);
			} catch (IllegalAccessException e) {
				Log.w(LOGTAG, "Illegal access exception in twitter4j sdk", e);
			} catch (InvocationTargetException e) {
				Log.w(LOGTAG, "Invocation target exception in twitter4j sdk", e);
			} catch (NoSuchFieldException e) {
				Log.w(LOGTAG, "No such field exception in twitter4j sdk", e);
			} catch (InstantiationException e) {
				Log.w(LOGTAG, "Instantiation exception in twitter4j sdk", e);
			} catch (IllegalArgumentException e) {
				Log.w(LOGTAG, "Illegal argument exception in twitter4j sdk", e);
			}
		}
	};
	private Handler mTwitterInfoHandler = new Handler();

	public static Plus1BannerAsker create(
		Plus1Request request, Plus1BannerView view
	) {
		return new Plus1BannerAsker(request, view);
	}

	public Plus1BannerAsker(Plus1Request request, Plus1BannerView view) {
		mRequest = request;
		mView = view;
	}

	public void onPause() {
		stop();

		mReInitHandler.removeCallbacks(mReInitRequestTask);
		mFacebookInfoHandler.removeCallbacks(mFacebookInfoTask);
		mTwitterInfoHandler.removeCallbacks(mTwitterInfoTask);

		if (!isDisabledAutoDetectLocation())
			removeLocationUpdates();

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
		init();

		if (!mView.isExpanded())
			start();

		mReInitHandler.post(mReInitRequestTask);
		mFacebookInfoHandler.post(mFacebookInfoTask);
		mTwitterInfoHandler.post(mTwitterInfoTask);

		if (!isDisabledAutoDetectLocation())
			requestLocationUpdates(false); // NOTE: include disabled providers

		mView.onResume();

		if (mWebViewCorePaused) {
			new WebView(mView.getContext()).resumeTimers();
			Log.d(LOGTAG, "WebView core thread was RESUMED");

			mWebViewCorePaused = false;
		}
	}

	public boolean isDisabledAutoDetectLocation() {
		return mDisabledAutoDetectLocation;
	}

	public Plus1BannerAsker disableAutoDetectLocation() {
		mDisabledAutoDetectLocation = true;

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
		return mDisabledWebViewCorePausing;
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
		mDisabledWebViewCorePausing = orly;

		return this;
	}

	public Plus1BannerAsker setRefreshDelay(int delayInSeconds) {
		mRefreshDelay = delayInSeconds;

		return this;
	}

	public Plus1BannerAsker setRefreshRetryNum(int refreshRetryNum) {
		mRefreshRetryNum = refreshRetryNum;

		return this;
	}

	public Plus1BannerAsker setReInitDelay(int delayInSeconds) {
		mReInitDelay = delayInSeconds;

		return this;
	}

	public Plus1BannerAsker setLocationRefreshDelay(int delayInSeconds) {
		mLocationRefreshDelay = delayInSeconds;

		return this;
	}

	public boolean isAutoRefreshEnabled() {
		return mRefreshDelay > 0;
	}

	public boolean isLocationAutoRefreshEnabled() {
		return mLocationRefreshDelay > 0;
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

			mLocationListener = new LocationListener() {
				public void onLocationChanged(Location location) {
					mRequest.setLocation(location);

					if (!isLocationAutoRefreshEnabled())
						removeLocationUpdates();
				}

				public void onProviderDisabled(String provider) {
					Log.d(LOGTAG, "Location provider '"+provider+"' is disabled");
					requestLocationUpdates();
				}

				public void onProviderEnabled(String provider) {
					Log.d(LOGTAG, "Location provider '"+provider+"' is enabled");
					// NOTE: discard another providers, detect new best provider
					removeLocationUpdates();
					requestLocationUpdates();
				}

				public void onStatusChanged(String provider, int status, Bundle extras) {
					Log.d(LOGTAG, "Location provider '"+provider+"' new status: "+status);

					switch (status) {
						case LocationProvider.AVAILABLE:
							removeLocationUpdates();
						case LocationProvider.OUT_OF_SERVICE:
							requestLocationUpdates();
							break;
						case LocationProvider.TEMPORARILY_UNAVAILABLE:
							break;
						default:
							Log.w(
								LOGTAG,
								"Illegal status value of location provider '"
								+provider+"' was found: "+status
							);
					}
				}
			};
		}

		mView
			.addListener(new Plus1BannerView.OnCloseButtonListener() {
				public void onCloseButton(Plus1BannerView view) {
					stop();
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

		// NOTE: useful in case when timers are paused and activity was destroyed
		new WebView(mView.getContext()).resumeTimers();

		mRequest.setUid(Plus1Helper.getClientSessionId(mView.getContext()));

		mInitialized = true;

		return this;
	}

	// NOTE: for manual refreshing
	public void refreshBanner() {
		if (!mView.isExpanded()) {
			stop();
			start();
		} else
			Log.w(LOGTAG, "Banner view is expanded, so refresh was prevented");
	}

	private void start() {
		Log.d(LOGTAG, "start() method fired");

		if (mRequest == null || mView == null || mDownloaderTask != null)
			return;

		mExecuteDownloadHandler.post(mExecuteDownloadTask);
	}

	private void stop() {
		Log.d(LOGTAG, "stop() method fired");

		if (mDownloaderTask != null) {
			mDownloaderTask.cancel(true);
			mDownloaderTask = null;
		}

		mExecuteDownloadHandler.removeCallbacks(mExecuteDownloadTask);
	}

	private void requestLocationUpdates() {
		requestLocationUpdates(true);
	}

	private void requestLocationUpdates(boolean enabledProviderOnly) {
		Criteria criteria = new Criteria();

		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setCostAllowed(false);

		String provider =
			mLocationManager.getBestProvider(criteria, enabledProviderOnly);

		if (provider == null) {
			Log.i(LOGTAG, "Location provider is not found, updates turned off");
			return;
		}

		try {
			mLocationManager.requestLocationUpdates(
				provider,
				mLocationRefreshDelay * 1000,
				500f,
				mLocationListener
			);

			if (mRequest.getLocation() == null) {
				mRequest.setLocation(
					mLocationManager.getLastKnownLocation(provider)
				);
			}

			Log.d(
				LOGTAG,
				"Location provider '"+provider+"' was choosen for updates"
			);
		} catch (IllegalArgumentException e) {
			if (!enabledProviderOnly) {
				Log.d(LOGTAG, "Location provider '"+provider+"' doesn't exist - request enabled only providers");
				requestLocationUpdates(true);
			} else
				Log.i(LOGTAG, "Location provider '"+provider+"' doesn't exist on this device, updates turned off");
		}
	}

	private void removeLocationUpdates() {
		mLocationManager.removeUpdates(mLocationListener);
	}

	private HtmlBannerDownloader makeDownloaderTask()
	{
		mRefreshRetryCount = 0;

		HtmlBannerDownloader task = new HtmlBannerDownloader();
		task.addDownloadListener(new Plus1BannerDownloadListener() {
			public void onBannerLoaded(String content, BannerAdType adType) {
				mRefreshRetryCount = 0;

				mView.loadAd(content, adType);
			}

			public void onBannerLoadFailed(LoadError error) {
				if (++mRefreshRetryCount >= mRefreshRetryNum)
					stop();
			}
		});

		if (mDownloadListener != null)
			task.addDownloadListener(mDownloadListener);

		modifyRequestLoaderTask(task);

		return task;
	}

	private InitRequestLoader makeInitRequestTask()
	{
		InitRequestLoader task = new InitRequestLoader();
		task.addInitRequestLoadListener(new InitRequestLoadListener() {
			public void onUniqueIdLoaded(String uid) {
				mRequest.setUid(uid);

				Plus1Helper.setClientSessionId(mView.getContext(), uid);
			}

			public void onUniqueIdLoadFailed() {
				Log.w(LOGTAG, "Failed to load init request");
			}
		});

		modifyRequestLoaderTask(task);

		return task;
	}

	private void modifyRequestLoaderTask(BaseRequestLoader task) {
		task.addChangeSdkPropertiesListener(new ChangeSdkPropertiesListener() {
			public void onSdkParametersLoaded(EnumMap<SdkParameter, String> parameters) {
				for(Entry<SdkParameter, String> entry : parameters.entrySet()) {
					SdkParameter key = entry.getKey();
					String value = entry.getValue();

					if (
						value.equals(VALUE_RESTORE_LAST)
						&& null != mLastSdkParameterMap.get(key)
					) {
						value = mLastSdkParameterMap.get(key);
					} else {
						mLastSdkParameterMap.put(key, value);
					}

					switch (key) {
						case refreshDelay:
							mRefreshDelay = Integer.parseInt(value);
							break;
						case reInitDelay:
							mReInitDelay = Integer.parseInt(value);
							break;
						case openIn:
							mView.setOpenInApplication(
								VALUE_OPEN_IN_APPLICATION.equals(value)
							);
							break;
					}
				}
			}

			public void onSdkActionsLoaded(EnumMap<SdkAction, String> actions) {
				for(Entry<SdkAction, String> entry : actions.entrySet()) {
					SdkAction key = entry.getKey();
					String value = entry.getValue();

					if (
						value.equals(VALUE_RESTORE_LAST)
						&& null != mLastSdkActionMap.get(key)
					) {
						value = mLastSdkActionMap.get(key);
					} else {
						mLastSdkActionMap.put(key, value);
					}

					switch (key) {
						case openLink:
							openLink(value);
							break;
					}
				}
			}

			public void onSdkChangeUid(String newUid) {
				mRequest.setUid(newUid);
			}
		});

		task.addRequestProperty("User-Agent", Plus1Helper.getUserAgent());
		task.addRequestProperty("x-original-user-agent", mView.getWebViewUserAgent());
	}

	private void openLink(String url)
	{
		// TODO: add another parametrization
		url.replaceAll("%reinitTimeout%", String.valueOf(mReInitDelay));
		url.replaceAll("%refreshTimeout%", String.valueOf(mRefreshDelay));
		url.replaceAll("%uid%", mRequest.getUID());

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mView.getContext().getApplicationContext().startActivity(intent);
	}

	// FIXME: think about another request-update logic
	private void modifyRequest(Plus1Request request) {
		request.setDisplayMetrics(
			Plus1Helper.getDisplayMetrics(
				((Activity)mView.getContext())
					.getWindowManager().getDefaultDisplay()
			)
		);

		request.setContainerMetrics(
			Plus1Helper.getContainerMetrics(mView)
		);
	}
}
