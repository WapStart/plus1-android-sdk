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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import ru.wapstart.plus1.sdk.HtmlBannerDownloader.HtmlBannerInfo;
import ru.wapstart.plus1.sdk.Plus1BannerDownloadListener.LoadError;
import ru.wapstart.plus1.sdk.Plus1BannerDownloadListener.BannerAdType;

import android.util.Log;

final class HtmlBannerDownloader extends BaseRequestLoader<HtmlBannerInfo> {
	private static final String LOGTAG = "HtmlBannerDownloader";

	protected class HtmlBannerInfo {
		private Integer mResponseCode;
		private String mBannerContent;
		private String mBannerAdType;
	}

	private ArrayList<Plus1BannerDownloadListener> mDownloadListenerList =
			new ArrayList<Plus1BannerDownloadListener>();

	public void addDownloadListener(Plus1BannerDownloadListener bannerDownloadListener) {
		mDownloadListenerList.add(bannerDownloadListener);
	}

	protected String getRequestUrl(Plus1Request request) {
		return request.getUrl();
	}

	protected UrlEncodedFormEntity getUrlEncodedFormEntity(Plus1Request request)
		throws UnsupportedEncodingException
	{
		// FIXME: think about strict form entity
		return request.getUrlEncodedFormEntity();
	}

	protected HtmlBannerInfo makeResult(String content, HttpURLConnection connection)
		throws IOException
	{
		HtmlBannerInfo bannerInfo = new HtmlBannerInfo();
		bannerInfo.mResponseCode = connection.getResponseCode();
		bannerInfo.mBannerContent = content;
		bannerInfo.mBannerAdType = connection.getHeaderField("X-Adtype");

		Log.d(LOGTAG, "Response code: "		+ bannerInfo.mResponseCode);
		if (!bannerInfo.mResponseCode.equals(HttpStatus.SC_NO_CONTENT)) {
			Log.d(LOGTAG, "X-Adtype: "			+ bannerInfo.mBannerAdType);
			Log.d(LOGTAG, "Banner content: "	+ bannerInfo.mBannerContent);
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
					// FIXME: type of banner must exist
					null == bannerInfo.mBannerAdType
						? BannerAdType.plus1
						: BannerAdType.valueOf(bannerInfo.mBannerAdType)
				);
			} catch (IllegalArgumentException e) {
				Log.e(LOGTAG, "Unsupported ad type: " + bannerInfo.mBannerAdType, e);

				notifyOnBannerLoadFailed(LoadError.UnknownAnswer);
			}
		}
	}

	private void notifyOnBannerLoaded(String content, BannerAdType adType) {
		for (Plus1BannerDownloadListener listener : mDownloadListenerList)
			listener.onBannerLoaded(content, adType);
	}

	private void notifyOnBannerLoadFailed(LoadError loadError) {
		for (Plus1BannerDownloadListener listener : mDownloadListenerList)
			listener.onBannerLoadFailed(loadError);
	}
}
