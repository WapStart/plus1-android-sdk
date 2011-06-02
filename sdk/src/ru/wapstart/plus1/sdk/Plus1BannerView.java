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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * @author Alexander Klestov <a.klestov@co.wapstart.ru>
 * @copyright Copyright (c) 2011, Wapstart
 */
public class Plus1BannerView extends ImageView {

	private Plus1Banner banner;
	private Bitmap shild;

	public Plus1BannerView(Context context) {
		super(context);

		init();
	}

	public Plus1BannerView(Context context, AttributeSet attr) {
		super(context, attr);

		init();
	}

	public Plus1BannerView(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);

		init();
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		if (banner == null) {
			Log.d("Plus1", "no have banners");

			return;
		}

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
		paint.setTextSize(getDip(10f));
		paint.setFakeBoldText(true);
		canvas.drawText(banner.getTitle(), 10, getDip(10f), paint);

		paint.setTextSize(getDip(8f));
		paint.setFakeBoldText(false);
		canvas.drawText(banner.getContent(), 10, getDip(20f), paint);

		canvas.drawBitmap(shild, new Matrix(), paint);
	}

	public Plus1Banner getBanner() {
		return banner;
	}

	public void setBanner(Plus1Banner banner) {
		this.banner = banner;
	}

	private void init() {
		setBackgroundResource(R.drawable.wp_banner_background);

		this.shild = 
			BitmapFactory.decodeResource(
				getContext().getResources(), 
				R.drawable.wp_banner_shild
			);

		setOnClickListener(
			new OnClickListener() {
				public void onClick(View view) {
					if (
						(banner == null)
						|| (banner.getLink() == null)
					)
						return;

					getContext().startActivity(
						new Intent(
							Intent.ACTION_VIEW,
							android.net.Uri.parse(banner.getLink())
						)
					);
				}
			}
		);
	}
	
	private float getDip(float pixels)
	{
		return pixels * (getContext().getResources().getDisplayMetrics().density + 0.5f);
	}
}
