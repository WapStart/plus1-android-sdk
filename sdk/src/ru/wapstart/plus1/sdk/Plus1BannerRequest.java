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

import android.util.Log;

import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import android.location.Location;

/**
 * @author Alexander Klestov <a.klestov@co.wapstart.ru>
 * @copyright Copyright (c) 2010, Wapstart
 */
public final class Plus1BannerRequest {
	private static final String LOGTAG = "Plus1BannerRequest";
	private static final String SDK_VERSION = "2.0";
	private static final Integer REQUEST_VERSION = 2;

	public static enum Gender {Unknown, Male, Female;}
	public static enum RequestType {XML, JSON};
	public static enum BannerType {Undefined, Mixed, Text, Graphic};

	private String rotatorUrl		= "http://ro.plus1.wapstart.ru/";
	private int age					= 0;
	private int applicationId		= 0;
	private Gender gender			= Gender.Unknown;
	private String login			= null;
	private Set<BannerType> types	= null;

	private String pageId			= null;
	private RequestType requestType	= RequestType.XML;
	private Location location		= null;

	public static Plus1BannerRequest create() {
		return new Plus1BannerRequest();
	}

	public Plus1BannerRequest() {}

	public int getAge() {
		return age;
	}

	public Plus1BannerRequest setAge(int age) {
		this.age = age;

		return this;
	}

	public int getApplicationId() {
		return applicationId;
	}

	public Plus1BannerRequest setApplicationId(int applicationId) {
		this.applicationId = applicationId;

		return this;
	}

	public Gender getGender() {
		return gender;
	}

	public Plus1BannerRequest setGender(Gender sex) {
		this.gender = sex;

		return this;
	}

	public String getLogin() {
		return login;
	}

	public Plus1BannerRequest setLogin(String login) {
		this.login = login;

		return this;
	}

	public Plus1BannerRequest addType(BannerType type) {
		if (types == null)
			this.types = new HashSet<BannerType>();

		if (!type.equals(BannerType.Undefined))
			types.add(type);

		return this;
	}

	public Plus1BannerRequest clearTypes() {
		if (types != null)
			types.clear();

		return this;
	}

	public String getRotatorUrl() {
		return rotatorUrl;
	}

	public Plus1BannerRequest setRotatorUrl(String rotatorUrl) {
		this.rotatorUrl = rotatorUrl;

		return this;
	}

	public Location getLocation() {
		return location;
	}

	public Plus1BannerRequest setLocation(Location location) {
		this.location = location;

		return this;
	}

	public String getRequestUri() {

		String url =
			getRotatorUrl()
			+ "?area=application"
			+ "&version=" + REQUEST_VERSION
			+ "&sdkver=" + SDK_VERSION
			+ "&id=" + getApplicationId()
			+ "&pageId=" + getPageId();

		if (!getGender().equals(Gender.Unknown))
			url += "&sex=" + getGender().ordinal();

		if (getAge() != 0)
			url += "&age=" + getAge();

		if (getLogin() != null)
			url += "&login=" + getLogin();

		if ((types != null) && !types.isEmpty())
			for (BannerType bt : types)
				url += "&type[]=" + bt.ordinal();

		if (getLocation() != null)
			url +=
				"&location=" + getLocation().getLatitude()
				+ ";" + getLocation().getLongitude();

		return url;
	}

	private String getPageId() {
		try {
			if (pageId == null)
				pageId = Plus1Helper.getUniqueHash();
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOGTAG, "NoSuchAlgorithmException: " + e.toString());
		}

		return pageId;
	}
}
