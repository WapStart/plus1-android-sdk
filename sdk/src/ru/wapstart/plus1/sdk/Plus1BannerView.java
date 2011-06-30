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
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;


/**
 * @author Alexander Klestov <a.klestov@co.wapstart.ru>
 * @copyright Copyright (c) 2011, Wapstart
 */
public class Plus1BannerView extends LinearLayout {

	private Plus1Banner banner;

	private TextView title;
	private TextView content;
	private ImageView image;
	
	private Animation hideAnimation = null;
	private Animation showAnimation = null;

	public Plus1BannerView(Context context) {
		super(context);

		init();
	}

	public Plus1BannerView(Context context, AttributeSet attr) {
		super(context, attr);

		init();
	}

	public Plus1Banner getBanner() {
		return banner;
	}

	public void setBanner(Plus1Banner banner) {
		this.banner = banner;
		
		if ((banner != null) && (banner.getId() > 0)) {
			if (getVisibility() == INVISIBLE) {
				startAnimation(showAnimation);
			
				setVisibility(VISIBLE);
			}
				
			title.setText(banner.getTitle());
			content.setText(banner.getTitle());
			
			String imageUrl = null;
			
			if (!banner.getPictureUrl().equals(""))
				imageUrl = banner.getPictureUrl();
			else if (!banner.getPictureUrlPng().equals(""))
				imageUrl = banner.getPictureUrlPng();
			
			if (imageUrl != null)
				new ImageDowloader(this.image).execute(imageUrl);
			
		} else if (getVisibility() == VISIBLE) {
			startAnimation(hideAnimation);
			setVisibility(INVISIBLE);
		}
	}

	private void init() {
		setBackgroundResource(R.drawable.wp_banner_background);

		ImageView shild = new ImageView(getContext());
		shild.setImageResource(R.drawable.wp_banner_shild);
		shild.setMaxWidth(9);
		addView(shild);
		
		LinearLayout ll = new LinearLayout(getContext());
		ll.setOrientation(VERTICAL);
		
		this.title = new TextView(getContext());
		title.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		title.setTextSize(14f);
		title.setTextColor(Color.WHITE);
		ll.addView(title);
		
		this.content = new TextView(getContext());
		content.setTypeface(Typeface.SANS_SERIF);
		content.setTextSize(13f);
		content.setTextColor(Color.WHITE);
		ll.addView(content);
		
		this.image = new ImageView(getContext());
		ll.addView(image);
		
		addView(ll);
		
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
		
		this.showAnimation = new TranslateAnimation(
			Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
			Animation.RELATIVE_TO_SELF, -1f, Animation.RELATIVE_TO_SELF, 0f
		);
		showAnimation.setDuration(500);
		
		this.hideAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1f
		);
		hideAnimation.setDuration(500);
		
		setVisibility(INVISIBLE);
	}
}
