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
	private BaseAdView mNewView;
	private BaseAdView mRemovedAdView;

	public Plus1AdAnimator(Context context) {
		super(context);

		mBaseView = new FrameLayout(context);
	}

	public ViewGroup getBaseView() {
		return mBaseView;
	}

	public void setAdView(BaseAdView child) {
		if (mNewView != null) {
			safeRemove(mNewView);
			Log.w(LOGTAG, "Not shown ad view was removed. Did you call setAdView() twice?");
		}

		mNewView = child;
	}

	public void showAd() {
		if (mNewView != null) {

			if (mCurrentView != null) {
				mCurrentView.startAnimation(makeFadeOutAnimation());

				mCurrentView.getAnimation().setAnimationListener(new Animation.AnimationListener() {
					public void onAnimationStart(Animation anmtn) {
						// nothing
					}

					public void onAnimationEnd(Animation anmtn) {
						setNewRemovedAdView(null); // safe destroy removed ad
						Log.d(LOGTAG, "WebView was removed");
					}

					public void onAnimationRepeat(Animation anmtn) {
						// nothing
					}
				});

				safeRemove(mCurrentView);
			}

			mNewView.startAnimation(makeFadeInAnimation());
			mNewView.setVisibility(VISIBLE);
			mBaseView.addView(
				mNewView,
				new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.FILL_PARENT,
					FrameLayout.LayoutParams.FILL_PARENT
				)
			);

			mCurrentView = mNewView;
			mNewView = null;
		}
	}

	public void removeAllViews() {
		if (mCurrentView != null) {
			mCurrentView.destroy();
			mCurrentView = null;
		}

		if (mNewView != null) {
			mNewView.destroy();
			mNewView = null;
		}

		setNewRemovedAdView(null); // safe destroy removed ad

		mBaseView.removeAllViews();
	}

	private Animation makeFadeInAnimation()
	{
		Animation animation =
			AnimationUtils.loadAnimation(
				getContext(), 
				android.R.anim.fade_in
			);

		animation.setDuration(1400);

		return animation;
	}

	private Animation makeFadeOutAnimation()
	{
		Animation animation =
			AnimationUtils.loadAnimation(
				getContext(), 
				android.R.anim.fade_out
			);

		animation.setDuration(600);

		return animation;
	}

	private void safeRemove(BaseAdView view) {
		mBaseView.removeView(view);
		setNewRemovedAdView(view);
	}

	// FIXME: more flexible way to unregister events, think about moving to onAnimationEnd
	private void setNewRemovedAdView(BaseAdView view) {
		if (mRemovedAdView != null)
			mRemovedAdView.destroy();

		mRemovedAdView = view;
	}
}
