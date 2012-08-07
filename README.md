Plus1 WapStart Android SDK
==========================
It is an open source library to use for integration with [Plus1 WapStart](https://plus1.wapstart.ru) ad network.

Plus1 WapStart Android SDK is provided by the free BSD license (as is).

**Contents:**
* [Setup Guide](#Setup-Guide)
  * [Manifest configuration](#manifest-configuration)
  * [Test application setup](#test-application-setup)
* [SDK using](#sdk-using)
  * [Adding the banner to the application](#adding-the-banner-to-the-application)
* [Contacts](#contacts)


# Setup Guide

1. Download the latest SDK: https://github.com/WapStart/plus1-android-sdk/tags
2. Add the SDK to your project to start working;
3. Follow these steps to set up the manifest.

## Manifest configuration
The application must have the permission to access the Internet and current location for correct SDK working:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

При этом **ACCESS_FINE_LOCATION** не является обязательным, но рекомендуется для подбора релевантных рекламных объявлений.

Если ваше приложение использует геолокацию, вы можете самостоятельно устанавливать текущее местоположение (см. описание интерфейсов - [Plus1BannerRequest](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerRequest.md)). При этом рекомендуется отключать автоматическое определние местоположения в SDK (метод *disableAutoDetectLocation()* в [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md)).

В блок *<application>* требуется добавить информацию об используемых Activity:

```xml
<application android:label="Bart" android:icon="@drawable/icon">
	<activity android:name="<app Activity name>" ... >
		...
	</activity>

	<activity android:name="ru.wapstart.plus1.sdk.MraidBrowser" />
</application>
```

*[MraidBrowser](https://github.com/WapStart/plus1-android-sdk/blob/master/sdk/src/ru/wapstart/plus1/sdk/MraidBrowser.java)* используется для перехода по ссылкам баннеров формата Rich Media внутри приложения.

## Test application setup
Для работы тестового приложения вам нужно передать идентификатор площадки [Plus1 WapStart](https://plus1.wapstart.ru) в методе **setApplicationId()** в файле *[BartActivity.java](https://github.com/WapStart/plus1-android-sdk/blob/master/examples/Bart/src/ru/wapstart/plus1/bart/BartActivity.java#L51)*.

Идентификатор площадки можно узнать на странице **Код для площадки** после регистрации в сети [Plus1 WapStart](https://plus1.wapstart.ru) и добавления площадки типа Android.


# SDK using

Примеры настройки и конфигурации баннеров можно посмотреть в тестовом приложении **Bart**. В этом разделе даются краткие пояснения для быстрой настройки собственного проекта.

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
View адаптировано под размер 320x50. Рекомендуется использовать именно данные параметры.

Подключите необходимые классы в файле вашего Activity, где планируете показывать рекламу:

```java
import ru.wapstart.plus1.sdk.Plus1BannerView;
import ru.wapstart.plus1.sdk.Plus1BannerRequest;
import ru.wapstart.plus1.sdk.Plus1BannerAsker;
```

На этапе инициализации создайте и настройте объекты [Plus1BannerRequest](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerRequest.md) и [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md) следующим образом:

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
			new Plus1BannerRequest()
				.setApplicationId(...),
			mBannerView
				.enableAnimationFromTop()
				.enableCloseButton()
		)

}
```

В методе **setApplicationId()** задайте идентификатор вашей рекламной площадки. Его можно узнать на странице **Код для площадки** после регистрации в сети [Plus1 WapStart](https://plus1.wapstart.ru) и добавления площадки типа Android.

Затем необходимо предусмотреть вызов обработчиков *onResume()* и *onPause()* класса [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md):

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

Для баннеров формата Rich Media в [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md) необходимо передавать событие нажатия клавиши "Назад".

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
* [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md) - отвечает за получение объявлений с сервера
* [Plus1BannerRequest](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerRequest.md) - отвечает за хранение информации о пользователе и формирование запроса к серверу
* [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md) - отвечает за отображение объявления
* [Plus1BannerViewStateListener](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerViewStateListener.md) - интерфейс наблюдателя за состоянием видимости [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md)
* [Plus1BannerDownloadListener](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerDownloadListener.md) - интерфейс наблюдателя загрузки баннера


# Contacts

If you have the questions, please, contact our clientsupport:  
E-Mail: clientsupport@co.wapstart.ru  
ICQ: 553425962

---------------------------------------
Мы постоянно улучшаем наши SDK, делаем их удобнее и стабильнее. Будем рады вашему [участию в разработке](https://github.com/Wapstart/plus1-android-sdk/pulls), с радостью рассмотрим и обсудим [ваши предложениия](https://github.com/WapStart/plus1-android-sdk/issues)!