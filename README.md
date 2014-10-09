Plus1 WapStart Android SDK
==========================
It is an open source library to use for integration with the [Plus1 WapStart](https://plus1.wapstart.ru) ad network.

Plus1 WapStart Android SDK is under the terms of the BSD license (as is).

**Contents:**
* [Setup Guide](#Setup-Guide)
  * [Manifest configuration](#manifest-configuration)
  * [Test application setup](#test-application-setup)
* [SDK using](#sdk-using)
  * [Adding the banner to the application](#adding-the-banner-to-the-application)
  * [SDK events processing by application](#sdk-events-processing-by-application)
  * [WebViews collision resolving in application](#webviews-collision-resolving-in-application)
* [Contacts](#contacts)


# Setup Guide

1. Download the latest SDK: https://github.com/WapStart/plus1-android-sdk/tags
2. Add the SDK to your project to start working;
3. Follow these steps to set up the manifest.

## Manifest configuration
The first step is adding custom url-scheme and the host. It is a necessary action in order to be able to return to the App from Browser after syncing cookie of user. If you are using *setDisabledOpenLinkAction(true)* of [Plus1Request](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1Request.md), you may skip this section (not recommended).

Example:

```xml
<intent-filter>
	<data android:scheme="wsp1bart" android:host="ru.wapstart.plus1.bart" />
	<action android:name="android.intent.action.VIEW"/>
	<category android:name="android.intent.category.DEFAULT" />
	<category android:name="android.intent.category.BROWSABLE" />
</intent-filter>
```

For the test application scheme is *wsp1bart://*

**Attention:** scheme-host combination must be unique to provide the return to your application.

The application must have permissions to access the Internet and the current location for correct SDK working:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

**ACCESS_FINE_LOCATION** is optional but it is recommended for matching the relevant ads.

If your application is using geolocation, you can set the current location by yourself (see more - [Plus1Request](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1Request.md)). It is recommended to turn off the automatic location detection in the SDK (method *disableAutoDetectLocation()* in [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md)).

You need to add the information about the used Activity to the *<application>* block:

```xml
<application android:label="Bart" android:icon="@drawable/icon">
	<activity android:name="<app Activity name>" ... >
		...
	</activity>

	<activity android:name="ru.wapstart.plus1.sdk.ApplicationBrowser" />
</application>
```

*[ApplicationBrowser](https://github.com/WapStart/plus1-android-sdk/blob/master/sdk/src/ru/wapstart/plus1/sdk/ApplicationBrowser.java)* is using for open banners in application context.

## Test application setup
For correct test app working you must transfer a [Plus1 WapStart](https://plus1.wapstart.ru) unique site identifier in the method **setApplicationId()** in the *[BartActivity.java](https://github.com/WapStart/plus1-android-sdk/blob/master/examples/Bart/src/ru/wapstart/plus1/bart/BartActivity.java#L51)* file.

You can find the unique site identifier on the **Adspace code** page after signing up for a [Plus1 WapStart](https://plus1.wapstart.ru) account and adding the Android application to the account.


# SDK using

You can find the examples of the settings and configurations of banners in the **Bart** test application. The short explanations for a quick setup are given below.

## Adding the banner to the application
Add [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md) into layout:

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent"
    >
	<ru.wapstart.plus1.sdk.Plus1BannerView
			android:id="@+id/plus1BannerView"
			android:layout_width="320dp"
			android:layout_height="50dp"
			android:layout_gravity="top|center"
		/>
</FrameLayout>

```
View is adapted to the 320x50 size. You should use these settings.

Turn on the necessary classes in the file of your Activity where you are planning to view the ads:

```java
import ru.wapstart.plus1.sdk.Plus1BannerView;
import ru.wapstart.plus1.sdk.Plus1Request;
import ru.wapstart.plus1.sdk.Plus1BannerAsker;
```

On the initialization step, create and setup the objects [Plus1Request](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1Request.md) and [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md) like it is provided below:

```java
@Override
protected void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	...

	mBannerView =
		(Plus1BannerView) findViewById(R.id.plus1BannerView);

	mAsker =
		new Plus1BannerAsker(
			new Plus1Request()
				.setApplicationId(...),
			mBannerView
				.enableAnimationFromTop()
				.enableCloseButton()
		)
		.setCallbackUrl(...)

}
```

You must set the unique identifier of your site in the **setApplicationId()** method. You can find it on the **Adspace code** page after signing up for a [Plus1 WapStart](https://plus1.wapstart.ru) account and adding the Android application to the account.

Set the *callback url* using method **setCallbackUrl()** with your preferences of manifest. If you are using *setDisabledOpenLinkAction(true)* of [Plus1Request](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1Request.md), it can be unnecessary (not recommended).

Example:

```java
        mAsker =
                new Plus1BannerAsker(
                        ...
                )
                .setCallbackUrl("wsp1bart://ru.wapstart.plus1.bart")

}
```

Then it is necessary to provide the call of *onResume()* and *onPause()* handlers of the [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md) class:

```java
@Override
protected void onResume() {
	super.onResume();

	...

	mAsker.onResume();
}

@Override
protected void onPause() {
	super.onPause();

	...

	mAsker.onPause();
}
```

For Rich Media banners you need to send the "Back" button event in [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md):

```java
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
	if ((keyCode == KeyEvent.KEYCODE_BACK) && mBannerView.canGoBack()) {
		mBannerView.goBack();
		return true;
	}

	return super.onKeyDown(keyCode, event);
}
```

You can find the detailed descriptions of the classes, interfaces and the source code below:
* [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md) - about the ads from the server
* [Plus1Request](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1Request.md) - about the user information storage and the requests from the server
* [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md) - about the displaying of the ads
* [Plus1BannerDownloadListener](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerDownloadListener.md) - the interface of the observer of the banner loading

## SDK events processing by application
There are often situations when the application requires reaction to event associated with a banner. There are observers in [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md) for this case:

```java
public Plus1BannerView addListener(OnShowListener listener);
public Plus1BannerView addListener(OnHideListener listener);
public Plus1BannerView addListener(OnCloseButtonListener listener);
public Plus1BannerView addListener(OnExpandListener listener);
public Plus1BannerView addListener(OnCollapseListener listener);
public Plus1BannerView addListener(OnImpressionListener listener);
public Plus1BannerView addListener(OnTrackClickListener listener);
```

To handle the events in the application you need to add observer for [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md). For example:

```java
mBannerView
        .addListener(new Plus1BannerView.OnShowListener() {
                public void onShow(Plus1BannerView pbv) {
                        Log.d(LOGTAG, "Advertising block appeared on the screen");
                }
        })
        .addListener(new Plus1BannerView.OnHideListener() {
                public void onHide(Plus1BannerView pbv) {
                        Log.d(LOGTAG, "SDK hid the ad unit");
                }
        })
        .addListener(new Plus1BannerView.OnTrackClickListener() {
                public void onTrackClick(Plus1BannerView pbv) {
                        Log.d(LOGTAG, "There was a click event on the banner");
                }
        });
```

A description of all observers you can find in [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md#Наблюдатели).

## WebViews collision resolving in application
When you use *WebView* in several Activity of your application, the logic of this component will be violated. The causes of collision that the sdk by default call handlers [pauseTimers()](http://developer.android.com/reference/android/webkit/WebView.html#pauseTimers%28%29) and [resumeTimers()](http://developer.android.com/reference/android/webkit/WebView.html#resumeTimers%28%29) of class *WebView* in appropriate *onPause* and *onResume* event contexts in Activity of your application. Calls of these methods affect all *WebView* instances and allow to exclude *WebView* processing at a time when your application (Activity) is not shown to the user.

To resolve this collision you may use the method **setDisabledWebViewCorePausing()** of class [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md). It is important to understand that *WebView* continues processing at a time when Activity with a banner suspended. To conserve CPU cycles recommended to remove *WebView* in Activity using the method **setRemoveBannersOnPause()** of class [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md).

# Contacts

If you have any questions, please, contact our client support:  
E-Mail: clientsupport@co.wapstart.ru  
ICQ: 553425962

---------------------------------------
We are always looking to improve our SDK to make things easier for you and all of our customers. You can help us if you have any [comments](https://github.com/Wapstart/plus1-android-sdk/pulls) or [suggestions](https://github.com/WapStart/plus1-android-sdk/issues)!
