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
import android.util.Log;

import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Alexander Klestov <a.klestov@co.wapstart.ru>
 * @copyright Copyright (c) 2011, Wapstart
 */
final class JSONBannerDownloader extends BaseBannerDownloader {

	public JSONBannerDownloader(Context context) {
		super(context);
	}

	@Override
	protected Plus1Banner parse(String answer) {
		Plus1Banner banner = new Plus1Banner();
		
		try {
			JSONObject jsonObject = new JSONObject(answer);
			Iterator<String> iterator = jsonObject.keys();
			String propertyName = null;
			
			while (iterator.hasNext()) {
				propertyName = iterator.next();
				
				banner.setProperty(
					propertyName, 
					jsonObject.get(propertyName).toString()
				);
			}
		} catch (JSONException e) {
			Log.e(getClass().getName(), "Can't handle json answer");
		}
		
		return banner;
	}
}
