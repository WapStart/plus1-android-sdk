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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * @author Alexander Klestov <a.klestov@co.wapstart.ru>
 * @copyright Copyright (c) 2010, Wapstart
 */
final class Plus1Banner {
	private static enum ResponseType { UNKNOWN, LINK, CALL};
	
	private Integer id = 0;
	private String title = null;
	private String content = null;
	private String singleLineContent = null;
	private String link = null;
	private String pictureUrl = null;
	private String pictureUrlPng = null;
	private String cookieSetterUrl = null;
	private ResponseType responseType = ResponseType.UNKNOWN;
	private Bitmap image = null;

	public static Plus1Banner create() {
		return new Plus1Banner();
	}

	public int getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSingleLineContent() {
		return singleLineContent;
	}
	
	public void setSingleLineContent(String singleLineContent) {
		this.singleLineContent = singleLineContent;
	}
	
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	public String getPictureUrlPng() {
		return pictureUrlPng;
	}

	public void setPictureUrlPng(String pictureUrlPng) {
		this.pictureUrlPng = pictureUrlPng;
	}
	
	public String getCookieSetterUrl() {
		return cookieSetterUrl;
	}

	public void setCookieSetterUrl(String cookieSetterUrl) {
		this.cookieSetterUrl = cookieSetterUrl;
	}
	
	public ResponseType getResponseType()
	{
		return responseType;
	}

	public void setResponseType(ResponseType responseType)
	{
		this.responseType = responseType;
	}
	
	public void setResponseType(Integer responseType)
	{
		switch (responseType) {
			case 1:
				this.responseType = ResponseType.LINK;
				break;
			case 2: 
				this.responseType = ResponseType.CALL;
				break;
			default:
				this.responseType = ResponseType.UNKNOWN;
		}
	}
	
	public Bitmap getImage() {
		if ((image == null) && (pictureUrl != null)) {
			// FIXME: getting image in thread
		}

		return image;
	}
	
	public void setProperty(String propertyName, String propertyValue) {
		try {
			Method method = 
				getClass().getMethod(
					"set" 
					+ propertyName.substring(0, 1).toUpperCase()
					+ propertyName.substring(1),
					isIntegerProperty(propertyName)
						? Integer.class
						: String.class
				);
			
			method.invoke(
				this, 
				isIntegerProperty(propertyName)
					? Integer.parseInt(propertyValue)
					: propertyValue
			);
		} catch (NoSuchMethodException e) {
			Log.e(
				getClass().getName(), 
				"No found method for " + propertyName
			);
		} catch (IllegalAccessException e) {
			Log.e(
				getClass().getName(),
				"Illegal access exception"
			);
		} catch (InvocationTargetException e) {
			Log.e(
				getClass().getName(),
				"Ivocation target exception"
			);
		}
	}
	
	private Boolean isIntegerProperty(String propertyName) {
		return propertyName.equals("id") || propertyName.equals("responseType");
	}
	
}
