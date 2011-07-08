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

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;

//import android.graphics.Movie;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * @author Alexander Klestov <a.klestov@co.wapstart.ru>
 * @copyright Copyright (c) 2011, Wapstart
 */
final class ImageDowloader extends BaseDownloader {

	private Plus1BannerView bannerView = null;
	
	public ImageDowloader(Plus1BannerView bannerView) {
		this.bannerView = bannerView;
	}
	
	@Override
	public void run() {
		super.run();
		
		if (stream == null) 
			return;
		
		if (url.substring(url.length() - 4).toLowerCase().equals(".gif")) {
			ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
			
			byte[] buffer = new byte[1024];
            int len;
            try {
                while ((len = stream.read(buffer)) >= 0) {
                    os.write(buffer, 0, len);
                }
            } catch (java.io.IOException e) { 
            	Log.d(getClass().getName(), e.getMessage());
            }
			
			bannerView.setMovie(
				Movie.decodeByteArray(
					os.toByteArray(), 
					0, 
					os.toByteArray().length
				)
			);
		} else
			bannerView.setImage(Drawable.createFromStream(stream, "src"));
	}
	
	protected void modifyConnection(HttpURLConnection connection) { /*_*/ }

}
