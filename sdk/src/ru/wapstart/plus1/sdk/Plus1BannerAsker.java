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
import android.telephony.TelephonyManager;

/**
 * @author Alexander Klestov <a.klestov@co.wapstart.ru>
 * @copyright Copyright (c) 2011, Wapstart
 */
public class Plus1BannerAsker implements BannerViewStateListener {
	private Plus1BannerRequest request				= null;
	private Plus1BannerView view					= null;
	private Handler handler							= null;
	private Runnable askerStoper					= null;
	private	BaseBannerDownloader downloader			= null;
	
	private String deviceId							= null;
	private boolean disableDispatchIMEI				= false;
	private boolean disableAutoDetectLocation		= false;
	private int timeout								= 10;
	private int visibilityTimeout					= 0;
	
	private boolean initialized						= false;

	private LocationManager locationManager			= null;
	private Plus1LocationListener locationListener	= null;
	
	
	public static Plus1BannerAsker create(
		Plus1BannerRequest request, Plus1BannerView view
	) {
		return new Plus1BannerAsker(request, view);
	}

	public Plus1BannerAsker(Plus1BannerRequest request, Plus1BannerView view) {
		this.request = request;
		this.view = view;
	}

	public boolean isDisabledIMEIDispatch() {
		return disableDispatchIMEI;
	}

	public Plus1BannerAsker disableDispatchIMEI(boolean disable) {
		this.disableDispatchIMEI = disable;

		return this;
	}

	public boolean isDisabledAutoDetectLocation() {
		return disableAutoDetectLocation;
	}
	
	public Plus1BannerAsker disableAutoDetectLocation(boolean disable) {
		this.disableAutoDetectLocation = disable;

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
		
		if (!isDisabledIMEIDispatch()) {
			TelephonyManager telephonyManager = 
				(TelephonyManager) view.getContext().getSystemService(
					Context.TELEPHONY_SERVICE
				);
			
			this.deviceId = telephonyManager.getDeviceId();
		}		
	
		if (request.getRequestType() == Plus1BannerRequest.RequestType.JSON)
			this.downloader = new JSONBannerDownloader(view);
		else
			this.downloader = new XMLBannerDownloader(view);
		
		downloader
			.setDeviceId(deviceId)
			.setRequest(request)
			.setTimeout(timeout);
		
		this.handler = new Handler();

		if (visibilityTimeout == 0)
			visibilityTimeout = timeout * 3;

		view.setStateListener(this);

		initialized = true;
		
		return this;
	}
	
	public Plus1BannerAsker start() {
		if ((request == null) || (view == null))
			return this;

		init();
		
		if (!isDisabledAutoDetectLocation()) {
			locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				timeout * 10000,
				500f,
				locationListener
			);
		}
		
		downloader.setHandler(handler);
		handler.removeCallbacks(downloader);
		handler.postDelayed(downloader, 100);
		
		return this;
	}
	
	public Plus1BannerAsker stop() {
		if (!isDisabledAutoDetectLocation())
			locationManager.removeUpdates(locationListener);
		
		handler.removeCallbacks(downloader);
		
		return this;
	}
	
	public Plus1BannerAsker startOnce() {
		if ((request == null) || (view == null))
			return this;

		init();
		downloader.setRunOnce();
		downloader.setHandler(handler);
		handler.removeCallbacks(downloader);
		handler.postDelayed(downloader, 100);
		
		return this;
	}

	public void onShowBannerView() {
		if (askerStoper != null) {
			handler.removeCallbacks(askerStoper);

			askerStoper = null;
		}
	}

	public void onHideBannerView() {
		askerStoper =
			new Runnable() {
				public void run() {
					stop();
				}
			};

		handler.postDelayed(askerStoper, visibilityTimeout * 1000);
	}

	public void onCloseBannerView() {
		stop();
	}
}
	
