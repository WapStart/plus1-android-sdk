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

public class Constants {
	public static final String SDK_VERSION							= "2.3.4";

	protected static final String PREFERENCES_STORAGE				= "Plus1WapStart.xml";
	protected static final String PREFERENCES_KEY_LOCK_TASK_PREFIX	= "lock_task_";
	protected static final String PREFERENCES_KEY_UID				= "uid";
	protected static final String PREFERENCES_KEY_FACEBOOK_USER_ID	= "facebook_user_id";
	protected static final String PREFERENCES_KEY_TWITTER_USER_ID	= "twitter_user_id";
	protected static final String PREFERENCES_KEY_ADVERTISING_ID	= "advertising_id";
	protected static final String PREFERENCES_KEY_LIMIT_AD_TRACKING_ENABLED	= "limit_ad_tracking_enabled";
	protected static final String PREFERENCES_KEY_REINIT_TASK_REFRESH		= "reinit_task_refresh";
	protected static final String PREFERENCES_KEY_ADVERTISING_ID_REFRESH	= "advertising_id_refresh";
	protected static final String PREFERENCES_KEY_FACEBOOK_INFO_REFRESH		= "facebook_info_refresh";
	protected static final String PREFERENCES_KEY_TWITTER_INFO_REFRESH		= "twitter_info_refresh";

	protected static final int DEFAULTS_BANNER_REFRESH_INTERVAL			= 10;
	protected static final int DEFAULTS_REFRESH_RETRY_NUM				= 3;
	protected static final int DEFAULTS_LOCATION_REFRESH_INTERVAL		= 300;
	protected static final int DEFAULTS_REINIT_INTERVAL					= 3600;
	protected static final int DEFAULTS_FACEBOOK_INFO_REFRESH_INTERVAL	= 60;
	protected static final int DEFAULTS_TWITTER_INFO_REFRESH_INTERVAL	= 60;

	protected static final String PLACEHOLDER_REINIT_DELAY				= "%reinitDelay%";
	protected static final String PLACEHOLDER_REFRESH_RETRY_NUM			= "%refreshRetryNum%";
	protected static final String PLACEHOLDER_BANNER_REFRESH_INTERVAL	= "%bannerRefreshInterval%";
	protected static final String PLACEHOLDER_FACEBOOK_INFO_REFRESH_INTERVAL	= "%facebookInfoRefreshInterval%";
	protected static final String PLACEHOLDER_TWITTER_INFO_REFRESH_INTERVAL		= "%twitterInfoRefreshInterval%";
	protected static final String PLACEHOLDER_UID						= "%uid%";
	protected static final String PLACEHOLDER_ENCODED_CALLBACK			= "%encodedCallback%";
}
