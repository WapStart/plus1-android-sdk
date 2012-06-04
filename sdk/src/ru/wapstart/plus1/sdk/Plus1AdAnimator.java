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

import android.content.Context;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout;

final public class Plus1AdAnimator extends FrameLayout {
	private static final String LOGTAG = "Plus1AdAnimator";

	private ViewGroup mBaseView;
	private BaseAdView mCurrentView;
	private BaseAdView mLoadView;
	private BaseAdView mFadeOutAdView;

	private boolean mHtmlLoading	= false;

	public Plus1AdAnimator(Context context) {
		super(context);

		mBaseView = new FrameLayout(context);
	}

	public BaseAdView getCurrentView() {
		return mCurrentView;
	}

	public ViewGroup getBaseView() {
		return mBaseView;
	}

	public void loadAdView(BaseAdView child, String html) {
		stopLoading();

		mLoadView = child;
		mHtmlLoading = true;
		mLoadView.loadHtmlData(html);
	}

	public void stopLoading() {
		if (mHtmlLoading) {
			mLoadView.stopLoading();
			mHtmlLoading = false;
			mLoadView.destroy();
			mLoadView = null;
			Log.w(LOGTAG, "Not shown ad view was removed");
		}
	}

	// NOTE: need to clear animation on pause
	public void clearAnimation() {
		if (mFadeOutAdView != null) {
			try {
				mFadeOutAdView.clearAnimation();
				mFadeOutAdView.destroy();
				mFadeOutAdView = null;
			} catch (NullPointerException e) {
				// already destroyed in animation end context
			}
		}
	}

	// NOTE: fires after success html loading
	public void showAd() {
		Log.d(LOGTAG, "showAd method fired");

		if (mLoadView == null)
			return;

		if (mCurrentView != null) {
			if (mCurrentView instanceof MraidView)
				((MraidView)mCurrentView).unregisterBroadcastReceiver();

			mCurrentView.startAnimation(makeFadeOutAnimation());

			mBaseView.removeView(mCurrentView);
			clearAnimation();

			mCurrentView.getAnimation().setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation anmtn) {
					// nothing
				}

				public void onAnimationEnd(Animation anmtn) {
					if (mFadeOutAdView != null) {
						mFadeOutAdView.destroy();
						mFadeOutAdView = null;
					}

					Log.d(LOGTAG, "Ad view was destroyed in animation end context");
				}

				public void onAnimationRepeat(Animation anmtn) {
					// nothing
				}
			});

			// NOTE: adView will be destroyed after fade out animation
			mFadeOutAdView = mCurrentView;
		}

		mCurrentView = mLoadView;
		mLoadView = null;
		mHtmlLoading = false;

		mCurrentView.setVisibility(VISIBLE);
		mBaseView.addView(
			mCurrentView,
			new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.FILL_PARENT
			)
		);
		mCurrentView.startAnimation(makeFadeInAnimation());

		if (mCurrentView instanceof MraidView)
			((MraidView)mCurrentView).registerBroadcastReceiver();
	}

	private Animation makeFadeInAnimation() {
		Animation animation =
			AnimationUtils.loadAnimation(
				getContext(), 
				android.R.anim.fade_in
			);

		animation.setDuration(1400);

		return animation;
	}

	private Animation makeFadeOutAnimation() {
		Animation animation =
			AnimationUtils.loadAnimation(
				getContext(), 
				android.R.anim.fade_out
			);

		animation.setDuration(600);

		return animation;
	}
}
