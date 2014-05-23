/**
 * Copyright (c) 2012, Alexander Zaytsev <a.zaytsev@co.wapstart.ru>
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

import android.content.Intent;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;

public class AdView extends BaseAdView {

	private OnTouchListener mTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			return (event.getAction() == MotionEvent.ACTION_MOVE);
		}
	};

	private OnReadyListener mOnReadyListener;
	private OnClickListener mOnClickListener;

	private boolean mOpenInApplication	= false;

	public AdView(Context context) {
		super(context);

		disableScrollingAndZoom();
		getSettings().setJavaScriptEnabled(true);
		setBackgroundColor(Color.TRANSPARENT);
		setWebViewClient(new AdWebViewClient());
	}

	public void loadHtmlData(String data) {
		data = completeHtml(data);

		loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);
	}

	@Override
	public void pauseAdView() {
		setOnTouchListener(null);

		super.pauseAdView();
	}

	@Override
	public void resumeAdView() {
		setOnTouchListener(mTouchListener);

		super.resumeAdView();
	}

	public void setOnReadyListener(OnReadyListener listener) {
		mOnReadyListener = listener;
	}

	public OnReadyListener getOnReadyListener() {
		return mOnReadyListener;
	}

	public void setOnClickListener(OnClickListener listener) {
		mOnClickListener = listener;
	}

	public OnClickListener getOnClickListener() {
		return mOnClickListener;
	}

	public void setOpenInApplication(boolean orly) {
		mOpenInApplication = orly;
	}

	private void disableScrollingAndZoom() {
		setHorizontalScrollBarEnabled(false);
		setHorizontalScrollbarOverlay(false);
		setVerticalScrollBarEnabled(false);
		setVerticalScrollbarOverlay(false);
		getSettings().setSupportZoom(false);
	}

	private class AdWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// Handle phone intents
			if (
				   url.startsWith("tel:")
				|| url.startsWith("voicemail:")
				|| url.startsWith("sms:")
				|| url.startsWith("mailto:")
				|| url.startsWith("geo:")
				|| url.startsWith("google.streetview:")
			) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				try {
					getContext().startActivity(intent);
				} catch (ActivityNotFoundException e) {
					Log.w("Plus1", "Could not handle intent with URI: " + url);
					return false;
				}
			} else if (mOpenInApplication) {
				// FIXME: use another in-app browser for this ad
				Intent intent = new Intent(getContext(), MraidBrowser.class);
				intent.putExtra(MraidBrowser.URL_EXTRA, url);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try {
					getContext().startActivity(intent);
				} catch (ActivityNotFoundException e) {
					return false;
				}
			} else {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				try {
					getContext().startActivity(intent);
				} catch (ActivityNotFoundException e) {
					return false;
				}
			}

			if (getOnClickListener() != null)
				getOnClickListener().onClick((AdView)view);

			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			if (getOnReadyListener() != null)
				getOnReadyListener().onReady((AdView)view);
		}
	}

	public interface OnReadyListener {
		public void onReady(AdView view);
	}

	public interface OnClickListener {
		public void onClick(AdView view);
	}
}
