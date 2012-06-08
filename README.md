WapStart Plus1 Android SDK
==========================
Это open source библиотека для интеграции рекламы системы WapStart Plus1 в ваши Android-приложения.

WapStart Plus1 Android SDK распространяется под свободной лицензией BSD (as is).

# Установка и настройка

1. Скачайте последнюю версию SDK: https://github.com/Wapstart/plus1-android-sdk/tags
2. Для начала работы необходимо добавить SDK к проекту в качестве библиотеки;

Приложение должно обладать правами на доступ к сети интернет (**android.permission.INTERNET**) и на получение текущего метоположения (**android.permission.ACCESS_FINE_LOCATION**). Второе не является обязательным, но желательно для более точного определения подходящего рекламного объявления.

Если ваше приложение использует геолокацию, вы можете самостоятельно устанавливать текущее местоположение (см. описание интерфейсов - Plus1BannerRequest.class). При этом рекомендуется отключать автоматическое определение местоположения.

Для работы тестового приложения вам нужно передать идентификатор площадки WapStart Plus1 в методе *setApplicationId()* в файле *BartActivity.java*.

Идентификатор площадки можно узнать на странице **Код для площадки** после регистрации в системе [WapStart Plus1](https://plus1.wapstart.ru/) и добавления площадки типа Android.

# Использование SDK
Примеры настройки и конфигурации баннеров можно посмотреть в тестовом приложении **Bart**. В этом разделе даются краткие пояснения для быстрой настройки собственного проекта.

## Добавление баннера в приложение
Добавьте *Plus1BannerView* в layout:

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

На этапе инициализации создайте и настройте объекты *Plus1BannerRequest* и *Plus1BannerAsker* следующим образом:

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

А затем предусмотреть вызов обработчиков *onResume()* и *onPause()*:

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

// FIXME ссылки на описания методов классов

# Контактная информация
По всем возникающим у вас вопросам интеграции вы можете обратиться в службу поддержки пользователей:  
E-mail: clientsupport@co.wapstart.ru  
ICQ: 553425962
