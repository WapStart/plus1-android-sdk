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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;


/**
 * @author Alexander Klestov <a.klestov@co.wapstart.ru>
 * @copyright Copyright (c) 2011, Wapstart
 */
public class Plus1BannerView extends LinearLayout {

	private Plus1Banner banner;

	private TextView title;
	private TextView content;
	private Plus1ImageView image;
	
	private ViewFlipper flipper		= null;
	
	private Animation hideAnimation = null;
	private Animation showAnimation = null;
	
	private boolean haveCloseButton	= false;
	private boolean closed			= false;
	private boolean initialized		= false;

	private Plus1BannerViewStateListener viewStateListener = null;

	public Plus1BannerView(Context context) {
		super(context);
	}

	public Plus1BannerView(Context context, AttributeSet attr) {
		super(context, attr);
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
	
	public Plus1Banner getBanner() {
		return banner;
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

	public Plus1BannerView setViewStateListener(
		Plus1BannerViewStateListener viewStateListener
	) {
		this.viewStateListener = viewStateListener;

		return this;
	}

	public void setBanner(Plus1Banner banner) {
		try {
			if (!initialized)
				init();

			this.banner = banner;
			
			if ((banner != null) && (banner.getId() > 0)) {
				flipper.stopFlipping();
				
				SpannableStringBuilder text = 
					new SpannableStringBuilder(banner.getTitle());
				text.setSpan(new UnderlineSpan(), 0, banner.getTitle().length(), 0);				
				title.setText(text);
				content.setText(banner.getContent());
					
				if (!banner.isImageBanner()) {
					if (flipper.getCurrentView().equals(image))
						flipper.showNext();
					
					show();
				}
				
			} else if (getVisibility() == VISIBLE) {
				if (hideAnimation != null)
					startAnimation(hideAnimation);
				
				setVisibility(INVISIBLE);
	
				if (viewStateListener != null)
					viewStateListener.onHideBannerView();
			}
		} catch(OutOfMemoryError e) {
			Log.e(
				getClass().getName(),
				"Out of memory error while setting banner: " + e.getMessage()
			);
		}
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
		if (banner != null && banner.isImageBanner()) {
			if (!flipper.getCurrentView().equals(image))
				flipper.showNext();
		} else
			flipper.startFlipping();
		
		show();
	}
	
	private void init() {
		if (initialized)
			return;
		
		setBackgroundResource(R.drawable.wp_banner_background);

		ImageView shield = new ImageView(getContext());
		shield.setImageResource(R.drawable.wp_banner_shild);
		shield.setMaxWidth(9);
		addView(
			shield,
			new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT,
				0.03125f
			)
		);
		
		this.flipper = new ViewFlipper(getContext());
		flipper.setFlipInterval(3000);
		flipper.setInAnimation(
			AnimationUtils.loadAnimation(
				getContext(), 
				android.R.anim.fade_in
			)
		);
		flipper.setOutAnimation(
			AnimationUtils.loadAnimation(
				getContext(), 
				android.R.anim.fade_out
			)
		);
		
		LinearLayout ll = new LinearLayout(getContext());
		ll.setOrientation(VERTICAL);
		
		this.title = new TextView(getContext());
		title.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		title.setTextSize(14f);
		title.setTextColor(Color.rgb(115, 154, 208));
		title.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		ll.addView(title);
		
		this.content = new TextView(getContext());
		content.setTypeface(Typeface.SANS_SERIF);
		content.setTextSize(13f);
		content.setTextColor(Color.WHITE);
		content.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		ll.addView(content);
		flipper.addView(ll);
		
		this.image = new Plus1ImageView(getContext());
		flipper.addView(image);
		
		addView(
			flipper, 			
			new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT,
				0.90625f + (isHaveCloseButton() ? 0f : 0.0625f)
			)
		);

		if (isHaveCloseButton()) {
			Button closeButton = new Button(getContext());
			closeButton.setBackgroundResource(R.drawable.wp_banner_close);
			
			closeButton.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						closed = true;
						flipper.stopFlipping();
						
						if (getVisibility() == VISIBLE) {
							if (hideAnimation != null)
								startAnimation(hideAnimation);
							
							setVisibility(INVISIBLE);

							if (viewStateListener != null)
								viewStateListener.onCloseBannerView();
						}
					}
				}
			);
			
			addView(
				closeButton, 
				new LinearLayout.LayoutParams(
						18,
						17,
						0.0625f
					)
			);
		}
		
		setOnClickListener(
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
		);

		setVisibility(INVISIBLE);

		if (viewStateListener != null)
			viewStateListener.onHideBannerView();

		initialized = true;
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

			if (viewStateListener != null)
				viewStateListener.onShowBannerView();
		}
	}
}
