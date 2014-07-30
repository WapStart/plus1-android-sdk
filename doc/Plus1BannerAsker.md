Plus1BannerAsker
================
Отвечает за получение объявлений с сервера. Взаимодействует с [Plus1Request](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1Request.md), [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md) и Activity вашего приложения.

Основные методы
------------------
* `public void onPause()`  
  `public void onResume()`  
  Методы должны вызываться из соответствующих обработчиков в Activity вашего приложения.

* `public Plus1BannerAsker setRemoveBannersOnPause(boolean orly)`  
  Включает или отключает удаление баннеров в контексте события *onPause* в Activity вашего приложения.

* `public Plus1BannerAsker setDisabledWebViewCorePausing(boolean orly)`  
  Отвечает за логику работы с потоком *WebView* в приложении, позволяет разрешить коллизии при использовании *WebView* в разных Activity вашего приложения. Подробнее см. раздел [Разрешение коллизий работы WebView в приложении](https://github.com/WapStart/plus1-android-sdk/blob/master/README-RUS.md#Разрешение-коллизий-работы-webview-в-приложении).

* `public Plus1BannerAsker setDownloadListener(Plus1BannerDownloadListener downloadListener)`  
  Устанавливает наблюдателя загрузки объявления. Подробнее см. интерфейс [Plus1BannerDownloadListener](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerDownloadListener.md).

* `public void refreshBanner()`  
  Метод для обновления баннера в "ручном" режиме.

* `public Plus1BannerAsker setRefreshDelay(int delayInSeconds)`  
  Периодичность запроса объявлений в секундах (необязательный параметр, по умолчанию 10 секунд).

* `public Plus1BannerAsker setRefreshRetryNum(int refreshRetryNum)`  
  Количество попыток обновления баннера (необязательный параметр, по умолчанию 3). 
 
* `public Plus1BannerAsker setLocationRefreshDelay(int delayInSeconds)`  
  Периодичность обновлений информации о местоположении пользователя (необязательный параметр по умолчанию 5 минут).

* `public Plus1BannerAsker disableAutoDetectLocation()`  
  Отключает механизм автоматического определения местоположения пользователя через *LocationManager*.

Остальные public-методы, не описанные в этом документе, используются для взаимодействия классов sdk, и могут быть использованы в отладочных целях.
