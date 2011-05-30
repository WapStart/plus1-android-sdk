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

import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Alexander Klestov <a.klestov@co.wapstart.ru>
 * @copyright Copyright (c) 2010, Wapstart
 */
public final class Plus1BannerRequest {
	public static enum Gender {Unknown, Male, Female;}
	
	private static final String ROTATOR_URI =
		"http://ro.plus1.wapstart.ru/?area=application";

	private static final String PREFERENCES_STORAGE = "WapstartPlus1";
	private static final String PREFERENCES_KEY		= "session";

	private int age					= 0;
	private int applicationId		= 0;
	private Gender gender			= Gender.Unknown;

	private String pageId			= null;
	private String clientSessionId	= null;

	private Context context			= null;
	
	public Plus1BannerRequest(Context context) {
		this.context = context;
	}

	public Plus1BannerRequest(Context context, int applicationId) {
		this.context = context;
		this.applicationId = applicationId;
	}

	public Plus1BannerRequest(Context context, int applicationId, int age) {
		this.context = context;
		this.applicationId = applicationId;
		this.age = age;
	}

	public Plus1BannerRequest(Context context, int applicationId, Gender gender) {
		this.context = context;
		this.applicationId = applicationId;
		this.gender = gender;
	}

	public Plus1BannerRequest(Context context, int applicationId, int age, Gender gender) {
		this.context = context;
		this.applicationId = applicationId;
		this.age = age;
		this.gender = gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(int applicationId) {
		this.applicationId = applicationId;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender sex) {
		this.gender = sex;
	}

	public String getRequestUri() {

		String url = ROTATOR_URI
				+ "&site=" + this.getApplicationId()
				+ "&pageId=" + this.getPageId()
				+ "&clientSession=" + this.getClientSessionId()
				+ "&userAgent=" + URLEncoder.encode(this.getUserAgent());

		if (!this.getGender().equals(Gender.Unknown))
			url += "&sex=" + this.getGender().ordinal();

		if (this.getAge() != 0)
			url += "&age=" + this.getAge();

		return url;
	}

	private String getPageId() {
		try {
			if (pageId == null)
				pageId = Plus1Helper.getUniqueHash();
		} catch (NoSuchAlgorithmException e) {
			// FIXME: log errors
		}

		return pageId;
	}

	private String getClientSessionId() {
		SharedPreferences preferences =
			context.getSharedPreferences(PREFERENCES_STORAGE, 0);
		
		if (clientSessionId == null)
			clientSessionId = preferences.getString(PREFERENCES_KEY, null);

		try {
			if (clientSessionId == null) {
				clientSessionId = Plus1Helper.getUniqueHash();

				SharedPreferences.Editor editor = preferences.edit();
				editor.putString(PREFERENCES_KEY, clientSessionId);
				editor.commit();
			}
		} catch (NoSuchAlgorithmException e) {
			// FIXME: log errors
		}

		return clientSessionId;
	}

	// TODO: find out needs of our network
	private String getUserAgent() {
		return
			"Android "
			+ android.os.Build.VERSION.RELEASE + " "
			+ android.os.Build.DEVICE + " "
			+ android.os.Build.MODEL;
	}

}
