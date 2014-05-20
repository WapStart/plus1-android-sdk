/**
 * Copyright (c) 2014, Alexander Zaytsev <a.zaytsev@co.wapstart.ru>
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

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import android.util.Log;

final class InitRequestLoader extends BaseRequestLoader<String> {
	private static final String LOGTAG = "InitRequestLoader";

	private ArrayList<InitRequestLoadListener> mInitRequestLoadListenerList =
			new ArrayList<InitRequestLoadListener>();

	public void addInitRequestLoadListener(InitRequestLoadListener listener) {
		mInitRequestLoadListenerList.add(listener);
	}

	protected String getRequestUrl(Plus1Request request) {
		return request.getUrl(Plus1Request.RequestType.init);
	}

	protected UrlEncodedFormEntity getUrlEncodedFormEntity(Plus1Request request)
		throws UnsupportedEncodingException
	{
		return request.getUrlEncodedFormEntity();
	}

	protected String makeResult(String content, HttpURLConnection connection) {
		Log.d(LOGTAG, "Unique identifier: " + content);

		return content;
	}

	@Override
	protected void onPostExecute(String uid) {
		if (null != uid)
			notifyOnUniqueIdLoaded(uid);
		else
			notifyOnUniqueIdLoadFailed();
	}

	private void notifyOnUniqueIdLoaded(String uid) {
		for (InitRequestLoadListener listener : mInitRequestLoadListenerList)
			listener.onUniqueIdLoaded(uid);
	}

	private void notifyOnUniqueIdLoadFailed() {
		for (InitRequestLoadListener listener : mInitRequestLoadListenerList)
			listener.onUniqueIdLoadFailed();
	}

	public interface InitRequestLoadListener {
		public void onUniqueIdLoaded(String uid);
		public void onUniqueIdLoadFailed();
	}
}
