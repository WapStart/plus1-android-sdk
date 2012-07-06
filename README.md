WapStart Plus1 Android SDK
==========================
Это open source библиотека для интеграции рекламы системы [Plus1 WapStart](http://plus1.wapstart.ru/) в ваши Android-приложения.

Plus1 WapStart Android SDK распространяется под свободной лицензией BSD (as is).

# Установка и настройка

1. Скачайте последнюю версию SDK: https://github.com/WapStart/plus1-android-sdk/tags
2. Для начала работы необходимо добавить SDK к проекту в качестве библиотеки;

Приложение должно обладать правами на доступ к сети интернет (**android.permission.INTERNET**) и на получение текущего метоположения (**android.permission.ACCESS_FINE_LOCATION**). Второе не является обязательным, но желательно для более точного определения подходящего рекламного объявления.

Если ваше приложение использует геолокацию, вы можете самостоятельно устанавливать текущее местоположение (см. описание интерфейсов - [Plus1BannerRequest](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerRequest.md)). При этом рекомендуется отключать автоматическое определение местоположения.

Для работы тестового приложения вам нужно передать идентификатор площадки WapStart Plus1 в методе **setApplicationId()** в файле *[BartActivity.java](https://github.com/WapStart/plus1-android-sdk/blob/master/examples/Bart/src/ru/wapstart/plus1/bart/BartActivity.java#L51)*.

Идентификатор площадки можно узнать на странице **Код для площадки** после регистрации в системе [WapStart Plus1](http://plus1.wapstart.ru/) и добавления площадки типа Android.

# Использование SDK
Примеры настройки и конфигурации баннеров можно посмотреть в тестовом приложении **Bart**. В этом разделе даются краткие пояснения для быстрой настройки собственного проекта.

## Добавление баннера в приложение
Добавьте [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md) в layout:

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
				.setApplicationId(4242),
			mBannerView
				.enableAnimationFromTop()
				.enableCloseButton()
		)

}
```

А затем предусмотреть вызов обработчиков *onResume()* и *onPause()* класса [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md):

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

Для подробного ознакомления смотрите описания интерфейсов и классов, а также исходные коды sdk:
* [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md) - отвечает за получение объявлений с сервера
* [Plus1BannerRequest](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerRequest.md) - отвечает за хранение информации о пользователе и формирование запроса к серверу
* [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md) - отвечает за отображение объявления
* [Plus1BannerViewStateListener](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerViewStateListener.md) - интерфейс наблюдателя за состоянием видимости [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md)
* [Plus1BannerDownloadListener](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerDownloadListener.md) - интерфейс наблюдателя загрузки баннера

# Контактная информация
По всем возникающим у вас вопросам интеграции вы можете обратиться в службу поддержки пользователей:  
E-Mail: clientsupport@co.wapstart.ru  
ICQ: 553425962
