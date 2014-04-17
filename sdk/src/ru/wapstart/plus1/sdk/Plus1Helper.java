/**
 * Copyright (c) 2010, Alexander Klestov <a.klestov@co.wapstart.ru>
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.DisplayMetrics;
import android.provider.Settings.Secure;
import android.view.Display;

final class Plus1Helper {

	private static final String PREFERENCES_STORAGE = "WapstartPlus1";
	private static final String PREFERENCES_KEY		= "session";
	private static final String HEX_DIGITS			= "0123456789abcdef";
	private static final String LOGTAG				= "Plus1Helper";
	
	private static String mClientSessionId = null;
	
	private Plus1Helper() { /*_*/ }

	public static String getUniqueHash()
	{
		String uniqueStr = Calendar.getInstance().getTime().toString();
		Random rnd = new Random();

		for (int i = 0; i < 10; i++)
			uniqueStr += rnd.nextInt(255);

		return getHash(uniqueStr);
	}

	public static String getHash(String text)
	{
		try {
			return SHA1(text);
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOGTAG, "NoSuchAlgorithmException: " + e.toString(), e);

			return null; // FIXME: add other hash logic
		}
	}

	// TODO: find out needs of our network
	public static String getUserAgent() {
		return
			"Android "
			+ android.os.Build.VERSION.RELEASE + " "
			+ android.os.Build.DEVICE + " "
			+ android.os.Build.MODEL;
	}

	public static String getClientSessionId(Context context) {
		if (mClientSessionId == null) {
			SharedPreferences preferences =
				context.getSharedPreferences(PREFERENCES_STORAGE, 0);

			mClientSessionId = preferences.getString(PREFERENCES_KEY, null);

			if (mClientSessionId == null) {
				String androidId =
					Secure.getString(
						context.getContentResolver(),
						Secure.ANDROID_ID
					);

				mClientSessionId =
					androidId != null
						? getHash(androidId)
						: getUniqueHash();

				setClientSessionId(context, mClientSessionId);
			}
		}

		return mClientSessionId;
	}

	public static void setClientSessionId(Context context, String clientSessionId) {
		SharedPreferences preferences =
				context.getSharedPreferences(PREFERENCES_STORAGE, 0);

		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCES_KEY, clientSessionId);
		editor.commit();

		mClientSessionId = clientSessionId;
	}

	public static String getDisplayMetrics(Display display)
	{
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);

		return
			String.format(
				"%ux%u",
				metrics.widthPixels,
				metrics.heightPixels
			);
	}

	public static String getContainerMetrics(Plus1BannerView view)
	{
		float density =
			((Activity)view.getContext())
				.getResources()
				.getDisplayMetrics()
				.density;

		return
			String.format(
				"%ux%u",
				view.getLayoutParams().width / density + 0.5f,
				view.getLayoutParams().height / density + 0.5f
			);
	}

	private static String SHA1(String text) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-1");

		byte[] sha1hash = new byte[40];
		md.update(text.getBytes());
		sha1hash = md.digest();

		return convertToHex(sha1hash);
	}

	private static String convertToHex(byte[] raw)
	{
		final StringBuilder hex = new StringBuilder(raw.length * 2);

		for (final byte b : raw) {
			hex
				.append(HEX_DIGITS.charAt((b & 0xF0) >> 4))
				.append(HEX_DIGITS.charAt((b & 0x0F)));
		}
		
		return hex.toString();
	}
}
