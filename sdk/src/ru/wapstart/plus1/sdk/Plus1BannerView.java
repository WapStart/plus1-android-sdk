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
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.util.Log;
import android.webkit.WebView;

import ru.wapstart.plus1.sdk.MraidView.ViewState;

public class Plus1BannerView extends FrameLayout {
    private static final String LOGTAG = "Plus1BannerView";

	private OnAutorefreshStateListener mOnAutorefreshChangeListener;

	private Plus1AdAnimator mAdAnimator	= null;

	private Animation mHideAnimation	= null;
	private Animation mShowAnimation	= null;
	private String mWebViewUserAgent	= null;

	private boolean mHaveCloseButton	= false;
	private boolean mClosed				= false;
	private boolean mInitialized		= false;
	private boolean mAutorefreshEnabled = true;
	private boolean mExpanded			= false;

	private Plus1BannerViewStateListener mViewStateListener = null;

	public Plus1BannerView(Context context) {
		this(context, null);
	}

	public Plus1BannerView(Context context, AttributeSet attr) {
		super(context, attr);

		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);

		mWebViewUserAgent = new WebView(context).getSettings().getUserAgentString();
	}

	public void onPause() {
		if (mAdAnimator != null) {
			mAdAnimator.stopLoading();
			mAdAnimator.clearAnimation();

			if (mAdAnimator.getCurrentView() instanceof MraidView) {
				((MraidView)mAdAnimator.getCurrentView())
						.unregisterBroadcastReceiver();
			}
		}

		new WebView(getContext()).pauseTimers();
	}

	public void onResume() {
		if (
			mAdAnimator != null
			&& mAdAnimator.getCurrentView() instanceof MraidView
		) {
			((MraidView)mAdAnimator.getCurrentView())
					.registerBroadcastReceiver();
		}

		new WebView(getContext()).resumeTimers();
	}

	public boolean canGoBack() {
		return
			mAdAnimator.getCurrentView() != null
			&& mAdAnimator.getCurrentView().canGoBack();
	}

	public void goBack() {
		if (mAdAnimator.getCurrentView() != null)
			mAdAnimator.getCurrentView().goBack();
	}

	public String getWebViewUserAgent() {
		return mWebViewUserAgent;
	}

	public boolean isHaveCloseButton() {
		return mHaveCloseButton;
	}

	public Plus1BannerView enableCloseButton() {
		mHaveCloseButton = true;

		return this;
	}

	public Plus1BannerView setCloseButtonEnabled(boolean closeButtonEnabled) {
		mHaveCloseButton = closeButtonEnabled;

		return this;
	}

	public boolean isClosed() {
		return mClosed;
	}

	public Plus1BannerView enableAnimationFromTop() {
		return enableAnimation(-1f);
	}

	public Plus1BannerView enableAnimationFromBottom() {
		return enableAnimation(1f);
	}

	public Plus1BannerView disableAnimation() {
		mShowAnimation = null;
		mHideAnimation = null;

		return this;
	}

	public void loadAd(String html, String adType) {
		init();

		BaseAdView adView =
			"mraid".equals(adType)
				? makeMraidView()
				: makeAdView();

		mAdAnimator.loadAdView(adView, html);
	}

	public MraidView makeMraidView() {
		MraidView adView = new MraidView(getContext());
		Log.d(LOGTAG, "MraidView instance created");
		adView.setOnReadyListener(new MraidView.OnReadyListener() {
			public void onReady(MraidView view) {
				show();
			}
		});
		adView.setOnExpandListener(new MraidView.OnExpandListener() {
			public void onExpand(MraidView view) {
				setExpanded(true);

				//setVisibility(INVISIBLE); // hide without animation
			}
		});
		adView.setOnCloseListener(new MraidView.OnCloseListener() {
			public void onClose(MraidView view, ViewState newViewState) {
				setExpanded(false);
			}
		});
		adView.setOnFailureListener(new MraidView.OnFailureListener() {
			public void onFailure(MraidView view) {
				Log.e(LOGTAG, "Mraid ad failed to load");
			}
		});

		return adView;
	}

	public AdView makeAdView() {
		AdView adView = new AdView(getContext());
		Log.d(LOGTAG, "AdView instance created");
		adView.setOnReadyListener(new AdView.OnReadyListener() {
			public void onReady() {
				show();
			}
		});

		return adView;
	}

	public void setOnAutorefreshChangeListener(OnAutorefreshStateListener listener) {
		mOnAutorefreshChangeListener = listener;
	}

	public Plus1BannerView setAutorefreshEnabled(boolean enabled) {
		if (mAutorefreshEnabled != enabled) { // NOTE: really changed
			mAutorefreshEnabled = enabled;

			if (mOnAutorefreshChangeListener != null)
				mOnAutorefreshChangeListener.onAutorefreshStateChanged(this);
		}

		return this;
	}

	public Plus1BannerView setViewStateListener(
		Plus1BannerViewStateListener viewStateListener
	) {
		mViewStateListener = viewStateListener;

		return this;
	}

	public boolean getAutorefreshEnabled() {
		return mAutorefreshEnabled;
	}

	private void setExpanded(boolean orly) {
		if (mExpanded != orly) { // NOTE: really changed
			mExpanded = orly;

			if (mExpanded)
				mAdAnimator.stopLoading();

			if (mOnAutorefreshChangeListener != null)
				mOnAutorefreshChangeListener.onAutorefreshStateChanged(this);
		}
	}

	public boolean isExpanded() {
		return mExpanded;
	}

	private void init() {
		if (mInitialized)
			return;

		setVisibility(INVISIBLE);

		// background
		setBackgroundResource(R.drawable.wp_banner_background);

		// shild
		ImageView shild = new ImageView(getContext());
		shild.setImageResource(R.drawable.wp_banner_shild);
		shild.setMaxWidth(9);
		shild.setLayoutParams(
			new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.LEFT | Gravity.CENTER_VERTICAL
			)
		);

		mAdAnimator = new Plus1AdAnimator(getContext());

		addView(
			mAdAnimator.getBaseView(),
			new FrameLayout.LayoutParams(
				getWidth() - 8,
				FrameLayout.LayoutParams.FILL_PARENT,
				Gravity.RIGHT
			)
		);

		addView(shild);

		// close button
		if (isHaveCloseButton()) {
			Button closeButton = new Button(getContext());
			closeButton.setBackgroundResource(R.drawable.wp_banner_close);

			closeButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					mClosed = true;
					setAutorefreshEnabled(false);
					hide();
				}
			});

			addView(
				closeButton,
				new FrameLayout.LayoutParams(
					18,
					17,
					Gravity.RIGHT
				)
			);
		}

		mInitialized = true;
	}

	private Plus1BannerView enableAnimation(float toYDelta) {
		mShowAnimation = new TranslateAnimation(
			Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
			Animation.RELATIVE_TO_SELF, toYDelta, Animation.RELATIVE_TO_SELF, 0f
		);
		mShowAnimation.setDuration(500);

		mHideAnimation = new TranslateAnimation(
			Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
			Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, toYDelta
		);
		mHideAnimation.setDuration(500);

		return this;
	}

	private void show() {
		if (getVisibility() == INVISIBLE) {
			if (mShowAnimation != null)
				startAnimation(mShowAnimation);

			setVisibility(VISIBLE);

			if (mViewStateListener != null)
				mViewStateListener.onShowBannerView();
		}

		mAdAnimator.showAd();
	}

	private void hide() {
		if (getVisibility() == VISIBLE) {
			if (mHideAnimation != null)
				startAnimation(mHideAnimation);

			setVisibility(INVISIBLE);

			if (mViewStateListener != null)
				mViewStateListener.onCloseBannerView();
		}
	}

	public interface OnAutorefreshStateListener {
		public void onAutorefreshStateChanged(Plus1BannerView view);
	}
}
