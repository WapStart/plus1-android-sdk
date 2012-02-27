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
import android.content.Intent;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.ViewAnimator;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.util.Log;
import ru.wapstart.plus1.sdk.MraidView.ViewState;

/**
 * @author Alexander Klestov <a.klestov@co.wapstart.ru>
 * @copyright Copyright (c) 2011, Wapstart
 */
public class Plus1BannerView extends FrameLayout {

	private Plus1Banner banner;

	private TextView title;
	private TextView content;
	private Plus1ImageView image;
	
	private ViewAnimator animator	= null;
	
	private Animation hideAnimation = null;
	private Animation showAnimation = null;
	
	private boolean haveCloseButton	= false;
	private boolean closed			= false;
	private boolean initialized		= false;
	private boolean mAutorefreshEnabled = true;

	public Plus1BannerView(Context context) {
		this(context, null);
	}

	public Plus1BannerView(Context context, AttributeSet attr) {
		super(context, attr);

		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
	}

	public boolean isHaveCloseButton()
	{
		return haveCloseButton;
	}
	
	public Plus1BannerView enableCloseButton() {
		this.haveCloseButton = true;
		
		return this;
	}
	
	public Plus1BannerView setCloseButtonEnabled(boolean closeButtonEnabled) {
		this.haveCloseButton = closeButtonEnabled;
		
		return this;
	}
	
	public boolean isClosed() {
		return closed;
	}
	
	/**
	 * @deprecated
	 */
	public Plus1Banner getBanner() {
		return null;
	}
	
	public Plus1BannerView enableAnimationFromTop() {
		return enableAnimation(-1f);
	}
	
	public Plus1BannerView enableAnimationFromBottom() {
		return enableAnimation(1f);
	}
	
	public Plus1BannerView disableAnimation() {
		this.showAnimation = null;
		this.hideAnimation = null;
		
		return this;
	}

	/*public void setBanner(Plus1Banner banner) {
		if (!initialized)
			init();
		
		this.banner = banner;
		
		if ((banner != null) && (banner.getId() > 0)) {
			animator.stopFlipping();
			
			SpannableStringBuilder text = 
				new SpannableStringBuilder(banner.getTitle());
			text.setSpan(new UnderlineSpan(), 0, banner.getTitle().length(), 0);				
			title.setText(text);
			content.setText(banner.getContent());
			
			String imageUrl = null;
			
			if (!banner.getPictureUrl().equals(""))
				imageUrl = banner.getPictureUrl();
			else if (!banner.getPictureUrlPng().equals(""))
				imageUrl = banner.getPictureUrlPng();
			
			if (imageUrl != null)
				new ImageDowloader(this).setUrl(imageUrl).run();
				
			if (!banner.isImageBanner()) {
				if (animator.getCurrentView().equals(image))
					animator.showNext();
				
				show();
			}
			
		} else if (getVisibility() == VISIBLE) {
			if (hideAnimation != null)
				startAnimation(hideAnimation);
			
			setVisibility(INVISIBLE);
		}
	}*/

	public void loadAd(String html, String adType) {
		if (!initialized)
			init();

		AbstractAdView adView =
			"mraid".equals(adType)
				? makeMraidView()
				: makeAdView();

		adView.loadHtmlData(html);
		adView.setVisibility(VISIBLE);

		if (getVisibility() == INVISIBLE) {
			removeAllViews();

			addAdView(adView);
		} else {
			// FIXME XXX: delete from animator and destroy
			animator.addView(adView);
		}
	}

	public MraidView makeMraidView()
	{
		MraidView adView = new MraidView(getContext());
		adView.setOnReadyListener(new MraidView.OnReadyListener() {
			public void onReady(MraidView view) {
				show();
			}
		});
		adView.setOnExpandListener(new MraidView.OnExpandListener() {
			public void onExpand(MraidView view) {
				setAutorefreshEnabled(false);
			}
		});
		adView.setOnCloseListener(new MraidView.OnCloseListener() {
			public void onClose(MraidView view, ViewState newViewState) {
				setAutorefreshEnabled(true);
			}
		});
		// FIXME: add another listeners

		return adView;
	}

