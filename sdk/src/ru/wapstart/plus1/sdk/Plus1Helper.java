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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.WindowManager;

final class Plus1Helper {
	private static final String HEX_DIGITS	= "0123456789abcdef";
	private static final String LOGTAG		= "Plus1Helper";

	private Plus1Helper() { /*_*/ }

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

	public static String getStorageValue(Context context, String key) {
		return
			context.getSharedPreferences(Constants.PREFERENCES_STORAGE, 0)
				.getString(key, null);
	}

	public static boolean setStorageValue(Context context, String key, String value) {
		return
			context.getSharedPreferences(Constants.PREFERENCES_STORAGE, 0).edit()
				.putString(key, value)
				.commit();
	}

	public static boolean removeStorageValue(Context context, String key) {
		return
			context.getSharedPreferences(Constants.PREFERENCES_STORAGE, 0).edit()
				.remove(key)
				.commit();
	}

	public static Boolean getStorageBooleanValue(Context context, String key) {
		SharedPreferences preferences =
			context.getSharedPreferences(Constants.PREFERENCES_STORAGE, 0);

		return
			preferences.contains(key)
				? Boolean.valueOf(preferences.getBoolean(key, false))
				: null;
	}

	public static boolean setStorageBooleanValue(Context context, String key, Boolean value) {
		return
			context.getSharedPreferences(Constants.PREFERENCES_STORAGE, 0).edit()
				.putBoolean(key, value.booleanValue())
				.commit();
	}

	public static String getAndroidId(Context context) {
		String androidId =
			android.provider.Settings.Secure.getString(
				context.getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID
			);

		if (androidId != null)
			return getHash(androidId);

		return null;
	}

	public static String getBuildSerial() {
		try {
			String serial = (String)Build.class.getField("SERIAL").get(null);
			if (
				!serial.equals(Build.class.getField("UNKNOWN").get(null))
				&& !serial.equals("00000000000000") // NOTE: bugs on some devices
			)
				return getHash(serial);
		} catch (Exception e) {
			// NOTE: may be API < 9
		}

		return null;
	}

	public static String getDisplayMetrics(Context context)
	{
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

		DisplayMetrics metrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(metrics);

		return
			String.format(
				"%dx%d",
				metrics.widthPixels,
				metrics.heightPixels
			);
	}

	public static String getDisplayOrientation(Context context)
	{
		String text;

		switch (context.getResources().getConfiguration().orientation) {
			case Configuration.ORIENTATION_PORTRAIT:
				text = "portrait";
				break;
			case Configuration.ORIENTATION_LANDSCAPE:
				text = "landscape";
				break;
			case Configuration.ORIENTATION_SQUARE:
				text = "square";
				break;
			default:
				text = null;
		};

		return text;
	}

	public static String getContainerMetrics(Plus1BannerView view)
	{
		float density =
			view.getContext()
				.getResources()
				.getDisplayMetrics()
				.density;

		return
			String.format(
				"%dx%d",
				(int)(view.getLayoutParams().width / density + 0.5f),
				(int)(view.getLayoutParams().height / density + 0.5f)
			);
	}

	public static boolean isIntentUrl(String url)
	{
		return isCommonIntentUrl(url) || isPlayMarketIntentUrl(url);
	}

	public static boolean isCommonIntentUrl(String url)
	{
		return
			url.startsWith("tel:")
			|| url.startsWith("voicemail:")
			|| url.startsWith("sms:")
			|| url.startsWith("mailto:")
			|| url.startsWith("geo:")
			|| url.startsWith("google.streetview:");
	}

	public static boolean isPlayMarketIntentUrl(String url)
	{
		return url.startsWith("market:");
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
