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
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ViewAnimator;
import android.webkit.WebView;

final public class Plus1AdAnimator extends FrameLayout {
    private static final String LOGTAG = "Plus1AdAnimator";
	// NOTE: order and numbers are important
	private static final int INDEX_FIRST = 0;
	private static final int INDEX_SECOND = 1;

	private int mCurrentIndex;

	private ViewAnimator mAnimator;

	public Plus1AdAnimator(Context context) {
		super(context);

		mAnimator = new ViewAnimator(context);
		mAnimator.setInAnimation(
			AnimationUtils.loadAnimation(
				context, 
				android.R.anim.fade_in
			)
		);
		mAnimator.setOutAnimation(
			AnimationUtils.loadAnimation(
				context, 
				android.R.anim.fade_out
			)
		);
	}

	public ViewAnimator getViewAnimator() {
		return mAnimator;
	}

	public void addView(WebView child) {
		int index = getNextIndex();

		if (mAnimator.getChildAt(index) != null) {
			cleanViewAt(index);
			mAnimator.addView(child, index);
		} else
			mAnimator.addView(child);
	}

	public void destroy() {
		cleanViewAt(INDEX_FIRST);
		cleanViewAt(INDEX_SECOND);
	}

	private int getNextIndex() {
		mCurrentIndex =
			mCurrentIndex == INDEX_FIRST
				? INDEX_SECOND
				: INDEX_FIRST;

		Log.d(LOGTAG, "Next inner index for WebView: " + mCurrentIndex);

		return mCurrentIndex;
	}

	private void cleanViewAt(int index) {
		WebView child = (WebView)mAnimator.getChildAt(index);

		if (child != null) {
			Log.d(LOGTAG, "Destroy WebView at index=" + index);
			child.destroy();
			mAnimator.removeView(child);
		}
	}
}