	public AdView makeAdView()
	{
		AdView adView = new AdView(getContext());
		adView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				show();
			}
		});
		// FIXME: add another listeners

		return adView;
	}

	public void setAutorefreshEnabled(boolean enabled) {
		mAutorefreshEnabled = enabled;
	}

	public boolean getAutorefreshEnabled() {
		return mAutorefreshEnabled;
	}

	public void setImage(Drawable drawable) {
		image.setImage(drawable);
		imageDownloaded();
	}
	
	public void setMovie(Movie movie) {
		image.setMovie(movie);
		imageDownloaded();
	}
	
	private void imageDownloaded()
	{
		/*if (banner.isImageBanner()) {
			if (!animator.getCurrentView().equals(image))
				animator.showNext();
		} else
			animator.startFlipping();
		
		show();*/
	}
	
	private void init() {
		if (initialized)
			return;

		setVisibility(INVISIBLE);

		animator = new ViewAnimator(getContext());
		animator.setInAnimation(
			AnimationUtils.loadAnimation(
				getContext(), 
				android.R.anim.fade_in
			)
		);
		animator.setOutAnimation(
			AnimationUtils.loadAnimation(
				getContext(), 
				android.R.anim.fade_out
			)
		);

		/*LinearLayout ll = new LinearLayout(getContext());
		ll.setOrientation(LinearLayout.VERTICAL);

		animator.addView(ll);

		addView(
			animator,
			new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT,
				0.90625f + (isHaveCloseButton() ? 0f : 0.0625f)
			)
		);*/

		// FIXME: intent in AdView
		/*setOnClickListener(
			new OnClickListener() {
				public void onClick(View view) {
					if (
						(banner == null)
						|| (banner.getLink() == null)
					)
						return;

					// TODO: click2call
					getContext().startActivity(
						new Intent(
							Intent.ACTION_VIEW,
							android.net.Uri.parse(banner.getLink())
						)
					);
				}
			}
		);*/
		
		initialized = true;
	}

	private void addAdView(WebView view)
	{
		// background
		setBackgroundResource(R.drawable.wp_banner_background);

		animator.addView(view);

		addView(
			animator,
			new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_VERTICAL | Gravity.RIGHT
			)
		);

		// shild
		ImageView shild = new ImageView(getContext());
		shild.setImageResource(R.drawable.wp_banner_shild);
		shild.setMaxWidth(9);
		addView(
			shild,
			new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.LEFT | Gravity.CENTER_VERTICAL
			)
		);

		// close button
		if (isHaveCloseButton()) {
			Button closeButton = new Button(getContext());
			closeButton.setBackgroundResource(R.drawable.wp_banner_close);

			closeButton.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						closed = true;
						hide();
					}
				}
			);

			addView(
				closeButton, 
				new FrameLayout.LayoutParams(
					18,
					17,
					Gravity.RIGHT
				)
			);
		}
	}

	private Plus1BannerView enableAnimation(float toYDelta) {
		this.showAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, toYDelta, Animation.RELATIVE_TO_SELF, 0f
			);
		showAnimation.setDuration(500);
		
		this.hideAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, toYDelta
		);
		hideAnimation.setDuration(500);
		
		return this;
	}
	
	private void show() {
		if (getVisibility() == INVISIBLE) {
			if (showAnimation != null)
				startAnimation(showAnimation);

			setVisibility(VISIBLE);
		} else if (animator.getChildCount() > 1)
			animator.showNext();
	}

	private void hide() {
		if (getVisibility() == VISIBLE) {
			if (hideAnimation != null)
				startAnimation(hideAnimation);

			setVisibility(INVISIBLE);
		}
	}
}
