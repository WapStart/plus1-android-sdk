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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Alexander Klestov <a.klestov@co.wapstart.ru>
 * @copyright Copyright (c) 2010, Wapstart
 */
final class Plus1Helper {

	private static final String PREFERENCES_STORAGE = "WapstartPlus1";
	private static final String PREFERENCES_KEY		= "session";
	
	private static String clientSessionId = null;
	
	private Plus1Helper() { /*_*/ }

	public static String getUniqueHash() throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		sha1.update(Calendar.getInstance().getTime().toString().getBytes());

		Random rnd = new Random();

		for (int i = 0; i < 10; i++)
			sha1.update((byte)rnd.nextInt(255));

		return new BigInteger(sha1.digest()).abs().toString(16);
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
		SharedPreferences preferences =
			context.getSharedPreferences(PREFERENCES_STORAGE, 0);
		
		if (clientSessionId == null)
			clientSessionId = preferences.getString(PREFERENCES_KEY, null);

		try {
			if (clientSessionId == null) {
				clientSessionId = getUniqueHash();

				SharedPreferences.Editor editor = preferences.edit();
				editor.putString(PREFERENCES_KEY, clientSessionId);
				editor.commit();
			}
		} catch (NoSuchAlgorithmException e) {
			// FIXME: log errors
		}

		return clientSessionId;		
	}
	
	
}
